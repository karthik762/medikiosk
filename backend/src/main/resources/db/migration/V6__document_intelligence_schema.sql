-- ==============================================================================
-- MediKiosk Database Migration V6: Document Intelligence & OCR Schema Extension
-- Conforming to docs/DATABASE.md, docs/PRODUCT_SPEC.md, and docs/SECURITY.md
-- ==============================================================================

-- 1. Extend documents table with file_size
ALTER TABLE documents ADD COLUMN file_size BIGINT;

-- 2. Ensure indexes for efficient document queries
CREATE INDEX idx_documents_processing_status ON documents(processing_status);
CREATE INDEX idx_extractions_status ON document_extractions(status);
