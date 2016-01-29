/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.palantir.dropwizard.websecurity.filters.AppSecurityFilter;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.servlets.CrossOriginFilter;

/**
 * Applies and configures security filters to the application.
 */
public final class WebSecurityBundle implements ConfiguredBundle<WebSecurityConfigurable> {

    /**
     * The default value of CORS Allowed Methods. It includes commonly used methods.
     */
    public static final String DEFAULT_ALLOWED_METHODS = "DELETE,GET,HEAD,POST,PUT";

    private static final String ROOT_PATH = "/*";

    private final WebSecurityConfiguration applicationDefaults;
    private WebSecurityConfiguration derivedConfiguration = null;

    /**
     * Constructs a bundle with the out of the box defaults.
     */
    public WebSecurityBundle() {
        this(new WebSecurityConfiguration.Builder().build());
    }

    /**
     * Constructs a bundle with the {@link #applicationDefaults} as the application defaults.
     */
    public WebSecurityBundle(WebSecurityConfiguration applicationDefaults) {
        checkNotNull(applicationDefaults);
        this.applicationDefaults = applicationDefaults;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // do nothing
    }

    @Override
    public void run(WebSecurityConfigurable configuration, Environment environment) throws Exception {
        checkNotNull(configuration);
        checkNotNull(environment);

        WebSecurityConfiguration.Builder builder = new WebSecurityConfiguration.Builder();
        builder.from(applicationDefaults);

        if (configuration.getWebSecurityConfiguration().isPresent()) {
            builder.from(configuration.getWebSecurityConfiguration().get());
        }

        this.derivedConfiguration = builder.build();

        applyCors(this.derivedConfiguration, environment);
        applyAppSecurity(this.derivedConfiguration, environment, getJerseyRootPath(configuration));
    }

    /**
     * Returns the derived configuration. Must be called after {@link #run(WebSecurityConfigurable, Environment)}.
     */
    public WebSecurityConfiguration getDerivedConfiguration() {
        checkState(this.derivedConfiguration != null);
        return derivedConfiguration;
    }

    private static void applyCors(WebSecurityConfiguration derivedConfig, Environment environment) {
        if (!derivedConfig.cors().isPresent() || !derivedConfig.cors().get().enabled()) {
            return;
        }

        CrossOriginFilter filter = new CrossOriginFilter();

        FilterRegistration.Dynamic dynamic = environment.servlets().addFilter("CrossOriginFilter", filter);
        dynamic.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, ROOT_PATH);
        dynamic.setInitParameters(buildCorsPropertyMap(derivedConfig.cors().get()));
    }

    private static Map<String, String> buildCorsPropertyMap(CorsConfiguration cors) {
        ImmutableMap.Builder<String, String> propertyBuilder = ImmutableMap.builder();

        propertyBuilder.put(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, cors.allowedOrigins().get());
        propertyBuilder.put(CrossOriginFilter.ALLOWED_METHODS_PARAM, cors.allowedMethods().or(DEFAULT_ALLOWED_METHODS));

        if (cors.allowedHeaders().isPresent()) {
            propertyBuilder.put(CrossOriginFilter.ALLOWED_HEADERS_PARAM, cors.allowedHeaders().get());
        }

        if (cors.preflightMaxAge().isPresent()) {
            propertyBuilder.put(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, Long.toString(cors.preflightMaxAge().get()));
        }

        if (cors.allowCredentials().isPresent()) {
            propertyBuilder.put(
                    CrossOriginFilter.ALLOW_CREDENTIALS_PARAM,
                    Boolean.toString(cors.allowCredentials().get()));
        }

        if (cors.exposedHeaders().isPresent()) {
            propertyBuilder.put(CrossOriginFilter.EXPOSED_HEADERS_PARAM, cors.exposedHeaders().get());
        }

        return propertyBuilder.build();
    }

    private static void applyAppSecurity(WebSecurityConfiguration derivedConfig, Environment env, String jerseyRoot) {
        AppSecurityFilter filter = new AppSecurityFilter(derivedConfig, jerseyRoot);
        env.servlets()
                .addFilter("AppSecurityFilter", filter)
                .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, ROOT_PATH);
    }

    /**
     * Determines the Jersey Root Path by pulling it from the {@link AbstractServerFactory}. If the value cannot be
     * found, then the default value of {@code /*} is used instead.
     */
    private static String getJerseyRootPath(WebSecurityConfigurable configuration) {
        String rootPath = "/*";

        if (configuration instanceof Configuration) {
            Configuration dwConfig = (Configuration) configuration;
            if (dwConfig.getServerFactory() instanceof AbstractServerFactory) {
                AbstractServerFactory abstractServerFactory = (AbstractServerFactory) dwConfig.getServerFactory();
                rootPath = abstractServerFactory.getJerseyRootPath();
            }
        }

        return rootPath;
    }
}
