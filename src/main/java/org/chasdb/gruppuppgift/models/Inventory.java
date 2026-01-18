package org.chasdb.gruppuppgift.models;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;

@Entity
public class Inventory {

    @Id
    private Long id;

    @Column(nullable = false)
    private int qty = 0;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Product product;

    public Inventory(){

    }
    public Inventory(Product product, int quantity) {
        this.product = product;
        setQty(quantity);
    }

    public Inventory(Product product_stock){
        this.product = product_stock;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        if (qty < 0) {
            throw new IllegalArgumentException("Lagersaldo får inte vara negativt");
        }
        this.qty = qty;
    }

    public Product getProduct() {return product;}


}
