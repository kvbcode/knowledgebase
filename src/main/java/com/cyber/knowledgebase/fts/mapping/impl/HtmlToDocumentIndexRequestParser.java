package com.cyber.knowledgebase.fts.mapping.impl;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class HtmlToDocumentIndexRequestParser extends AbstractDocumentIndexRequestParser {
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".htm", ".html");
    private static final String DOC_TYPE = "HTML";

    public List<String> getSupportedFileExtensions() {
        return SUPPORTED_EXTENSIONS;
    }

    public Optional<DocumentIndexRequest> parse(URI location, LocalDateTime modified, byte[] content) {
        String htmlFileContent = new String(content);
        Document htmlDoc = Jsoup.parse(htmlFileContent);
        Elements headerElements = htmlDoc.select("h1, h2, h3, h4, h5, h6, h7, h8, h9");

        DocumentIndexRequest indexRequest = new DocumentIndexRequest();
        indexRequest.setDocType(DOC_TYPE);
        indexRequest.setLocation(location.toString());
        indexRequest.setModified(modified);
        indexRequest.setTitle(htmlDoc.title().trim());

        if (indexRequest.getTitle().isEmpty()){
            indexRequest.setTitle(getFilenameFromURI(location));
        }

        for (Element el : headerElements) {
            indexRequest.addHeader(el.text());
        }

        if (htmlDoc.body().hasText()) {
            indexRequest.setContent(htmlDoc.body().text());
        }

        if (indexRequest.getContent() == null || indexRequest.getContent().isBlank()){
            return Optional.empty();
        }

        return Optional.of(indexRequest);
    }

}
