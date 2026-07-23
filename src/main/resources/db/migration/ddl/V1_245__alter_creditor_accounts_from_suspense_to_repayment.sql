/**
* OPAL Program
*
* MODULE      : alter_creditor_accounts_from_suspense_to_repayment.sql
*
* DESCRIPTION : Rename CREDITOR_ACCOUNTS.FROM_SUSPENSE to REPAYMENT and update the column comment.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 09/07/2026    C Cho       1.0         PO-2973 Rename CREDITOR_ACCOUNTS.FROM_SUSPENSE to REPAYMENT.
*
**/

ALTER TABLE creditor_accounts
    RENAME COLUMN from_suspense TO repayment;

COMMENT ON COLUMN creditor_accounts.repayment IS 'If the creditor is a repayment creditor. If so, there will be no relating impositions for this creditor account.';
