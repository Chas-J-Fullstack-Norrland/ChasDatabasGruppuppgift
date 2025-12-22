package org.chasdb.gruppuppgift.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "order_item",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_id", "product_id"})
        }
)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, columnDefinition = "INTEGER CHECK(quantity > 0)")
    private int quantity;

    /** Pris sparas per orderrad (historiskt korrekt) */
    @Column(nullable = false, columnDefinition = "DECIMAL CHECK(price > 0)")
    private BigDecimal price;

    public OrderItem() {}

    public OrderItem(Order order, Product product, int quantity, BigDecimal price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity:
        this.price = price;
    }
    public BigDecimal getRowTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
    public int getQuantity(){
        return quantity;
    }
    public BigDecimal getPrice() {
        return price;
    }
}
