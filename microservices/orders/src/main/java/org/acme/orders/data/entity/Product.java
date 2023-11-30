package org.acme.orders.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.acme.microservices.common.crud.CrudableEntity;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "products", indexes = {
        @Index(name = "_search", columnList = "name, id, stock, stock_on_hold, stock_outstanding")
})
public class Product extends CrudableEntity {
    @Id
    @JsonProperty("id")
    @Column(length = 36, nullable = false)
    private String id;
    @JsonProperty("name")
    private String name;

    @JsonProperty("unit_id")
    @Column(name = "unit_id", length = 36)
    private String unitId;

    @JsonProperty("category_id")
    @Column(name = "category_id")
    private String categoryId;

    @JsonProperty("price")
    @Column(nullable = false, scale = 2, precision = 14)
    private BigDecimal price = BigDecimal.ZERO;


    @JsonProperty("cogs")
    @Column(nullable = false, scale = 2, precision = 14)
    private BigDecimal cogs = BigDecimal.ZERO;

    @JsonProperty("stock")
    @Column(nullable = false)
    private Long stock = 0L;


    @JsonProperty("stock_on_hold")
    @Column(name = "stock_on_hold", nullable = false)
    private Long stockOnHold = 0L;
    @JsonProperty("stock_outstanding")
    @Column(name = "stock_outstanding", nullable = false)
    private Long stockOutstanding = 0L;

    public Product() {
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

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

    @JsonIgnore
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @JsonIgnore
    public BigDecimal getCogs() {
        return cogs;
    }

    public void setCogs(BigDecimal cogs) {
        this.cogs = cogs;
    }

    @JsonIgnore
    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    @JsonIgnore
    public Long getStockOnHold() {
        return stockOnHold;
    }

    public void setStockOnHold(Long stockOnHold) {
        this.stockOnHold = stockOnHold;
    }

    @JsonIgnore
    public Long getStockOutstanding() {
        return stockOutstanding;
    }

    public void setStockOutstanding(Long stockOutstanding) {
        this.stockOutstanding = stockOutstanding;
    }

    @JsonIgnore
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    @JsonIgnore
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", unitId=" + unitId +
                ", categoryId=" + categoryId +
                ", price=" + price +
                ", cogs=" + cogs +
                ", stock=" + stock +
                ", stockOnHold=" + stockOnHold +
                ", stockOutstanding=" + stockOutstanding +
                '}';
    }
}
