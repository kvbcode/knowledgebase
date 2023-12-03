package com.cyber.knowledgebase.fts;

import com.cyber.knowledgebase.fts.postgres.PostgresTextSearchService;
import com.cyber.knowledgebase.fts.service.FileIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@SpringBootApplication
public class FtsJdbcApplication {

    @Autowired
    FileIndexService fileIndexService;

    public static void main(String[] args) {
        SpringApplication.run(FtsJdbcApplication.class, args);
    }

    //@Bean
    public CommandLineRunner run1() {
        return args -> {
            List<String> sources = Files.readAllLines(Path.of("sources.txt"));
            for (String item : sources) {
                fileIndexService.indexDirectory(Path.of(item));
            }
        };
    }
}
