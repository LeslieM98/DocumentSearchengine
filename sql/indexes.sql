--- Index on tfs table
CREATE INDEX tfs_term_did_tf ON tfs(term, did, tf);

--- Index on dfs table
CREATE INDEX dfs_term_df ON dfs(term, df);

--- Index on dls table
CREATE INDEX dls_did_len ON dls(did, len);

--- Index on docs table
CREATE INDEX docs_did_title_url ON docs(did, title, url);