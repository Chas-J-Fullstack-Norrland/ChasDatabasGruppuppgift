package org.chasdb.gruppuppgift.models;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private LocalDate createdAt = LocalDate.now();
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<OrderItem> items = new HashSet<>();
    public Order() { }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public void setItems(Set<OrderItem> items) {
        this.items = items;
    }

    public Long getId() {
        return id;
    }
    public LocalDate getCreatedAt() {
        return createdAt;
    }
    public Set<OrderItem> getItems() {
        return items;
    }
    /** Affärsregel: order måste ha minst 1 item*/
    @PrePersist
    @PreUpdate
    private void validateItems() {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Order must contain at least one OrderItem");
        }
    }
    /** Exakt totalsumma (BigDecimal-safe)*/
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(OrderItem::getRowTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void addOrderItem(Product newProduct, int quantity) {
        items.add(new OrderItem(this, newProduct, quantity, newProduct.getPrice().multiply(BigDecimal.valueOf(quantity))));
    }
}
