DELETE FROM payments_in
WHERE payment_in_id IN (99000000836240, 99000000836250);

DELETE FROM defendant_account_parties
WHERE defendant_account_party_id = 99000000836230;

DELETE FROM parties
WHERE party_id = 99000000836220;

DELETE FROM defendant_accounts
WHERE defendant_account_id = 99000000836210;

DELETE FROM tills
WHERE till_id = 99000000836200;

DELETE FROM business_units
WHERE business_unit_id = 8362;
