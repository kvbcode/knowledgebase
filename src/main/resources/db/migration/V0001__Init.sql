CREATE EXTENSION IF NOT EXISTS "pgcrypto";

create table ftsdocument(
    id bigserial,
    doc_type varchar(20) not null default '',
    location varchar(1024) not null UNIQUE,
    modified timestamp not null default CURRENT_TIMESTAMP,
    title varchar(512) not null default '',
    content tsvector not null,
    primary key (id)
);

create table ftsheader(
    id bigserial,
    doc_id bigserial,
    header varchar(512) not null,
    primary key (id),
    foreign key (doc_id) references ftsdocument (id)
);

CREATE OR REPLACE FUNCTION make_tsvector(title TEXT, content tsvector)
   RETURNS tsvector AS $$
BEGIN
  RETURN (setweight(to_tsvector('russian', title), 'A') || setweight(content, 'B'));
END
$$ LANGUAGE 'plpgsql' IMMUTABLE;

create index idx_ftsdocument ON ftsdocument using gin(make_tsvector(title, content));

create index idx_ftsheader ON ftsheader using gin(to_tsvector('russian', header));
