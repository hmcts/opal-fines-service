INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
) VALUES (
    3976,
    'PO-10346 Integration Test Business Unit',
    'P346',
    'Area',
    false
);

INSERT INTO suspense_accounts (
    suspense_account_id,
    business_unit_id,
    account_number
) VALUES (
    99000001034600,
    3976,
    'PO10346-SUSPENSE'
);

INSERT INTO suspense_items (
    suspense_item_id,
    suspense_account_id,
    suspense_item_number,
    suspense_item_type,
    created_date,
    payment_method
) VALUES (
    99000001034601,
    99000001034600,
    1,
    'UN',
    '2026-11-03 09:00:00',
    'CT'
);
