package org.acme.crudReactiveHibernate.data;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.inject.Singleton;
import org.acme.crudReactiveHibernate.data.entity.Permission;

@Singleton
public class PermissionRepository  implements PanacheRepositoryBase<Permission, String> {
}
