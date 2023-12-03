package com.cyber.knowledgebase.fts.postgres;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import com.cyber.knowledgebase.fts.dto.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
public class PostgresTextSearchService {
    private static final String INSERT_DOC_QUERY = """
            insert into ftsdocument
              (doc_type, location, modified, title, content)
            values
              (?, ?, ?, ?, to_tsvector('russian', ?))
            returning id;
            """;

    private static final String UPDATE_DOC_QUERY = """
            update ftsdocument
              set doc_type=?, modified=?, title=?, content=to_tsvector('russian', ?)
            where
              id = ?;
            """;

    private static final String DELETE_HEADERS_BY_DOCID = "delete from ftsheader where doc_id=?;";

    private static final String INSERT_HEADER = "insert into ftsheader (doc_id, header) values (?,?);";

    private static final String FIND_BY_ID = """
            select id, doc_type, location, modified, title
            from ftsdocument
            where id=?;
            """;

    private static final String FIND_BY_LOCATION = """
            select id, doc_type, location, modified, title
            from ftsdocument
            where location=?;
            """;

    private static final String SEARCH_QUERY = """
            select id, doc_type, location, modified, title
            from ftsdocument
            where make_tsvector(title, content) @@ plainto_tsquery(?);
            """;

    private static final String HEADERS_SEARCH_QUERY = """
            select d.id, d.doc_type, d.location, d.modified, d.title, h.header
            from ftsheader h
            left join ftsdocument d on h.doc_id = d.id
            where to_tsvector('russian', h.header) @@ plainto_tsquery(?);
            """;

    private static final String SEARCH_ALL_QUERY = """
            select d.id, d.doc_type, d.location, d.modified, d.title, h.header
            from ftsheader h
            left join ftsdocument d on h.doc_id = d.id
            where to_tsvector('russian', h.header) @@ plainto_tsquery(?)
            union
            select id, doc_type, location, modified, title, '' as header
            from ftsdocument
            where make_tsvector(title, content) @@ plainto_tsquery(?);
            """;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    public void index(DocumentIndexRequest request) {
        indexInTransaction(request);
    }

    private void indexInTransaction(DocumentIndexRequest request) {
        transactionTemplate.executeWithoutResult(ts -> {
            SearchResult searchResult = findByLocation(request.getLocation());

            if (searchResult == null) {
                insert(request);
            } else {
                Long docId = searchResult.getId();
                if (request.getModified().isAfter(searchResult.getModified())) {
                    update(docId, request);
                }
            }
        });
    }

    private Long insert(DocumentIndexRequest request) {
        Long docId = jdbcTemplate.queryForObject(INSERT_DOC_QUERY, Long.class,
                request.getDocType(),
                request.getLocation(),
                request.getModified(),
                request.getTitle(),
                request.getContent()
        );
        updateHeaders(docId, request.getHeaders());
        return docId;
    }

    private void update(Long docId, DocumentIndexRequest request) {
        jdbcTemplate.update(UPDATE_DOC_QUERY,
                request.getDocType(),
                request.getModified(),
                request.getTitle(),
                request.getContent(),
                docId
        );
    }

    private void updateHeaders(Long docId, List<String> headers) {
        jdbcTemplate.update(DELETE_HEADERS_BY_DOCID, docId);

        List<Object[]> batchArgs = headers.stream()
                .map(h -> new Object[]{docId, h})
                .toList();
        jdbcTemplate.batchUpdate(INSERT_HEADER, batchArgs);
    }

    public SearchResult findById(Long id) {
        List<SearchResult> resultList = jdbcTemplate.query(
                FIND_BY_ID,
                BeanPropertyRowMapper.newInstance(SearchResult.class),
                id
        );
        return resultList.isEmpty()
                ? null
                : resultList.iterator().next();
    }

    public SearchResult findByLocation(String location) {
        List<SearchResult> resultList = jdbcTemplate.query(
                FIND_BY_LOCATION,
                BeanPropertyRowMapper.newInstance(SearchResult.class),
                location
        );
        return resultList.isEmpty()
                ? null
                : resultList.iterator().next();
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
