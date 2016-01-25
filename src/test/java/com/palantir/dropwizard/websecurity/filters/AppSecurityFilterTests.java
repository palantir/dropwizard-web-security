/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.filters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.google.common.net.HttpHeaders;
import com.palantir.dropwizard.websecurity.WebSecurityConfiguration;
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
        WebSecurityConfiguration config = new WebSecurityConfiguration.Builder().build();
        AppSecurityFilter filter = new AppSecurityFilter(config);

        filter.doFilter(request, response, chain);

        // only testing 1 header, since the AppSecurityHeaderInjector is tested separately
        assertEquals(AppSecurityHeaderInjector.DEFAULT_FRAME_OPTIONS, response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
    }
}
