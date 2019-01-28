--- Table for document meta-data
CREATE TABLE docs (did integer not null, title text, url text, date integer);

--- Table for term frequencies
CREATE TABLE tfs ( did integer not null, term text not null, tf integer not null);