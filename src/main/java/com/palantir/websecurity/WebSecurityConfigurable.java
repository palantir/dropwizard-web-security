/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Used by the application's {@link io.dropwizard.Configuration} to provide a {@link WebSecurityConfiguration}.
 */
public interface WebSecurityConfigurable {

    @JsonProperty("webSecurity")
    @NotNull
    @Valid
    WebSecurityConfiguration getWebSecurityConfiguration();
}
