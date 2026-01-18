package org.chasdb.gruppuppgift.models;

import jakarta.persistence.*;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;
import org.chasdb.gruppuppgift.models.enums.PaymentStatus;

import java.time.LocalDateTime;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod method;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;

    @Column(nullable = false, columnDefinition = "TIMESTAMP default now()")
    LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "orders" ,referencedColumnName = "id", nullable = false)
    private Order order;

    public Payment() {
    }

    public Payment(PaymentMethod method, PaymentStatus status, Order order) {
        this.method = method;
        this.status = status;
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
