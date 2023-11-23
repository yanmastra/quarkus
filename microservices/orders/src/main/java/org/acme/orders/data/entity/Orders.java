package org.acme.orders.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.acme.microservices.common.crud.CrudableEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "_search", columnList = "order_number, customer_id, order_date"),
        @Index(name = "_calculation", columnList = "total, subtotal, total_discount")
})
public class Orders extends CrudableEntity {
    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @JsonProperty("order_number")
    @Column(name = "order_number", length = 36)
    private String orderNumber;
    @JsonProperty("customer_id")
    @Column(name = "customer_id", length = 36)
    private String customerId;

    @JsonProperty("order_date")
    @Column(name = "order_date")
    private Date orderDate;
    @JsonProperty("customer_name")
    @Column(name = "customer_name", length = 128)
    private String customerName;
    @JsonProperty("customer_address")
    @Column(name = "customer_address")
    private String customerAddress;
    @JsonProperty("customer_email")
    @Column(name = "customer_email")
    private String customerEmail;
    @JsonProperty("customer_phone")
    @Column(name = "customer_phone")
    private String customerPhone;
    @Column(scale = 2, precision = 16)
    private BigDecimal total;
    @Column(scale = 2, precision = 16)
    private BigDecimal subtotal;
    @JsonProperty("total_discount")
    @Column(scale = 2, precision = 16, name = "total_discount")
    private BigDecimal totalDiscount;
    @JsonProperty("total_payment")
    @Column(scale = 2, precision = 16, name = "total_payment")
    private BigDecimal totalPayment;

    @OneToMany(mappedBy = "order", orphanRemoval = true)
    private List<OrderLine> lines;
    @OneToMany(mappedBy = "order", orphanRemoval = true)
    private List<Payment> payments;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    public Orders() {
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
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @JsonIgnore
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @JsonIgnore
    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    @JsonIgnore
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @JsonIgnore
    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    @JsonIgnore
    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    @JsonIgnore
    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    @JsonIgnore
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @JsonIgnore
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @JsonIgnore
    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
