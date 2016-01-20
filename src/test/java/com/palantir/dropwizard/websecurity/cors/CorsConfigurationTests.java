/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.cors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import io.dropwizard.validation.BaseValidator;
import io.dropwizard.validation.ValidationMethod;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.Test;

/**
 * Tests for {@link CorsConfiguration}.
 */
public final class CorsConfigurationTests {

    private static final Validator VALIDATOR = BaseValidator.newValidator();

    @Test
    public void testDefaultValues() {
        assertEquals(CorsConfiguration.DEFAULT_ALLOWED_ORIGINS, CorsConfiguration.DEFAULT.allowedOrigins());
        assertFalse(CorsConfiguration.DEFAULT.enabled());
        assertFalse(CorsConfiguration.DEFAULT.allowCredentials().isPresent());
        assertFalse(CorsConfiguration.DEFAULT.allowedHeaders().isPresent());
        assertEquals(CorsConfiguration.DEFAULT_ALLOWED_METHODS, CorsConfiguration.DEFAULT.allowedMethods());
        assertFalse(CorsConfiguration.DEFAULT.exposedHeaders().isPresent());
        assertFalse(CorsConfiguration.DEFAULT.preflightMaxAge().isPresent());
    }

    @Test
    public void testDefaultPropertyMap() {
        Map<String, String> props = CorsConfiguration.DEFAULT.getPropertyMap();

        assertEquals(CorsConfiguration.DEFAULT_ALLOWED_ORIGINS, props.get(CrossOriginFilter.ALLOWED_ORIGINS_PARAM));
        assertEquals(CorsConfiguration.DEFAULT_ALLOWED_METHODS, props.get(CrossOriginFilter.ALLOWED_METHODS_PARAM));
        assertNull(props.get(CrossOriginFilter.ALLOWED_HEADERS_PARAM));
        assertNull(props.get(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM));
        assertNull(props.get(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM));
        assertNull(props.get(CrossOriginFilter.EXPOSED_HEADERS_PARAM));
    }

    @Test
    public void testPropertyMapAddsAll() {
        CorsConfiguration config = new CorsConfiguration.Builder()
                .allowedOrigins("origins")
                .allowedMethods("methods")
                .allowedHeaders("headers")
                .preflightMaxAge(123)
                .allowCredentials(true)
                .exposedHeaders("exposed")
                .build();

        Map<String, String> props = config.getPropertyMap();

        assertEquals(config.allowedOrigins(), props.get(CrossOriginFilter.ALLOWED_ORIGINS_PARAM));
        assertEquals(config.allowedMethods(), props.get(CrossOriginFilter.ALLOWED_METHODS_PARAM));
        assertEquals(config.allowedHeaders().get(), props.get(CrossOriginFilter.ALLOWED_HEADERS_PARAM));
        assertEquals(config.preflightMaxAge().get().toString(), props.get(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM));
        assertEquals(config.allowCredentials().get().toString(), props.get(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM));
        assertEquals(config.exposedHeaders().get(), props.get(CrossOriginFilter.EXPOSED_HEADERS_PARAM));
    }

    @Test
    public void testDisabled() {
        assertFalse(CorsConfiguration.DISABLED.enabled());
    }

    @Test
    public void testAllowedOrigins_allowStar() {
        CorsConfiguration config = new CorsConfiguration.Builder().allowedOrigins("*").build();

        Set<ConstraintViolation<CorsConfiguration>> violations = VALIDATOR.validate(config);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testAllowedOrigins_invalidUrl() {
        CorsConfiguration config = new CorsConfiguration.Builder()
                .allowedOrigins("http://good.url,:/123/this/is/not/a.valid.url")
                .build();

        ConstraintViolation<CorsConfiguration> violation = getFirstViolation(VALIDATOR.validate(config));
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod);
    }

    @Test
    public void testAllowedOrigins_urlCannotHavePath() {
        CorsConfiguration config = new CorsConfiguration.Builder()
                .allowedOrigins("http://good.url,http://url.with.path/")
                .build();

        ConstraintViolation<CorsConfiguration> violation = getFirstViolation(VALIDATOR.validate(config));
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod);
    }

    @Test
    public void testAllowedOrigins_validRegex() {
        CorsConfiguration config = new CorsConfiguration.Builder()
                .allowedOrigins("http://*(dfdfd).$")
                .build();

        Set<ConstraintViolation<CorsConfiguration>> violations = VALIDATOR.validate(config);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testAllowedOrigins_invalidRegex() {
        CorsConfiguration config = new CorsConfiguration.Builder()
                .allowedOrigins("http://*(dfdfd")
                .build();

        ConstraintViolation<CorsConfiguration> violation = getFirstViolation(VALIDATOR.validate(config));
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod);
    }

    @Test
    public void testPreflightMaxAge_cannotBeNegative() {
        CorsConfiguration config = new CorsConfiguration.Builder()
                .preflightMaxAge(-1L)
                .build();

        ConstraintViolation<CorsConfiguration> violation = getFirstViolation(VALIDATOR.validate(config));
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod);
    }

    private static <T> ConstraintViolation<T> getFirstViolation(Set<ConstraintViolation<T>> violations) {
        if (violations.isEmpty()) {
            throw new RuntimeException("No violations found when one was expected.");

        } else {
            return Iterables.getFirst(violations, null);
        }
    }
}
