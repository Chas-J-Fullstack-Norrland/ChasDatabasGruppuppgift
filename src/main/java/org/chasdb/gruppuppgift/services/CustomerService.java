package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public Customer registerCustomer(String name, String email) {
        return customerRepository.save(new Customer(name, email));
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
}
