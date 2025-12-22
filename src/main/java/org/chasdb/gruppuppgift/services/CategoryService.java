package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Category;
import org.chasdb.gruppuppgift.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired
    CategoryRepository repo;

    public Category newCategory(String name){
        return repo.save(new Category(name));
    }


}
