package com.acme.authorization.utils;

import io.quarkus.runtime.util.StringUtil;
import org.jboss.logging.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class KeyValueCacheUtils {
    private static final String WORKING_DIR = System.getenv("CACHE_DIRECTORY");
    private static final String CACHE_DIR = "/.cache";

    private static Logger logger = Logger.getLogger(KeyValueCacheUtils.class.getName());

    public static void saveCache(String cacheName, String key, String value, CacheUpdateMode cacheUpdateMode) {
        if (StringUtil.isNullOrEmpty(key) || key.contains("=") || StringUtil.isNullOrEmpty(value) || value.contains("=") || cacheUpdateMode == null)
            throw new IllegalArgumentException("key or value contain not supported character!, (\"=\",\";\")");

        File file = getCacheFileName(cacheName);
        StringBuilder cache = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean hasReplaced = false;
            while ((line = reader.readLine()) != null) {
                if (cacheUpdateMode == CacheUpdateMode.REPLACE || cacheUpdateMode == CacheUpdateMode.ADD) {
                    if (!StringUtil.isNullOrEmpty(line) && line.startsWith(key + '=')) {
                        cache.append(key).append('=').append(value);
                        hasReplaced = true;
                        cache.append('\n');
                    } else {
                        cache.append(line);
                        cache.append('\n');
                    }
                } else if (cacheUpdateMode == CacheUpdateMode.REMOVE) {
                    if (!(!StringUtil.isNullOrEmpty(line) && line.startsWith(key + '='))) {
                        cache.append(line);
                        cache.append('\n');
                    }
                }
            }

            if (!hasReplaced && cacheUpdateMode == CacheUpdateMode.ADD) {
                cache.append(key).append('=').append(value).append('\n');
            }
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new RuntimeException(ioe);
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(cache.toString());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String findCache(String cacheName, String key) {
        File file = getCacheFileName(cacheName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtil.isNullOrEmpty(line) && line.startsWith(key)) {
                    String[] lines = line.split("=");
                    reader.close();
                    return lines[1];
                }
            }

            reader.close();
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Error finding cache:"+file.getAbsolutePath()+", cache:"+cacheName+"/"+key, e);
        }
    }

    private static File checkPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            boolean result = file.mkdir();
            if (!result) {
                try {
                    String[] pathSegment = path.split("/");
                    File root = null;
                    for (String segment : pathSegment) {
                        if (root == null) {
                            if (path.startsWith("/"))
                                root = new File("/" + segment);
                            else
                                root = new File(segment);
                        } else {
                            root = new File(root, segment);
                        }
                        if (root.exists()) continue;
                        boolean subResult = root.mkdir();
                    }
                } catch (Exception e){
                    logger.error(e.getMessage(), e);
                    file = new File(System.getProperty("user.dir"));
                }
            }
        }
        return file;
    }

    private static File getCacheFileName(String cacheName) {
        String cacheFileName = ".cache." + cacheName;
        File dir = checkPath(WORKING_DIR + CACHE_DIR);
        File file = new File(dir, cacheFileName);
        if (!file.exists()) {
            try {
                boolean result = file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }
}
