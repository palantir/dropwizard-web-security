/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.dropwizard.websecurity.app.AppSecurityConfiguration;
import com.palantir.dropwizard.websecurity.app.AppSecurityFilter;
import com.palantir.dropwizard.websecurity.app.AppSecurityHeaderInjector;
import com.palantir.dropwizard.websecurity.cors.CorsConfiguration;
import com.palantir.dropwizard.websecurity.hsts.HstsConfiguration;
import com.palantir.dropwizard.websecurity.hsts.HstsFilter;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
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
        checkNotNull(configuration);
        checkNotNull(environment);

        applyCors(configuration, environment);
        applyAppSecurity(configuration, environment);
        applyHstsFilter(configuration, environment);
    }

    private void applyCors(WebSecurityConfigurable configuration, Environment environment) {
        CorsConfiguration corsConfiguration = configuration.getWebSecurityConfiguration().cors();

        if (!corsConfiguration.enabled()) {
            return;
        }

        CrossOriginFilter crossOriginFilter = new CrossOriginFilter();
        FilterRegistration.Dynamic dynamic = environment.servlets().addFilter("CrossOriginFilter", crossOriginFilter);

        dynamic.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, getRootPath(configuration));

        CorsConfiguration derivedConfiguration = new CorsConfiguration.Builder()
                .from(this.applicationDefaults.cors())
                .from(corsConfiguration)
                .build();

        dynamic.setInitParameters(derivedConfiguration.getPropertyMap());
    }

    private void applyAppSecurity(WebSecurityConfigurable configuration, Environment environment) {
        AppSecurityConfiguration appSecurityConfiguration = configuration.getWebSecurityConfiguration().appSecurity();

        if (!appSecurityConfiguration.enabled()) {
            return;
        }

        AppSecurityConfiguration derivedConfiguration = new AppSecurityConfiguration.Builder()
                .from(this.applicationDefaults.appSecurity())
                .from(appSecurityConfiguration)
                .build();

        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(derivedConfiguration);
        AppSecurityFilter appSecurityFilter = new AppSecurityFilter(injector);

        environment.servlets()
                .addFilter("AppSecurityFitler", appSecurityFilter)
                .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, getRootPath(configuration));
    }

    private void applyHstsFilter(WebSecurityConfigurable configuration, Environment environment) {
        HstsConfiguration hstsConfiguration = configuration.getWebSecurityConfiguration().hsts();

        if (!hstsConfiguration.enabled()) {
            return;
        }

        HstsConfiguration derivedConfiguration = new HstsConfiguration.Builder()
                .from(this.applicationDefaults.hsts())
                .from(hstsConfiguration)
                .build();

        HstsFilter hstsFilter = new HstsFilter(derivedConfiguration.headerValue());

        environment.servlets()
                .addFilter("HstsFilter", hstsFilter)
                .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, getRootPath(configuration));
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
