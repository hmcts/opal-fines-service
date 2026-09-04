DELETE FROM defendant_transactions
WHERE defendant_transaction_id = 99000201;

DELETE FROM defendant_accounts
WHERE defendant_account_id IN (990001, 990002);
