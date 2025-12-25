package org.chasdb.gruppuppgift.helpers;

import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.models.Reservation;
import org.chasdb.gruppuppgift.services.CustomerService;
import org.chasdb.gruppuppgift.services.ProductService;
import org.chasdb.gruppuppgift.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.Optional;

@Component
public class CustomerCart {

    private final ProductService productService;
    private final CustomerService customerService;
    private final ReservationService reservationService;

    private Customer customer;
    private List <CartItem> items = new ArrayList<>();

    @Autowired
    public CustomerCart(ProductService productService, CustomerService customerService, ReservationService reservationService) {
        this.productService = productService;
        this.customerService = customerService;
        this.reservationService = reservationService;
    }


    public void select(String customerEmail) {
        this.customer = customerService.getCustomerByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        List<Reservation> reservations = reservationService.getReservationsForCustomer(customerEmail);
        this.items.clear();
        for (Reservation res : reservations) {
            Product product = res.getInventory().getProduct();
            this.items.add(new CartItem(
                    product.getSku(),
                    product.getName(),
                    product.getPrice(),
                    res.getQuantity()
            ));
        }
    }


    public void create(String email) {
        List<Reservation> reservations = reservationService.getReservationsForCustomer(email);
        if (!reservations.isEmpty()) {
            throw new IllegalStateException("Customer already has reserved products. Use Select instead.");
        }

        this.customer = customerService.getCustomerByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        this.items = new ArrayList<>();
    }

    public void add(String sku, int quantity) {
        if (this.customer == null) {
            throw new IllegalStateException("No customer selected for the cart.");
        }

        // Skicka reservation till databasen via ReservationService
        reservationService.reserveProduct(sku, this.customer.getEmail(), quantity);

        // Hämta produktinfo för att uppdatera den lokala listan med namn och pris
        Product product = productService.findProductByID(null)

                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Uppdatera den lokala listan
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getSku().equals(sku))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {

            items.add(new CartItem(product.getSku(), product.getName(), product.getPrice(), quantity));
        }
    }



    public void remove(String sku) {
        if (this.customer == null) {
            throw new IllegalStateException("No customer selected.");
        }

        // Hämta reservationen för att få dess ID (behövs för ReservationService)
        List <Reservation> reservations = reservationService.getReservationsForCustomer(this.customer.getEmail());
        Reservation toRemove = reservations.stream()
                .filter(r -> r.getInventory().getProduct().getSku().equals(sku))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No reservation found for this SKU"));


        reservationService.releaseReservationByID(toRemove.getId());


        items.removeIf(item -> item.getSku().equals(sku));
    }


    public Customer getCustomer() {
        return customer;
    }

    public List<CartItem> getItems() {
        return items;
    }


    public static class CartItem {
        private String sku;
        private String name;      // Nytt fält
        private BigDecimal price; // Nytt fält
        private int quantity;

        public CartItem(String sku, String name, BigDecimal price, int quantity) {
            this.sku = sku;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getSku() { return sku; }
        public String getName() { return name; }      // Ny getter
        public BigDecimal getPrice() { return price; } // Ny getter
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}