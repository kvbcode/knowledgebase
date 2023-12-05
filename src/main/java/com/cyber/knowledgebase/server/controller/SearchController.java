package com.cyber.knowledgebase.server.controller;

import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.service.TextSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@RestController
public class SearchController {

    @Autowired
    TextSearchService searchService;

    @CrossOrigin
    @GetMapping(path = "/search")
    public List<SearchResult> search(@RequestParam("q") String query) {
        try (Stream<SearchResult> resultStream = searchService.searchByQuery(query)) {
            return resultStream
                    .map(item -> {
                        item.setLocation("http://cyberwork:8080/res/" + item.getId() + "/");
                        return item;
                    }).toList();
        }
    }

}
