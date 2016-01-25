/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import io.dropwizard.validation.ValidationMethod;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.immutables.value.Value;

/**
 * Configuration class used to set the properties for a {@link CrossOriginFilter}. If a value is not set it will not be
 * passed in as an initial parameter.
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonDeserialize(as = ImmutableCorsConfiguration.class)
@SuppressWarnings("checkstyle:designforextension")
public abstract class CorsConfiguration {

    private static final String DISABLED_ORIGINS = "";

    /**
     * If set, will be used to set the initial property {@code allowCredentials}.
     */
    public abstract Optional<Boolean> allowCredentials();

    /**
     * If set, will be used to set the initial property {@code allowedHeaders}.
     */
    public abstract Optional<String> allowedHeaders();

    /**
     * If set, will be used to set the initial property {@code allowedMethods}.
     */
    public abstract Optional<String> allowedMethods();

    /**
     * If set, will be used to set the initial property {@code allowedOrigins}.
     */
    public abstract Optional<String> allowedOrigins();

    /**
     * Determines if {@link CrossOriginFilter} is applied. Returns true if there is an {@link #allowedOrigins()} value
     * set to a non-empty string, false otherwise.
     */
    @Value.Derived
    public boolean enabled() {
        return !allowedOrigins().or(DISABLED_ORIGINS).isEmpty();
    }

    /**
     * If set, will be used to set the initial property {@code exposedHeaders}.
     */
    public abstract Optional<String> exposedHeaders();

    /**
     * If set, will be used to set the initial property {@code preflightMaxAge}.
     */
    public abstract Optional<Long> preflightMaxAge();

    @ValidationMethod(message = "preflightMaxAge can't be negative")
    private boolean isPreflightMaxAgeNegative() {
        return preflightMaxAge().or(0L) >= 0L;
    }

    @ValidationMethod(message = "allowedOrigins can't contain malformed URLs, URLs with a path, or malformed regex")
    private boolean isAllowedOriginsValid() {
        if (!allowedOrigins().isPresent()) {
            return true;
        }

        if ("*".equals(allowedOrigins().get())) {
            return true;
        }

        List<String> origins = Splitter.on(",")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(allowedOrigins().get());

        for (String origin : origins) {
            if (!validateOrigin(origin)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the origin, by the following rules: <ul> <li>Origins that are just {@code "*"} are not passed
     * through</li> <li>If it contains a {@code *}, then it must be a valid regular expression.</li> <li>It must be a
     * valid URL, without a path component</li> </ul>
     */
    private static boolean validateOrigin(String origin) {
        try {
            // ensure the regex is a valid pattern
            if (origin.contains("*")) {
                Pattern.compile(origin);
                return true;
            }

            // ensure there is no path in the valid URL
            if (new URL(origin).getPath().length() > 0) {
                return false;
            }

        } catch (PatternSyntaxException e) {
            return false;

        } catch (MalformedURLException e) {
            return false;
        }

        return true;
    }

    /**
     * Provides a configuration with default values.
     */
    public static final CorsConfiguration DEFAULT = new CorsConfiguration.Builder().build();

    /**
     * Provides a configuration that is disabled.
     */
    public static final CorsConfiguration DISABLED = new CorsConfiguration.Builder()
            .allowedOrigins(DISABLED_ORIGINS)
            .build();

    public static class Builder extends ImmutableCorsConfiguration.Builder {}
}
