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

    public Inventory(int qty_in_stock){
        this.qty = qty_in_stock;
    }


}
