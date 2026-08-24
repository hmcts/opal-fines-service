INSERT INTO defendant_accounts (
    defendant_account_id,
    version_number,
    business_unit_id,
    account_number,
    imposed_hearing_date,
    amount_paid,
    account_balance,
    amount_imposed,
    account_status,
    allow_writeoffs,
    allow_cheques,
    account_type,
    collection_order,
    payment_card_requested,
    originator_name,
    imposed_by_name,
    prosecutor_case_reference
)
SELECT
    233400 + i,
    20 + i,
    78,
    '2334' || lpad(i::text, 2, '0') || 'C',
    TIMESTAMP '2026-01-21 10:15:00',
    0.00,
    100.00,
    100.00,
    'L',
    'N',
    'N',
    'Fine',
    'N',
    'N',
    'PO-2333 Court',
    'Bulk Child Court',
    'BULK-' || lpad(i::text, 2, '0')
  FROM generate_series(0, 19) AS i;

INSERT INTO defendant_transactions (
    defendant_transaction_id,
    defendant_account_id,
    posted_date,
    posted_by,
    transaction_type,
    transaction_amount,
    status_date,
    associated_record_type,
    associated_record_id,
    status,
    posted_by_name
)
SELECT
    23340000 + i,
    233300,
    TIMESTAMP '2026-01-21 12:00:00',
    'po2333',
    'CONSOL',
    0.00,
    TIMESTAMP '2026-01-21 12:00:00',
    'defendant_accounts',
    (233400 + i)::text,
    'P',
    'PO-2333 User'
  FROM generate_series(0, 19) AS i;
