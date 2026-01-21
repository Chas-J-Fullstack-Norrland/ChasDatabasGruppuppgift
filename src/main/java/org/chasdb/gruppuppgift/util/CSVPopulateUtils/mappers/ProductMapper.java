package org.chasdb.gruppuppgift.util.CSVPopulateUtils.mappers;

import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVRecord;
import org.chasdb.gruppuppgift.models.Category;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.CategoryRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductMapper implements CsvEntityMapper<Product>{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Map<String, Product> cache = new HashMap<>();

    public ProductMapper(ProductRepository productRepository,CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productRepository.findAll().forEach(p -> cache.put(p.getName(), p)
        );
    }



    @Override
    public Product map(CSVRecord record) {
        if (!record.isSet("name") || !record.isSet("sku") || !record.isSet("price")) {
            throw new IllegalArgumentException("Record missing one or more mandatory columns (name, sku, price)");
        }

        String name = record.get("name").trim();
        String sku = record.get("sku").trim();
        String description = record.isSet("description") ? record.get("description").trim() : "";
        String priceStr = record.get("price").trim();
        String createdAt = record.get("createdAt").trim();
        Boolean active = Boolean.parseBoolean(record.get("active")); //Booleans are safe to parse due to not throwing exceptions
        String qty = record.get("qty").trim();
        String categortstr = record.get("category");

        if (name.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Product contained blank column 'name'");
        if (sku.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Product contained blank column 'sku'");
        if (priceStr.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Product contained blank column 'price'");

        return cache.computeIfAbsent(sku, s -> {
            Product product;
            try{
                product = new Product(name,sku,description,new BigDecimal(priceStr));
                product.setCreatedAt(LocalDate.parse(createdAt));
                product.setQty(Integer.parseInt(qty));
            } catch (NumberFormatException e) {
                System.err.println("Record #"+ record.getRecordNumber() +" for Product contained unparsable field for 'Price' or 'qty'");
                return null;
            } catch (DateTimeParseException e) {
                System.err.println("Could not parse datetime of Record#"+record.getRecordNumber()+", Column createdAt was not a valid datetime format");
                return null;
            }

            //Any value other than TRUE results in product being deactivated, even null due to parseboolean earlier.
                product.setActive(active);


            // Handle categories
            if (categortstr != null && !categortstr.isBlank()) {
                String[] categoryNames = categortstr.split("\\|");
                for (String categoryName : categoryNames) {
                    Category category = categoryRepository.findByName(categoryName).orElse(new Category(categoryName)); // pseudo
                    product.addCategory(category);
                }
            }

            return product;
        });


    }

    @Override
    @Transactional
    public void save(Product entity) {
        try{
            productRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Product with SKU "+ entity.getSku() +" already exists in database");
        }
    }

    @Override
    public String supportsType() {
        return "PRODUCT";
    }
}
