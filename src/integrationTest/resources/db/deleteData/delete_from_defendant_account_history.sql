SET search_path TO public;

DELETE FROM notes
WHERE note_id = 26220005
   OR associated_record_id = '262200';

DELETE FROM defendant_transactions
WHERE defendant_account_id = 262200;

DELETE FROM payment_terms
WHERE defendant_account_id = 262200;

DELETE FROM enforcements
WHERE defendant_account_id = 262200;

DELETE FROM amendments
WHERE amendment_id = 26220001
   OR associated_record_id = '262200';

DELETE FROM defendant_accounts
WHERE defendant_account_id = 262200;

DELETE FROM courts
WHERE court_id = 262200;

DELETE FROM results
WHERE result_id = 'HST01';
