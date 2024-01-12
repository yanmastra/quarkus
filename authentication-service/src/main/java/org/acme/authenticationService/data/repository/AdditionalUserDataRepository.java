package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.authenticationService.data.entity.AdditionalUserData;

@ApplicationScoped
public class AdditionalUserDataRepository implements PanacheRepositoryBase<AdditionalUserData, String> {
}
