package org.chasdb.gruppuppgift.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Column(nullable = false)
    String sku;

    @Column(nullable = false)
    String name;

    @Column
    String description;

    @Column(nullable = false, columnDefinition = "DECIMAL CHECK(price>0")
    BigDecimal price;

    @Column(nullable = false,columnDefinition = "default TRUE")
    boolean active = true;

    @Column(nullable = false, columnDefinition = "DATE CHECK createdAt<=now()")
    LocalDate createdAt = LocalDate.now();

     @ManyToMany
     Set<Category> categories; //uncomment once category done

    public Product() {
    }

    public Product(
            String name,
            String sku,
            BigDecimal price
    ){
        this.name = name;
        this.sku = sku;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
