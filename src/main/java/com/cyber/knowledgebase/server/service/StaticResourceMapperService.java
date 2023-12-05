package com.cyber.knowledgebase.server.service;

import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.postgres.PostgresTextSearchService;
import com.cyber.knowledgebase.server.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class StaticResourceMapperService {
    @Autowired
    PostgresTextSearchService searchService;

    @Autowired
    MarkDownService markDownService;

    public Resource loadRootFile(@PathVariable Long id) {
        SearchResult searchResult = searchService.findById(id)
                .orElseThrow(NotFoundException::new);

        String location = searchResult.getLocation();
        Resource urlResource = FileUrlResource.from(URI.create(location));

        if (location.endsWith(".md")) {
            urlResource = markDownService.mapToHtmlResource(urlResource);
        }

        return urlResource;
    }

    public Resource loadDirResources(Long resourceId, String resourceDir, String resourceFile) {
        SearchResult searchResult = searchService.findById(resourceId)
                .orElseThrow(NotFoundException::new);

        URI fileUri = searchResult.getLocationURI();
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
