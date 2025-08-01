/**
* OPAL Program
*
* MODULE      : add_version_number_column_to_defendant_accounts.sql
*
* DESCRIPTION : Add the column DEFENDANT_ACCOUNTS.VERSION_NUMBER to check that related items have not changed since retrieval.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------------------------
* 22/07/2025    C Cho       1.0         PO-1724 Add VERSION_NUMBER column to DEFENDANT_ACCOUNTS table to check that related items have not changed since retrieval.
*
**/
ALTER TABLE defendant_accounts
ADD COLUMN version_number bigint NULL;

COMMENT ON COLUMN defendant_accounts.version_number IS 'Used to check that related items have not changed since retrieval and prior to being amended';