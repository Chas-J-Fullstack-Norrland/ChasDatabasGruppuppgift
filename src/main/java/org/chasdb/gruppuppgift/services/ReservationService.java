package org.chasdb.gruppuppgift.services;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.models.Reservation;
import org.chasdb.gruppuppgift.repositories.CustomerRepository;
import org.chasdb.gruppuppgift.repositories.InventoryRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.chasdb.gruppuppgift.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReservationService {

    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ProductRepository productRepository;




    @Transactional
    public Reservation reserveProduct(
            String productsku,
            String email,
            int quantity
    ) {

        Product product = productRepository.findBySku(productsku)
                .orElseThrow(()-> new NoSuchElementException("No such productFound"));

        Inventory inventory = inventoryRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

        if (inventory.getQty() < quantity) {
            throw new IllegalStateException("Not enough stock available");
        }

        // Find or create customer by email
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + email));

        // Decrease available stock
        inventory.setQty(
                inventory.getQty() - quantity
        );

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setInventory(inventory);
        reservation.setCustomer(customer);
        reservation.setQuantity(quantity);

        return reservationRepository.save(reservation);
    }


    @Transactional
    public void releaseReservation(Reservation r) {
        Inventory inv = r.getInventory();
        inv.setQty(inv.getQty() + r.getQuantity());
        reservationRepository.delete(r);
    }

    public void releaseAllReservationsFromCustomer(Customer c) {
        reservationRepository.findByCustomerEmail(c.getEmail()).forEach(this::releaseReservation);
    }

    public void deleteReservationByCustomerId(Long id){
        reservationRepository.deleteAllByCustomerId(id);
    }



    //Cancel a reservation and release stock
    @Transactional
    public void releaseReservationByID(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        Inventory inventory = reservation.getInventory();
        inventory.setQty(
                inventory.getQty() + reservation.getQuantity()
        );

        reservationRepository.delete(reservation);
    }

    //Get all reservations for a customer email
    public List<Reservation> getReservationsForCustomer(String email) {
        return reservationRepository.findByCustomerEmail(email);
    }


}



