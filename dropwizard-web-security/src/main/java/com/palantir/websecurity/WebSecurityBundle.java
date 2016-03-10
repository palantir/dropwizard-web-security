/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.palantir.websecurity.filters.JerseyAwareWebSecurityFilter;
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

    private static final String ROOT_PATH = "/*";

    private final WebSecurityConfiguration applicationDefaults;
    private WebSecurityConfiguration derivedConfiguration = null;

    /**
     * Constructs a bundle with the out of the box defaults.
     */
    public WebSecurityBundle() {
        this(WebSecurityConfiguration.DEFAULT);
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

        this.derivedConfiguration = WebSecurityConfiguration.builder()
                .from(applicationDefaults)
                .from(configuration.getWebSecurityConfiguration())
                .build();

        applyCors(this.derivedConfiguration, environment);
        applyWebSecurity(this.derivedConfiguration, environment, getJerseyRootPath(configuration));
    }

    /**
     * Returns the derived configuration. Must be called after {@link #run(WebSecurityConfigurable, Environment)}.
     */
    public WebSecurityConfiguration getDerivedConfiguration() {
        checkState(this.derivedConfiguration != null, "Must be called after 'run'.");
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
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        builder.put(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, cors.allowedOrigins().get());

        builder.put(CrossOriginFilter.ALLOWED_METHODS_PARAM, cors.allowedMethods()
                .or(CorsConfiguration.DEFAULT_ALLOWED_METHODS));

        builder.put(CrossOriginFilter.ALLOWED_HEADERS_PARAM, cors.allowedHeaders()
                .or(CorsConfiguration.DEFAULT_ALLOWED_HEADERS));

        builder.put(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, Boolean.toString(cors.allowCredentials()
                .or(CorsConfiguration.DEFAULT_ALLOW_CREDENTIALS)));

        if (cors.preflightMaxAge().isPresent()) {
            builder.put(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, Long.toString(cors.preflightMaxAge().get()));
        }

        if (cors.exposedHeaders().isPresent()) {
            builder.put(CrossOriginFilter.EXPOSED_HEADERS_PARAM, cors.exposedHeaders().get());
        }

        return builder.build();
    }

    private static void applyWebSecurity(WebSecurityConfiguration derivedConfig, Environment env, String jerseyRoot) {
        JerseyAwareWebSecurityFilter filter = new JerseyAwareWebSecurityFilter(derivedConfig, jerseyRoot);
        env.servlets()
                .addFilter("JerseyAwareWebSecurityFilter", filter)
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
