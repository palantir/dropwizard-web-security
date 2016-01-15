/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.validation.ValidationMethod;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
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

    /**
     * The default value of {@link #allowedOrigins()}. It's set to {@code ""}, blocking all origins to provide a secure
     * out-of-the-box experience.
     */
    public static final String DEFAULT_ALLOWED_ORIGINS = "";

    /**
     * The default value for {@link #enabled()}.
     */
    public static final boolean DEFAULT_ENABLED = true;

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
     * Used to set the initial property {@code allowedOrigins}. The default value is {@value #DEFAULT_ALLOWED_ORIGINS}.
     */
    @Value.Default
    public String allowedOrigins() {
        return DEFAULT_ALLOWED_ORIGINS;
    }

    /**
     * Determines if {@link CrossOriginFilter} is applied. The default value is {@value #DEFAULT_ENABLED}.
     */
    @Value.Default
    public boolean enabled() {
        return DEFAULT_ENABLED;
    }

    /**
     * If set, will be used to set the initial property {@code exposedHeaders}.
     */
    public abstract Optional<String> exposedHeaders();

    /**
     * If set, will be used to set the initial property {@code preflightMaxAge}.
     */
    public abstract Optional<Long> preflightMaxAge();

    /**
     * Constructs a property map used to initiate the {@link CrossOriginFilter}.
     */
    public Map<String, String> getPropertyMap() {
        ImmutableMap.Builder<String, String> propertiesBuilder = ImmutableMap.builder();

        propertiesBuilder.put(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, allowedOrigins());

        if (allowedMethods().isPresent()) {
            propertiesBuilder.put(CrossOriginFilter.ALLOWED_METHODS_PARAM, allowedMethods().get());
        }

        if (allowedHeaders().isPresent()) {
            propertiesBuilder.put(CrossOriginFilter.ALLOWED_HEADERS_PARAM, allowedHeaders().get());
        }

        if (preflightMaxAge().isPresent()) {
            propertiesBuilder.put(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, Long.toString(preflightMaxAge().get()));
        }

        if (allowCredentials().isPresent()) {
            propertiesBuilder.put(
                    CrossOriginFilter.ALLOW_CREDENTIALS_PARAM,
                    Boolean.toString(allowCredentials().get()));
        }

        if (exposedHeaders().isPresent()) {
            propertiesBuilder.put(CrossOriginFilter.EXPOSED_HEADERS_PARAM, exposedHeaders().get());
        }

        return propertiesBuilder.build();
    }

    @ValidationMethod(message = "preflightMaxAge can't be negative")
    private boolean isPreflightMaxAgeNegative() {
        return preflightMaxAge().or(0L) >= 0L;
    }

    @ValidationMethod(message = "allowedOrigins can't contain malformed URLs, URLs with a path, or malformed regex")
    private boolean isAllowedOriginsValid() {
        if ("*".equals(allowedOrigins())) {
            return true;
        }

        List<String> origins = Splitter.on(",")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(allowedOrigins());

        for (String origin : origins) {
            if (!validateOrigin(origin)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the origin, by the following rules: <ul> <li>If the origin contains a {@code *}, then it must be a
     * valid regular expression.</li> <li>It must be a valid URL, without a path component</li> </ul>
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
    public static final CorsConfiguration DISABLED = new CorsConfiguration.Builder().enabled(false).build();

    public static class Builder extends ImmutableCorsConfiguration.Builder {}
}
