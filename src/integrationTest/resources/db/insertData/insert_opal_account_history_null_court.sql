INSERT INTO defendant_accounts (
    defendant_account_id, version_number, business_unit_id, account_number, amount_paid,
    account_balance, amount_imposed, account_status, allow_writeoffs, allow_cheques, account_type,
    collection_order, payment_card_requested, originator_name
) VALUES (
    262210, 0, 78, '262210A', 0.00, 125.00, 125.00, 'L', 'N', 'N', 'Fine', 'N', 'N',
    'History Sending Court'
) ON CONFLICT (defendant_account_id) DO NOTHING;

INSERT INTO enforcements (
    enforcement_id, defendant_account_id, posted_date, posted_by, result_id, reason, jail_days,
    warrant_reference, case_reference, hearing_date, hearing_court_id, posted_by_name,
    enforcement_account_type
) VALUES (
    26221001, 262210, TIMESTAMP '2026-01-08 09:00:00', 'hist-user-null-court', 'HST01',
    'Null hearing court enforcement', 11, 'WR262210', 'CASE-HIST-NULL',
    TIMESTAMP '2026-02-08 10:00:00', NULL, 'History User Null Court', 'COLL'
) ON CONFLICT (enforcement_id) DO NOTHING;
