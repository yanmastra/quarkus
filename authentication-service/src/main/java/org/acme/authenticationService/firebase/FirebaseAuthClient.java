package org.acme.authenticationService.firebase;


import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@ClientQueryParam(name = "key", value = "${firebase.api_key}")
@Consumes(MediaType.APPLICATION_JSON)
public interface FirebaseAuthClient {

    String FIREBASE_VERIFIED = "FIREBASE_VERIFIED";
    String FIREBASE_LOCAL_ID = "FIREBASE_LOCAL_ID";

    @POST
    @Path("/v1/accounts:signUp")
    Uni<FirebaseAuthResponse> signUp(FirebaseAuthRequest request);
    @POST
    @Path("/v1/accounts:signInWithPassword")
    Uni<FirebaseAuthResponse> signIn(FirebaseAuthRequest request);

    @POST
    @Path("/v1/accounts:update")
    Uni<FirebaseAuthResponse> updateProfile(FirebaseAuthRequest request);

}
