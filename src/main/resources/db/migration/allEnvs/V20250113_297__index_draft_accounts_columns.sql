/**
* OPAL Program
*
* MODULE      : index_draft_accounts_columns.sql
*
* DESCRIPTION : Create indexes on columns in DRAFT_ACCOUNTS table used by the backend for searches. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------
* 13/01/2025    A Dennis    1.0         PO-1054 Create indexes on columns in DRAFT_ACCOUNTS table used by the backend for searches.
**/
CREATE INDEX IF NOT EXISTS da_business_unit_id_idx ON draft_accounts(business_unit_id);
CREATE INDEX IF NOT EXISTS da_account_status_idx ON draft_accounts(account_status);
CREATE INDEX IF NOT EXISTS da_submitted_by_idx ON draft_accounts(submitted_by);
CREATE INDEX IF NOT EXISTS da_submitted_by_name_idx ON draft_accounts(submitted_by_name);
CREATE INDEX IF NOT EXISTS da_account_status_date_idx ON draft_accounts(account_status_date);
