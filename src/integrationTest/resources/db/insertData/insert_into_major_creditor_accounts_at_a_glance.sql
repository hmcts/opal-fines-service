INSERT INTO business_units (
    business_unit_id, business_unit_name, business_unit_code, business_unit_type,
    account_number_prefix, parent_business_unit_id, opal_domain, welsh_language
)
VALUES
    (978, 'Major Creditor BU', 'MCBU', 'Area', 'MC', NULL, 'Fines', false)
ON CONFLICT (business_unit_id) DO NOTHING;

INSERT INTO major_creditors (
    major_creditor_id, business_unit_id, major_creditor_code,
    name, address_line_1, address_line_2, address_line_3, postcode
)
VALUES (
    978001, 978, 'MC01',
    'Major Creditor Services Ltd', '1 Credit Lane', 'Creditville', 'Credittown', 'MC1 1AA'
);

INSERT INTO creditor_accounts (
    creditor_account_id, business_unit_id, account_number,
    creditor_account_type, prosecution_service, major_creditor_id,
    minor_creditor_party_id, from_suspense, hold_payout, pay_by_bacs,
    bank_sort_code, bank_account_number, bank_account_name,
    bank_account_reference, bank_account_type, version_number, last_changed_date
)
VALUES
    (
        978010, 978, 'MC123456',
        'MJ', false, 978001,
        NULL, false, false, true,
        '112233', '12345678', 'Major Creditor',
        'MCREF001', '1', 7, '2026-02-27 10:00:00'
    ),
    (
        978011, 978, 'CF123456',
        'CF', false, NULL,
        NULL, false, false, false,
        NULL, NULL, NULL,
        NULL, NULL, 3, '2026-02-27 10:05:00'
    );

INSERT INTO configuration_items (
    configuration_item_id, item_name, business_unit_id, item_value, item_values
)
VALUES (
    978020, 'CENTRAL_FUND_ACCOUNT', 978, NULL,
    '{"name":"HM Courts & Tribunals Service","address_line_1":"HMCS add 1","address_line_2":"HMCS add 2","address_line_3":"HMCS add 3","pay_by_bacs":"N"}'
);
