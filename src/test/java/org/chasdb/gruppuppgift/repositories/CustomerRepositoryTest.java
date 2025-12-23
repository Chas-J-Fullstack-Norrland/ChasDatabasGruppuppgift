package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testfindId() {
        Customer c = new Customer();
        c.setName("Test User");
        c.setEmail("test.user@example.com");

        Customer saved = customerRepository.save(c);

        assertThat(saved.getId()).isNotNull();


        Optional<Customer> fetched = customerRepository.findById(saved.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getEmail()).isEqualTo("test.user@example.com");
    }


    @Test
    void testFindAllCustomers() {
        Customer c1 = new Customer();
        c1.setName("User One");
        c1.setEmail("test.user1@example.com");
        customerRepository.save(c1);
        List<Customer> all = customerRepository.findAll();
        assertThat(all).hasSize(1);
    }

}
