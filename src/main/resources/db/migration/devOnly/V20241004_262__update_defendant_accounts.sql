/**
* OPAL Program
*
* MODULE      : update_defendant_accounts.sql
*
* DESCRIPTION : Update the account_type column in the DEFENDANT_ACCOUNTS table with test data.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------
* 04/10/2024    A Dennis    1.0         PO-805 Update the account_type column in the DEFENDANT_ACCOUNTS table with test data.
*
**/

UPDATE defendant_accounts
SET    account_type = 'Fixed Penalty'
WHERE defendant_account_id IN (500000000, 500000001, 500000002, 500000003);

UPDATE defendant_accounts
SET    account_type = 'Fines'
WHERE defendant_account_id IN (500000004, 500000005, 500000006, 500000007);

UPDATE defendant_accounts
SET    account_type = 'Conditional Caution'
WHERE defendant_account_id IN (500000008, 500000009, 500000010);
