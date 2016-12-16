/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Used by the application's {@link io.dropwizard.Configuration} to provide a {@link WebSecurityConfiguration}.
 */
public interface WebSecurityConfigurable {

    @NotNull
    @Valid
    WebSecurityConfiguration getWebSecurityConfiguration();
}
