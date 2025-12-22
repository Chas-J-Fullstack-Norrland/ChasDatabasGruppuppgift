package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Category;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.AutoConfigureDataJpa;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import javax.swing.text.html.parser.Entity;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    CategoryRepository repository;

    @Autowired
    TestEntityManager entityManager;

    Category newCategory;

    @BeforeEach
    void setup(){
        newCategory = repository.save(new Category("TestCategory"));
    }

    @Test
    void shouldSaveCategoryToDB(){
        repository.save(newCategory);
        assertEquals(newCategory,entityManager.find(Category.class,newCategory.getId()));
    }

    @Test
    void findByID(){
        Optional<Category> fetchedCategory = repository.findById(newCategory.getId());
        assertTrue(fetchedCategory.isPresent());
        assertEquals(newCategory.getName(),fetchedCategory.get().getName());
    }

    @Test
    void updateCategory(){
        newCategory.setName("UPDATED");
        Category categoryToUpdate = repository.save(newCategory);
        assertEquals(newCategory.getName(),entityManager.find(Category.class,categoryToUpdate.getId()).getName());
    }

    @Test
    void deleteCategory(){
        repository.deleteById(newCategory.getId());
        assertFalse(repository.findById(newCategory.getId()).isPresent());
    }




}