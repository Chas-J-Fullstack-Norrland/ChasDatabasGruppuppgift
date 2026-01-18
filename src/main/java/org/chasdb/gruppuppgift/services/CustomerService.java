package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.repositories.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer registerCustomer(String name, String email) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Namn krävs");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email krävs");

        if (customerRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("En kund med email '" + email + "' finns redan.");
        }

        return customerRepository.save(new Customer(name.trim(), email.trim()));
    }

    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findCustomerByID(Long id) {
        return customerRepository.findById(id);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException("Kan inte ta bort: Kund ID " + id + " finns inte.");
        }
        customerRepository.deleteById(id);
    }




}
