package com.cyber.knowledgebase.fts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private UUID docId;
    private String docType;
    private String location;
    private LocalDateTime modified;
    private String title;
    private String header = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchResult that = (SearchResult) o;

        if (!Objects.equals(docId, that.docId)) return false;
        return Objects.equals(header, that.header);
    }

    @Override
    public int hashCode() {
        int result = docId != null ? docId.hashCode() : 0;
        result = 31 * result + (header != null ? header.hashCode() : 0);
        return result;
    }
}
