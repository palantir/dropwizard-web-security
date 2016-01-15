/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.hsts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Tests for {@link HstsConfiguration}.
 */
public final class HstsConfigurationTests {

    @Test
    public void testDefaultValues() {
        assertEquals(HstsConfiguration.DEFAULT_ENABLED, HstsConfiguration.DEFAULT.enabled());
        assertEquals(HstsConfiguration.DEFAULT_HEADER_VALUE, HstsConfiguration.DEFAULT.headerValue());
    }

    @Test
    public void testDisabled() {
        assertFalse(HstsConfiguration.DISABLED.enabled());
    }
}
