/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.app;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link AppSecurityFilter}.
 */
public final class AppSecurityFilterTests {

    private final HttpServletRequest request = new MockHttpServletRequest();
    private final HttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    public void testInjectInHttpServletRequests() throws IOException, ServletException {
        AppSecurityHeaderInjector injector = new AppSecurityHeaderInjector(AppSecurityConfiguration.DEFAULT);
        AppSecurityFilter filter = new AppSecurityFilter(injector);

        filter.doFilter(request, response, chain);

        assertEquals(AppSecurityConfiguration.DEFAULT_FRAME_OPTIONS, response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
    }
}
