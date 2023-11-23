package org.acme.orders.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_line", indexes = {
        @Index(name = "_search", columnList = "product_id, qty"),
        @Index(name = "_calculate", columnList = "price, cogs, subtotal")
})
public class OrderLine {
    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JsonProperty("product_id")
    @Column(length = 36, name = "product_id")
    private String productId;
    @JsonProperty("product_name")
    @Column(length = 128, name = "product_name")
    private String productName;
    private long qty = 0;
    @Column(precision = 16, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
    @Column(precision = 16, scale = 2)
    private BigDecimal cogs = BigDecimal.ZERO;
    @Column(precision = 16, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Orders order;

    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @JsonIgnore
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @JsonIgnore
    public long getQty() {
        return qty;
    }

    public void setQty(long qty) {
        this.qty = qty;
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
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @JsonIgnore
    public Orders getOrder() {
        return order;
    }

    public void setOrder(Orders order) {
        this.order = order;
    }
}
