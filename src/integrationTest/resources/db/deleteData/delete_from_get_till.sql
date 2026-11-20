DELETE FROM payments_in
WHERE payment_in_id IN (99000000836240, 99000000836250, 99000000836290, 99000000836291, 99000000836292);

DELETE FROM defendant_account_parties
WHERE defendant_account_party_id = 99000000836230;

DELETE FROM miscellaneous_accounts
WHERE miscellaneous_account_id IN (99000000836280, 99000000836281);

DELETE FROM parties
WHERE party_id IN (99000000836220, 99000000836270, 99000000836271);

DELETE FROM defendant_accounts
WHERE defendant_account_id = 99000000836210;

DELETE FROM tills
WHERE till_id = 99000000836200;

DELETE FROM business_units
WHERE business_unit_id = 8362;
