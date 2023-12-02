package com.cyber.knowledgebase.fts.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class DocumentIndexRequest {
    private String docType;
    private String location;
    private LocalDateTime modified;
    private String title;
    private List<String> headers;
    private String content;

    public DocumentIndexRequest() {
        modified = LocalDateTime.now();
        docType = "";
        headers = new LinkedList<>();
    }

    public DocumentIndexRequest(String location, String title, String content) {
        this();
        this.location = location;
        this.title = title;
        this.content = content;
    }

    public void addHeader(String header) {
        headers.add(header);
    }

}
