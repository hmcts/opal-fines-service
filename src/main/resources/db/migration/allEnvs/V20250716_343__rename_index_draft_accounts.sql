/**
* CGI OPAL Program
*
* MODULE      : rename_index_draft_accounts.sql
*
* DESCRIPTION : Drop and re-create all, non PK, indexes on the DRAFT_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1586 Drop and re-create all, non PK, indexes on the DRAFT_ACCOUNTS table so they have a 'dra' prefix
*
**/

DROP INDEX IF EXISTS da_account_status_date_idx;
CREATE INDEX dra_account_status_date_idx ON draft_accounts (account_status_date);

DROP INDEX IF EXISTS da_account_status_idx;
CREATE INDEX dra_account_status_idx ON draft_accounts (account_status);

DROP INDEX IF EXISTS da_business_unit_id_idx;
CREATE INDEX dra_business_unit_id_idx ON draft_accounts (business_unit_id);

DROP INDEX IF EXISTS da_submitted_bu_status_idx;
CREATE INDEX dra_submitted_bu_status_idx ON draft_accounts (submitted_by, business_unit_id, account_status);

DROP INDEX IF EXISTS da_submitted_by_idx;
CREATE INDEX dra_submitted_by_idx ON draft_accounts (submitted_by);

DROP INDEX IF EXISTS da_submitted_by_name_idx;
CREATE INDEX dra_submitted_by_name_idx ON draft_accounts (submitted_by_name);
