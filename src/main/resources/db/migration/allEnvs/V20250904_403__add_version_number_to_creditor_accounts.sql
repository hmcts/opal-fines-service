/**
* CGI OPAL Program
*
* MODULE      : add_version_number_to_creditor_accounts.sql
*
* DESCRIPTION : Add VERSION_NUMBER column to the CREDITOR_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------
* 28/08/2025    TMc         1.0         PO-2098 - Add VERSION_NUMBER column to the CREDITOR_ACCOUNTS table.
*
**/
ALTER TABLE creditor_accounts 
    ADD COLUMN version_number   bigint;

-- Add comments to new column
COMMENT ON COLUMN creditor_accounts.version_number IS 'Used to check that related items have not changed since retrieval and prior to being amended';