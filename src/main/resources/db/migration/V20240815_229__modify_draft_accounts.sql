/**
*
* OPAL Program
*
* MODULE      : modify_draft_accounts.sql
*
* DESCRIPTION : Modified DRAFT_ACCOUNTS table with some new columns and renamed others.
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ------------------------------------------------------------------------------
* 16/08/2024    A Dennis     1.0         PO-623 Modified DRAFT_ACCOUNTS table with some new columns and renamed others.
*
**/     

ALTER TABLE draft_accounts
RENAME COLUMN created_by TO submitted_by;

ALTER TABLE draft_accounts
RENAME COLUMN account_summary_data TO account_snapshot;

ALTER TABLE draft_accounts
ADD COLUMN account_number        varchar(25);

COMMENT ON COLUMN draft_accounts.submitted_by IS 'ID of the user that last submitted this record for checking';
COMMENT ON COLUMN draft_accounts.account_number IS 'The Opal Account Number (2char letter code+account number)';
COMMENT ON COLUMN draft_accounts.account_id IS 'Account ID created on validation';
COMMENT ON COLUMN draft_accounts.created_date IS 'Date this record was first created (the created date is not updated by successive submits, only the submitted by)';
