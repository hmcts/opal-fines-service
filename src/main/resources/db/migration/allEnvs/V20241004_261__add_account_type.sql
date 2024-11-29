/**
* OPAL Program
*
* MODULE      : add_account_type.sql
*
* DESCRIPTION : Add the account_type column to the DEFENDANT_ACCOUNTS table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------
* 04/10/2024    A Dennis    1.0         PO-805 Add the account_type column to the DEFENDANT_ACCOUNTS table.
*
**/

ALTER TABLE defendant_accounts
ADD COLUMN account_type VARCHAR(20);

COMMENT ON COLUMN defendant_accounts.account_type IS 'One of Fixed Penalty, Fines, Conditional Caution, Confiscation';
