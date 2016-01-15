/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

/**
 * Root-level Configuration for the {@link WebSecurityBundle}.
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonDeserialize(as = ImmutableWebSecurityConfiguration.class)
@SuppressWarnings("checkstyle:designforextension")
public abstract class WebSecurityConfiguration {

    /**
     * The {@link CorsConfiguration}, which is set to {@link CorsConfiguration#DEFAULT} if not set.
     */
    @JsonProperty("cors")
    @Value.Default
    public CorsConfiguration cors() {
        return CorsConfiguration.DEFAULT;
    }

    /**
     * Provides a configuration with default values.
     */
    public static final WebSecurityConfiguration DEFAULT = new WebSecurityConfiguration.Builder().build();

    public static class Builder extends ImmutableWebSecurityConfiguration.Builder {}
}
