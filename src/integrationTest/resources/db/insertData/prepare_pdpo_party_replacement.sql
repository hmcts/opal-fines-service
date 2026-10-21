UPDATE defendant_accounts
SET account_status = 'L'
WHERE defendant_account_id = 20010;

UPDATE defendant_account_parties
SET debtor = false
WHERE defendant_account_party_id = 920011;

DELETE FROM debtor_detail WHERE party_id = 920010;
