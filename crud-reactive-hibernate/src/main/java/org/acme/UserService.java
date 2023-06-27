package org.acme;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.acme.dao.User;
import org.acme.data.UserRepository;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    Logger logger;

    @WithTransaction
    public Uni<Response> saveUser(User user) {
        return userRepository.createUser(user.toDto()).onItem().transform(r -> {
            User dUser = User.fromDto(r);
            return Response.status(Response.Status.CREATED).entity(dUser).build();
                }).onFailure().transform(throwable -> new HttpException(500, throwable.getMessage()));
    }

    @WithSession
    public Uni<List<User>> findAll() {
        return userRepository.findAll(Sort.ascending("email")).list()
                .map(obj -> obj.stream().map(User::fromDto).toList());
    }
}
