package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.models.Reservation;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CartServiceContract {

    private final CustomerService customerService;
    private final ReservationService reservationService;

    public CartServiceContract(CustomerService customerService, ReservationService reservationService) {
        this.customerService = customerService;
        this.reservationService = reservationService;
    }

    public Cart getCart(String customerEmail) {
        Customer customer = getCustomerOrThrow(customerEmail);

        List<Reservation> reservations = reservationService.getReservationsForCustomer(customerEmail);

        List<CartItem> items = reservations.stream()
                .map(res -> new CartItem(
                        res.getInventory().getProduct(),
                        res.getQuantity(),
                        res.getId()))
                .toList();

        return new Cart(customer, items);
    }

    @Transactional
    public void addToCart(String customerEmail, String sku, int quantity) {
        getCustomerOrThrow(customerEmail);

        reservationService.reserveProduct(sku, customerEmail, quantity);
    }


    @Transactional
    public void removeFromCart(String customerEmail, String sku) {

        List <Reservation> reservations = reservationService.getReservationsForCustomer(customerEmail);

        Reservation toRemove = reservations.stream()
                .filter(r -> r.getInventory().getProduct().getSku().equals(sku))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Produkten finns inte i varukorgen: " + sku));

        reservationService.releaseReservationByID(toRemove.getId());
    }

    public void createCart(String customerEmail) {
        Customer customer = getCustomerOrThrow(customerEmail);

        List<Reservation> reservations = reservationService.getReservationsForCustomer(customerEmail);
        if (!reservations.isEmpty()) {
            throw new IllegalStateException("Kunden har redan en aktiv varukorg. Använd 'select' istället.");
        }
    }

    private Customer getCustomerOrThrow(String email) {
        return customerService.getCustomerByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Kund hittades inte: " + email));
    }

    public record Cart(Customer customer, List<CartItem> items) {
        public double getTotalPrice() {
            return items.stream()
                    .mapToDouble(i -> i.product().getPrice().doubleValue() * i.quantity())
                    .sum();
        }
    }

    public record CartItem(Product product, int quantity, Long reservationId) {}
}