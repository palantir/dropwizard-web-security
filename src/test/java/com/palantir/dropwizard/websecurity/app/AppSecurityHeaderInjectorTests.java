/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.net.HttpHeaders;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link AppSecurityHeaderInjector}.
 */
public final class AppSecurityHeaderInjectorTests {

    private static final String USER_AGENT_NOT_IE = "not-ie-10-or-11";
    private static final String TEST_VALUE = "test";

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    public void testDisabledNoHeaders() {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(AppSecurityConfiguration.DISABLED);

        injector.injectHeaders(request, response);

        assertNull(response.getHeader(HttpHeaders.CONTENT_SECURITY_POLICY));
        assertNull(response.getHeader(AppSecurityHeaderInjector.HEADER_IE_X_CONTENT_SECURITY_POLICY));
        assertNull(response.getHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS));
        assertNull(response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
        assertNull(response.getHeader(HttpHeaders.X_XSS_PROTECTION));
    }

    @Test
    public void testHeadersReplacedNotAppended() {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(AppSecurityConfiguration.DEFAULT);

        request.addHeader(HttpHeaders.USER_AGENT, AppSecurityHeaderInjector.USER_AGENT_IE_10);

        response.addHeader(HttpHeaders.CONTENT_SECURITY_POLICY, TEST_VALUE);
        response.addHeader(AppSecurityHeaderInjector.HEADER_IE_X_CONTENT_SECURITY_POLICY, TEST_VALUE);
        response.addHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, TEST_VALUE);
        response.addHeader(HttpHeaders.X_FRAME_OPTIONS, TEST_VALUE);
        response.addHeader(HttpHeaders.X_XSS_PROTECTION, TEST_VALUE);

        injector.injectHeaders(request, response);

        assertEquals(1, response.getHeaders(HttpHeaders.CONTENT_SECURITY_POLICY).size());
        assertEquals(1, response.getHeaders(AppSecurityHeaderInjector.HEADER_IE_X_CONTENT_SECURITY_POLICY).size());
        assertEquals(1, response.getHeaders(HttpHeaders.X_CONTENT_TYPE_OPTIONS).size());
        assertEquals(1, response.getHeaders(HttpHeaders.X_FRAME_OPTIONS).size());
        assertEquals(1, response.getHeaders(HttpHeaders.X_XSS_PROTECTION).size());
    }

    @Test
    public void testContentSecurityPolicyNonIe10or11() {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(new AppSecurityConfiguration.Builder()
                .contentSecurityPolicy(TEST_VALUE)
                .build());

        request.addHeader(HttpHeaders.USER_AGENT, USER_AGENT_NOT_IE);

        injector.injectHeaders(request, response);

        assertEquals(TEST_VALUE, response.getHeader(HttpHeaders.CONTENT_SECURITY_POLICY));
        assertNull(response.getHeader(AppSecurityHeaderInjector.HEADER_IE_X_CONTENT_SECURITY_POLICY));
    }

    @Test
    public void testContentSecurityPolicyIe10() {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(new AppSecurityConfiguration.Builder()
                .contentSecurityPolicy(TEST_VALUE)
                .build());

        request.addHeader(HttpHeaders.USER_AGENT, AppSecurityHeaderInjector.USER_AGENT_IE_10);

        injector.injectHeaders(request, response);

        assertEquals(TEST_VALUE, response.getHeader(HttpHeaders.CONTENT_SECURITY_POLICY));
        assertEquals(TEST_VALUE, response.getHeader(AppSecurityHeaderInjector.HEADER_IE_X_CONTENT_SECURITY_POLICY));
    }

    @Test
    public void testContentSecurityPolicyIe11() {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(new AppSecurityConfiguration.Builder()
                .contentSecurityPolicy(TEST_VALUE)
                .build());

        request.addHeader(HttpHeaders.USER_AGENT, AppSecurityHeaderInjector.USER_AGENT_IE_11);

        injector.injectHeaders(request, response);

        assertEquals(TEST_VALUE, response.getHeader(HttpHeaders.CONTENT_SECURITY_POLICY));
        assertEquals(TEST_VALUE, response.getHeader(AppSecurityHeaderInjector.HEADER_IE_X_CONTENT_SECURITY_POLICY));
    }

    @Test
    public void testContentTypeOptions() {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(new AppSecurityConfiguration.Builder()
                .contentTypeOptions(TEST_VALUE)
                .build());

        injector.injectHeaders(request, response);

        assertEquals(TEST_VALUE, response.getHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS));
    }

    @Test
    public void testFrameOptions() {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(new AppSecurityConfiguration.Builder()
                .frameOptions(TEST_VALUE)
                .build());

        injector.injectHeaders(request, response);

        assertEquals(TEST_VALUE, response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
    }

    @Test
    public void testXssProtection() {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(new AppSecurityConfiguration.Builder()
                .xssProtection(TEST_VALUE)
                .build());

        injector.injectHeaders(request, response);

        assertEquals(TEST_VALUE, response.getHeader(HttpHeaders.X_XSS_PROTECTION));
    }

    @Test
    public void testTurningHeadersOff() {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(new AppSecurityConfiguration.Builder()
                .contentSecurityPolicy(AppSecurityConfiguration.TURN_OFF)
                .contentTypeOptions(AppSecurityConfiguration.TURN_OFF)
                .frameOptions(AppSecurityConfiguration.TURN_OFF)
                .xssProtection(AppSecurityConfiguration.TURN_OFF)
                .build());

        request.addHeader(HttpHeaders.USER_AGENT, AppSecurityHeaderInjector.USER_AGENT_IE_10);

        injector.injectHeaders(request, response);

        assertNull(response.getHeader(HttpHeaders.CONTENT_SECURITY_POLICY));
        assertNull(response.getHeader(AppSecurityHeaderInjector.HEADER_IE_X_CONTENT_SECURITY_POLICY));
        assertNull(response.getHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS));
        assertNull(response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
        assertNull(response.getHeader(HttpHeaders.X_XSS_PROTECTION));
    }
}
