/**
* CGI OPAL Program
*
* MODULE      : V20250627_324__add_columns_to_defendant_accounts.sql
*
* DESCRIPTION : Add columns to DEFENDANT_ACCOUNTS
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------
* 26/06/2025    TMc         1.0         PO-1438 Add new columns to the DEFENDANT_ACCOUNTS table
*
**/

-- Add new columns to DEFENDANT_ACCOUNTS table
ALTER TABLE defendant_accounts 
ADD COLUMN account_comments TEXT,
ADD COLUMN account_note_1   TEXT,
ADD COLUMN account_note_2   TEXT,
ADD COLUMN account_note_3   TEXT,
ADD COLUMN jail_days INTEGER;

-- Add comments to new columns
COMMENT ON COLUMN defendant_accounts.account_comments IS 'Holds comments for this account.';
COMMENT ON COLUMN defendant_accounts.account_note_1 IS 'First free text note for this account.';
COMMENT ON COLUMN defendant_accounts.account_note_2 IS 'Second free text note for this account.';
COMMENT ON COLUMN defendant_accounts.account_note_3 IS 'Third free text note for this account.';
COMMENT ON COLUMN defendant_accounts.jail_days IS 'The number of days in jail the defendant will spend in default of payment.';
