/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Tests for {@link AppSecurityConfiguration}.
 */
public final class AppSecurityConfigurationTests {

    @Test
    public void testDefaultValues() {
        AppSecurityConfiguration defaultConfig = AppSecurityConfiguration.DEFAULT;

        assertEquals(AppSecurityConfiguration.DEFAULT_CONTENT_SECURITY_POLICY, defaultConfig.contentSecurityPolicy());
        assertEquals(AppSecurityConfiguration.DEFAULT_CONTENT_TYPE_OPTIONS, defaultConfig.contentTypeOptions());
        assertEquals(AppSecurityConfiguration.DEFAULT_ENABLED, defaultConfig.enabled());
        assertEquals(AppSecurityConfiguration.DEFAULT_FRAME_OPTIONS, defaultConfig.frameOptions());
        assertEquals(AppSecurityConfiguration.DEFAULT_XSS_PROTECTION, defaultConfig.xssProtection());
    }

    @Test
    public void testDisabled() {
        assertFalse(AppSecurityConfiguration.DISABLED.enabled());
    }
}
