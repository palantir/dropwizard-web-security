/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.hsts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonDeserialize(as = ImmutableHstsConfiguration.class)
@SuppressWarnings("checkstyle:designforextension")
public class HstsConfiguration {

    public static final boolean DEFAULT_ENABLED = true;
    public static final String DEFAULT_HEADER_VALUE = "";

    @Value.Default
    public boolean enabled() {
        return DEFAULT_ENABLED;
    }

    @Value.Default
    public String headerValue() {
        return DEFAULT_HEADER_VALUE;
    }

    /**
     * Provides a configuration with default values.
     */
    public static final HstsConfiguration DEFAULT = new HstsConfiguration.Builder().build();

    /**
     * Provides a configuration that is disabled.
     */
    public static final HstsConfiguration DISABLED = new HstsConfiguration.Builder().enabled(false).build();

    public static class Builder extends ImmutableHstsConfiguration.Builder {}
}
