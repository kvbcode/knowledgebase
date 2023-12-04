package com.cyber.knowledgebase.fts.mapping.impl;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class MarkdownToDocumentIndexRequestParser extends AbstractDocumentIndexRequestParser {
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".md");
    private static final String DOC_TYPE = "MARKDOWN";

    public List<String> getSupportedFileExtensions() {
        return SUPPORTED_EXTENSIONS;
    }

    public Optional<DocumentIndexRequest> parse(URI location, LocalDateTime modified, byte[] content) {
        DocumentIndexRequest doc = new DocumentIndexRequest();
        doc.setDocType(DOC_TYPE);
        doc.setLocation(location.toString());
        doc.setTitle(getFilenameFromURI(location));
        doc.setModified(modified);

        String[] lines = new String(content).split("[\\n\\r]");
        StringBuilder contentBody = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("#")) {
                doc.addHeader(line);
            } else {
                contentBody.append(line).append(" ");
            }
        }
        doc.setContent(contentBody.toString());

        return Optional.of(doc);
    }

}
