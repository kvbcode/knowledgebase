package com.cyber.knowledgebase.fts.controller;

import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.postgres.PostgresTextSearchService;
import com.cyber.knowledgebase.fts.service.MarkDownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class ResourceController {
    @Autowired
    PostgresTextSearchService searchService;

    @Autowired
    MarkDownService markDownService;

    @GetMapping(path = "/res/{id}/")
    public Resource loadRootFile(@PathVariable Long id) {
        SearchResult searchResult = searchService.findById(id);
        if (searchResult == null) throw new RuntimeException("Not Found");

        String location = searchResult.getLocation();
        Resource urlResource = FileUrlResource.from(URI.create(location));

        if (location.endsWith(".md")) {
            urlResource = markDownService.mapToHtmlResource(urlResource);
        }

        return urlResource;
    }

    @GetMapping(path = "/res/{resourceId}/{resourceDir}/{resourceFile}")
    public Resource loadDirResources(
            @PathVariable Long resourceId,
            @PathVariable String resourceDir,
            @PathVariable String resourceFile
    ) {
        SearchResult searchResult = searchService.findById(resourceId);
        if (searchResult == null) throw new RuntimeException("Not Found");

        String location = searchResult.getLocation();
        URI fileUri = URI.create(location);
        Path workDir = Path.of(fileUri).getParent();
        Path filePath = workDir.resolve(resourceDir).resolve(resourceFile);

        if (!Files.exists(filePath)) {
            Path htmFixPath = Path.of(filePath + ".htm");
            if (Files.exists(htmFixPath)) {
                filePath = htmFixPath;
            }
        }

        return FileUrlResource.from(filePath.toUri());
    }

}
