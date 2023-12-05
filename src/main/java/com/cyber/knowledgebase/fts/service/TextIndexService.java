package com.cyber.knowledgebase.fts.service;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;

import java.util.Collection;

public interface TextIndexService {
    void index(DocumentIndexRequest request);

    void deleteByIds(Collection<Long> ids);
}
