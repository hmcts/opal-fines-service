INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
) VALUES (
    8365,
    'Manual Suspense Item Deletion Test Business Unit',
    'MSID',
    CAST('Area' AS t_business_unit_type_enum),
    false
);

INSERT INTO court_fees (
    court_fee_id,
    business_unit_id,
    court_fee_code,
    description,
    amount,
    stats_code
) VALUES (
    99000000836501,
    8365,
    'SUS001',
    'Suspense item deletion test court fee',
    10.00,
    'SUS001'
),(
    99000000836502,
    8365,
    'SUS002',
    'Suspense item deletion test court fee 2',
    10.00,
    'SUS002'
);

INSERT INTO suspense_accounts (
    suspense_account_id,
    business_unit_id,
    account_number
) VALUES (
    99000000836502,
    8365,
    'SUSP-DELETE-TEST'
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
    99000000836503,
    99000000836502,
    1,
    CAST('CF' AS t_suspense_item_type_enum),
    '2026-11-06 10:00:00',
    CAST('NC' AS t_payment_method_enum),
    99000000836501
);

INSERT INTO suspense_transactions (
    suspense_transaction_id,
    suspense_item_id,
    posted_date,
    posted_by,
    posted_by_name,
    transaction_type,
    amount
) VALUES
    (
        99000000836510,
        99000000836503,
        '2026-11-06 10:00:00',
        'L080JG',
        'Suspense Item Test User',
        CAST('CF' AS t_suspense_transaction_type_enum),
        10.00
    ),
    (
        99000000836511,
        99000000836503,
        '2026-11-06 10:05:00',
        'L080JG',
        'Suspense Item Test User',
        CAST('UN' AS t_suspense_transaction_type_enum),
        20.00
    );

INSERT INTO control_totals (
    control_total_id,
    business_unit_id,
    item_number,
    amount,
    associated_record_type,
    associated_record_id
) VALUES
    (
        99000000836520,
        8365,
        1,
        10.00,
        CAST('suspense_transactions' AS t_associated_record_type_enum),
        '99000000836510'
    ),
    (
        99000000836521,
        8365,
        2,
        20.00,
        CAST('suspense_transactions' AS t_associated_record_type_enum),
        '99000000836511'
    );

INSERT INTO court_fees_received (
    court_fee_received_id,
    business_unit_id,
    court_fee_id,
    overpayment,
    suspense_transaction_id,
    number_of_items,
    received_date
) VALUES
    (
        99000000836530,
        8365,
        99000000836501,
        false,
        99000000836510,
        1,
        '2026-11-06 10:00:00'
    ),
    (
        99000000836531,
        8365,
        99000000836502,
        false,
        99000000836510,
        1,
        '2026-11-06 10:00:00'
    );

INSERT INTO notes (
    note_id,
    note_type,
    associated_record_type,
    associated_record_id,
    note_text,
    posted_date,
    posted_by,
    posted_by_name
) VALUES (
    99000000836540,
    CAST('AA' AS t_note_type_enum),
    CAST('suspense_items' AS t_associated_record_type_enum),
    '99000000836503',
    'Suspense item deletion test note',
    '2026-11-06 10:00:00',
    'L080JG',
    'Suspense Item Test User'
);
