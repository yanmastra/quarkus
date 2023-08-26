package org.acme.authenticationService.resources.api.v1;

import com.acme.authorization.json.AuthenticationResponse;
import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.json.SignInCredential;
import com.acme.authorization.security.UserPrincipal;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.acme.authenticationService.services.AuthenticationService;
import org.jboss.logging.Logger;

@Path("api/v1/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    @Inject
    AuthenticationService authenticationService;
    @Inject
    Logger logger;

    @PermitAll
    @POST
    @Path("authenticate")
    public Uni<ResponseJson<AuthenticationResponse>> authenticate(SignInCredential credential) {
        if (StringUtil.isNullOrEmpty(credential.username) || StringUtil.isNullOrEmpty(credential.password) || StringUtil.isNullOrEmpty(credential.appCode)) {
            throw new IllegalArgumentException("Incorrect credential, you should fill in the username, password, and appCode with the correct values");
        }

        logger.info("request:"+credential);
        return authenticationService.authenticate(credential).map(response -> new ResponseJson<>(true, null, response));
    }

    @PermitAll
    @GET
    @Path("authenticate")
    public Uni<ResponseJson<AuthenticationResponse>> refreshToken(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth) {
        return authenticationService.refreshToken(auth).map(r -> new ResponseJson<>(true, null, r));
    }

    @PermitAll
    @GET
    @Path("authorize")
    public Uni<ResponseJson<UserPrincipal>> authorize(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth) {
        logger.info("request received:"+auth.substring(1, 10));
        return authenticationService.authorizeToken(auth).map(r -> new ResponseJson<>(true, null, r));
    }


}
