/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.palantir.dropwizard.websecurity.filters.HstsFilter;
import io.dropwizard.setup.Environment;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for {@link WebSecurityBundle}.
 */
public final class WebSecurityBundleTests {

    private final WebSecurityConfigurable appConfig = mock(WebSecurityConfigurable.class);
    private final FilterRegistration.Dynamic dynamic = mock(FilterRegistration.Dynamic.class);
    private final Environment environment = mock(Environment.class, RETURNS_DEEP_STUBS);

    @Test
    public void testDefaultFiltersApplied() throws Exception {
        WebSecurityBundle bundle = new WebSecurityBundle();
        WebSecurityConfiguration webSecurityConfig = new WebSecurityConfiguration.Builder().build();

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(Optional.of(webSecurityConfig));

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(CrossOriginFilter.class));
        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(HstsFilter.class));
    }

    @Test
    public void testFiltersAppliedWhenEnabled() throws Exception {
        WebSecurityBundle bundle = new WebSecurityBundle();
        WebSecurityConfiguration webSecurityConfig = new WebSecurityConfiguration.Builder()
                .cors(new CorsConfiguration.Builder().allowedOrigins("http://origin").build())
                .hsts("on")
                .build();

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(Optional.of(webSecurityConfig));

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets()).addFilter(anyString(), isA(CrossOriginFilter.class));
        verify(this.environment.servlets()).addFilter(anyString(), isA(HstsFilter.class));
    }

    @Test
    public void testFiltersNotAppliedWhenDisabled() throws Exception {
        WebSecurityBundle bundle = new WebSecurityBundle();
        WebSecurityConfiguration webSecurityConfig = new WebSecurityConfiguration.Builder()
                .cors(CorsConfiguration.DISABLED)
                .hsts(WebSecurityConfiguration.TURN_OFF)
                .build();

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(Optional.of(webSecurityConfig));

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(CrossOriginFilter.class));
        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(HstsFilter.class));
    }

    @Test
    public void testYamlOverridesAppDefaults() throws Exception {
        WebSecurityConfiguration appDefaultConfig = new WebSecurityConfiguration.Builder()
                .cors(new CorsConfiguration.Builder().allowedOrigins("http://origin").build())
                .hsts("on")
                .build();
        WebSecurityConfiguration yamlConfig = new WebSecurityConfiguration.Builder()
                .cors(CorsConfiguration.DISABLED)
                .hsts(WebSecurityConfiguration.TURN_OFF)
                .build();
        WebSecurityBundle bundle = new WebSecurityBundle(appDefaultConfig);

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(Optional.of(yamlConfig));

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(CrossOriginFilter.class));
        verify(this.environment.servlets(), never()).addFilter(anyString(), isA(HstsFilter.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultPropertyMap() throws Exception {
        WebSecurityConfiguration appDefaultConfig = new WebSecurityConfiguration.Builder()
                .cors(new CorsConfiguration.Builder()
                        .allowedOrigins("http://origin")
                        .build())
                .build();
        WebSecurityBundle bundle = new WebSecurityBundle(appDefaultConfig);

        when(environment.servlets().addFilter(anyString(), any(Filter.class))).thenReturn(dynamic);
        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(Optional.of(appDefaultConfig));

        bundle.run(appConfig, environment);

        ArgumentCaptor<Map> paramCaptor = ArgumentCaptor.forClass(Map.class);
        verify(dynamic).setInitParameters(paramCaptor.capture());

        Map<String, String> captured = paramCaptor.getValue();
        assertEquals(WebSecurityBundle.DEFAULT_ALLOWED_METHODS, captured.get(CrossOriginFilter.ALLOWED_METHODS_PARAM));
        assertNull(captured.get(CrossOriginFilter.ALLOWED_HEADERS_PARAM));
        assertNull(captured.get(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM));
        assertNull(captured.get(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM));
        assertNull(captured.get(CrossOriginFilter.EXPOSED_HEADERS_PARAM));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPropertyMapAddsAll() throws Exception {
        CorsConfiguration config = new CorsConfiguration.Builder()
                .allowedOrigins("origins")
                .allowedMethods("methods")
                .allowedHeaders("headers")
                .preflightMaxAge(123)
                .allowCredentials(true)
                .exposedHeaders("exposed")
                .build();
        WebSecurityConfiguration appDefaultConfig = new WebSecurityConfiguration.Builder()
                .cors(config)
                .build();
        WebSecurityBundle bundle = new WebSecurityBundle(appDefaultConfig);

        when(environment.servlets().addFilter(anyString(), any(Filter.class))).thenReturn(dynamic);
        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(Optional.of(appDefaultConfig));

        bundle.run(appConfig, environment);

        ArgumentCaptor<Map> paramCaptor = ArgumentCaptor.forClass(Map.class);
        verify(dynamic).setInitParameters(paramCaptor.capture());

        Map<String, String> props = paramCaptor.getValue();
        assertEquals(config.allowedOrigins().get(), props.get(CrossOriginFilter.ALLOWED_ORIGINS_PARAM));
        assertEquals(config.allowedMethods().get(), props.get(CrossOriginFilter.ALLOWED_METHODS_PARAM));
        assertEquals(config.allowedHeaders().get(), props.get(CrossOriginFilter.ALLOWED_HEADERS_PARAM));
        assertEquals(config.preflightMaxAge().get().toString(), props.get(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM));
        assertEquals(config.allowCredentials().get().toString(), props.get(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM));
        assertEquals(config.exposedHeaders().get(), props.get(CrossOriginFilter.EXPOSED_HEADERS_PARAM));
    }
}
