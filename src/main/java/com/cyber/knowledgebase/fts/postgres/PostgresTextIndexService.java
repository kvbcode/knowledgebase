package com.cyber.knowledgebase.fts.postgres;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import com.cyber.knowledgebase.fts.service.TextIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;

@Service
public class PostgresTextIndexService implements TextIndexService {
    private static final String INSERT_DOC_QUERY = """
            insert into ftsdocument
              (doc_type, location, modified, title, content)
            values
              (:doc_type, :location, :modified, :title, to_tsvector('russian', :content))
            returning id;
            """;

    private static final String UPDATE_DOC_QUERY = """
            update ftsdocument
              set doc_type=:doc_type, modified=:modified, title=:title, content=to_tsvector('russian', :content)
            where
              id = :id;
            """;

    private static final String DELETE_BY_IDS = "delete from ftsdocument where id in (:ids);";

    private static final String DELETE_HEADERS_BY_DOC_IDS = "delete from ftsheader where doc_id in (:doc_ids);";

    private static final String INSERT_HEADER = "insert into ftsheader (doc_id, header) values (:doc_id,:header);";


    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Override
    public void index(DocumentIndexRequest request) {
        indexInTransaction(request);
    }

    private void indexInTransaction(DocumentIndexRequest request) {
        transactionTemplate.executeWithoutResult(ts -> {
            if (request.getId() != null) {
                update(request);
            } else {
                insert(request);
            }
        });
    }

    private MapSqlParameterSource mapDocumentIndexRequestToSqlParams(DocumentIndexRequest request) {
        return new MapSqlParameterSource()
                .addValue("id", request.getId())
                .addValue("doc_type", request.getDocType())
                .addValue("location", request.getLocation())
                .addValue("modified", request.getModified())
                .addValue("title", request.getTitle())
                .addValue("content", request.getContent());
    }

    private Long insert(DocumentIndexRequest request) {
        MapSqlParameterSource params = mapDocumentIndexRequestToSqlParams(request);
        Long docId = jdbcTemplate.queryForObject(INSERT_DOC_QUERY, params, Long.class);
        updateHeaders(docId, request.getHeaders());
        return docId;
    }

    private void update(DocumentIndexRequest request) {
        MapSqlParameterSource params = mapDocumentIndexRequestToSqlParams(request);
        jdbcTemplate.update(UPDATE_DOC_QUERY, params);
        updateHeaders(request.getId(), request.getHeaders());
    }

    private void updateHeaders(Long docId, List<String> headers) {
        jdbcTemplate.update(DELETE_HEADERS_BY_DOC_IDS, new MapSqlParameterSource("doc_ids", List.of(docId)));

        MapSqlParameterSource[] batchInsertArgs = headers.stream()
                .map(header -> new MapSqlParameterSource()
                        .addValue("doc_id", docId)
                        .addValue("header", header)
                )
                .toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(INSERT_HEADER, batchInsertArgs);
    }

    @Override
    public void deleteByIds(Collection<Long> ids) {
        transactionTemplate.executeWithoutResult(ts -> {
            jdbcTemplate.update(DELETE_HEADERS_BY_DOC_IDS, new MapSqlParameterSource("doc_ids", ids));
            jdbcTemplate.update(DELETE_BY_IDS, new MapSqlParameterSource("ids", ids));
        });
    }

}
