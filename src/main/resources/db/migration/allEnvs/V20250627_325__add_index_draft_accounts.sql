/**
* CGI OPAL Program
*
* MODULE      : V20250627_325__add_index_draft_accounts.sql
*
* DESCRIPTION : Add index to DRAFT_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------
* 27/06/2025    TMc         1.0         PO-1439 Add index da_submitted_bu_status_idx to DRAFT_ACCOUNTS table
*
**/

CREATE INDEX IF NOT EXISTS da_submitted_bu_status_idx ON draft_accounts (submitted_by, business_unit_id, account_status);