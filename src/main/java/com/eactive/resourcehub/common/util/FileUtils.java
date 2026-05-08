package com.eactive.resourcehub.common.util;

import java.util.Set;

public final class FileUtils {

    private FileUtils() {}

    public static String extension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1).toLowerCase() : "";
    }

    public static boolean isAllowedExtension(String filename, Set<String> allowed) {
        return allowed.contains(extension(filename));
    }
}
