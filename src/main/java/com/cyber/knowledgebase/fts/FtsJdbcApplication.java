package com.cyber.knowledgebase.fts;

import com.cyber.knowledgebase.fts.service.FileIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class FtsJdbcApplication {

    @Autowired
    FileIndexService fileIndexService;

    public static void main(String[] args) {
        SpringApplication.run(FtsJdbcApplication.class, args);
    }


    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.MINUTES, initialDelay = 1)
    public void runTaskIndex() throws IOException {
        log.debug("run task: index locations");
        List<String> sources = Files.readAllLines(Path.of("sources.txt"));
        for (String item : sources) {
            fileIndexService.scanDirectory(Path.of(item));
        }
    }

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.MINUTES, initialDelay = 5)
    public void runTaskRemoveUnavailableLocation() {
        log.debug("run task: remove unavailable locations");
        long count = fileIndexService.removeUnavailableEntries();
        log.debug("removed unavailable locations: " + count);
    }
}
