/**
* OPAL Program
*
* MODULE      : create_report_instance_files.sql
*
* DESCRIPTION : Create REPORT_INSTANCE_FILES table and related components.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------
* 03/10/2026    BE          1.0         Create REPORT_INSTANCE_FILES table and related components.
*
**/

CREATE TABLE report_instance_files
(
    report_instance_id      BIGINT                     NOT NULL,
    file_type               r_supported_file_type_enum NOT NULL,
    location_uuid           UUID                       NOT NULL,
    created_timestamp       TIMESTAMP                  NOT NULL,
    last_accessed_timestamp TIMESTAMP                  NOT NULL,
    CONSTRAINT report_instance_files_pk PRIMARY KEY (report_instance_id, file_type)
);

COMMENT ON COLUMN report_instance_files.report_instance_id IS 'The unique identifier of the report instance record this file relates to';
COMMENT ON COLUMN report_instance_files.file_type IS 'The type of this report instance file';
COMMENT ON COLUMN report_instance_files.location_uuid IS 'The Azure unique identifier of the stored report instance file';
COMMENT ON COLUMN report_instance_files.created_timestamp IS 'The date and time the report instance file was created';
COMMENT ON COLUMN report_instance_files.last_accessed_timestamp IS 'The date and time the report instance file was last accessed';

ALTER TABLE report_instance_files
    ADD CONSTRAINT rif_report_instance_id_fk FOREIGN KEY (report_instance_id) REFERENCES report_instances (report_instance_id);

CREATE INDEX rif_report_instance_id_idx
    ON report_instance_files (report_instance_id);
