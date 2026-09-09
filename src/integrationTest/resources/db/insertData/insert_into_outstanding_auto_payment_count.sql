/**
* OPAL Program
*
* DESCRIPTION : Inserts outstanding auto payment count rows for integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 25/08/2026  R DODD      1.0      Insert outstanding auto payment count test data.
*
*/

INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    account_number_prefix,
    opal_domain,
    welsh_language)
VALUES
    (2470, 'Luton', 'LUTN', 'Area', 'LU', 'Fines', true),
    (2471, 'Cardiff', 'CARD', 'Area', 'CA', 'Fines', true),
    (2472, 'Swansea', 'SWAN', 'Area', 'SW', 'Fines', true);

INSERT INTO interface_jobs (
    interface_job_id,
    business_unit_id,
    interface_name,
    status,
    created_datetime)
VALUES
    (247001, 2470, 'Auto Payments In', 'CREATED', '2026-08-01 10:00:00'),
    (247002, 2470, 'Auto Payments In', 'FAILED', '2026-08-01 10:05:00'),
    (247003, 2470, 'Auto Payments In', 'COMPLETED', '2026-08-01 10:10:00'),
    (247101, 2471, 'Auto Payments In', 'CREATED', '2026-08-01 11:00:00'),
    (247201, 2472, 'Auto Payments In', 'FAILED', '2026-08-01 12:00:00');

INSERT INTO tills (
    till_id,
    business_unit_id,
    till_number,
    owned_by,
    owned_by_name,
    status,
    auto_payment)
VALUES
    (247011, 2470, 1, 'test-user', 'Test User', 'Created', true),
    (247012, 2470, 2, 'test-user', 'Test User', 'Allocated', true),
    (247013, 2470, 3, 'test-user', 'Test User', 'Created', false),
    (247111, 2471, 1, 'test-user', 'Test User', 'Created', true),
    (247112, 2471, 2, 'test-user', 'Test User', 'Failed', true),
    (247211, 2472, 1, 'test-user', 'Test User', 'Created', true);
