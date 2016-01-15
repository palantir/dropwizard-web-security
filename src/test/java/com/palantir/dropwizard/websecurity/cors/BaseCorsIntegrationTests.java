/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.cors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.net.HttpHeaders;
import com.palantir.dropwizard.websecurity.WebSecurityBundle;
import com.palantir.dropwizard.websecurity.WebSecurityConfigurable;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SuppressWarnings("checkstyle:designforextension")
public abstract class BaseCorsIntegrationTests {

    public static final String CORS_DEFAULT_ALLOW_CREDS = "true";
    public static final String EXAMPLE_RESPONSE = "hello world";
    public static final String GOOD_ORIGIN = "http://good.origin";
    public static final String VALID_PATH = "/example/hello";

    @Path("example")
    public static final class ExampleResource {

        @Path("hello")
        @GET
        @Produces(MediaType.TEXT_HTML)
        public String helloWorld() {
            return EXAMPLE_RESPONSE;
        }
    }

    /**
     * Base configuration that provides a {@link com.palantir.dropwizard.websecurity.WebSecurityConfiguration}.
     */
    public abstract static class BaseCorsConfiguration extends Configuration implements WebSecurityConfigurable {
        // implemented by each configuration
    }

    /**
     * Base application that applies the {@link WebSecurityBundle}.
     *
     * @param <T> The custom configuration for the test case.
     */
    public abstract static class BaseCorsApplication<T extends BaseCorsConfiguration> extends Application<T> {

        @Override
        public void initialize(Bootstrap<T> bootstrap) {
            bootstrap.addBundle(new WebSecurityBundle());
        }

        @Override
        public final void run(T configuration, Environment environment) throws Exception {
            environment.jersey().register(new ExampleResource());
        }
    }

    public static void assertInvalidCorsRequest(Response response) {
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
        assertNull(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }
}
