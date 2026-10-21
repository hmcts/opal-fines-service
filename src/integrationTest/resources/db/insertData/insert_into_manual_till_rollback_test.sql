INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
) VALUES (
    8364,
    'Manual Till Rollback Test Business Unit',
    'MTRB',
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
    99000000836400,
    8364,
    1,
    'L080JG',
    'Manual Till Rollback Test User',
    false,
    '2026-10-20 10:00:00'
);

INSERT INTO payments_in (
    payment_in_id,
    till_id,
    payment_amount,
    payment_date,
    payment_method,
    destination_type,
    receipt,
    allocated,
    auto_payment
) VALUES (
    99000000836410,
    99000000836400,
    15.00,
    '2026-10-20 10:00:00',
    CAST('NC' AS t_payment_method_enum),
    CAST('F' AS t_pi_destination_type_enum),
    false,
    false,
    false
);
