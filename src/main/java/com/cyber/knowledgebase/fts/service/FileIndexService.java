package com.cyber.knowledgebase.fts.service;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.mapping.DocumentIndexRequestParserChain;
import com.cyber.knowledgebase.fts.postgres.PostgresTextSearchService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
public class FileIndexService {
    @Autowired
    PostgresTextSearchService indexerService;

    @Autowired
    DocumentIndexRequestParserChain documentIndexRequestParserChain;

    public void indexDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                LocalDateTime lastModified = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
                URI location = file.toAbsolutePath().toUri();
                Optional<DocumentIndexRequest> documentIndexRequest = Optional.empty();

                if (documentIndexRequestParserChain.isSupported(location)) {
                    if (isUpdateNeeded(location, lastModified)) {
                        byte[] content = Files.readAllBytes(file);
                        documentIndexRequest = documentIndexRequestParserChain.parse(location, lastModified, content);
                    }
                }

                documentIndexRequest.ifPresent(indexerService::index);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                log.warn("file visiting failed", exc);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private boolean isUpdateNeeded(URI location, LocalDateTime lastModified) {
        SearchResult searchResult = indexerService.findByLocation(location.toString());
        if (searchResult == null) return true;

        LocalDateTime dbTime = searchResult.getModified().withNano(0);
        LocalDateTime fileTime = lastModified.withNano(0);
        return fileTime.isAfter(dbTime);
    }

}
