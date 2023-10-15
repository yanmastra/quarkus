package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.authenticationService.data.entity.Application;

@ApplicationScoped
public class ApplicationRepository implements PanacheRepositoryBase<Application, String> {
}
