package org.chasdb.gruppuppgift.cli.handlers;

import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.cli.CommandHandler;
import org.chasdb.gruppuppgift.cli.CommandInput;
import org.chasdb.gruppuppgift.repositories.CategoryRepository;
import org.chasdb.gruppuppgift.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryCommandHandler implements CommandHandler {

    @Autowired
    private CategoryService categoryService;

    @Override
    public void handle(CommandInput input) {
        try {
            switch (input.action()) {
                case "add" -> add(input.args());
                case "list" -> list();
                default -> System.out.println("Okänd åtgärd för kategori.");
            }
        } catch (Exception e) {
            System.out.println("FEL: " + e.getMessage());
        }
    }

    @Override
    public String getDomain() {
        return "category";
    }

    //@Transactional //Undo the comment of transactional if partial successes are unacceptable... probably is acceptable
    private void add(List<String> args){
        args.forEach(categoryService::newCategory);
    }

    private void list(){
        System.out.println("Available Categories");
        categoryService.listCategories().forEach(c->System.out.println("> " + c.getName()));
    }


}
