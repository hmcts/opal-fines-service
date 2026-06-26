/**
* OPAL Program
*
* MODULE      : alter_defendant_accounts_originator_name.sql
*
* DESCRIPTION : Alter DEFENDANT_ACCOUNTS table ORIGINATOR_NAME column size to VARCHAR(200).
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 25/06/2026    P Brumby    1.0         PO-7418 Change the DEFENDANT_ACCOUNTS table ORIGINATOR_NAME column size from VARCHAR(50) to VARCHAR(200) to allow for LJA and Prosecutor names.
*
**/

ALTER TABLE defendant_accounts
    ALTER COLUMN originator_name TYPE VARCHAR(200);

COMMENT ON COLUMN defendant_accounts.originator_name IS 'The name of the court, local justice area, prosecutor or system where the account came from';
