package org.chasdb.gruppuppgift.models;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private BigDecimal total_Price = BigDecimal.valueOf(0);

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<OrderItem> items = new HashSet<>();


    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Customer customer;


    public Order() { }


    public Order(Customer customer, LocalDateTime orderDate, OrderStatus status) {
        this.customer = customer;
        this.createdAt = orderDate;
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setItems(Set<OrderItem> items) {
        this.items = items;
    }
    public Set<OrderItem> getItems() {
        return items;
    }
    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void setItems(List<OrderItem> itemList) {
        this.items = new HashSet<>(itemList);
        this.items.forEach(item -> item.setOrder(this));
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /** Affärsregel: order måste ha minst 1 item*/
    @PrePersist
    @PreUpdate
    private void validateItems() {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Order must contain at least one OrderItem");
        }
    }
   /** Exakt totalsumma (BigDecimal-safe) om du skulle köpt produkterna idag*/
    public BigDecimal calculatePriceOfProducts() {
        return items.stream()
                .map(OrderItem::getRowTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void addOrderItem(Product newProduct, int quantity) {
        items.add(new OrderItem(this, newProduct, quantity));
    }

    public BigDecimal getTotal_Price() {
        return total_Price;
    }

    public void setTotal_Price(BigDecimal total_Price) {
        this.total_Price = total_Price;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
