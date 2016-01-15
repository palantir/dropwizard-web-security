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
public final class AppConfigurationTests {

    @Test
    public void testDefaultValues() {
        assertEquals(
                AppSecurityConfiguration.DEFAULT_CONTENT_SECURITY_POLICY,
                AppSecurityConfiguration.DEFAULT.contentSecurityPolicy());
        assertEquals(
                AppSecurityConfiguration.DEFAULT_CONTENT_TYPE_OPTIONS,
                AppSecurityConfiguration.DEFAULT.contentTypeOptions());
        assertEquals(AppSecurityConfiguration.DEFAULT_ENABLED, AppSecurityConfiguration.DEFAULT.enabled());
        assertEquals(AppSecurityConfiguration.DEFAULT_FRAME_OPTIONS, AppSecurityConfiguration.DEFAULT.frameOptions());
        assertEquals(AppSecurityConfiguration.DEFAULT_XSS_PROTECTION, AppSecurityConfiguration.DEFAULT.xssProtection());
    }

    @Test
    public void testDisabled() {
        assertFalse(AppSecurityConfiguration.DISABLED.enabled());
    }
}
