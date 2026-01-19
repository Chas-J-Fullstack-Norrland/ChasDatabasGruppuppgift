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
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, columnDefinition = "INTEGER CHECK(qty > 0)")
    private int qty;

    @Column(nullable = false, columnDefinition = "DECIMAL CHECK(unit_price>=0)")
    private BigDecimal unitPrice;


    /**
     * Pris sparas per orderrad (historiskt korrekt)
     */
    @Column(nullable = false, columnDefinition = "DECIMAL CHECK(row_Total>=0)")
    private BigDecimal rowTotal;

    @PrePersist
    @PreUpdate
    private void validateItems() {
        if (qty<1) {
            throw new IllegalStateException("Order must contain at least one OrderItem");
        }
    }


    public OrderItem() {}

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
        this.qty = quantity;
        this.unitPrice = product.getPrice();
        this.rowTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getQuantity(){
        return qty;
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
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.qty = quantity;
    }

    public void setRowTotal(BigDecimal rowTotal) {
        this.rowTotal = rowTotal;
    }

    public BigDecimal getRowTotal() {
        return rowTotal;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
