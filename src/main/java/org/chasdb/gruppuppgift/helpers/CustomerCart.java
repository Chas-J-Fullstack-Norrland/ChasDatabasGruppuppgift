package org.chasdb.gruppuppgift.helpers;

import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.models.Reservation;
import org.chasdb.gruppuppgift.services.CustomerService;
import org.chasdb.gruppuppgift.services.ProductService;
import org.chasdb.gruppuppgift.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void select(String customerEmail) {
        this.customer = customerService.getCustomerByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        List<Reservation> reservations = reservationService.getReservationsForCustomer(customerEmail);
        this.items.clear();
        for (Reservation res : reservations) {
            // Vi hämtar hela Product-objektet via Inventory
            Product product = res.getInventory().getProduct();
            this.items.add(new CartItem(product, res.getQuantity()));
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

    @Transactional
    public void add(String sku, int quantity) {
        if (this.customer == null) {
            throw new IllegalStateException("No customer selected for the cart.");
        }

        // ReservationService sköter databaslogiken och returnerar reservationen
        Reservation res = reservationService.reserveProduct(sku, this.customer.getEmail(), quantity);
        Product product = res.getInventory().getProduct();

        // Uppdatera den lokala listan
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProduct().getSku().equals(sku))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            items.add(new CartItem(product, quantity));
        }
    }


    @Transactional
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


        items.removeIf(item -> item.getProduct().equals(toRemove.getInventory().getProduct()));
    }


    public Customer getCustomer() {
        return customer;
    }

    public List<CartItem> getItems() {
        return items;
    }


    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}