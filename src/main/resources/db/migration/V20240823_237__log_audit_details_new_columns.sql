/**
*
* OPAL Program
*
* MODULE : log_audit_details_new_columns.sql
* 
* DESCRIPTION : Add new columns to the LOG_AUDIT_DETAILS table.
* 
* VERSION HISTORY:
* 
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------------
- 23/08/2024    I Readman   1.0         PO-646 Add new columns to the LOG_AUDIT_DETAILS table
*
**/
-- Drop columns to maintain column order from data model spreadsheet
ALTER TABLE log_audit_details DROP COLUMN json_request;

ALTER TABLE log_audit_details ADD COLUMN associated_record_type varchar(30);
ALTER TABLE log_audit_details ADD COLUMN associated_record_id varchar(30);
ALTER TABLE log_audit_details ADD COLUMN json_request text;

COMMENT ON COLUMN log_audit_details.associated_record_type IS 'Type of record identified by associated_record_id. Could be transaction, account, suspense line etc';
COMMENT ON COLUMN log_audit_details.associated_record_id IS 'ID of the associated record. So ID of the record or transaction etc being logged';
COMMENT ON COLUMN log_audit_details.json_request IS 'The REST request information received that initiated this action and written in a json format but stored as TEXT';