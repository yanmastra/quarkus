package org.acme.crudReactiveHibernate.data;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.inject.Singleton;
import org.acme.crudReactiveHibernate.data.entity.Role;
import org.acme.crudReactiveHibernate.data.entity.RoleId;

@Singleton
public class RoleRepository implements PanacheRepositoryBase<Role, RoleId> {
}
