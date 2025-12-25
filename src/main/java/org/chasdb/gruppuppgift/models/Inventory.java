package org.chasdb.gruppuppgift.models;

import jakarta.persistence.*;


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

    public Inventory(Product product_stock){
        this.product = product_stock;
    }

    public Inventory(int qty_in_stock){
        this.qty = qty_in_stock;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Product getProduct() {return product;}


}
