package org.acme.authenticationService.firebase;


import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@ClientQueryParam(name = "key", value = "${firebase.api_key}")
public interface FirebaseTokenClient {
    @POST
    @Path("v1/accounts:signInWithCustomToken")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<FirebaseAuthResponse> signInWithCustomToken(@RequestBody FirebaseAuthRequest request);
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("v1/token")
    Uni<FirebaseAuthResponse> signIn(@FormParam("grant_type") String grantType, @FormParam("refresh_token") String refreshToken);

}
