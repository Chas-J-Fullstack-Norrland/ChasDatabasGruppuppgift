package org.chasdb.gruppuppgift.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


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

    @Column(nullable = false)
    private LocalDate createdAt = LocalDate.now();

    @OneToOne(optional = false, cascade = CascadeType.ALL,mappedBy = "product")
    private Inventory inventory = new Inventory(this);

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Category> categories = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void validateCreatedAt() {
        if (createdAt.isAfter(LocalDate.now())) {
            throw new IllegalStateException("createdAt cannot be in the future");
        }
    }


    public Product() {
    }

    public Product(
            String name,
            String sku,
            BigDecimal price
    ){
       this(name,sku,"",price);
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
        this.createdAt = LocalDate.now();
        this.inventory = new Inventory(this, 0);
    }

    public void removeCategory(Category category) {
        this.categories.remove(category);
    }

    public int getQty() {
        return inventory != null ? inventory.getQty() : 0;
    }

    public void setQty(int qty) {
            this.inventory.setQty(qty);
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
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Priset kan inte vara negativt");
        }
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


    public String printString() {

        return "Product{" +
                "sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", id=" + id +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", categories= " + categories.stream().map(Category::getName).collect(Collectors.joining(" "))+
                '}';
    }
}
