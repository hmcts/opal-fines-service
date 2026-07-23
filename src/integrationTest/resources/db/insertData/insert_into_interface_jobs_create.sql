/**
* OPAL Program
*
* DESCRIPTION : Inserts reference rows for interface job create integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 14/07/2026  R DODD      1.0      Insert interface job create test data.
*
*/

INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    account_number_prefix,
    opal_domain,
    welsh_language
)
VALUES
    (2577, 'Stevenage', 'STEV', 'Area', 'SV', 'Fines', true);
