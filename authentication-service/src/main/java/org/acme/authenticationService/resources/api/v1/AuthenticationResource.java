package org.acme.authenticationService.resources.api.v1;

import org.acme.authorization.json.AuthenticationResponse;
import org.acme.authorization.json.ResponseJson;
import org.acme.authorization.json.SignInCredential;
import com.acme.authorization.json.SignInCredentialWeb;
import com.acme.authorization.security.UserPrincipal;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.services.AuthenticationService;
import org.jboss.logging.Logger;

@Path("api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    @Inject
    AuthenticationService authenticationService;
    @Inject
    Logger logger;

    @PermitAll
    @POST
    @Path("authenticate_json")
    @Consumes({MediaType.APPLICATION_JSON})
    public Uni<ResponseJson<AuthenticationResponse<UserOnly>>> authenticate(SignInCredential credential, @Context ContainerRequestContext context) {
        if (StringUtil.isNullOrEmpty(credential.getUsername()) || StringUtil.isNullOrEmpty(credential.getPassword()) || StringUtil.isNullOrEmpty(credential.getAppCode())) {
            throw new IllegalArgumentException("Incorrect credential, you should fill in the username, password, and appCode with the correct values!, yours:"+credential);
        }
        return authenticationService.authenticate(credential, context).map(response -> new ResponseJson<>(true, null, response));
    }

    @PermitAll
    @POST
    @Path("authenticate")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Uni<ResponseJson<AuthenticationResponse<UserOnly>>> authenticate(SignInCredentialWeb credential, @Context ContainerRequestContext context) {
        if (StringUtil.isNullOrEmpty(credential.getUsername()) || StringUtil.isNullOrEmpty(credential.getPassword()) || StringUtil.isNullOrEmpty(credential.getAppCode())) {
            throw new IllegalArgumentException("Incorrect credential, you should fill in the username, password, and appCode with the correct values!, yours:"+credential);
        }
        return authenticationService.authenticate(credential, context).map(response -> new ResponseJson<>(true, null, response));
    }

    @PermitAll
    @GET
    @Path("authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<ResponseJson<AuthenticationResponse<UserOnly>>> refreshToken(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @Context ContainerRequestContext context) {
        return authenticationService.refreshToken(auth, context).map(r -> new ResponseJson<>(true, null, r));
    }

    @PermitAll
    @GET
    @Path("authorize")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<ResponseJson<UserPrincipal>> authorize(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @Context ContainerRequestContext context) {
        logger.info("Authorizing:"+context.getUriInfo().getRequestUri().getHost()+context.getUriInfo().getPath());
        return authenticationService.authorizeToken(auth).map(r -> new ResponseJson<>(true, null, r));
    }


}
