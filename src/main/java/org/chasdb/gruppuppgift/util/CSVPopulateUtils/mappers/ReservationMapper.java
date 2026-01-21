package org.chasdb.gruppuppgift.util.CSVPopulateUtils.mappers;

import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVRecord;
import org.chasdb.gruppuppgift.models.Category;
import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.Reservation;
import org.chasdb.gruppuppgift.repositories.CustomerRepository;
import org.chasdb.gruppuppgift.repositories.InventoryRepository;
import org.chasdb.gruppuppgift.repositories.ReservationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

@Service
public class ReservationMapper implements CsvEntityMapper<Reservation> {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final InventoryRepository inventoryRepository;
    private final Map<AbstractMap.SimpleEntry<String, String>, Reservation> cache = new HashMap<>();


    public ReservationMapper(ReservationRepository reservationRepository, CustomerRepository customerRepository, InventoryRepository inventoryRepository) {
        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
        this.inventoryRepository = inventoryRepository;

        //caching all by customer.email and product.sku TODO! Refactor so all inventory handling goes through product instead.
        this.reservationRepository.findAll().forEach(r -> cache.put(new AbstractMap.SimpleEntry<>(r.getCustomer().getEmail(), r.getInventory().getProduct().getSku()), r));


    }


    @Override
    public Reservation map(CSVRecord record) {
        if (!record.isSet("email") || !record.isSet("sku") || !record.isSet("qty")) {
            throw new IllegalArgumentException("Record#" + record.getRecordNumber() + " missing one or more mandatory columns (email, sku, price)");
        }
        String email = record.get("email").trim();
        String sku = record.get("sku").trim();
        String qty = record.get("qty").trim();
        String reservationInstant = record.get("createdAt");
        String reservationExpiration = record.get("expiresAt");


        if (email.isBlank())
            throw new IllegalArgumentException("Record #" + record.getRecordNumber() + " for Reservation contained blank column 'email'");
        if (sku.isBlank())
            throw new IllegalArgumentException("Record #" + record.getRecordNumber() + " for Reservation contained blank column 'sku'");
        if (qty.isBlank())
            throw new IllegalArgumentException("Record #" + record.getRecordNumber() + " for Reservation contained blank column 'qty'");


        AbstractMap.SimpleEntry<String, String> keyPair = new AbstractMap.SimpleEntry<>(email, sku);
        return cache.compute(keyPair, (k, v) -> {
            if (v == null) {
                Reservation newReservation = new Reservation();
                try {
                    Inventory i = inventoryRepository.findByProduct_Sku(sku).orElseThrow();
                    Customer c = customerRepository.findByEmail(email).orElseThrow();
                    newReservation.setCustomer(c);
                    newReservation.setInventory(i);
                    newReservation.setQuantity(Integer.parseInt(qty));
                    newReservation.setReservedAt(Instant.parse(reservationInstant));
                    newReservation.setExpiresAt(Instant.parse(reservationExpiration));
                    return newReservation;
                } catch (NoSuchElementException e) {
                    System.err.println("Record#" + record.getRecordNumber() + ", referenced an object not in database");
                    return null;
                } catch (NumberFormatException e) {
                    System.err.println("Record #" + record.getRecordNumber() + " for Reservation contained unparsable field 'qty'");
                    return null;
                } catch (DateTimeParseException e) {
                    System.err.println("Could not parse instant of Record#" + record.getRecordNumber() + ", not of valid instant timestamp format");
                    return null;
                }
            } else {
                try {
                    v.setQuantity(Integer.parseInt(v.getQuantity() + qty));
                    return v;
                } catch (NumberFormatException e) {
                    System.err.println("qty in Record#" + record.getRecordNumber() + "Was invalid");
                }
            }
            return null;

        });


    }

    @Override
    @Transactional
    public void save(Reservation entity) {
        try {
            reservationRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Product SKU " + entity.getInventory().getProduct().getSku() + " already reserved by " + entity.getCustomer().getEmail());
        }
    }

    @Override
    public String supportsType() {
        return "RESERVATION";
    }
}


