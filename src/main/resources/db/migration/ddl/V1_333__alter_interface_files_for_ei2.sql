/**
* OPAL Program
*
* MODULE      : alter_interface_files_for_ei2.sql
*
* DESCRIPTION : Add EI2 JSON reference columns to INTERFACE_FILES and drop records column.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ---------------------------------------------
* 02/09/2026    TMc            1.0         PO-5784 - Alter INTERFACE_FILES table for EI2
*
**/

ALTER TABLE interface_files
    ADD COLUMN source_json_id BIGINT,
    ADD COLUMN transformed_json_id BIGINT,
    DROP COLUMN IF EXISTS records;

COMMENT ON COLUMN interface_files.source_json_id IS 'Primary key value of the related source JSON file in File Handler interface_files.';
COMMENT ON COLUMN interface_files.transformed_json_id IS 'Primary key value of the related transformed JSON file in File Handler interface_files.';
