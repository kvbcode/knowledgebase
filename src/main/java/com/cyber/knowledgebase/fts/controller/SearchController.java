package com.cyber.knowledgebase.fts.controller;

import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.postgres.PostgresTextSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    @Autowired
    PostgresTextSearchService searchService;

    @CrossOrigin
    @GetMapping(path = "/search")
    public List<SearchResult> search(@RequestParam("q") String query) {
        List<SearchResult> searchResults = searchService.searchAll(query);
        searchResults.forEach(item -> {
            item.setLocation("http://cyberwork:8080/res/" + item.getId() + "/");
        });
        return searchResults;
    }

}
