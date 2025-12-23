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
    @Column(nullable = false)
    private BigDecimal rowTotal;

    public OrderItem() {}

    public BigDecimal getRowTotal() {
        return rowTotal;
    }

    public Product getProduct() {
        return product;
    }

    public Order getOrder() {
        return order;
    }

    public Long getId() {
        return id;
    }

    public OrderItem(Order order, Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.rowTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
    public int getQuantity(){
        return quantity;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setRowTotal(BigDecimal rowTotal) {
        this.rowTotal = rowTotal;
    }

    public BigDecimal getPrice() {
        return rowTotal;
    }
}
