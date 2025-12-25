package org.chasdb.gruppuppgift.models;
import jakarta.annotation.Nullable;
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
    private BigDecimal total_Price = BigDecimal.valueOf(0);

    @Column(nullable = false, columnDefinition = "DATE default now()")
    private LocalDate createdAt = LocalDate.now();

    @Column(nullable = false,columnDefinition = "Varchar(10) default 'NEW' CHECK(status='NEW' OR status='PAID' OR status='CANCELED')")
    private String status = "NEW";

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<OrderItem> items = new HashSet<>();


    @ManyToOne
    @JoinColumn(nullable = false)
    private Customer customer;


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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
