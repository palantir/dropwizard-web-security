/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity.filters;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.net.HttpHeaders;
import com.palantir.websecurity.WebSecurityConfiguration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Injects different security headers based on a {@link WebSecurityConfiguration}. These headers include:
 * <ul>
 * <li>Content Security Policy (including support for IE 10 + 11)</li>
 * <li>Content Type Options</li>
 * <li>Frame Options</li>
 * <li>XSS Protection</li>
 * </ul>
 */
public final class AppSecurityHeaderInjector {

    public static final String DEFAULT_CONTENT_SECURITY_POLICY = "default-src 'self'";
    public static final String DEFAULT_CONTENT_TYPE_OPTIONS = "nosniff";
    public static final String DEFAULT_FRAME_OPTIONS = "sameorigin";
    public static final String DEFAULT_XSS_PROTECTION = "1; mode=block";

    public static final String HEADER_IE_X_CONTENT_SECURITY_POLICY = "X-Content-Security-Policy";
    public static final String USER_AGENT_IE_10 = "MSIE 10";
    public static final String USER_AGENT_IE_11 = "rv:11.0";

    private final String contentSecurityPolicy;
    private final String contentTypeOptions;
    private final String frameOptions;
    private final String xssProtection;

    public AppSecurityHeaderInjector(WebSecurityConfiguration config) {
        checkNotNull(config);

        this.contentSecurityPolicy = config.contentSecurityPolicy().or(DEFAULT_CONTENT_SECURITY_POLICY);
        this.contentTypeOptions = config.contentTypeOptions().or(DEFAULT_CONTENT_TYPE_OPTIONS);
        this.frameOptions = config.frameOptions().or(DEFAULT_FRAME_OPTIONS);
        this.xssProtection = config.xssProtection().or(DEFAULT_XSS_PROTECTION);
    }

    public void injectHeaders(HttpServletRequest request, HttpServletResponse response) {
        checkNotNull(request);
        checkNotNull(response);

        if (!this.contentSecurityPolicy.isEmpty()) {
            response.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, this.contentSecurityPolicy);

            String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
            if (userAgent != null) {
                // send the CSP header so that IE10 and IE11 recognise it
                if (userAgent.contains(USER_AGENT_IE_10) || userAgent.contains(USER_AGENT_IE_11)) {
                    response.setHeader(HEADER_IE_X_CONTENT_SECURITY_POLICY, this.contentSecurityPolicy);
                }
            }
        }

        if (!this.contentTypeOptions.isEmpty()) {
            response.setHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, this.contentTypeOptions);
        }

        if (!this.frameOptions.isEmpty()) {
            response.setHeader(HttpHeaders.X_FRAME_OPTIONS, this.frameOptions);
        }

        if (!this.xssProtection.isEmpty()) {
            response.setHeader(HttpHeaders.X_XSS_PROTECTION, this.xssProtection);
        }
    }
}
