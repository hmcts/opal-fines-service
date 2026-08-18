DELETE FROM defendant_transactions
WHERE defendant_transaction_id IN (23330001, 23330002)
   OR defendant_transaction_id BETWEEN 23340000 AND 23340019;

DELETE FROM defendant_account_parties
WHERE defendant_account_party_id IN (233301, 233303);

DELETE FROM parties
WHERE party_id IN (233301, 233303);

DELETE FROM defendant_accounts
WHERE defendant_account_id IN (233300, 233301, 233302, 233303, 233304)
   OR defendant_account_id BETWEEN 233400 AND 233419;
