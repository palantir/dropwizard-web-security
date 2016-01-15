/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.websecurity.app;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.net.HttpHeaders;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class AppSecurityHeaderInjector {

    public static final String HEADER_IE_X_CONTENT_SECURITY_POLICY = "X-Content-Security-Policy";
    public static final String USER_AGENT_IE_10 = "MSIE 10";
    public static final String USER_AGENT_IE_11 = "rv:11.0";

    private final AppSecurityConfiguration config;

    public AppSecurityHeaderInjector(AppSecurityConfiguration config) {
        checkNotNull(config);
        this.config = config;
    }

    public void injectHeaders(HttpServletRequest request, HttpServletResponse response) {
        checkNotNull(request);
        checkNotNull(response);

        if (!this.config.enabled()) {
            return;
        }

        if (!AppSecurityConfiguration.TURN_OFF.equals(this.config.contentSecurityPolicy())) {
            response.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, this.config.contentSecurityPolicy());

            String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
            if (userAgent != null) {
                // send the CSP header so that IE10 and IE11 recognise it
                if (userAgent.contains(USER_AGENT_IE_10) || userAgent.contains(USER_AGENT_IE_11)) {
                    response.setHeader(HEADER_IE_X_CONTENT_SECURITY_POLICY, this.config.contentSecurityPolicy());
                }
            }
        }

        if (!AppSecurityConfiguration.TURN_OFF.equals(this.config.contentTypeOptions())) {
            response.setHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, this.config.contentTypeOptions());
        }

        if (!AppSecurityConfiguration.TURN_OFF.equals(this.config.frameOptions())) {
            response.setHeader(HttpHeaders.X_FRAME_OPTIONS, this.config.frameOptions());
        }

        if (!AppSecurityConfiguration.TURN_OFF.equals(this.config.xssProtection())) {
            response.setHeader(HttpHeaders.X_XSS_PROTECTION, this.config.xssProtection());
        }
    }
}
