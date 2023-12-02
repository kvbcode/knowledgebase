package com.cyber.knowledgebase.fts.mapping;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class DocumentIndexRequestParserChain {

    private final List<DocumentIndexRequestParser> parserList;

    public DocumentIndexRequestParserChain(List<DocumentIndexRequestParser> parserList) {
        this.parserList = parserList;
    }

    public boolean isSupported(URI location) {
        for (DocumentIndexRequestParser parser : parserList) {
            if (parser.isSupported(location)) return true;
        }
        return false;
    }

    public Optional<DocumentIndexRequest> parse(URI location, LocalDateTime modified, byte[] content) {
        for (DocumentIndexRequestParser parser : parserList) {
            if (parser.isSupported(location)) {
                Optional<DocumentIndexRequest> indexRequest = parser.parse(location, modified, content);
                if (indexRequest.isPresent()) return indexRequest;
            }
        }
        return Optional.empty();
    }

}
