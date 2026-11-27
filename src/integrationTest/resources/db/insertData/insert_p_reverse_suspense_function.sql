INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
) VALUES (
    3977,
    'PO-10350 Integration Test Business Unit',
    'P350',
    'Area',
    false
);

INSERT INTO reports (
    report_id,
    report_title,
    report_group,
    report_parameters,
    audited_report,
    supports_multi_bu,
    is_bespoke_journey,
    shown_as_worklist,
    retention_period,
    permission,
    supported_file_types,
    can_manually_create
) VALUES (
    'reversed_payments_report',
    'PO-10350 Test Report Fixture',
    'Cash reports',
    NULL,
    false,
    false,
    false,
    false,
    NULL,
    'REVERSE_PAYMENT_REPORT',
    ARRAY['PDF']::r_supported_file_type_enum[],
    true
)
ON CONFLICT (report_id) DO NOTHING;

INSERT INTO suspense_accounts (
    suspense_account_id,
    business_unit_id,
    account_number
) VALUES (
    99000001035000,
    3977,
    'PO10350-SUSPENSE'
);

INSERT INTO suspense_items (
    suspense_item_id,
    suspense_account_id,
    suspense_item_number,
    suspense_item_type,
    created_date,
    version_number
)
SELECT
    99000001035000 + item_number,
    99000001035000,
    item_number,
    'UN'::t_suspense_item_type_enum,
    TIMESTAMP '2026-11-27 09:00:00',
    1
FROM generate_series(1, 8) AS item_number;

INSERT INTO suspense_transactions (
    suspense_transaction_id,
    suspense_item_id,
    posted_date,
    posted_by,
    posted_by_name,
    transaction_type,
    amount
)
VALUES
    (nextval('public.suspense_transaction_id_seq'), 99000001035001, TIMESTAMP '2026-11-27 09:01:00', 'PO10350_SETUP', 'PO-10350 Test Setup', 'UN', 100.00),
    (nextval('public.suspense_transaction_id_seq'), 99000001035001, TIMESTAMP '2026-11-27 09:02:00', 'PO10350_SETUP', 'PO-10350 Test Setup', 'UN', -30.00),
    (nextval('public.suspense_transaction_id_seq'), 99000001035002, TIMESTAMP '2026-11-27 09:03:00', 'PO10350_SETUP', 'PO-10350 Test Setup', 'UN', 75.00),
    (nextval('public.suspense_transaction_id_seq'), 99000001035004, TIMESTAMP '2026-11-27 09:04:00', 'PO10350_SETUP', 'PO-10350 Test Setup', 'UN', 50.00),
    (nextval('public.suspense_transaction_id_seq'), 99000001035004, TIMESTAMP '2026-11-27 09:05:00', 'PO10350_SETUP', 'PO-10350 Test Setup', 'UN', -50.00),
    (nextval('public.suspense_transaction_id_seq'), 99000001035005, TIMESTAMP '2026-11-27 09:06:00', 'PO10350_SETUP', 'PO-10350 Test Setup', 'UN', -10.00),
    (nextval('public.suspense_transaction_id_seq'), 99000001035006, TIMESTAMP '2026-11-27 09:07:00', 'PO10350_SETUP', 'PO-10350 Test Setup', 'UN', 50.00),
    (nextval('public.suspense_transaction_id_seq'), 99000001035007, TIMESTAMP '2026-11-27 09:08:00', 'PO10350_SETUP', 'PO-10350 Test Setup', 'UN', 60.00),
    (nextval('public.suspense_transaction_id_seq'), 99000001035008, TIMESTAMP '2026-11-27 09:09:00', 'PO10350_SETUP', 'PO-10350 Test Setup', 'UN', 80.00);
