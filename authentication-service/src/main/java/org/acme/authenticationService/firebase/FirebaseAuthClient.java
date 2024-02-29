package org.acme.authenticationService.firebase;


import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

@RegisterRestClient(baseUri = "https://identitytoolkit.googleapis.com")
public interface FirebaseAuthClient {

    String FIREBASE_VERIFIED = "FIREBASE_VERIFIED";
    String FIREBASE_LOCAL_ID = "FIREBASE_LOCAL_ID";

    @POST
    @Path("/v1/accounts:signUp")
    Uni<FirebaseAuthResponse> signUp(FirebaseAuthRequest request, @RestQuery("key") String apiKey);
    @POST
    @Path("/v1/accounts:signInWithPassword")
    Uni<FirebaseAuthResponse> signIn(FirebaseAuthRequest request, @RestQuery("key") String apiKey);

    @POST
    @Path("/v1/accounts:update")
    Uni<FirebaseAuthResponse> updateProfile(FirebaseAuthRequest request, @RestQuery("key") String apiKey);

}
