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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Tests that check non-preflight requests work as expected.
 */
public final class RequestCorsIntegrationTests extends BaseCorsIntegrationTests {

    private static final String ALLOWED_HEADERS = "Origin,Content-Type,Accept";
    private static final String DISALLOWED_HEADER = "X-Not-Allowed-Or-Real";

    private static final String ALLOWED_METHODS = "GET,PUT,POST,OPTIONS";

    public static final class RequestCorsConfiguration extends BaseCorsConfiguration {

        @Override
        public WebSecurityConfiguration getWebSecurityConfiguration() {
            return new WebSecurityConfiguration.Builder()
                    .cors(new CorsConfiguration.Builder()
                            .allowedHeaders(ALLOWED_HEADERS)
                            .allowedMethods(ALLOWED_METHODS)
                            .allowedOrigins(GOOD_ORIGIN)
                            .build())
                    .build();
        }
    }

    public static final class RequestCorsApplication extends BaseCorsApplication<RequestCorsConfiguration> {}

    @ClassRule
    public static final DropwizardAppRule<RequestCorsConfiguration> RULE = new DropwizardAppRule<>(
            RequestCorsApplication.class,
            RequestCorsIntegrationTests.class.getClassLoader().getResource("test-server.yaml").getPath());

    private static Client client;

    @BeforeClass
    public static void beforeClass() {
        client = new JerseyClientBuilder(RULE.getEnvironment()).build("request-tests");
    }

    @Test
    public void testFilterHeaderOut() {
        Response response = client
                .target(String.format("http://localhost:%d" + VALID_PATH, RULE.getLocalPort())).request()
                .header(HttpHeaders.ORIGIN, GOOD_ORIGIN)
                .header(DISALLOWED_HEADER, "123")
                .get();

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals(CORS_DEFAULT_ALLOW_CREDS, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals(GOOD_ORIGIN, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
        assertNull(response.getHeaderString(DISALLOWED_HEADER));
    }

    @Test
    public void testDenyMethod() {
        Response response = client
                .target(String.format("http://localhost:%d" + VALID_PATH, RULE.getLocalPort())).request()
                .header(HttpHeaders.ORIGIN, GOOD_ORIGIN)
                .delete();

        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());
        assertEquals(CORS_DEFAULT_ALLOW_CREDS, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals(GOOD_ORIGIN, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }
}
