/**
* OPAL Program
*
* MODULE      : alter_suspense_transactions_add_additional_information.sql
*
* DESCRIPTION : Add nullable JSON metadata to suspense transactions
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 19/11/2026    C Cho          1.0         PO-11489 - Add additional_information column to suspense transactions
*
**/

ALTER TABLE suspense_transactions
    ADD COLUMN additional_information JSON;

COMMENT ON COLUMN suspense_transactions.additional_information IS
    'Column to hold additional Information that are used as durable Payments In and Tills metadata for later use in Suspense History.';
