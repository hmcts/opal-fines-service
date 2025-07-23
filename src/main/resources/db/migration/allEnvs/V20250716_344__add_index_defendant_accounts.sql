/**
* CGI OPAL Program
*
* MODULE      : add_index_defendant_accounts.sql
*
* DESCRIPTION : Add indexes to the DEFENDANT_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1559 Add indexes to the DEFENDANT_ACCOUNTS table
*
**/
CREATE INDEX da_account_number_idx      ON defendant_accounts(account_number);
CREATE INDEX da_prosecutor_case_ref_idx ON defendant_accounts(prosecutor_case_reference);
CREATE INDEX da_business_unit_id_idx    ON defendant_accounts(business_unit_id);
CREATE INDEX da_account_balance_idx     ON defendant_accounts(account_balance);