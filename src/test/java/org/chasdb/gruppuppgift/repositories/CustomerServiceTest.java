package org.chasdb.gruppuppgift.repositories;


import org.chasdb.gruppuppgift.models.Customer;

import org.chasdb.gruppuppgift.services.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CustomerServiceTest {

    @Autowired
    private CustomerService service;

    @Autowired
    private CustomerRepository repo;

    @Test
    void testRegisterAndFindCustomer() {
        Customer customer = service.registerCustomer("TestUser", "test@example.com");
        assertNotNull(customer.getId());

        Optional<Customer> found = service.getCustomerByEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals("TestUser", found.get().getName());
    }

    @Test
    void testListCustomers() {
        service.registerCustomer("A", "a@example.com");
        service.registerCustomer("B", "b@example.com");

        List<Customer> customers = service.listCustomers();
        assertTrue(customers.size() >= 2);
    }

    @Test
    void testDeleteCustomer() {
        Customer customer = service.registerCustomer("DeleteMe", "deleteme@example.com");
        Long id = customer.getId();

        service.deleteCustomer(id);

        assertFalse(repo.findById(id).isPresent());
    }
}