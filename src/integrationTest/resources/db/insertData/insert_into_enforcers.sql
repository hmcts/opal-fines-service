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

INSERT INTO business_units
(
business_unit_id,business_unit_name,business_unit_code,business_unit_type,
account_number_prefix,parent_business_unit_id,opal_domain,welsh_language
)
VALUES
(
10059,'Business Unit 5','BU05','Area'
,'XX',NULL,'Fines',false
);

INSERT INTO enforcers
(
enforcer_id, business_unit_id, enforcer_code, name, name_cy
,address_line_1, address_line_2, address_line_3
,address_line_1_cy, address_line_2_cy, address_line_3_cy
,postcode, warrant_reference_sequence, warrant_register_sequence
)
VALUES
(
001, 10059, 001, 'AAA Enforcers', NULL
, '9 Enforcement Street', 'Enformentville', 'Enforcementon'
, NULL, NULL, NULL
, 'EF1 1EF', '101/09/00000', 666
);
