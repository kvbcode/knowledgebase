package com.cyber.knowledgebase.rest.controller;

import com.cyber.knowledgebase.rest.service.StaticResourceMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceController {

    @Autowired
    StaticResourceMapperService resourceMapperService;

    @GetMapping(path = "/res/{id}/")
    public Resource loadRootFile(@PathVariable Long id) {
        return resourceMapperService.loadRootFile(id);
    }

    @GetMapping(path = "/res/{resourceId}/{resourceDir}/{resourceFile}")
    public Resource loadDirResources(
            @PathVariable Long resourceId,
            @PathVariable String resourceDir,
            @PathVariable String resourceFile
    ) {
        return resourceMapperService.loadDirResources(resourceId, resourceDir, resourceFile);
    }

}
