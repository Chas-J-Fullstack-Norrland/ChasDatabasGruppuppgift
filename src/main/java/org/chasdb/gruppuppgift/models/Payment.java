package org.chasdb.gruppuppgift.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    @Column(nullable = false, columnDefinition = "varchar(10) CHECK(method='CARD' OR method='INVOICE')")
    String method;
    @Column(nullable = false, columnDefinition = "varchar(10) CHECK(status='CANCELLED' OR status='PENDING' OR status='APPROVED' or status = 'DECLINED')")
    String status;

    @Column(nullable = false, columnDefinition = "TIMESTAMP default now()")
    LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "orders" ,referencedColumnName = "id")
    Order order;

    public Payment() {
    }

    public Payment(String method, String status, Order order) {
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
