/**
* OPAL Program
*
* MODULE      : insert_into_business_units.sql
*
* DESCRIPTION : Inserts rows of data into the BUSINESS_UNITS table for integration tests
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 18/02/2024    R DODD        1.0       Inserts rows of data into the BUSINESS_UNITS table for integration tests
*
**/

INSERT INTO business_units (
    business_unit_id, business_unit_name, business_unit_code, business_unit_type,
    account_number_prefix, parent_business_unit_id, opal_domain, welsh_language
)
VALUES
(
9091,'Parent Business Unit','PRNT',
'Area','XX',NULL,'Fines',
true
);

INSERT INTO business_units (
    business_unit_id, business_unit_name, business_unit_code, business_unit_type,
    account_number_prefix, parent_business_unit_id, opal_domain, welsh_language
)
VALUES
(
9092,'AAA Business Unit 9092','AAAA',
'Accounting Division','XX',9091,
'Fines',true
);
