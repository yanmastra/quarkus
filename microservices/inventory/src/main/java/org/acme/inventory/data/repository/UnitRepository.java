package org.acme.inventory.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.inventory.data.entity.Unit;

@ApplicationScoped
public class UnitRepository implements PanacheRepositoryBase<Unit, String> {
}
