package com.cyber.knowledgebase.fts.postgres;

import com.cyber.knowledgebase.fts.dto.DocumentIndexRequest;
import com.cyber.knowledgebase.fts.dto.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;

@Service
public class PostgresTextSearchService {
    private static final String INSERT_DOC_QUERY = """
            insert into ftsdocument
              (doc_type, location, modified, title, content)
            values
              (:doc_type, :location, :modified, :title, to_tsvector('russian', :content))
            returning id;
            """;

    private static final String UPDATE_DOC_QUERY = """
            update ftsdocument
              set doc_type=?, modified=?, title=?, content=to_tsvector('russian', ?)
            where
              id = :id;
            """;

    private static final String DELETE_HEADERS_BY_DOCID = "delete from ftsheader where doc_id=:id;";

    private static final String INSERT_HEADER = "insert into ftsheader (doc_id, header) values (:doc_id,:header);";

    private static final String FIND_BY_ID = """
            select id, doc_type, location, modified, title
            from ftsdocument
            where id=:id;
            """;

    private static final String FIND_BY_LOCATIONS = """
            select id, doc_type, location, modified, title
            from ftsdocument
            where location in (:locations);
            """;

    private static final String SEARCH_QUERY = """
            select id, doc_type, location, modified, title
            from ftsdocument
            where make_tsvector(title, content) @@ plainto_tsquery(:query);
            """;

    private static final String HEADERS_SEARCH_QUERY = """
            select d.id, d.doc_type, d.location, d.modified, d.title, h.header
            from ftsheader h
            left join ftsdocument d on h.doc_id = d.id
            where to_tsvector('russian', h.header) @@ plainto_tsquery(:query);
            """;

    private static final String SEARCH_ALL_QUERY = """
            select d.id, d.doc_type, d.location, d.modified, d.title, h.header
            from ftsheader h
            left join ftsdocument d on h.doc_id = d.id
            where to_tsvector('russian', h.header) @@ plainto_tsquery(:query)
            union
            select id, doc_type, location, modified, title, '' as header
            from ftsdocument
            where make_tsvector(title, content) @@ plainto_tsquery(:query);
            """;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

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
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("doc_type", request.getDocType())
                .addValue("location", request.getLocation())
                .addValue("modified", request.getModified())
                .addValue("title", request.getTitle())
                .addValue("content", request.getContent());

        Long docId = jdbcTemplate.queryForObject(INSERT_DOC_QUERY, params, Long.class);
        updateHeaders(docId, request.getHeaders());
        return docId;
    }

    private void update(Long docId, DocumentIndexRequest request) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("doc_type", request.getDocType())
                .addValue("modified", request.getModified())
                .addValue("title", request.getTitle())
                .addValue("content", request.getContent())
                .addValue("id", docId);

        jdbcTemplate.update(UPDATE_DOC_QUERY, params);
    }

    private void updateHeaders(Long docId, List<String> headers) {
        jdbcTemplate.update(DELETE_HEADERS_BY_DOCID, new MapSqlParameterSource("id", docId));

        MapSqlParameterSource[] batchInsertArgs = headers.stream()
                .map(header -> new MapSqlParameterSource()
                        .addValue("doc_id", docId)
                        .addValue("header", header)
                )
                .toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(INSERT_HEADER, batchInsertArgs);
    }

    public SearchResult findById(Long id) {
        List<SearchResult> resultList = jdbcTemplate.query(
                FIND_BY_ID,
                new MapSqlParameterSource("id", id),
                BeanPropertyRowMapper.newInstance(SearchResult.class)
        );
        return resultList.isEmpty()
                ? null
                : resultList.iterator().next();
    }

    public SearchResult findByLocation(String location) {
        List<SearchResult> resultList = findByLocationList(List.of(location));
        return resultList.isEmpty()
                ? null
                : resultList.iterator().next();
    }

    public List<SearchResult> findByLocationList(Collection<String> locationList) {
        return jdbcTemplate.query(
                FIND_BY_LOCATIONS,
                new MapSqlParameterSource("locations", locationList),
                BeanPropertyRowMapper.newInstance(SearchResult.class)
        );
    }

    public List<SearchResult> search(String plainQuery) {
        return jdbcTemplate.query(
                SEARCH_QUERY,
                new MapSqlParameterSource("query", plainQuery),
                BeanPropertyRowMapper.newInstance(SearchResult.class)
        );
    }

    public List<SearchResult> searchHeaders(String plainQuery) {
        return jdbcTemplate.query(
                HEADERS_SEARCH_QUERY,
                new MapSqlParameterSource("query", plainQuery),
                BeanPropertyRowMapper.newInstance(SearchResult.class)
        );
    }

    public List<SearchResult> searchAll(String plainQuery) {
        return jdbcTemplate.query(
                SEARCH_ALL_QUERY,
                new MapSqlParameterSource("query", plainQuery),
                BeanPropertyRowMapper.newInstance(SearchResult.class)
        );
    }


}
