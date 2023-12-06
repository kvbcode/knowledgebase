package com.cyber.knowledgebase.fts.mapping;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
public class DocumentParserChain implements DocumentParser {

    private final List<DocumentParser> parserList;

    public DocumentParserChain(List<DocumentParser> parserList) {
        this.parserList = parserList.stream()
                .filter(parserItem -> parserItem != this)
                .toList();
    }

    @Override
    public boolean isSupported(URI location) {
        for (DocumentParser parser : parserList) {
            if (parser.isSupported(location)) return true;
        }
        return false;
    }

    @Override
    public Optional<DocumentIndexRequest> parse(URI location, byte[] content) {
        for (DocumentParser parser : parserList) {
            if (parser.isSupported(location)) {
                Optional<DocumentIndexRequest> indexRequest = parser.parse(location, content);
                if (indexRequest.isPresent()) return indexRequest;
            }
        }
        return Optional.empty();
    }

}
