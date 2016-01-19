/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.app;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

/**
 * Configuration class used to set the properties for a {@link AppSecurityFilter}.
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonDeserialize(as = ImmutableAppSecurityConfiguration.class)
@SuppressWarnings("checkstyle:designforextension")
public class AppSecurityConfiguration {

    public static final String DEFAULT_CONTENT_SECURITY_POLICY = "default-src 'self'";
    public static final String DEFAULT_CONTENT_TYPE_OPTIONS = "nosniff";
    public static final boolean DEFAULT_ENABLED = true;
    public static final String DEFAULT_FRAME_OPTIONS = "sameorigin";
    public static final String DEFAULT_XSS_PROTECTION = "1; mode=block";

    /**
     * Sentinel value used to turn off a given security header.
     */
    public static final String TURN_OFF = "";

    /**
     * Value to be returned in the response header {@link com.google.common.net.HttpHeaders#CONTENT_SECURITY_POLICY}.
     */
    @Value.Default
    public String contentSecurityPolicy() {
        return DEFAULT_CONTENT_SECURITY_POLICY;
    }

    /**
     * Value to be returned in the response header {@link com.google.common.net.HttpHeaders#X_CONTENT_TYPE_OPTIONS}.
     */
    @Value.Default
    public String contentTypeOptions() {
        return DEFAULT_CONTENT_TYPE_OPTIONS;
    }

    /**
     * Determines if {@link AppSecurityFilter} is applied. The default value is {@value #DEFAULT_ENABLED}.
     */
    @Value.Default
    public boolean enabled() {
        return DEFAULT_ENABLED;
    }

    /**
     * Value to be returned in the response header {@link com.google.common.net.HttpHeaders#X_FRAME_OPTIONS}.
     */
    @Value.Default
    public String frameOptions() {
        return DEFAULT_FRAME_OPTIONS;
    }

    /**
     * Value to be returned in the response header {@link com.google.common.net.HttpHeaders#X_XSS_PROTECTION}.
     */
    @Value.Default
    public String xssProtection() {
        return DEFAULT_XSS_PROTECTION;
    }

    /**
     * Provides a configuration with default values.
     */
    public static final AppSecurityConfiguration DEFAULT = new AppSecurityConfiguration.Builder().build();

    /**
     * Provides a configuration that is disabled.
     */
    public static final AppSecurityConfiguration DISABLED = new AppSecurityConfiguration.Builder()
            .enabled(false)
            .build();

    public static class Builder extends ImmutableAppSecurityConfiguration.Builder {}
}
