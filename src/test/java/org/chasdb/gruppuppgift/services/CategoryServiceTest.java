package org.chasdb.gruppuppgift.services;

import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.cli.AppRunner;
import org.chasdb.gruppuppgift.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class CategoryServiceTest {

    @MockitoBean
    AppRunner appRunner;
    @Autowired
    CategoryService service;



    @Test
    @Transactional
    void savesAndListsCategories(){
        service.newCategory("Test 1");
        service.newCategory("Test 2");
        service.newCategory("test 3");

        assertEquals("test 3",service.listCategories().get(2).getName());

    }



}