CREATE EXTENSION IF NOT EXISTS "pgcrypto";

create table ftsdocument(
    doc_uid uuid default gen_random_uuid(),
    doc_type varchar(20) not null default '',
    location varchar(1024) not null UNIQUE,
    modified timestamp not null default CURRENT_TIMESTAMP,
    title text not null default '',
    content tsvector not null,
    primary key (doc_uid)
);

create table ftsheader(
    doc_uid uuid not null,
    header text not null,
    primary key (doc_uid),
    foreign key (doc_uid) references ftsdocument (doc_uid)
);

CREATE OR REPLACE FUNCTION make_tsvector(title TEXT, content tsvector)
   RETURNS tsvector AS $$
BEGIN
  RETURN (setweight(to_tsvector('russian', title), 'A') || setweight(content, 'B'));
END
$$ LANGUAGE 'plpgsql' IMMUTABLE;

create index idx_ftsdocument ON ftsdocument using gin(make_tsvector(title, content));

create index idx_ftsheader ON ftsheader using gin(to_tsvector('russian', header));
