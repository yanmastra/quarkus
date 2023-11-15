package org.acme.crud.endpoint;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

public abstract class CrudableEntity extends PanacheEntityBase {

    public abstract String getId();
    public abstract void setId(String id);
}
