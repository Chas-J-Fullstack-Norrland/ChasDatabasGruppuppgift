package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Category;
import org.chasdb.gruppuppgift.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category addCategory(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Kategorinamn får inte vara tomt");
        }

        // Kontrollera dubbletter
        Optional<Category> duplicateCategory = categoryRepository.findByName(name);
        if (duplicateCategory.isPresent()) {
            throw new IllegalStateException("Kategorin '" + name + "' finns redan");
        }

        return categoryRepository.save(new Category(name.trim()));
    }

    public List<Category> listCategories() {
        return categoryRepository.findAll();
    }

}
