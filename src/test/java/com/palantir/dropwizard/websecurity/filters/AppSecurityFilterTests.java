/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import com.google.common.net.HttpHeaders;
import com.palantir.dropwizard.websecurity.WebSecurityConfiguration;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link AppSecurityFilter}.
 */
public final class AppSecurityFilterTests {

    private final WebSecurityConfiguration config = new WebSecurityConfiguration.Builder().build();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    public void testInjectInHttpServletRequests() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");

        AppSecurityFilter filter = new AppSecurityFilter(this.config, "/jersey/root/*");
        request.setPathInfo("/api");

        filter.doFilter(request, response, chain);

        // only testing 1 header, since the AppSecurityHeaderInjector is tested separately
        assertEquals(AppSecurityHeaderInjector.DEFAULT_FRAME_OPTIONS, response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
    }

    @Test
    public void testNotInjectForJerseyPathWithStar() throws IOException, ServletException {
        AppSecurityFilter filter = new AppSecurityFilter(this.config, "/api/*");
        assertNotInjecting(filter);
    }

    @Test
    public void testNotInjectForJerseyPathNoStar() throws IOException, ServletException {
        AppSecurityFilter filter = new AppSecurityFilter(this.config, "/api/");
        assertNotInjecting(filter);
    }

    @Test
    public void testNotInjectForJerseyPathNoSlash() throws IOException, ServletException {
        AppSecurityFilter filter = new AppSecurityFilter(this.config, "/api");
        assertNotInjecting(filter);
    }

    private void assertNotInjecting(AppSecurityFilter filter) throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/hello");
        // the servlet path is used to check if the request is for Jersey
        request.setServletPath("/api");

        filter.doFilter(request, response, chain);

        assertNull(response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
    }
}
