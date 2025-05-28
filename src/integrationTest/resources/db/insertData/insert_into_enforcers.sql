/**
* OPAL Program
*
* MODULE      : insert_into_enforcers.sql
*
* DESCRIPTION : Insert test data for enforcers integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 27/05/2025    R DODD        1.0         PO1047 Insert test data for enforcers integration tests
*
**/

INSERT INTO enforcers
(
enforcer_id, business_unit_id, enforcer_code, name, name_cy
,address_line_1, address_line_2, address_line_3
,address_line_1_cy, address_line_2_cy, address_line_3_cy
,postcode, warrant_reference_sequence, warrant_register_sequence
)
VALUES
(
001, 5, 001, 'AAA Enforcers', NULL
, '9 Enforcement Street', 'Enformentville', 'Enforcementon'
, NULL, NULL, NULL
, 'EF1 1EF', '101/09/00000', 666
);
