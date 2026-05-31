package com.eactive.resourcehub.common.util;

import java.net.URI;

public final class RedirectUtils {

    private RedirectUtils() {}

    /** Referer 헤더에서 경로만 추출해 open redirect를 방지한다. */
    public static String safeReferer(String referer, String fallback) {
        if (referer == null || referer.isBlank()) return fallback;
        if (referer.startsWith("/") && !referer.startsWith("//")) return referer;
        try {
            URI uri = new URI(referer);
            String path = uri.getRawPath();
            if (path == null || path.isBlank()) return fallback;
            String query = uri.getRawQuery();
            return query != null ? path + "?" + query : path;
        } catch (Exception e) {
            return fallback;
        }
    }
}
