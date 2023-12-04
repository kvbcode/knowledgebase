package com.cyber.knowledgebase.fts.mapping;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

public interface DocumentIndexRequestParser {
    boolean isSupported(URI location);

    Optional<DocumentIndexRequest> parse(URI location, LocalDateTime modified, byte[] content);
}
