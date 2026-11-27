/**
* OPAL Program
*
* MODULE      : create_document_instance_files.sql
*
* DESCRIPTION : Create DOCUMENT_INSTANCE_FILES table and related components.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------
* 24/11/2026    TT          1.0         PO-11362 Create DOCUMENT_INSTANCE_FILES table and related components.
*
**/

CREATE TABLE document_instance_files
(
    document_instance_id      BIGINT                     NOT NULL,
    file_type                 r_supported_file_type_enum NOT NULL,
    location_uuid             UUID                       NOT NULL,
    created_timestamp         TIMESTAMP                  NOT NULL,
    last_accessed_timestamp   TIMESTAMP                  NOT NULL,
    CONSTRAINT document_instance_files_pk PRIMARY KEY (document_instance_id, file_type)
);

COMMENT ON COLUMN document_instance_files.document_instance_id IS 'The unique identifier of the document instance record this file relates to';
COMMENT ON COLUMN document_instance_files.file_type IS 'The type of this document instance file';
COMMENT ON COLUMN document_instance_files.location_uuid IS 'The Azure unique identifier of the stored document instance file';
COMMENT ON COLUMN document_instance_files.created_timestamp IS 'The date and time the document instance file was created';
COMMENT ON COLUMN document_instance_files.last_accessed_timestamp IS 'The date and time the document instance file was last accessed';

ALTER TABLE document_instance_files
    ADD CONSTRAINT dif_document_instance_id_fk FOREIGN KEY (document_instance_id) REFERENCES document_instances (document_instance_id);

CREATE INDEX dif_document_instance_id_idx
    ON document_instance_files (document_instance_id);
