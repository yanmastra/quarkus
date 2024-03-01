package com.acme.authorization.utils;

import io.quarkus.runtime.util.StringUtil;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UriMatcher {

    private UriMatcher(){
    }

    private static Map<String, Matcher> matcherMap = null;

    public static Matcher getMatcher(String pathPattern) {
        try {
            if (matcherMap == null) matcherMap = new HashMap<>();
            if (!matcherMap.containsKey(pathPattern)) matcherMap.put(pathPattern, new Matcher(pathPattern));
            return matcherMap.get(pathPattern);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Pattern '%s' is not supported".formatted(pathPattern), e);
        }
    }

    public static boolean isMatch(String pathPattern, String path) {
        if (StringUtil.isNullOrEmpty(pathPattern)) return false;
        if (pathPattern.contains(",")) {
            String[] patterns = pathPattern.split(",");
            return isMatch(patterns, path);
        } else
            return UriMatcher.getMatcher(pathPattern).isMatch(path);
    }

    public static boolean isMatch(String[] pathPattern, String path) {
        if (pathPattern == null) return false;
        for (String pattern: pathPattern) {
            if (UriMatcher.getMatcher(pattern).isMatch(path)) {
                return true;
            }
        }
        return false;
    }

    public static class Matcher{
        private final PathMatcher matcher;

        public Matcher(String pathPattern) {
            matcher = FileSystems.getDefault().getPathMatcher("glob:"+pathPattern);
        }

        public boolean isMatch(String path) {
            return matcher.matches(Paths.get(path));
        }
    }
}
