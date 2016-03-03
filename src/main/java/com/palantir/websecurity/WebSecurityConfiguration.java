/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;

/**
 * Root-level Configuration for the {@link WebSecurityBundle}.
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonDeserialize(as = ImmutableWebSecurityConfiguration.class)
public abstract class WebSecurityConfiguration {

    public static final String TURN_OFF = "";

    /**
     * Value to be returned in the response header {@link com.google.common.net.HttpHeaders#CONTENT_SECURITY_POLICY}.
     */
    public abstract Optional<String> contentSecurityPolicy();

    /**
     * Value to be returned in the response header {@link com.google.common.net.HttpHeaders#X_CONTENT_TYPE_OPTIONS}.
     */
    public abstract Optional<String> contentTypeOptions();

    /**
     * Value to be returned in the response header {@link com.google.common.net.HttpHeaders#X_FRAME_OPTIONS}.
     */
    public abstract Optional<String> frameOptions();

    /**
     * Value to be returned in the response header {@link com.google.common.net.HttpHeaders#X_XSS_PROTECTION}.
     */
    public abstract Optional<String> xssProtection();

    /**
     * Configuration for CORS functionality.
     */
    public abstract Optional<CorsConfiguration> cors();

    public static class Builder extends ImmutableWebSecurityConfiguration.Builder {}
}
