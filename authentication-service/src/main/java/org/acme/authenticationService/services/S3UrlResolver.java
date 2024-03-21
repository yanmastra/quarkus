package org.acme.authenticationService.services;

import io.smallrye.jwt.util.ResourceUtils;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@ApplicationScoped
public class S3UrlResolver extends ResourceUtils.UrlStreamResolver{
    @Override
    public InputStream resolve(String keyLocation) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) ((new URL(keyLocation)).openConnection());
        connection.addRequestProperty("Authorization", "");
        return connection.getInputStream();
    }
}
