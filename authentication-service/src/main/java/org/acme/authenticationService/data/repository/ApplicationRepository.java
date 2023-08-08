package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.inject.Singleton;
import org.acme.authenticationService.data.entity.Application;

@Singleton
public class ApplicationRepository implements PanacheRepositoryBase<Application, String> {
}
