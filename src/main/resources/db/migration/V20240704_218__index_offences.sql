/**
* OPAL Program
*
* MODULE      : index_offences.sql
*
* DESCRIPTION : Create indexes on columns in OFFENCES table that will be used in searches. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 04/07/2024    A Dennis    1.0         PO-387 Create an index on log_timestamp column in the log_audit_details table.
*
**/
CREATE INDEX IF NOT EXISTS off_cjs_code_idx ON offences(cjs_code);
CREATE INDEX IF NOT EXISTS off_offence_title_idx ON offences(offence_title);
CREATE INDEX IF NOT EXISTS off_offence_oas_idx ON offences(offence_oas);