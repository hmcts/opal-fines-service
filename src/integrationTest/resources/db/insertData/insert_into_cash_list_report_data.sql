INSERT INTO reports (
    report_id,
    report_title,
    report_group,
    audited_report,
    report_parameters,
    supports_multi_bu,
    is_bespoke_journey,
    shown_as_worklist,
    retention_period,
    permission,
    supported_file_types,
    can_manually_create
) VALUES (
    'cash_list',
    'Cash List',
    'Fines',
    true,
    NULL,
    false,
    false,
    false,
    NULL,
    NULL,
    NULL,
    false
);

INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
) VALUES (
    1777,
    'Cash List Business Unit',
    'CLST',
    CAST('Area' AS t_business_unit_type_enum),
    false
);

INSERT INTO tills (
    till_id,
    business_unit_id,
    till_number,
    owned_by
) VALUES (
    99000000343100,
    1777,
    9010,
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
    99000000343200,
    1777,
    'ACC123',
    250.00,
    125.50,
    124.50,
    CAST('L' AS t_da_account_status_enum),
    CAST('Fine' AS t_da_account_type_enum),
    1
);

INSERT INTO parties (
    party_id,
    organisation,
    surname,
    forenames,
    title
) VALUES (
    99000000343300,
    false,
    'Doe',
    'Jane',
    'Ms'
);

INSERT INTO defendant_account_parties (
    defendant_account_party_id,
    defendant_account_id,
    party_id,
    association_type,
    debtor
) VALUES (
    99000000343400,
    99000000343200,
    99000000343300,
    CAST('Defendant' AS t_association_type_enum),
    true
);

INSERT INTO suspense_accounts (
    suspense_account_id,
    business_unit_id,
    account_number
) VALUES (
    99000000343500,
    1777,
    'SUSP123'
);

INSERT INTO suspense_items (
    suspense_item_id,
    suspense_account_id,
    suspense_item_number,
    suspense_item_type,
    created_date,
    payment_method,
    court_fee_id
) VALUES (
    99000000343600,
    99000000343500,
    1,
    'UN',
    '2026-05-26 15:00:00',
    'CT',
    NULL
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
    99000000343700,
    99000000343100,
    125.50,
    '2026-05-26 14:30:00',
    'NC',
    'F',
    'FULL',
    'defendant_accounts',
    '99000000343200',
    'A Payer',
    'Account payment',
    true,
    true,
    false
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
    99000000343800,
    99000000343100,
    40.00,
    '2026-05-26 15:30:00',
    'CT',
    'S',
    'FULL',
    'suspense_items',
    '99000000343600',
    NULL,
    'Suspense payment',
    true,
    true,
    true
);

INSERT INTO report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    requested_by,
    report_parameters,
    requested_at,
    generation_status,
    requested_by_name
) VALUES (
    99000000343000,
    'cash_list',
    ARRAY[1777]::smallint[],
    1,
    12345678,
    '{"till_id":99000000343100}'::json,
    '2026-05-27 09:00:00',
    CAST('REQUESTED' AS ri_generation_status_enum),
    'opal-test'
);
