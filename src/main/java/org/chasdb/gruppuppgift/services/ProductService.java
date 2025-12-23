package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.InventoryRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repo;
    @Autowired
    private InventoryRepository inventory;


    public Product addProduct(String name, String SKU, BigDecimal price) throws IllegalArgumentException,IllegalStateException{



        if(name.isBlank()){
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if(SKU.isBlank()){
            throw new IllegalArgumentException("SKU cannot be blank");
        }

        Optional<Product> duplicateSKU = repo.findBySku(SKU);
        if(duplicateSKU.isPresent()){
            throw new IllegalStateException("SKU already exists in database");
        }

        return repo.save(new Product(name,SKU,price));

    }
    public Product addProduct(String name, String SKU ,String description, BigDecimal price ) throws IllegalArgumentException,IllegalStateException{

        if(name.isBlank()){
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if(SKU.isBlank()){
            throw new IllegalArgumentException("SKU cannot be blank");
        }

        Optional<Product> duplicateSKU = repo.findBySku(SKU);
        if(duplicateSKU.isPresent()){
            throw new IllegalStateException("SKU already exists in database");
        }

        return repo.save(new Product(name,SKU,description,price));

    }

    public Product saveProduct(Product p) throws IllegalArgumentException,IllegalStateException {

        if(p.getName().isBlank()){
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if(p.getSku().isBlank()){
            throw new IllegalArgumentException("SKU cannot be blank");
        }

        Optional<Product> duplicateSKU = repo.findBySku(p.getSku());
        if(duplicateSKU.isPresent()&& !Objects.equals(duplicateSKU.get().getId(), p.getId())){
            throw new IllegalStateException("SKU already exists in database under different id");
        }

        return repo.save(p);
    }

    public Optional<Product> findProductByID(Long id){
        return repo.findById(id);
    }

    public List<Product> listAllProducts(){
        return repo.findAll();
    }

    public List<Product> listProductsByCategories_name(String categoryName){
        return repo.findByCategories_Name(categoryName);
    }

    public List<Product> listProductsWithInventory_QtyLessThan(int limit){
        return repo.findByInventory_QtyLessThan(limit);
    }

    public void enableProduct(Long ID){
        repo.enableProduct(ID);
    }
    public void disableProduct(Long ID){
        repo.disableProduct(ID);
    }

    public void deleteProduct(Long id){
        repo.deleteById(id);
    }

    public void addStockToProduct(String SKU,int qty ){
        inventory.updateQuantityBySku(SKU,qty);
    }


}
