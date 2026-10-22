DELETE FROM defendant_account_parties
WHERE defendant_account_id = 24010;

DELETE FROM debtor_detail
WHERE party_id = 24010;

DELETE FROM parties
WHERE party_id IN (24010, 9900000999102);

DELETE FROM amendments
WHERE associated_record_id = '24010';

DELETE FROM notes
WHERE associated_record_id = '24010';

DELETE FROM defendant_accounts
WHERE defendant_account_id = 24010;
