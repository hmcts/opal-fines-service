/**
* OPAL Program
*
* MODULE      : delete_from_major_creditor_history.sql
*
* DESCRIPTION : Deletes major creditor history integration test data.
*
**/

DELETE FROM public.creditor_transactions
WHERE creditor_transaction_id IN (99264300002001, 99264300002002, 99264300002003);

DELETE FROM public.creditor_accounts
WHERE creditor_account_id = 99264300000001;

DELETE FROM public.major_creditors
WHERE major_creditor_id = 99264300000101;

DELETE FROM public.business_units
WHERE business_unit_id = 32643;
