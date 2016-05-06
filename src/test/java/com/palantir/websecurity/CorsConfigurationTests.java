/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity;

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
    public void testAllowedOrigins_allowStar() {
        CorsConfiguration config = CorsConfiguration.builder().allowedOrigins("*").build();

        Set<ConstraintViolation<CorsConfiguration>> violations = VALIDATOR.validate(config);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testAllowedOrigins_invalidUrl() {
        CorsConfiguration config = CorsConfiguration.builder()
                .allowedOrigins("http://good.url,:/123/this/is/not/a.valid.url")
                .build();

        ConstraintViolation<CorsConfiguration> violation = getFirstViolation(VALIDATOR.validate(config));
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod);
    }

    @Test
    public void testAllowedOrigins_urlCannotHavePath() {
        CorsConfiguration config = CorsConfiguration.builder()
                .allowedOrigins("http://good.url,http://url.with.path/")
                .build();

        ConstraintViolation<CorsConfiguration> violation = getFirstViolation(VALIDATOR.validate(config));
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod);
    }

    @Test
    public void testAllowedOrigins_validRegex() {
        CorsConfiguration config = CorsConfiguration.builder()
                .allowedOrigins("http://*(dfdfd).$")
                .build();

        Set<ConstraintViolation<CorsConfiguration>> violations = VALIDATOR.validate(config);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testAllowedOrigins_invalidRegex() {
        CorsConfiguration config = CorsConfiguration.builder()
                .allowedOrigins("http://*(dfdfd")
                .build();

        ConstraintViolation<CorsConfiguration> violation = getFirstViolation(VALIDATOR.validate(config));
        assertTrue(violation.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod);
    }

    @Test
    public void testPreflightMaxAge_cannotBeNegative() {
        CorsConfiguration config = CorsConfiguration.builder()
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
