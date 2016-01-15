/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link WebSecurityConfiguration}.
 */
public final class WebSecurityConfigurationTests {

    @Test
    public void testDefaults() {
        assertEquals(CorsConfiguration.DEFAULT, WebSecurityConfiguration.DEFAULT.cors());
    }
}
