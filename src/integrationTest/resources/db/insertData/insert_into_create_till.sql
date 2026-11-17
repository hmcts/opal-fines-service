INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number, amount_imposed,
    amount_paid, account_balance, account_status, account_type, version_number)
VALUES (11365001, 10, 'PO11365-1', 0, 0, 0, 'L', 'Fine', 0),
       (11365002, 77, 'PO11365-2', 0, 0, 0, 'L', 'Fine', 0)
ON CONFLICT (defendant_account_id) DO NOTHING;

INSERT INTO suspense_accounts (suspense_account_id, business_unit_id, account_number)
VALUES (11365001, 10, 'PO11365-S') ON CONFLICT (suspense_account_id) DO NOTHING;

INSERT INTO court_fees (court_fee_id, business_unit_id, court_fee_code, description, amount, stats_code)
VALUES (11365001, 10, 'FEE', 'PO11365 fee', 5.00, 'TEST') ON CONFLICT (court_fee_id) DO NOTHING;
