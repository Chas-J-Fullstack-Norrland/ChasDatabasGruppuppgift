package org.chasdb.gruppuppgift.util.CSVPopulateUtils.mappers;


import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVRecord;
import org.chasdb.gruppuppgift.models.Category;
import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerMapper implements CsvEntityMapper<Customer>{


    private final CustomerRepository customerRepository;
    private final Map<String, Customer> cache = new HashMap<>();

    public CustomerMapper(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;

    }


    public void init(){
        this.customerRepository.findAll().forEach(c -> cache.put(c.getName(), c));
    }

    @Override
    public Customer map(CSVRecord record) {

        if(!record.isSet("name")|| !record.isSet("email")|| !record.isSet("createdAt")){
            throw new IllegalArgumentException("record lacks one or more required columns <name>,<email>,<createdAt>");
        }

        String email = record.get("email").trim();
        String name = record.get("name").trim();
        if (name.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Customer contained blank column 'name'");
        if (email.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Customer contained blank column 'email'");


        return cache.computeIfAbsent(email, e -> {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setName(name);

            try{
                customer.setCreatedAt(LocalDateTime.parse(record.get("createdAt")));
            }catch (DateTimeParseException ex){
                System.err.println("Could not parse datetime of Record#"+record.getRecordNumber()+", Column createdAt was not a valid datetime format");
                return null;
            }

            return customer;}
        );
    }

    @Override
    @Transactional
    public void save(Customer entity) {
        try{
            customerRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Customer with email "+ entity.getEmail() +" already exists in database");
        }
    }

    @Override
    public String supportsType() {
        return "CUSTOMER";
    }
}
