/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.dropwizard.websecurity.app.AppSecurityConfiguration;
import com.palantir.dropwizard.websecurity.cors.CorsConfiguration;
import com.palantir.dropwizard.websecurity.hsts.HstsConfiguration;
import org.immutables.value.Value;

/**
 * Root-level Configuration for the {@link WebSecurityBundle}.
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonDeserialize(as = ImmutableWebSecurityConfiguration.class)
@SuppressWarnings("checkstyle:designforextension")
public abstract class WebSecurityConfiguration {

    @JsonProperty("cors")
    @Value.Default
    public CorsConfiguration cors() {
        return CorsConfiguration.DEFAULT;
    }

    @JsonProperty("appSecurity")
    @Value.Default
    public AppSecurityConfiguration appSecurity() {
        return AppSecurityConfiguration.DEFAULT;
    }

    @JsonProperty("hsts")
    @Value.Default
    public HstsConfiguration hsts() {
        return HstsConfiguration.DEFAULT;
    }

    /**
     * Provides a configuration with default values.
     */
    public static final WebSecurityConfiguration DEFAULT = new WebSecurityConfiguration.Builder().build();

    public static class Builder extends ImmutableWebSecurityConfiguration.Builder {}
}
