package org.chasdb.gruppuppgift.models;

import jakarta.persistence.*;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;
import org.chasdb.gruppuppgift.models.enums.PaymentStatus;
import org.apache.commons.text.RandomStringGenerator;



import java.time.LocalDateTime;
import java.util.List;

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

    @Column(nullable = false,unique = true)
    private String reference;

    public Payment() {
    }

    public Payment(PaymentMethod method, PaymentStatus status, Order order) {
        this.method = method;
        this.status = status;
        this.order = order;
    }

    @PrePersist
    private void assignReference(){
        RandomStringGenerator generator = RandomStringGenerator.builder().get();
        if(reference == null || reference.isBlank()){
            reference = method.toString()+"-"+ generator.generate(10);
        }

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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
