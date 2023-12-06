package com.cyber.knowledgebase.fts.service;

import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.mapping.DocumentParserChain;
import com.cyber.knowledgebase.fts.service.impl.PostgresTextSearchService;
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
import java.util.stream.Stream;

@Slf4j
@Service
public class FileIndexService {
    private static int BATCH_SIZE = 50;

    private record ModifiedLocation(URI location, LocalDateTime lastModified) {
    }

    @Autowired
    TextIndexService indexService;

    @Autowired
    PostgresTextSearchService searchService;

    @Autowired
    DocumentParserChain documentParserChain;

    public long removeUnavailableEntries() {
        ArrayList<Long> idsBatch = new ArrayList<>(BATCH_SIZE);

        try (Stream<SearchResult> resultStream = searchService.findAll()) {
            resultStream.filter(searchResult -> !isLocationAvailable(searchResult.getLocationURI()))
                    .map(SearchResult::getId)
                    .forEach(id -> {
                        idsBatch.add(id);
                        if (idsBatch.size() == BATCH_SIZE) {
                            indexService.deleteByIds(idsBatch);
                            idsBatch.clear();
                        }
                    });
            if (!idsBatch.isEmpty()) indexService.deleteByIds(idsBatch);
        }
        return idsBatch.size();
    }

    private boolean isLocationAvailable(URI location) {
        Path path = Path.of(location);
        return Files.exists(path);
    }

    public void scanDirectory(Path dir) throws IOException {
        ArrayList<ModifiedLocation> modifiedLocations = new ArrayList<>(BATCH_SIZE);

        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                LocalDateTime lastModified = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
                URI location = file.toAbsolutePath().toUri();

                if (documentParserChain.isSupported(location)) {
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

        Map<URI, SearchResult> locationResultMap = searchService.findByLocationList(locationList)
                .stream()
                .collect(Collectors.toMap(SearchResult::getLocationURI, v -> v));

        for (ModifiedLocation modifiedLocation : modifiedLocations) {
            URI location = modifiedLocation.location();
            Optional<SearchResult> searchResultOpt = Optional.ofNullable(locationResultMap.get(location));

            Long id = searchResultOpt.map(SearchResult::getId).orElse(null);
            LocalDateTime dbTime = searchResultOpt.map(SearchResult::getModified).orElse(null);
            LocalDateTime fileTime = modifiedLocation.lastModified();
            Path file = Path.of(location);

            if (isUpdateNeeded(fileTime, dbTime)) {
                byte[] content = Files.readAllBytes(file);
                documentParserChain.parse(location, content)
                        .ifPresent(docRequest -> {
                            docRequest.setId(id);
                            docRequest.setModified(fileTime);
                            indexService.index(docRequest);
                        });
                successCount++;
            }
        }
        return successCount;
    }

    private boolean isUpdateNeeded(LocalDateTime fileTime, LocalDateTime dbTime) {
        if (dbTime == null) return true;
        dbTime = dbTime.withNano(0);
        fileTime = fileTime.withNano(0);
        return fileTime.isAfter(dbTime);
    }

}
