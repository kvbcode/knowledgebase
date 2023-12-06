package com.cyber.knowledgebase.rest.service;

import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.service.TextSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class SearchService {
    @Autowired
    TextSearchService textSearchService;

    @Value("${app.resource.location.mapping.pattern}")
    String locationPattern;

    @Value("${app.search.result.limit}")
    Integer defaultSearchLimit;

    public List<SearchResult> search(String query, Integer limit) {
        if (limit == null) limit = defaultSearchLimit;
        try (Stream<SearchResult> resultStream = textSearchService.searchByQuery(query)) {
            return resultStream
                    .map(this::mapLocation)
                    .limit(limit)
                    .toList();
        }
    }

    private SearchResult mapLocation(SearchResult searchResult) {
        String location = locationPattern.formatted(searchResult.getId());
        searchResult.setLocation(location);
        return searchResult;
    }

}
