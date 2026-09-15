INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
) VALUES (
    8362,
    'Get Till Business Unit',
    'GTIL',
    CAST('Area' AS t_business_unit_type_enum),
    false
);

INSERT INTO tills (
    till_id,
    business_unit_id,
    till_number,
    owned_by,
    owned_by_name,
    auto_payment,
    created_date
) VALUES (
    99000000836200,
    8362,
    8362,
    'L080JG',
    'Alex Cashier',
    false,
    '2026-09-09 10:30:00'
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
    99000000836210,
    8362,
    'GTIL123',
    100.00,
    25.50,
    74.50,
    CAST('L' AS t_da_account_status_enum),
    CAST('Fine' AS t_da_account_type_enum),
    1
);

INSERT INTO parties (
    party_id,
    organisation,
    surname,
    forenames
) VALUES (
    99000000836220,
    false,
    'Doe',
    'Jane'
);

INSERT INTO defendant_account_parties (
    defendant_account_party_id,
    defendant_account_id,
    party_id,
    association_type,
    debtor
) VALUES (
    99000000836230,
    99000000836210,
    99000000836220,
    CAST('Defendant' AS t_association_type_enum),
    true
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
    99000000836240,
    99000000836200,
    25.50,
    '2026-09-09 11:00:00',
    CAST('NC' AS t_payment_method_enum),
    CAST('F' AS t_pi_destination_type_enum),
    'FULL',
    CAST('defendant_accounts' AS t_associated_record_type_enum),
    '99000000836210',
    NULL,
    '{"payment_received_from":"D"}',
    true,
    false,
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
    99000000836250,
    99000000836200,
    12.75,
    '2026-09-09 11:30:00',
    CAST('CT' AS t_payment_method_enum),
    CAST('S' AS t_pi_destination_type_enum),
    'FULL',
    CAST('suspense_items' AS t_associated_record_type_enum),
    '99000000836260',
    'Third Party Payer',
    '{"payment_received_from":"T","payer_details":{"individual":{"forenames":"Sam","surname":"Smith"}}}',
    false,
    false,
    false
);
