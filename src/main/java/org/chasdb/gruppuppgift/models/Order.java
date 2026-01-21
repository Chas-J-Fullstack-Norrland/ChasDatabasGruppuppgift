package org.chasdb.gruppuppgift.models;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column
    private String code;

    @Column(nullable = false)
    private BigDecimal total_Price = BigDecimal.valueOf(0);

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Map<String,OrderItem> items = new HashMap<>();


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

    public void setItems(Map<String,OrderItem> items) {
        this.items = items;
    }
    public Map<String,OrderItem> getItems() {
        return items;
    }
    public void addOrderItem(OrderItem item) {
        item.setOrder(this);
        items.put(item.getProduct().getSku(),item);
    }

    public void setItems(List<OrderItem> itemList) {
        this.items = new HashMap<>();
        itemList.forEach(i->{
            i.setOrder(this);
            items.put(i.getProduct().getSku(),i);
        });
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
        return items.values().stream()
                .map(OrderItem::getRowTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void addOrderItem(Product newProduct, int quantity) {
        items.put(newProduct.getSku(), new OrderItem(this, newProduct, quantity));
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

    public String getOrdercode() {
        return code;
    }

    public void setOrdercode(String code) {
        this.code = code;
    }
}
