package com.acme.authorization.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.runtime.util.StringUtil;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class KeyValueCacheUtils {
    private static final String CACHE_DIR = "/.cache_v1";

    private static Logger logger = Logger.getLogger(KeyValueCacheUtils.class.getName());

    public static synchronized void putCache(String cacheName, String key, String value){
        saveCache(cacheName, key, value, CacheUpdateMode.ADD);
    }

    public static synchronized void removeCache(String cacheName, String key) {
        saveCache(cacheName, key, "", CacheUpdateMode.REMOVE);
    }

    public static synchronized void saveCache(String cacheName, String key, String value, CacheUpdateMode cacheUpdateMode) {
//        if (StringUtil.isNullOrEmpty(key) || key.contains("=") || value.contains("=") || cacheUpdateMode == null)
//            throw new IllegalArgumentException("key or value contain not supported character!, (\"=\",\";\")");
        if (StringUtil.isNullOrEmpty(key) || cacheUpdateMode == null)
            throw new IllegalArgumentException("key can't be empty and cacheUpdateMode can't be null");

        if (StringUtil.isNullOrEmpty(value)) value = "";

        Map<String, String> mapLine = Map.of("key", key, "value", value);
        String sKey = JsonUtils.toJson(Map.of("key", key));
        sKey = sKey.substring(1, sKey.length()-1);
        String sLine = JsonUtils.toJson(mapLine);

        File file = getCacheFileName(cacheName);
        StringBuilder cache = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean hasReplaced = false;
            while ((line = reader.readLine()) != null) {
                if (cacheUpdateMode == CacheUpdateMode.REPLACE || cacheUpdateMode == CacheUpdateMode.ADD) {
                    if (!StringUtil.isNullOrEmpty(line) && line.startsWith(key + '=')) {
//                        cache.append(key).append('=').append(value);
                        cache.append(sLine);
                        hasReplaced = true;
                        cache.append('\n');
                    } else {
                        cache.append(line);
                        cache.append('\n');
                    }
                } else if (cacheUpdateMode == CacheUpdateMode.REMOVE) {
                    if (!(!StringUtil.isNullOrEmpty(line) && line.contains(sKey))) {
                        cache.append(line);
                        cache.append('\n');
                    }
                }
            }

            if (!hasReplaced && cacheUpdateMode == CacheUpdateMode.ADD) {
                cache.append(sLine);
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

    public static synchronized String findCache(String cacheName, String key) {
        File file = getCacheFileName(cacheName);

        String sKey = JsonUtils.toJson(Map.of("key", key));
        sKey = sKey.substring(1, sKey.length()-1);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtil.isNullOrEmpty(line) && line.contains(sKey)) {
//                    String[] lines = line.split("=");
//                    reader.close();
//                    return lines[1];
                    Map<String, String> mapLine = JsonUtils.fromJson(line, new TypeReference<>() {
                    });
                    return mapLine.get("value");
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

    private static String getCacheDir() {
        String cacheDir = null;
        try {
            cacheDir = ConfigProvider.getConfig().getConfigValue("cache_directory").getValue();
        }catch (Exception e) {
            logger.warn(e.getMessage());
        }
        if (StringUtil.isNullOrEmpty(cacheDir)) System.getenv("CACHE_DIRECTORY");
        if (StringUtil.isNullOrEmpty(cacheDir)) cacheDir = System.getenv("user.dir");
        return cacheDir;
    }

    private static File getCacheFileName(String cacheName) {
        String cacheFileName = ".cache." + cacheName;
        File dir = checkPath(getCacheDir() + CACHE_DIR);
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

    public static String hide(String s) {
        if (StringUtil.isNullOrEmpty(s)) return null;
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String showHiddenString(String s) {
        return new String(Base64.getDecoder().decode(s));
    }
}
