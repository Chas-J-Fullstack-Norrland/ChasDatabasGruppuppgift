package org.chasdb.gruppuppgift.cli.handlers;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.cli.CommandHandler;
import org.chasdb.gruppuppgift.cli.CommandInput;
import org.chasdb.gruppuppgift.models.Category;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.services.ProductService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class ProductCommandHandler implements CommandHandler {
    private final ProductService productService;

    public ProductCommandHandler(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public String getDomain() {
        return "product";
    }

    @Override
    public void handle(CommandInput input) {
        switch (input.action()) {
            case "list" -> handleList(input);
            case "add" -> addProduct(input);
            case "edit" -> editProduct(input);
            case "disable" -> handleDisable(input);
            default -> System.out.println("Okänd produkt-åtgärd.");
        }
    }

    private void addProduct(CommandInput input){
        if(!input.flags().containsKey("name")){
            System.out.println("Product require a name, include one with --name=");
            return;
        }
        if(input.flags().containsKey("sku")){
            System.out.println("Product require an SKU identifier, include one with --sku=");
            return;
        }
        if(!input.flags().containsKey("price")){
            System.out.println("Product require a non-negative price, include one with --price=");
            return;
        }

        BigDecimal price;
        try{
            price = new BigDecimal(input.flags().get("price"));
        } catch (NumberFormatException e) {
            System.err.println("could not parse value of price, ensure price is numeric");
            return;
        }

        Product p = new Product(
                input.flags().get("name"),
                input.flags().get("sku"),
                price
                );
        if(input.flags().containsKey("description")){
            p.setDescription(input.flags().get("description"));
        }
        if(input.flags().containsKey("active")){
            switch (input.flags().get("active")){
                case "false" -> p.setActive(false);
                //default -> p.setActive(true); // default is already true on entity level
            }
        }

        if (!input.args().isEmpty()){
            input.args().forEach(c-> p.addCategory(new Category(c)));
        }

        if (input.flags().containsKey("quantity")){
            try{
                int qty = Integer.parseInt(input.flags().get("quantity"));
                p.setQty(qty);
            } catch (NumberFormatException e) {
                System.err.println("Invalid numeric for optional field 'quantity'");
                return;
            }
        }

        productService.saveProduct(p);

    }

    @Transactional
    private void editProduct(CommandInput input){
        if(input.flags().containsKey("sku")){
            System.out.println("Product require an SKU identifier, include one with --sku=");
            return;
        }

        Product editedProduct;
        try{
            editedProduct = productService.findProductBySKU(input.flags().get("sku")).orElseThrow();
        } catch (Exception e) {
            System.err.println("Could not find product with that SKU");
            throw e; //throw to initiate rollback of managed entity
        }

        if(input.flags().containsKey("name")){
            editedProduct.setName(input.flags().get("name"));
        }
        if(input.flags().containsKey("new-sku")){
            editedProduct.setSku(input.flags().get("new-sku"));
        }
        if(input.flags().containsKey("description")){
            editedProduct.setDescription(input.flags().get("description"));
        }
        try {
            if(input.flags().containsKey("price")){
                BigDecimal newPrice = new BigDecimal(input.flags().get("price"));
                editedProduct.setPrice(newPrice);
            }
        }catch (Exception e){
            System.err.println("invalid argument for field 'price'");
            throw e;
        }
        System.out.println("Saved Product " + editedProduct.printString());
    }


    private void handleList(CommandInput input) {
        List<Product> products;

        if (input.flags().containsKey("category")) {
           String cat = input.flags().get("category");
           products = productService.listProductsByCategories_name(cat);
            System.out.println("Visar produkter i kategori: " + cat);
        } else {
            products = productService.listAllProducts();
            System.out.println("Visar alla produkter:");
        }

        System.out.printf("%-10s %-30s %-10s%n", "SKU", "Namn", "Pris");
        System.out.println("----------------------------------------------------");
        for (Product p : products) {
            System.out.printf("%-10s %-30s %-10s%n", p.getSku(), p.getName(), p.getPrice());
        }
    }

    private void handleDisable(CommandInput input) {
        String sku = input.args().getFirst();
        if (sku.isEmpty()) {
            System.out.println("Du måste ange en SKU.");
            return;
        }

        System.out.println("Inaktiverar " + sku + " (Logik behöver kopplas till service)");
    }
}
