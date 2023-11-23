package org.acme.inventory.data.entity;

import jakarta.persistence.*;
import org.acme.microservices.common.crud.CrudableEntity;

import java.math.BigDecimal;

@Entity
public class Product extends CrudableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;
    @Column(unique = true)
    private String code;
    private String name;
    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "unit_id", referencedColumnName = "id")
    private Unit unit;

    @Column(nullable = false, scale = 2, precision = 14)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false, scale = 2, precision = 14)
    private BigDecimal cogs = BigDecimal.ZERO;

    @Column(nullable = false)
    private Long stock = 0L;

    @Column(name = "stock_on_hold", nullable = false)
    private Long stockOnHold = 0L;
    @Column(name = "stock_outstanding", nullable = false)
    private Long stockOutstanding = 0L;

    public Product() {
    }

    public Product(String id, String code, String name, String imageUrl, BigDecimal price, BigDecimal cogs, Long stock, Long stockOnHold, Long stockOutstanding) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.cogs = cogs;
        this.stock = stock;
        this.stockOnHold = stockOnHold;
        this.stockOutstanding = stockOutstanding;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public Long getStockOnHold() {
        return stockOnHold;
    }

    public void setStockOnHold(Long stockOnHold) {
        this.stockOnHold = stockOnHold;
    }

    public Long getStockOutstanding() {
        return stockOutstanding;
    }

    public void setStockOutstanding(Long stockOutstanding) {
        this.stockOutstanding = stockOutstanding;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", unit=" + unit +
                ", price=" + price +
                ", cogs=" + cogs +
                ", stock=" + stock +
                ", stockOnHold=" + stockOnHold +
                ", stockOutstanding=" + stockOutstanding +
                '}';
    }
}
