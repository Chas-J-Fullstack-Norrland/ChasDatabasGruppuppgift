package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Category;
import org.chasdb.gruppuppgift.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repo;

    public Category newCategory(String name){
        Optional<Category> duplicateCategory = repo.findByName(name);
        if (duplicateCategory.isPresent()){
            throw new IllegalStateException("Category of that name already exists");
        }

        return repo.save(new Category(name));
    }

    public List<Category> listCategories(){
        return repo.findAll();
    }

}
