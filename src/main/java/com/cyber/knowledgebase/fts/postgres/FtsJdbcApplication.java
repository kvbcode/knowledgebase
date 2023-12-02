package com.cyber.knowledgebase.fts.postgres;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootApplication
public class FtsJdbcApplication {

    @Autowired
    PostgresTextSearchService textSearchService;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(FtsJdbcApplication.class, args);
    }

    //@Bean
    public CommandLineRunner run1() {
        int batchSize = 10;
        return args -> {
            List<String> films = Files.readAllLines(Path.of("films-viewed.txt"));

            //transactionTemplate.executeWithoutResult(ts -> {
                for (String line : films) {
                    System.out.println(line);
                    String[] parts = line.split(" _ ");

                    String location = parts.length > 1
                            ? parts[1]
                            : String.valueOf(System.nanoTime());

                    if (location.isBlank()) location = String.valueOf(System.nanoTime());

                    DocumentIndexRequest documentIndexRequest = new DocumentIndexRequest(location, parts[0], line);
                    textSearchService.index(documentIndexRequest);

                    String[] headerParts = line.split("\\(");
                    if (headerParts.length > 1) {
                        String year = "(" + headerParts[1];
                        try {
                            jdbcTemplate.update("insert into ftsheader (doc_uid, header) values (?,?);",
                                    documentIndexRequest.getDocId(), year);
                        } catch (DataIntegrityViolationException ex) {
                            log.warn("header storing error", ex);
                        }
                    }
                }
                ;
            //});
        };
    }

    @Bean
    public CommandLineRunner run2() {
        return args -> {
//            textSearchService.search("star wars").stream()
//                    .forEach(result -> System.out.println(result.getTitle()));
//
//            System.out.println("REQUEST-HEADERS");
//
//            textSearchService.searchHeaders("lostfilm").stream()
//                    .forEach(result -> System.out.println(result.getTitle()));

            textSearchService.searchAll("lost").stream()
                    .forEach(result -> System.out.println(result.getTitle() + " " + result.getHeader()));

        };
    }
}
