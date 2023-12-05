package com.cyber.knowledgebase.fts.service;

import com.cyber.knowledgebase.fts.dto.SearchResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface TextSearchService {
    Stream<SearchResult> findAll();

    Optional<SearchResult> findById(Long id);

    List<SearchResult> findByLocationList(Collection<String> locationList);

    Stream<SearchResult> searchByQuery(String plainQuery);
}
