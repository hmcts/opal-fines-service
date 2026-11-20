INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
) VALUES (
             8363,
             'Manual Till Deletion Test Business Unit',
             'MTDL',
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
) VALUES
      (
          99000000836300,
          8363,
          1,
          'L080JG',
          'Manual Till Test User',
          false,
          '2026-10-07 10:00:00'
      ),
      (
          99000000836301,
          8363,
          2,
          'L080JG',
          'Manual Till Test User',
          false,
          '2026-10-07 10:05:00'
      ),
      (
          99000000836302,
          8363,
          3,
          'L080JG',
          'Manual Till Test User',
          false,
          '2026-10-07 10:10:00'
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
) VALUES
      (
          99000000836310,
          99000000836300,
          10.00,
          '2026-10-07 10:00:00',
          CAST('NC' AS t_payment_method_enum),
          CAST('F' AS t_pi_destination_type_enum),
          false,
          false,
          false
      ),
      (
          99000000836311,
          99000000836301,
          20.00,
          '2026-10-07 10:05:00',
          CAST('NC' AS t_payment_method_enum),
          CAST('F' AS t_pi_destination_type_enum),
          false,
          false,
          false
      ),
      (
          99000000836312,
          99000000836302,
          30.00,
          '2026-10-07 10:10:00',
          CAST('NC' AS t_payment_method_enum),
          CAST('F' AS t_pi_destination_type_enum),
          false,
          false,
          false
      );