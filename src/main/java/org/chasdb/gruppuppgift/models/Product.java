package org.chasdb.gruppuppgift.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false,unique = true)
    private String sku;


    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false, columnDefinition = "DECIMAL CHECK(price>0)")
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, columnDefinition = "DATE CHECK(created_at<=now())")
    LocalDate createdAt = LocalDate.now();

    @OneToOne(optional = false,mappedBy = "product", cascade = CascadeType.ALL)
    Inventory inventory = new Inventory(this);

    @ManyToMany(cascade = CascadeType.PERSIST)
    Set<Category> categories = new HashSet<>();


    public int getQTY() {
        return inventory.getQty();
    }

    public void setQTY(int quantity) {
        this.inventory.setQty(quantity);
    }



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
    public Product(
            String name,
            String sku,
            String description,
            BigDecimal price
    ){
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.description = description;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category category){
        this.categories.add(category);
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
