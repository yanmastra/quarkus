package org.acme.authenticationService.resources.web;

import com.acme.authorization.utils.Constants;
import io.quarkus.runtime.util.StringUtil;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.authenticationService.dao.web.BaseModel;

import java.util.*;

public class WebUtils {

    public static Response.ResponseBuilder createOkResponse(Map<String, String> cookies, Object entity) {
        return createOkResponse(cookies, entity, null);
    }
    public static Response.ResponseBuilder createOkResponse(Map<String, String> cookies, Object entity, Set<String> removeCookies) {
        Response.ResponseBuilder responseBuilder = Response.ok();
        List<NewCookie> cookies1 = new ArrayList<>();
        buildCookies(cookies1, cookies);
        buildRemoveCookie(cookies1, removeCookies);

        if (!cookies1.isEmpty()) {
            responseBuilder.cookie(cookies1.toArray(new NewCookie[]{}));
        }

        return responseBuilder.entity(entity);
    }

    private static void buildCookies(List<NewCookie> cookies1, Map<String, String> cookies) {
        assert cookies1 != null:
                new IllegalArgumentException("cookies1 == null");

        if (cookies != null) {
            for (String key: cookies.keySet()) {
                cookies1.add(new NewCookie.Builder(key).value(cookies.get(key))
                        .path(Constants.PATH_AUTHORIZATION_COOKIE)
                        .secure(false)
                        .build());
            }
        }
    }

    private static void buildRemoveCookie(List<NewCookie> cookies1, Set<String> removeCookies) {
        assert cookies1 != null:
                new IllegalArgumentException("cookies1 == null");

        if (removeCookies != null) {
            for (String key: removeCookies) {
                cookies1.add(new NewCookie.Builder(key)
                        .path(Constants.PATH_AUTHORIZATION_COOKIE)
                        .value(null)
                        .expiry(new Date())
                        .build());
            }
        }
    }

    public static Response.ResponseBuilder createRedirectResponse(Map<String, String> cookies, String location) {
        return createRedirectResponse(cookies, location, null);
    }

    public static Response.ResponseBuilder createRedirectResponse(Map<String, String> cookies, String location, Set<String> removeCookies) {
        Response.ResponseBuilder responseBuilder = Response.status(302);

        List<NewCookie> cookies1 = new ArrayList<>();
        buildCookies(cookies1, cookies);
        buildRemoveCookie(cookies1, removeCookies);

        if (!cookies1.isEmpty()) {
            responseBuilder.cookie(cookies1.toArray(new NewCookie[]{}));
        }

        return responseBuilder.header("Location", location);
    }

    public static String logCookies(ContainerRequestContext context) {
        if (context.getCookies() == null || context.getCookies().isEmpty()) return "";

        StringBuilder logMsg = new StringBuilder();
        for (String key: context.getCookies().keySet()) {
            logMsg.append(key).append("=").append(context.getCookies().get(key).getValue()).append(";");
        }
        return logMsg.toString();
    }

    public static <E extends BaseModel> E createModel(E object) {
        return createModel(object, null);
    }

    public static <E extends BaseModel> E createModel(E object, String appName) {
        assert object != null:
                new IllegalArgumentException("object == null");
        if (!StringUtil.isNullOrEmpty(object.appName)) return object;

        if (StringUtil.isNullOrEmpty(appName)) appName = System.getenv("application-name");
        if (StringUtil.isNullOrEmpty(appName)) appName = System.getenv("APPLICATION_NAME");
        if (StringUtil.isNullOrEmpty(appName)) appName = "Example App";
        object.appName = appName;
        return object;
    }
}
