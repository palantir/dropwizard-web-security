/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * A complete REST Application example.
 */
public final class ExampleRest {

    public static final String EXAMPLES_RESOURCE_RESPONSE = "hello world";
    public static final String CSP_FROM_APP = "csp from app default";
    public static final String CTO_FROM_APP = "cto should be overridden";

    private ExampleRest() {
        // utility class for testing
    }

    public static void main(String[] args) throws Exception {
        new ExampleRestApplication().run(args);
    }

    /**
     * Example Jersey resource.
     */
    @Path("hello")
    public static final class ExampleResource {

        @GET
        @Produces(MediaType.TEXT_HTML)
        public String helloWorld() {
            return EXAMPLES_RESOURCE_RESPONSE;
        }
    }

    /**
     * Example REST Configuration class.
     */
    public static final class ExampleRestConfiguration extends Configuration implements WebSecurityConfigurable {

        @JsonProperty("webSecurity")
        @NotNull
        @Valid
        private final WebSecurityConfiguration webSecurity = WebSecurityConfiguration.DEFAULT;

        public WebSecurityConfiguration getWebSecurityConfiguration() {
            return this.webSecurity;
        }
    }

    /**
     * Example REST Application. Uses the application defaults using {@link #webSecurityDefaults}. The application is
     * configured to serve Jersey API requests from `/`.
     */
    public static final class ExampleRestApplication extends Application<ExampleRestConfiguration> {

        private final WebSecurityConfiguration webSecurityDefaults = WebSecurityConfiguration.builder()

                // set app defaults for different header values
                .contentSecurityPolicy(CSP_FROM_APP)
                .contentTypeOptions(CTO_FROM_APP)

                // CORS is still DISABLED, since the allowedOrigins is not set, but the default value will be
                // respected if it's ever turned on
                .cors(CorsConfiguration.builder()
                        .preflightMaxAge(60 * 10)
                        .build())

                .build();

        private final WebSecurityBundle webSecurityBundle = new WebSecurityBundle(this.webSecurityDefaults);

        @Override
        public void initialize(Bootstrap<ExampleRestConfiguration> bootstrap) {
            bootstrap.addBundle(this.webSecurityBundle);
        }

        @Override
        public void run(ExampleRestConfiguration configuration, Environment environment) throws Exception {
            environment.jersey().register(new ExampleResource());
        }
    }
}
