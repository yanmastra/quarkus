package org.acme.inventory.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.acme.microservices.common.crud.CrudableEntity;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Category extends CrudableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;
    @Column(length = 128)
    @JsonProperty("name")
    private String name;

    @JsonIgnore
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
