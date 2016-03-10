/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity.filters;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.websecurity.WebSecurityConfiguration;
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
 * A filter that injects the App Security headers using a {@link WebSecurityHeaderInjector} to all requests except for
 * those on the {@link #jerseyRoot} path.
 */
public final class JerseyAwareWebSecurityFilter implements Filter {

    private final WebSecurityHeaderInjector injector;
    private final String jerseyRoot;

    public JerseyAwareWebSecurityFilter(WebSecurityConfiguration config, String jerseyRoot) {
        checkNotNull(config);
        checkNotNull(jerseyRoot);

        this.injector = new WebSecurityHeaderInjector(config);
        this.jerseyRoot = cleanJerseyRoot(jerseyRoot);
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

        checkNotNull(request);
        checkNotNull(response);
        checkNotNull(chain);

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            if (!isJerseyRequest(httpRequest)) {
                this.injector.injectHeaders(httpRequest, (HttpServletResponse) response);
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isJerseyRequest(HttpServletRequest request) {
        String cleanedServletPath = cleanJerseyRoot(request.getServletPath().toLowerCase());
        return this.jerseyRoot.equals(cleanedServletPath);
    }

    /**
     * Cleans the Jersey root path to start with a slash and end without a star or slash.
     */
    private static String cleanJerseyRoot(String rawJerseyRoot) {
        String cleaned = rawJerseyRoot;

        if (cleaned.endsWith("*")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        if (!cleaned.startsWith("/")) {
            cleaned = "/" + cleaned;
        }

        return cleaned;
    }
}
