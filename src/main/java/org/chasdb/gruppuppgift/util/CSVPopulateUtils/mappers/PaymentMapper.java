package org.chasdb.gruppuppgift.util.CSVPopulateUtils.mappers;

import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVRecord;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Payment;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;
import org.chasdb.gruppuppgift.models.enums.PaymentStatus;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.PaymentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PaymentMapper implements CsvEntityMapper<Payment> {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final Map<String,Payment> cache = new HashMap<>();


    public PaymentMapper(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;

        this.paymentRepository.findAll().forEach(p -> cache.put(p.getReference(), p));
    }

    @Override
    public Payment map(CSVRecord record) {
        if (!record.isSet("method") || !record.isSet("status") || !record.isSet("createdAt")||!record.isSet("reference")||!record.isSet("order")) {
            throw new IllegalArgumentException("Record missing one or more mandatory columns (method,status,createdAt,reference,order)");
        }

        String method = record.get("method");
        String status = record.get("status");
        String createdAt = record.get("createdAt");
        String reference = record.get("reference");
        String ordercode = record.get("order");

        if (method.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Payment contained blank column 'method'");
        if (status.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Payment contained blank column 'status'");
        if (reference.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Payment contained blank column 'reference'");
        if (ordercode.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Payment contained blank column 'order'");

        return cache.computeIfAbsent(reference, s -> {
            Payment payment;
            try{
                Order order = orderRepository.findByCode(ordercode).orElseThrow();
                payment = new Payment(PaymentMethod.valueOf(method), PaymentStatus.valueOf(status),order);
                payment.setReference(reference);
                payment.setTimestamp(LocalDateTime.parse(createdAt));
            } catch (DateTimeParseException e) {
                System.err.println("Could not parse datetime of Record#"+record.getRecordNumber()+", Column createdAt was not a valid datetime format");
                return null;
            }  catch (NoSuchElementException e) {
                System.err.println("Record#" + record.getRecordNumber() + ", referenced Order Code not in database");
                return null;
            }  catch (IllegalArgumentException e) {
                System.err.println("Record#" + record.getRecordNumber() + ", field 'status' or 'Method' does not qualify as ENUM");
                return null;
            }


            return payment;
        });


    }

    @Override
    @Transactional
    public void save(Payment entity) {
        try{
            paymentRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            System.err.println("payment with reference "+ entity.getReference() +" already exists in database");
        }
    }

    @Override
    public String supportsType() {
        return "PAYMENT";
    }
}
