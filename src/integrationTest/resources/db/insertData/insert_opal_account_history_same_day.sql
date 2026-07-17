INSERT INTO amendments (
    amendment_id, business_unit_id, associated_record_type, associated_record_id, amended_date,
    amended_by, field_code, old_value, new_value, case_reference, function_code
) VALUES (
    26220020, 78, 'defendant_accounts', '262200', TIMESTAMP '2026-01-10 10:30:45.123',
    'same-day-user-1', 1, 'Old', 'New', 'CASE-SAME-DAY', 'UPD'
) ON CONFLICT (amendment_id) DO NOTHING;

INSERT INTO enforcements (
    enforcement_id, defendant_account_id, posted_date, posted_by, result_id, reason, jail_days,
    warrant_reference, case_reference, hearing_date, hearing_court_id, posted_by_name,
    enforcement_account_type
) VALUES (
    26220021, 262200, TIMESTAMP '2026-01-10 10:30:45.456', 'same-day-user-2', 'HST01',
    'Same day enforcement', 7, 'WR_SAME', 'CASE-SAME-DAY', TIMESTAMP '2026-02-10 10:00:00',
    262200, 'Same Day User Two', 'COLL'
) ON CONFLICT (enforcement_id) DO NOTHING;

INSERT INTO defendant_transactions (
    defendant_transaction_id, defendant_account_id, posted_date, posted_by, transaction_type,
    transaction_amount, payment_method, payment_reference, text, status, status_date, status_amount,
    posted_by_name, associated_record_type, associated_record_id
) VALUES (
    26220022, 262200, TIMESTAMP '2026-01-10 10:30:45.789', 'same-day-user-3', 'PAYMNT',
    -10.00, 'NC', 'PAY_SAME', 'Same day payment', 'C', TIMESTAMP '2026-01-10 10:30:45.790',
    -10.00, 'Same Day User Three', 'defendant_accounts', '262200'
) ON CONFLICT (defendant_transaction_id) DO NOTHING;
