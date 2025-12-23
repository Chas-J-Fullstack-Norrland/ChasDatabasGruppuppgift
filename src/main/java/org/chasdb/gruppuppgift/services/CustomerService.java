package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository repo;

    public Customer registerCustomer(String name, String email) {return repo.save(new Customer(name, email));}

    public List<Customer> listCustomers(){return repo.findAll();}

    public Optional<Customer> getCustomerByEmail(String email) { return repo.findByEmail(email);}

    public Optional<Customer> findCustomerByID(Long id){return repo.findById(id);}

    public void deleteCustomer(Long id){repo.deleteById(id);}




}
