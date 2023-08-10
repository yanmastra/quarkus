package org.acme.authenticationService.resources;

import com.acme.authorization.json.AuthenticationResponse;
import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.json.SignInCredential;
import io.quarkus.logging.Log;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.authenticationService.services.AuthenticationService;

@Path("api/v1/authentication")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    @Inject
    AuthenticationService authenticationService;

    @PermitAll
    @POST
    public Uni<ResponseJson<AuthenticationResponse>> authenticate(SignInCredential credential) {
        if (StringUtil.isNullOrEmpty(credential.username) || StringUtil.isNullOrEmpty(credential.password) || StringUtil.isNullOrEmpty(credential.appCode)) {
            throw new IllegalArgumentException("Incorrect credential, you should fill in the username, password, and appCode with the correct values");
        }

        Log.info("request:"+credential);

        return authenticationService.authenticate(credential).map(response -> new ResponseJson<>(true, null, response));
    }
}
