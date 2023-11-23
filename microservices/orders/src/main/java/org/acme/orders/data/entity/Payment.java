package org.acme.orders.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.acme.microservices.common.crud.CrudableEntity;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "payment", indexes = {
        @Index(name = "_search", columnList = "customer_id, payment_date, payment_method, payment_reference_id, payment_gateway"),
        @Index(name = "_calculation", columnList = "total")
})
public class Payment extends CrudableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @JsonProperty("payment_method")
    @Column(name = "payment_method", length = 64)
    private String paymentMethod;
    @JsonProperty("payment_gateway")
    @Column(name = "payment_gateway", length = 64)
    private String paymentGateway;
    @JsonProperty("payment_reference_id")
    @Column(name = "payment_reference_id", length = 40)
    private String paymentReferenceId;

    @JsonProperty("customer_id")
    @Column(name = "customer_id", length = 36)
    private String customerId;

    @JsonProperty("payment_date")
    @Column(name = "payment_date")
    private Date paymentDate;
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

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Orders order;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status = OrderStatus.WAITING_PAYMENT;

    public Payment() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public String getPaymentReferenceId() {
        return paymentReferenceId;
    }

    public void setPaymentReferenceId(String paymentReferenceId) {
        this.paymentReferenceId = paymentReferenceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Orders getOrder() {
        return order;
    }

    public void setOrder(Orders order) {
        this.order = order;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
