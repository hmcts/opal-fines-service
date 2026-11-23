DELETE FROM public.creditor_transactions
WHERE creditor_account_id = 9999010347001
   OR posted_by = 'PO10347-BAD-FK';

DELETE FROM public.creditor_accounts
WHERE creditor_account_id = 9999010347001;

DELETE FROM public.business_units
WHERE business_unit_id = 10347;
