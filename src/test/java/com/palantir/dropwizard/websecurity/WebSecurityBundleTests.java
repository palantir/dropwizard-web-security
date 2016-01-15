/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.palantir.dropwizard.websecurity.app.AppSecurityConfiguration;
import com.palantir.dropwizard.websecurity.app.AppSecurityFilter;
import com.palantir.dropwizard.websecurity.cors.CorsConfiguration;
import com.palantir.dropwizard.websecurity.hsts.HstsConfiguration;
import com.palantir.dropwizard.websecurity.hsts.HstsFilter;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.Test;

public final class WebSecurityBundleTests {

    private final Environment environment = mock(Environment.class, RETURNS_DEEP_STUBS);
    private final WebSecurityConfigurable appConfig = mock(WebSecurityConfigurable.class);

    @Test
    public void testFiltersAppliedWhenEnabled() throws Exception {
        WebSecurityBundle bundle = new WebSecurityBundle();

        ImmutableWebSecurityConfiguration webSecurityConfig = new WebSecurityConfiguration.Builder()
                .cors(new CorsConfiguration.Builder().enabled(true).build())
                .appSecurity(new AppSecurityConfiguration.Builder().enabled(true).build())
                .hsts(new HstsConfiguration.Builder().enabled(true).build())
                .build();

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(webSecurityConfig);

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets()).addFilter(anyString(), isA(CrossOriginFilter.class));
        verify(this.environment.servlets()).addFilter(anyString(), isA(AppSecurityFilter.class));
        verify(this.environment.servlets()).addFilter(anyString(), isA(HstsFilter.class));
    }

    @Test
    public void testFiltersNotAppliedWhenDisabled() throws Exception {
        WebSecurityBundle bundle = new WebSecurityBundle();

        WebSecurityConfiguration webSecurityConfig = new WebSecurityConfiguration.Builder()
                .cors(CorsConfiguration.DISABLED)
                .appSecurity(AppSecurityConfiguration.DISABLED)
                .hsts(HstsConfiguration.DISABLED)
                .build();

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(webSecurityConfig);

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(CrossOriginFilter.class));
        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(AppSecurityFilter.class));
        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(HstsFilter.class));
    }

    @Test
    public void testYamlOverridesAppDefaults() throws Exception {
        WebSecurityConfiguration appDefaultConfig = new WebSecurityConfiguration.Builder()
                .cors(new CorsConfiguration.Builder().enabled(true).build())
                .appSecurity(new AppSecurityConfiguration.Builder().enabled(true).build())
                .hsts(new HstsConfiguration.Builder().enabled(true).build())
                .build();
        WebSecurityBundle bundle = new WebSecurityBundle(appDefaultConfig);

        WebSecurityConfiguration yamlConfig = new WebSecurityConfiguration.Builder()
                .cors(CorsConfiguration.DISABLED)
                .appSecurity(AppSecurityConfiguration.DISABLED)
                .hsts(HstsConfiguration.DISABLED)
                .build();

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(yamlConfig);

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(CrossOriginFilter.class));
        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(AppSecurityFilter.class));
        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(HstsFilter.class));
    }
}
