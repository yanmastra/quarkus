package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.authenticationService.data.entity.Application;

@ApplicationScoped
public class ApplicationRepository implements PanacheRepositoryBase<Application, String> {
    @Override
    public Uni<Application> findById(String s) {
        return find("from Application A where (code=:id or id=:id)", Parameters.with("id", s)).firstResult();
    }
}
