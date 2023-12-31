package com.cyber.knowledgebase.fts.mapping;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;

import java.net.URI;
import java.util.Optional;

public interface DocumentParser {
    boolean isSupported(URI location);

    Optional<DocumentIndexRequest> parse(URI location, byte[] content);
}
