/**
* OPAL Program
*
* MODULE      : index_log_timestamp.sql
*
* DESCRIPTION : Create an index on log_timestamp column in the log_audit_details table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 11/04/2024    A Dennis    1.0         PO-238 Create an index on log_timestamp column in the log_audit_details table.
*
**/
CREATE INDEX IF NOT EXISTS lad_log_timestamp_idx ON log_audit_details(log_timestamp);