/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import com.google.common.net.HttpHeaders;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Tests for {@link Example.ExampleWebApplication}.
 */
public final class ExampleWebTests {

    public static final String ORIGIN_VALUE = "http://origin.com";

    @ClassRule
    public static final DropwizardAppRule<Example.ExampleConfiguration> RULE = new DropwizardAppRule<>(
            Example.ExampleWebApplication.class,
            Example.ExampleWebApplication.class.getClassLoader().getResource("example-web.yml").getPath());

    private static Client client;

    @BeforeClass
    public static void beforeClass() {
        client = new JerseyClientBuilder(RULE.getEnvironment())
                .withProperty(ClientProperties.CONNECT_TIMEOUT, 10000)
                .withProperty(ClientProperties.READ_TIMEOUT, 10000)
                .build("tests");
    }

    @Test
    public void testCorsHeadersAppliedToApi() {
        Response response = client
                .target(String.format("http://localhost:%d/example-context/api/hello", RULE.getLocalPort())).request()
                .header(HttpHeaders.ORIGIN, ORIGIN_VALUE)
                .get();

        // check basic functionality
        assertEquals(200, response.getStatus());
        assertEquals(Example.EXAMPLES_RESOURCE_RESPONSE, response.readEntity(String.class));

        // check for a YAML defined CORS entry
        assertEquals(ORIGIN_VALUE, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void testCorsHeadersAppliedToWeb() {
        Response response = client
                .target(String.format("http://localhost:%d/example-context/index.html", RULE.getLocalPort())).request()
                .header(HttpHeaders.ORIGIN, ORIGIN_VALUE)
                .get();

        // check basic functionality
        assertEquals(200, response.getStatus());

        // check for a YAML defined CORS entry
        assertEquals(ORIGIN_VALUE, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void testWebSecurityHeadersNotAppliedToApi() {
        Response response = client
                .target(String.format("http://localhost:%d/example-context/api/hello", RULE.getLocalPort())).request()
                .get();

        // check basic functionality
        assertEquals(200, response.getStatus());

        // check that no web security headers are on the response
        assertNull(response.getHeaderString(HttpHeaders.CONTENT_SECURITY_POLICY));
        assertNull(response.getHeaderString(HttpHeaders.X_CONTENT_TYPE_OPTIONS));
        assertNull(response.getHeaderString(HttpHeaders.X_FRAME_OPTIONS));
    }

    @Test
    public void testWebSecurityHeadersAppliedToWeb() {
        Response response = client
                .target(String.format("http://localhost:%d/example-context/index.html", RULE.getLocalPort())).request()
                .get();

        // check basic functionality
        assertEquals(200, response.getStatus());

        // test for application default settings
        assertEquals(Example.CSP_FROM_APP, response.getHeaderString(HttpHeaders.CONTENT_SECURITY_POLICY));
        assertNotEquals(Example.CTO_FROM_APP, response.getHeaderString(HttpHeaders.X_CONTENT_TYPE_OPTIONS));

        // check for YAML defined settings
        assertEquals(Example.CTO_FROM_YML, response.getHeaderString(HttpHeaders.X_CONTENT_TYPE_OPTIONS));
        assertNull(response.getHeaderString(HttpHeaders.X_FRAME_OPTIONS));
    }
}
