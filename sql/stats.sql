--- Table with document lengths
CREATE TABLE dls AS SELECT did, SUM(tf) AS len FROM tfs GROUP BY did;


--- Table with document frequencies
CREATE TABLE dfs AS SELECT term, COUNT(did) AS df FROM tfs GROUP BY term;

--- Table with document-collection size
CREATE TABLE d AS SELECT COUNT(*) as size FROM docs; 