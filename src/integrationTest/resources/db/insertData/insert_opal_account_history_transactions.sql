INSERT INTO creditor_accounts (
    creditor_account_id, business_unit_id, account_number, creditor_account_type, prosecution_service,
    from_suspense, hold_payout, pay_by_bacs, version_number
) VALUES (
    262200, 78, 'CR262200', 'MJ', TRUE, FALSE, FALSE, FALSE, 0
) ON CONFLICT (creditor_account_id) DO NOTHING;

INSERT INTO impositions (
    imposition_id, defendant_account_id, posted_date, posted_by, posted_by_name, result_id,
    imposing_court_id, imposed_date, imposed_amount, paid_amount, creditor_account_id, completed
) VALUES (
    26220012, 262200, TIMESTAMP '2026-01-06 08:00:00', 'hist-user-12',
    'History User Twelve', 'HST01', 262200, TIMESTAMP '2026-01-06 08:00:00',
    -125.00, 0.00, 262200, FALSE
) ON CONFLICT (imposition_id) DO NOTHING;

INSERT INTO defendant_transactions (
    defendant_transaction_id, defendant_account_id, posted_date, posted_by, transaction_type,
    transaction_amount, payment_method, payment_reference, text, status, status_date, status_amount,
    posted_by_name, associated_record_type, associated_record_id, imposed_amount
) VALUES (
    26220012, 262200, TIMESTAMP '2026-01-06 09:00:00', 'hist-user-12', 'PAYMNT',
    -25.00, 'NC', 'PAY262201', 'Second history payment', 'P',
    TIMESTAMP '2026-01-06 10:00:00', -25.00, 'History User Twelve',
    'impositions', '26220012', -125.00
);
