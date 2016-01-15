/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.cors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.net.HttpHeaders;
import com.palantir.dropwizard.websecurity.CorsConfiguration;
import com.palantir.dropwizard.websecurity.WebSecurityConfiguration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Tests that check pre-flight requests work as expected.
 */
public final class PreflightCorsIntegrationTests extends BaseCorsIntegrationTests {

    private static final String ALLOWED_HEADERS = "Origin,Content-Type,Accept";
    private static final String ALLOWED_HEADER = "Content-Type";
    private static final String DISALLOWED_HEADER = "X-Not-A-Header";

    private static final String ALLOWED_METHODS = "GET,PUT,POST,OPTIONS";
    private static final HttpMethod ALLOWED_METHOD = HttpMethod.GET;
    private static final HttpMethod DISALLOWED_METHOD = HttpMethod.DELETE;

    public static final Long MAX_AGE = 123L;
    public static final Boolean ALLOW_CREDS = false;

    public static final class PreflightCorsConfiguration extends BaseCorsConfiguration {

        @Override
        public WebSecurityConfiguration getWebSecurityConfiguration() {
            return new WebSecurityConfiguration.Builder()
                    .cors(new CorsConfiguration.Builder()
                            .allowedHeaders(ALLOWED_HEADERS)
                            .allowedMethods(ALLOWED_METHODS)
                            .allowedOrigins(GOOD_ORIGIN)
                            .preflightMaxAge(MAX_AGE)
                            .allowCredentials(ALLOW_CREDS)
                            .build())
                    .build();
        }
    }

    public static final class PreflightCorsApplication extends BaseCorsApplication<PreflightCorsConfiguration> {}

    @ClassRule
    public static final DropwizardAppRule<PreflightCorsConfiguration> RULE = new DropwizardAppRule<>(
            PreflightCorsApplication.class,
            PreflightCorsIntegrationTests.class.getClassLoader().getResource("test-server.yaml").getPath());

    private static Client client;

    @BeforeClass
    public static void beforeClass() {
        client = new JerseyClientBuilder(RULE.getEnvironment()).build("preflight-tests");
    }

    @Test
    public void testPreflightWorks() {
        Response response = fetchResponse(ALLOWED_METHOD, ALLOWED_HEADER);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(ALLOWED_HEADERS, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        assertEquals(ALLOWED_METHODS, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals(GOOD_ORIGIN, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
        assertEquals(MAX_AGE.toString(), response.getHeaderString(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    public void testInvalidMethod() {
        Response response = fetchResponse(DISALLOWED_METHOD, ALLOWED_HEADER);
        assertInvalidCorsRequest(response);
    }

    @Test
    public void testInvalidRequestHeader() {
        Response response = fetchResponse(ALLOWED_METHOD, DISALLOWED_HEADER);
        assertInvalidCorsRequest(response);
    }

    private Response fetchResponse(HttpMethod checkMethod, String checkHeader) {
        return client
                .target(String.format("http://localhost:%d" + VALID_PATH, RULE.getLocalPort())).request()
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, checkHeader)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, checkMethod.toString())
                .header(HttpHeaders.ORIGIN, GOOD_ORIGIN)
                .options();
    }
}
