/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.cors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.net.HttpHeaders;
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
 * Tests that check pre-flight requests work as expected when properties are parsed from a YAML file.
 */
public final class YamlCorsIntegrationTests extends BaseCorsIntegrationTests {

    public static final class YamlCorsConfiguration extends BaseCorsConfiguration {

        private WebSecurityConfiguration webSecurityConfiguration;

        @JsonCreator
        public YamlCorsConfiguration(@JsonProperty("webSecurity") WebSecurityConfiguration webSecurityConfiguration) {
            this.webSecurityConfiguration = webSecurityConfiguration;

        }

        @Override
        public WebSecurityConfiguration getWebSecurityConfiguration() {
            return this.webSecurityConfiguration;
        }
    }

    public static final class YamlCorsApplication extends BaseCorsApplication<YamlCorsConfiguration> {}

    @ClassRule
    public static final DropwizardAppRule<YamlCorsConfiguration> RULE = new DropwizardAppRule<>(
            YamlCorsApplication.class,
            YamlCorsIntegrationTests.class.getClassLoader().getResource("test-server-configured.yaml").getPath());

    private static Client client;

    @BeforeClass
    public static void beforeClass() {
        client = new JerseyClientBuilder(RULE.getEnvironment()).build("yaml-tests");
    }

    private static final String ALLOWED_HEADERS = "Origin,Content-Type,Accept";
    private static final String ALLOWED_HEADER = "Content-Type";

    private static final String ALLOWED_METHODS = "GET,PUT,POST,OPTIONS";
    private static final HttpMethod ALLOWED_METHOD = HttpMethod.GET;
    public static final Long MAX_AGE = 123L;

    @Test
    public void testPreflightWorksFromYaml() {
        Response response = client
                .target(String.format("http://localhost:%d" + VALID_PATH, RULE.getLocalPort())).request()
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, ALLOWED_HEADER)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, ALLOWED_METHOD.toString())
                .header(HttpHeaders.ORIGIN, GOOD_ORIGIN)
                .options();

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(ALLOWED_HEADERS, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        assertEquals(ALLOWED_METHODS, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals(GOOD_ORIGIN, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
        assertEquals(MAX_AGE.toString(), response.getHeaderString(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }
}
