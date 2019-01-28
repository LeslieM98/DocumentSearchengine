CREATE UNIQUE INDEX idx_docs_did ON docs (did);
CREATE UNIQUE INDEX idx_tfs_did ON tfs (did);
CREATE INDEX idx_dfs_term ON dfs (term);