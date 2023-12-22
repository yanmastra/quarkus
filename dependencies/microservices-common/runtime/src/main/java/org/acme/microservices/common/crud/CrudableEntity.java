package org.acme.microservices.common.crud;

import jakarta.persistence.MappedSuperclass;
import org.acme.microservices.common.entity.BaseEntity;

import java.io.Serializable;

@MappedSuperclass
public abstract class CrudableEntity extends BaseEntity implements Serializable {

    public abstract String getId();
    public abstract void setId(String id);
}
