package com.cyber.knowledgebase.fts.postgres;

import com.cyber.knowledgebase.fts.dto.SearchResult;
import com.cyber.knowledgebase.fts.service.TextSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class PostgresTextSearchService implements TextSearchService {
    private static final String FIND_ALL = """
            select id, doc_type, location, modified, title
            from ftsdocument;
            """;

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

    private static final String SEARCH_ALL_QUERY = """
            select d.id, d.doc_type, d.location, d.modified, d.title, h.header
            from ftsheader h
            left join ftsdocument d on h.doc_id = d.id
            where to_tsvector('russian', h.header) @@ plainto_tsquery(:query)
            union all
            select id, doc_type, location, modified, title, '' as header
            from ftsdocument
            where make_tsvector(title, content) @@ plainto_tsquery(:query);
            """;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Stream<SearchResult> findAll() {
        return jdbcTemplate.queryForStream(
                FIND_ALL,
                new MapSqlParameterSource(),
                BeanPropertyRowMapper.newInstance(SearchResult.class)
        );
    }

    @Override
    public Optional<SearchResult> findById(Long id) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
                FIND_BY_ID,
                new MapSqlParameterSource("id", id),
                BeanPropertyRowMapper.newInstance(SearchResult.class)
        ));
    }

    @Override
    public List<SearchResult> findByLocationList(Collection<String> locationList) {
        return jdbcTemplate.query(
                FIND_BY_LOCATIONS,
                new MapSqlParameterSource("locations", locationList),
                BeanPropertyRowMapper.newInstance(SearchResult.class)
        );
    }

    @Override
    public Stream<SearchResult> searchByQuery(String plainQuery) {
        return jdbcTemplate.queryForStream(
                SEARCH_ALL_QUERY,
                new MapSqlParameterSource("query", plainQuery),
                BeanPropertyRowMapper.newInstance(SearchResult.class)
        );
    }


}
