UPDATE reports
SET retention_period = 'P14D',
    permission = 'SEARCH_AND_VIEW_ACCOUNTS',
    supported_file_types = '{CSV,PDF,JSON}'
WHERE report_id = 'cash_till';

INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
) VALUES (
    1778,
    'Cash Till Business Unit',
    'CTIL',
    CAST('Area' AS t_business_unit_type_enum),
    false
);

INSERT INTO tills (
    till_id,
    business_unit_id,
    till_number,
    owned_by
) VALUES (
    99000000353100,
    1778,
    9011,
    'L080JG'
);

INSERT INTO defendant_accounts (
    defendant_account_id,
    business_unit_id,
    account_number,
    amount_imposed,
    amount_paid,
    account_balance,
    account_status,
    account_type,
    version_number
) VALUES (
    99000000353200,
    1778,
    'ACC456',
    250.00,
    125.50,
    124.50,
    CAST('L' AS t_da_account_status_enum),
    CAST('Fine' AS t_da_account_type_enum),
    1
);

INSERT INTO payments_in (
    payment_in_id,
    till_id,
    payment_amount,
    payment_date,
    payment_method,
    destination_type,
    allocation_type,
    associated_record_type,
    associated_record_id,
    third_party_payer_name,
    additional_information,
    receipt,
    allocated,
    auto_payment
) VALUES (
    99000000353300,
    99000000353100,
    125.50,
    '2026-05-26 14:30:00',
    'NC',
    'F',
    'FULL',
    'defendant_accounts',
    '99000000353200',
    'A Payer',
    'Account payment',
    true,
    false,
    false
);

INSERT INTO report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    requested_by,
    report_parameters,
    location,
    requested_at,
    generation_status,
    requested_by_name
) VALUES (
    99000000353000,
    'cash_till',
    ARRAY[1778]::smallint[],
    1,
    12345678,
    '{"till_id":99000000353100,"allocated_report":false}'::json,
    'stored-cash-till-report-location',
    '2026-05-27 09:00:00',
    CAST('READY' AS ri_generation_status_enum),
    'opal-test'
);
