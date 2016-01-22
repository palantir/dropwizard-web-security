/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.filters;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A filter that adds the {@code Strict-Transport-Security} header if the {@link HttpServletRequest#isSecure()} is true.
 */
public final class HstsFilter implements Filter {

    private final String headerValue;

    public HstsFilter(String headerValue) {
        checkNotNull(headerValue);
        this.headerValue = headerValue;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void destroy() {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        checkNotNull(request);
        checkNotNull(response);
        checkNotNull(chain);

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (request.isSecure()) {
                httpResponse.setHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY, this.headerValue);
            }
        }

        chain.doFilter(request, response);
    }
}
