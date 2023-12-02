package com.cyber.knowledgebase.fts.postgres;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import com.cyber.knowledgebase.fts.dto.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class PostgresTextSearchService {
    private static final String INSERT_QUERY = """
            insert into ftsdocument
              (doc_uid, doc_type, location, modified, title, content)
            values
              (?, ?, ?, ?, ?, to_tsvector('russian', ?))
            on conflict (location)
              do update set modified=?, title=?, content=to_tsvector('russian', ?);
            """;

    private static final String SEARCH_QUERY = """
            select doc_uid, doc_type, location, modified, title
            from ftsdocument
            where make_tsvector(title, content) @@ plainto_tsquery(?);
            """;

    private static final String HEADERS_SEARCH_QUERY = """
            select h.doc_uid, d.doc_type, d.location, d.modified, d.title, h.header
            from ftsheader h
            left join ftsdocument d on h.doc_uid = d.doc_uid
            where to_tsvector('russian', h.header) @@ plainto_tsquery(?);
            """;

    private static final String SEARCH_ALL_QUERY = """
            select h.doc_uid, d.doc_type, d.location, d.modified, d.title, h.header
            from ftsheader h
            left join ftsdocument d on h.doc_uid = d.doc_uid
            where to_tsvector('russian', h.header) @@ plainto_tsquery(?)
            union
            select doc_uid, doc_type, location, modified, title, '' as header
            from ftsdocument
            where make_tsvector(title, content) @@ plainto_tsquery(?);
            """;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void index(DocumentIndexRequest request) {
        jdbcTemplate.update(INSERT_QUERY,
                request.getDocId(),
                request.getDocType(),
                request.getLocation(),
                Timestamp.valueOf(request.getModified()),
                request.getTitle(),
                request.getContent(),
                // on conflict update fields
                request.getModified(),
                request.getTitle(),
                request.getContent()
        );
    }

    public List<SearchResult> search(String plainQuery) {
        return jdbcTemplate.query(
                SEARCH_QUERY,
                BeanPropertyRowMapper.newInstance(SearchResult.class),
                plainQuery
        );
    }

    public List<SearchResult> searchHeaders(String plainQuery) {
        return jdbcTemplate.query(
                HEADERS_SEARCH_QUERY,
                BeanPropertyRowMapper.newInstance(SearchResult.class),
                plainQuery
        );
    }

    public List<SearchResult> searchAll(String plainQuery) {
        return jdbcTemplate.query(
                SEARCH_ALL_QUERY,
                BeanPropertyRowMapper.newInstance(SearchResult.class),
                plainQuery,
                plainQuery
        );
    }


}
