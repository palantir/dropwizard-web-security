/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used by the application's {@link io.dropwizard.Configuration} to provide a {@link WebSecurityConfiguration}.
 */
public interface WebSecurityConfigurable {

    /**
     * The {@link WebSecurityConfiguration}. It should be represented in the YAML file as {@code webSecurity}.
     */
    @JsonProperty("webSecurity")
    WebSecurityConfiguration getWebSecurityConfiguration();
}
