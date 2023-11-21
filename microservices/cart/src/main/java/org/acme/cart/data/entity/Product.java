package org.acme.cart.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.acme.crud.endpoint.CrudableEntity;

import java.math.BigDecimal;

@Entity
public class Product extends CrudableEntity {

    @Id
    private String id;
    @Column(name = "name")
    private String mame;
    @Column(name = "price", precision = 14, scale = 2)
    private BigDecimal price;
    @Column(name = "cogs", precision = 14, scale = 2)
    private BigDecimal cogs;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getMame() {
        return mame;
    }

    public void setMame(String mame) {
        this.mame = mame;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCogs() {
        return cogs;
    }

    public void setCogs(BigDecimal cogs) {
        this.cogs = cogs;
    }
}
