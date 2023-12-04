package com.cyber.knowledgebase.fts.service;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.mapping.DocumentIndexRequestParserChain;
import com.cyber.knowledgebase.fts.postgres.PostgresTextSearchService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileIndexService {
    private static int BATCH_SIZE = 50;

    private record ModifiedLocation(URI location, LocalDateTime lastModified) {
    }

    @Autowired
    PostgresTextSearchService indexerService;

    @Autowired
    DocumentIndexRequestParserChain documentIndexRequestParserChain;

    public void scanDirectory(Path dir) throws IOException {
        ArrayList<ModifiedLocation> modifiedLocations = new ArrayList<>(BATCH_SIZE);

        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                LocalDateTime lastModified = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
                URI location = file.toAbsolutePath().toUri();

                if (documentIndexRequestParserChain.isSupported(location)) {
                    modifiedLocations.add(new ModifiedLocation(location, lastModified));
                    if (modifiedLocations.size() == BATCH_SIZE) {
                        indexLocationsBatch(modifiedLocations);
                        modifiedLocations.clear();
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                log.warn("file visiting failed", exc);
                return FileVisitResult.CONTINUE;
            }
        });

        if (!modifiedLocations.isEmpty()) {
            indexLocationsBatch(modifiedLocations);
        }
    }

    private long indexLocationsBatch(List<ModifiedLocation> modifiedLocations) throws IOException {
        long successCount = 0;

        List<String> locationList = modifiedLocations.stream()
                .map(ModifiedLocation::location)
                .map(String::valueOf)
                .toList();

        Map<URI, LocalDateTime> fileLocationTimeMap = modifiedLocations.stream()
                .collect(Collectors.toMap(ModifiedLocation::location, ModifiedLocation::lastModified));

        List<SearchResult> searchResults = indexerService.findByLocationList(locationList);

        for (SearchResult searchResult : searchResults) {
            URI location = URI.create(searchResult.getLocation());
            LocalDateTime lastModified = fileLocationTimeMap.get(location);
            Path file = Path.of(location);

            if (isUpdateNeeded(searchResult, lastModified)) {
                byte[] content = Files.readAllBytes(file);
                Optional<DocumentIndexRequest> documentIndexRequest =
                        documentIndexRequestParserChain.parse(location, lastModified, content);
                documentIndexRequest.ifPresent(indexerService::index);
                successCount++;
            }
        }
        return successCount;
    }

    private boolean isUpdateNeeded(SearchResult searchResult, LocalDateTime lastModified) {
        if (searchResult == null) return true;
        LocalDateTime dbTime = searchResult.getModified().withNano(0);
        LocalDateTime fileTime = lastModified.withNano(0);
        return fileTime.isAfter(dbTime);
    }

}
