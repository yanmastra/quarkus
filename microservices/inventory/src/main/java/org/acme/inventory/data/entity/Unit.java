package org.acme.inventory.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.acme.microservices.common.crud.CrudableEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class Unit extends CrudableEntity {
    @Id
    @Column(length = 36, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty("id")
    private String id;
    @Column(length = 36)
    @JsonProperty("name")
    private String name;

    public Unit() {
    }

    public Unit(String id, String name) {
        this.id = id;
        this.name = name;
    }

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

    @Override
    public String toString() {
        return "Unit{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
