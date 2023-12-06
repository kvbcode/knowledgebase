package com.cyber.knowledgebase.rest.controller;

import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.rest.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    @Autowired
    SearchService searchService;

    @CrossOrigin
    @GetMapping(path = "/search")
    public List<SearchResult> search(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return searchService.search(query, limit);
    }

}
