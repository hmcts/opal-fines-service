INSERT INTO notes (
    note_id, note_type, associated_record_type, associated_record_id, note_text, posted_date,
    posted_by, posted_by_name
) VALUES (
    26220010, 'AA', 'defendant_accounts', '262200', 'Second account note',
    TIMESTAMP '2026-01-06 09:00:00', 'hist-user-10', 'History User Ten'
);

INSERT INTO payment_terms (
    payment_terms_id, defendant_account_id, posted_date, posted_by, terms_type_code, effective_date,
    instalment_period, instalment_amount, instalment_lump_sum, jail_days, extension, account_balance,
    posted_by_name, active
) VALUES (
    26220011, 262200, TIMESTAMP '2026-01-07 09:00:00', 'hist-user-11', 'B',
    TIMESTAMP '2026-02-07 00:00:00', 'M', 35.00, 125.00, 9, FALSE, 450.00,
    'History User Eleven', FALSE
);
