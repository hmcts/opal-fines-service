/**
* OPAL Program
*
* MODULE      : delete_from_po_2642_minor_creditor_history.sql
*
* DESCRIPTION : Deletes PO-2642 minor creditor history integration test data.
*
**/

DELETE FROM public.creditor_transactions
WHERE creditor_transaction_id IN (99264200002001, 99264200002002, 99264200002003);

DELETE FROM public.notes
WHERE note_id IN (99264200003001, 99264200003002);

DELETE FROM public.amendments
WHERE amendment_id IN (99264200004001, 99264200004002, 99264200004003);

DELETE FROM public.creditor_accounts
WHERE creditor_account_id IN (99264200000001, 99264200000002);

DELETE FROM public.defendant_accounts
WHERE defendant_account_id = 99264200001001;

DELETE FROM public.parties
WHERE party_id IN (99264200000101, 99264200000102);

DELETE FROM public.business_units
WHERE business_unit_id = 32642;
