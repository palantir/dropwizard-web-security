/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import com.google.common.net.HttpHeaders;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.junit.ClassRule;
import org.junit.Test;

public final class ExampleTests {

    public static final String ORIGIN_VALUE = "http://origin.com";

    @ClassRule
    public static final DropwizardAppRule<Example.ExampleConfiguration> RULE = new DropwizardAppRule<>(
            Example.ExampleApplication.class,
            Example.ExampleApplication.class.getClassLoader().getResource("example.yml").getPath());

    @Test
    public void testHeadersApplied() {
        Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("tests");

        Response response = client
                .target(String.format("http://localhost:%d/example-context/api/hello", RULE.getLocalPort())).request()
                .header(HttpHeaders.ORIGIN, ORIGIN_VALUE)
                .get();

        // check basic functionality
        assertEquals(200, response.getStatus());
        assertEquals(Example.EXAMPLES_RESOURCE_RESPONSE, response.readEntity(String.class));

        // test for application default settings
        assertEquals(Example.CSP_FROM_APP, response.getHeaderString(HttpHeaders.CONTENT_SECURITY_POLICY));
        assertNotEquals(Example.CTO_FROM_APP, response.getHeaderString(HttpHeaders.X_CONTENT_TYPE_OPTIONS));

        // check for YAML defined settings
        assertEquals(Example.CTO_FROM_YML, response.getHeaderString(HttpHeaders.X_CONTENT_TYPE_OPTIONS));
        assertNull(response.getHeaderString(HttpHeaders.X_FRAME_OPTIONS));

        // check for a YAML defined CORS entry
        assertEquals(ORIGIN_VALUE, response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
