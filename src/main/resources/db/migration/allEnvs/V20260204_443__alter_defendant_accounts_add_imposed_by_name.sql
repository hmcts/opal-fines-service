/**
* CGI OPAL Program
*
* MODULE      : alter_defendant_accounts_add_imposed_by_name.sql
*
* DESCRIPTION : Add imposed_by_name column to the DEFENDANT_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/02/2026    T McCallion    1.0         PO-2749 - Add imposed_by_name column to the DEFENDANT_ACCOUNTS table
*
**/
ALTER TABLE defendant_accounts 
    ADD COLUMN imposed_by_name VARCHAR(100);

COMMENT ON COLUMN defendant_accounts.imposed_by_name IS 'Court or associated LJA that imposed the penalty against the Defendant account during account creation.';
