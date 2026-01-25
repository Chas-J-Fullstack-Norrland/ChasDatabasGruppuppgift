package org.chasdb.gruppuppgift.util.CSVPopulateUtils.mappers;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVRecord;
import org.chasdb.gruppuppgift.models.Category;
import org.chasdb.gruppuppgift.repositories.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
public class CategoryMapper implements CsvEntityMapper<Category>{


    private final CategoryRepository categoryRepository;
    private final Map<String, Category> cache = new HashMap<>();

    public CategoryMapper(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;

    }


    public void init(){
        this.categoryRepository.findAll().forEach(c -> cache.put(c.getName(), c));
    }
    @Override
    public Category map(CSVRecord record) {
        if(!record.isSet("name")){
            throw new IllegalArgumentException("no value in required CSV column 'Name'");
        }
        String name = record.get("name").trim();

        if (name.isBlank()) throw new IllegalArgumentException("Record #"+ record.getRecordNumber() +" for Category contained blank column 'name'");

        return cache.computeIfAbsent(name, n -> {
            Category newCategory = new Category();
            newCategory.setName(n);
            return newCategory;
        });
    }

    @Override
    @Transactional
    public void save(Category entity) {
        try{
            categoryRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Category "+ entity.getName() +" already exists in database");
        }
    }

    @Override
    public String supportsType() {
        return "CATEGORY";
    }
}
