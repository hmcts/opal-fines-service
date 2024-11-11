/**
* OPAL Program
*
* MODULE      : add_columns_to draft_accounts.sql
*
* DESCRIPTION : Add new columns submitted_by_name, account_status_date, status_message to the DRAFT_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------
* 11/11/2024    A Dennis    1.0         PO-937 Add new columns submitted_by_name, account_status_date, status_message to the DRAFT_ACCOUNTS table
*
**/

ALTER TABLE draft_accounts 
ADD COLUMN submitted_by_name varchar(100) NOT NULL,
ADD COLUMN account_status_date timestamp NOT NULL,
ADD COLUMN status_message text;

COMMENT ON COLUMN draft_accounts.submitted_by_name IS 'Name value of the submitting user from the AAD Access Token';
COMMENT ON COLUMN draft_accounts.account_status_date IS 'The date of update of account status';
COMMENT ON COLUMN draft_accounts.status_message IS 'Any system messages, warnings, related to the status';
COMMENT ON COLUMN draft_accounts.account_status IS 'One of Submitted, Resubmitted, Rejected, Deleted, Approved, Pending, Error in Publishing';
