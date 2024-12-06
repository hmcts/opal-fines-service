/**
* OPAL Program
*
* MODULE      : add_account_type_check.sql
*
* DESCRIPTION : Alter the DEFENDANT_ACCOUNTS table to make account_type NOT NULL and add check constraint.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------
* 04/10/2024    A Dennis    1.0         PO-805 Alter the DEFENDANT_ACCOUNTS table to make account_type NOT NULL and add check constraint.
*
**/

ALTER TABLE defendant_accounts
ALTER COLUMN account_type SET NOT NULL,
ADD CONSTRAINT da_account_type_cc CHECK(account_type IN ('Fixed Penalty', 'Fines', 'Conditional Caution', 'Confiscation'));
