package com.cyber.knowledgebase.fts.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class DocumentIndexRequest {
    private Long id;
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

    public void addHeader(String header) {
        headers.add(header);
    }

}
