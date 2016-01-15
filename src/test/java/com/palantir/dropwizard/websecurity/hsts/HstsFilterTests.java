/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.hsts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link HstsFilter}.
 */
public final class HstsFilterTests {

    private static final String HEADER_VALUE = "header value";

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);
    private final HstsFilter filter = new HstsFilter(HEADER_VALUE);

    @Test
    public void testWriteHeaderWhenSecure() throws IOException, ServletException {
        request.setSecure(true);
        this.filter.doFilter(request, response, chain);

        assertEquals(HEADER_VALUE, response.getHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY));
    }

    @Test
    public void testNoHeaderWhenNotSecure() throws IOException, ServletException {
        request.setSecure(false);
        this.filter.doFilter(request, response, chain);

        assertNull(response.getHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY));
    }
}
