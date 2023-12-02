package com.cyber.knowledgebase.fts.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class DocumentIndexRequest {

    private UUID docId;
    private String docType;
    private String location;
    private LocalDateTime modified;
    private String title;
    private String content;

    public DocumentIndexRequest() {
        docId = UUID.randomUUID();
        docType = "";
        modified = LocalDateTime.now();
    }

    public DocumentIndexRequest(String location, String title, String content) {
        this();
        this.location = location;
        this.title = title;
        this.content = content;
    }

}
