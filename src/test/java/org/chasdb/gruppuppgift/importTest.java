package org.chasdb.gruppuppgift;

import org.chasdb.gruppuppgift.cli.AppRunner;
import org.chasdb.gruppuppgift.repositories.CategoryRepository;
import org.chasdb.gruppuppgift.util.CSVImporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class importTest {

    @Autowired
    CSVImporter importer;
    @Autowired
    CategoryRepository repo;

    @MockitoBean
    AppRunner appRunner;

    @Test
    public void importingSuccessful(){
        Path path = Paths.get("src/TESTIMPORT.CSV");
        try{
            importer.importCsv(Files.newInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertThat(repo.findByName("Outerwear"));




    }



}
