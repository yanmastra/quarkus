package org.acme.microservices.common.crud;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

@MappedSuperclass
public abstract class CrudableEntity extends PanacheEntityBase implements Serializable {

    public abstract String getId();
    public abstract void setId(String id);
}
