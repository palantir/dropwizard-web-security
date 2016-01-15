/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.cors;

import com.google.common.net.HttpHeaders;
import com.palantir.dropwizard.websecurity.WebSecurityConfiguration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Tests that check the default {@link com.palantir.dropwizard.websecurity.CorsConfiguration}.
 */
public final class DefaultCorsIntegrationTests extends BaseCorsIntegrationTests {

    public static final class DefaultCorsConfiguration extends BaseCorsConfiguration {

        @Override
        public WebSecurityConfiguration getWebSecurityConfiguration() {
            return WebSecurityConfiguration.DEFAULT;
        }
    }

    public static final class DefaultCorsApplication extends BaseCorsApplication<DefaultCorsConfiguration> {}

    @ClassRule
    public static final DropwizardAppRule<DefaultCorsConfiguration> RULE = new DropwizardAppRule<>(
            DefaultCorsApplication.class,
            DefaultCorsIntegrationTests.class.getClassLoader().getResource("test-server.yaml").getPath());

    private static Client client;

    @BeforeClass
    public static void beforeClass() {
        client = new JerseyClientBuilder(RULE.getEnvironment()).build("default-tests");
    }

    @Test
    public void testDefaultDenyGoodOrigin() throws Exception {
        Response response = client
                .target(String.format("http://localhost:%d" + VALID_PATH, RULE.getLocalPort())).request()
                .header(HttpHeaders.ORIGIN, GOOD_ORIGIN)
                .options();

        assertInvalidCorsRequest(response);
    }
}
