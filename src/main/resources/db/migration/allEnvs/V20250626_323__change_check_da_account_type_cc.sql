/**
* CGI OPAL Program
*
* MODULE      : V20250626_323__change_check_da_account_type_cc.sql
*
* DESCRIPTION : Fix typo in check constraint defendant_accounts.da_account_type_cc IN clause
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -----------------------------------------------------------------------------------------------------------
* 26/06/2025    TMc         1.0         PO-1436 Fix typo in check constraint defendant_accounts.da_account_type_cc IN clause (Fines should be Fine)
*
**/

ALTER TABLE defendant_accounts 
DROP CONSTRAINT IF EXISTS da_account_type_cc;

ALTER TABLE defendant_accounts 
ADD CONSTRAINT da_account_type_cc CHECK (account_type IN ('Fixed Penalty', 'Fine', 'Conditional Caution', 'Confiscation'));


COMMENT ON COLUMN defendant_accounts.account_type IS 'One of Fixed Penalty, Fine, Conditional Caution, Confiscation';