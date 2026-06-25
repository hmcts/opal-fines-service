/**
* OPAL Program
*
* MODULE      : alter_defendant_accounts_originator_name_length.sql
*
* DESCRIPTION : Increase DEFENDANT_ACCOUNTS.ORIGINATOR_NAME length to match prosecutor reference data.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 25/06/2026    ???????     1.0         PO-7287 Increase DEFENDANT_ACCOUNTS.ORIGINATOR_NAME from 50 to 200 characters.
*
**/

ALTER TABLE defendant_accounts
    ALTER COLUMN originator_name TYPE character varying(200);

COMMENT ON COLUMN defendant_accounts.originator_name IS 'The name of the court, local justice area, prosecutor or system where the account came from';
