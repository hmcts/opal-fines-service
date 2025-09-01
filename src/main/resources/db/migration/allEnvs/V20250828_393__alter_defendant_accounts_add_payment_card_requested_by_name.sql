/**
* CGI OPAL Program
*
* MODULE      : alter_defendant_accounts_add_payment_card_requested_by_name.sql
*
* DESCRIPTION : Add a payment_card_requested_by_name column to the defendant_accounts table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------------------------
* 26/08/2025    C Larkin    1.0         PO-1855 - Add a payment_card_requested_by_name column to the defendant_accounts table
*
**/
ALTER TABLE defendant_accounts
    ADD COLUMN payment_card_requested_by_name VARCHAR(100);

COMMENT ON COLUMN defendant_accounts.payment_card_requested_by_name IS 'Name value of the user that requested card from the AAD Access Token';