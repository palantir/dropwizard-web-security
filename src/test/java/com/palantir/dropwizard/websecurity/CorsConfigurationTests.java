/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import io.dropwizard.validation.BaseValidator;
import io.dropwizard.validation.ValidationMethod;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.junit.Test;

/**
 * Tests for {@link CorsConfiguration}.
 */
public final class CorsConfigurationTests {

    private static final Validator VALIDATOR = BaseValidator.newValidator();

    @Test
    public void testDefaultValues() {
        assertEquals(CorsConfiguration.DEFAULT_ALLOWED_ORIGINS, CorsConfiguration.DEFAULT.allowedOrigins());
        assertEquals(CorsConfiguration.DEFAULT_ENABLED, CorsConfiguration.DEFAULT.enabled());
        assertFalse(CorsConfiguration.DEFAULT.allowCredentials().isPresent());
        assertFalse(CorsConfiguration.DEFAULT.allowedHeaders().isPresent());
        assertFalse(CorsConfiguration.DEFAULT.allowedMethods().isPresent());
        assertFalse(CorsConfiguration.DEFAULT.exposedHeaders().isPresent());
        assertFalse(CorsConfiguration.DEFAULT.preflightMaxAge().isPresent());
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
