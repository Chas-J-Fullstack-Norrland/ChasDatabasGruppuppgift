package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.Reservation;
import org.chasdb.gruppuppgift.repositories.CustomerRepository;
import org.chasdb.gruppuppgift.repositories.InventoryRepository;
import org.chasdb.gruppuppgift.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final InventoryRepository inventoryRepository;
    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationService(InventoryRepository inventoryRepository,
                              CustomerRepository customerRepository,
                              ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation reserveProduct(String productSku, String email, int quantity) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + email));

        Inventory inventory = inventoryRepository.findByProductSkuWithLock(productSku)
                .orElseThrow(() -> new IllegalArgumentException("Product/Inventory not found for SKU: " + productSku));

        if (inventory.getQuantity() < quantity) {
            throw new IllegalStateException("Not enough stock available");
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);

        Reservation reservation = new Reservation(inventory, customer, quantity);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void releaseReservation(Reservation r) {
        Inventory inv = r.getInventory();
        inv.setQuantity(inv.getQuantity() + r.getQuantity());
        reservationRepository.delete(r);
    }

    @Transactional
    public void releaseReservationByID(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        Inventory inventory = reservation.getInventory();
        inventory.setQuantity(inventory.getQuantity() + reservation.getQuantity());

        reservationRepository.delete(reservation);
    }

    public List<Reservation> getReservationsForCustomer(String email) {
        return reservationRepository.findByCustomerEmail(email);
    }
}