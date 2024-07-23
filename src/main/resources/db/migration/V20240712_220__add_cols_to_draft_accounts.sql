/**
* OPAL Program
*
* MODULE      : add_cols_to_draft_acounts.sql
*
* DESCRIPTION : Add new columns to DRAFT_ACCOUNTS to capture the validationn of draft accounts during manual account creation. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------
* 12/07/2024    A Dennis    1.0         PO-512 Add new columns to DRAFT_ACCOUNTS to capture the validationn of draft accounts during manual account creation 
*
**/
ALTER TABLE draft_accounts
ADD COLUMN account_summary_data        json,
ADD COLUMN account_status              varchar(30),
ADD COLUMN timeline_data               json;

COMMENT ON COLUMN draft_accounts.account_summary_data IS 'Business data to identify the account';
COMMENT ON COLUMN draft_accounts.account_status IS 'One of Unvalidated, Validated etc';
COMMENT ON COLUMN draft_accounts.timeline_data IS 'A timeline of when the account has undergone validation';
COMMENT ON COLUMN draft_accounts.account_id IS 'Account ID created on validation. Either the Opal Account ID or GoB Account Number';
