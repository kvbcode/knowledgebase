package com.cyber.knowledgebase.fts.controller;

import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.postgres.PostgresTextSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    @Autowired
    PostgresTextSearchService searchService;

    @GetMapping(path = "/search")
    public List<SearchResult> search(@RequestParam("q") String query){
        return searchService.searchAll(query);
    }

    @GetMapping(path = "/search2")
    public List<SearchResult> search2(@RequestParam("q") String query){
        return searchService.search(query);
    }


}
