/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.server.ServerFactory;
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

    private final WebSecurityConfiguration applicationDefaults;

    /**
     * Constructs a bundle with the {@link WebSecurityConfiguration#DEFAULT} as the application defaults.
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
        applyCors(configuration, environment);
    }

    private void applyCors(WebSecurityConfigurable configuration, Environment environment) {
        CorsConfiguration corsConfiguration = configuration.getWebSecurityConfiguration().cors();

        if (!corsConfiguration.enabled()) {
            return;
        }

        CrossOriginFilter crossOriginFilter = new CrossOriginFilter();
        FilterRegistration.Dynamic dynamic = environment.servlets().addFilter("CrossOriginFilter", crossOriginFilter);

        // apply the filter at the root path
        dynamic.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, getRootPath(configuration));

        // set the initial parameters based on the application defaults and the provided configuration
        Map<String, String> propertyMap = Maps.newHashMap(applicationDefaults.cors().getPropertyMap());
        propertyMap.putAll(corsConfiguration.getPropertyMap());
        dynamic.setInitParameters(propertyMap);
    }

    private static String getRootPath(WebSecurityConfigurable configuration) {
        String rootPath = "/*";

        if (configuration instanceof Configuration) {
            ServerFactory serverFactory = ((Configuration) configuration).getServerFactory();
            if (serverFactory instanceof AbstractServerFactory) {
                rootPath = ((AbstractServerFactory) serverFactory).getJerseyRootPath();
                rootPath += "*";
            }
        }

        return rootPath;
    }
}
