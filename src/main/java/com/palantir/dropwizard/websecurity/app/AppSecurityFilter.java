/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.app;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class AppSecurityFilter implements Filter {

    private final AppSecurityHeaderInjector injector;

    public AppSecurityFilter(AppSecurityHeaderInjector injector) {
        checkNotNull(injector);
        this.injector = injector;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    @Override
    public void destroy() {
        // intentionally left blank
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            this.injector.injectHeaders((HttpServletRequest) request, (HttpServletResponse) response);
        }

        chain.doFilter(request, response);
    }
}
