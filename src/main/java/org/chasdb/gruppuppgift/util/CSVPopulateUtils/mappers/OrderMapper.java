package org.chasdb.gruppuppgift.util.CSVPopulateUtils.mappers;

import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVRecord;
import org.chasdb.gruppuppgift.models.*;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.chasdb.gruppuppgift.repositories.CustomerRepository;
import org.chasdb.gruppuppgift.repositories.OrderItemRepository;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class OrderMapper implements CsvEntityMapper<Order>{

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final Map<String, Order> cache = new HashMap<>();

    public OrderMapper(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CustomerRepository customerRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;


        this.orderRepository.findAll().forEach(o -> cache.put(o.getOrdercode(), o)
        );
    }

    @Override
    public Order map(CSVRecord record) {
        if (!record.isSet("order") || !record.isSet("items") || !record.isSet("status")||!record.isSet("price")|| !record.isSet("email")) {
            throw new IllegalArgumentException("Record"+ record.getRecordNumber() +" missing one or more mandatory columns (order, items, price,status,email)");
        }

        String code = record.get("order").trim();
        String price = record.get("price").trim();
        String status = record.get("status").trim();
        String customerEmail = record.get("email").trim();
        String createdAt = record.get("createdAt").trim();

        String itemsStr = record.get("items").trim();
        String itemsQTY = record.get("qty").trim();

        if (code.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Order contained blank column 'order'");
        if (status.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Order contained blank column 'status'");
        if (customerEmail.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Order contained blank column 'email'");
        if (itemsStr.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Order contained blank column 'items'");

        return cache.computeIfAbsent(code, s -> {
            Order order;

            try {
                Customer c = customerRepository.findByEmail(customerEmail).orElseThrow();
                LocalDateTime orderDate = LocalDateTime.parse(createdAt);

                order = new Order(c, orderDate, OrderStatus.valueOf(status));
                order.setOrdercode(code);
                order.setTotal_Price(new BigDecimal(price));

            } catch (NumberFormatException e) {
                System.err.println("Record #" + record.getRecordNumber() + " for Order contained unparsable field for 'Price'");
                return null;
            } catch (DateTimeParseException e) {
                System.err.println("Could not parse datetime of Record#" + record.getRecordNumber() + ", Column createdAt was not a valid datetime format");
                return null;
            } catch (NoSuchElementException e) {
                System.err.println("Record#" + record.getRecordNumber() + ", referenced Customer Email not in database");
                return null;
            } catch (IllegalArgumentException e) {
                System.err.println("Record#" + record.getRecordNumber() + ", field 'status' does not qualify as ENUM");
                return null;
            }

            try {
                // Handle items
                if (!itemsStr.isBlank() && !itemsQTY.isBlank()) {
                    String[] orderItems = itemsStr.split("\\|");
                    String[] orderItemQuantities = itemsQTY.split("\\|");

                    if (orderItems.length != orderItemQuantities.length) {
                        throw new IllegalArgumentException("Record#" + record.getRecordNumber() + ", field 'status' does not qualify as ENUM");
                    }
                    Map<String,OrderItem> itemsToSave = new HashMap<>();
                    for (int i = 0; i < orderItems.length; i++) {

                        Product p = productRepository.findBySku(orderItems[i]).orElseThrow();
                        int qty = Integer.parseInt(orderItemQuantities[i]);

                        itemsToSave.compute(p.getSku(), (k,v)-> {
                                    if (v == null) {
                                        return new OrderItem(order,p,qty);
                                    } else {
                                        v.addQuantity(qty);
                                        return v;
                                    }
                                }
                        );

                    }
                    itemsToSave.values().forEach(order::addOrderItem);

                }
            } catch (NumberFormatException e) {
                System.err.println("Record #" + record.getRecordNumber() + " for Order contained invalid entry in field 'qty'");
                return null;
            } catch (NoSuchElementException e) {
                System.err.println("Record#" + record.getRecordNumber() + ", referenced ProductSKU not in database");
                return null;
            } catch (IllegalArgumentException e) {
                System.err.println("Record#" + record.getRecordNumber() + ", has fields 'items' and 'qty' of mismatched length");
                return null;
            }

            return order;
        });

    }

    @Override
    @Transactional
    public void save(Order entity) {
        try{
            orderRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Product with SKU "+ entity.getOrdercode() +" already exists in database");
        } catch (IllegalStateException e) {
            System.err.println("Order with code"+ entity.getOrdercode() +"failed validation");
        }
    }

    @Override
    public String supportsType() {
        return "ORDER";
    }
}
