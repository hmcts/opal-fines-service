/**
* OPAL Program
*
* DESCRIPTION : Inserts interface job summary rows for integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 02/07/2026  R DODD      1.0      Insert interface job summary test data.
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
    (2574, 'Luton', 'LUTN', 'Area', 'LU', 'Fines', true),
    (2575, 'Cardiff', 'CARD', 'Area', 'CA', 'Fines', true);

INSERT INTO interface_jobs (
    interface_job_id,
    business_unit_id,
    interface_name,
    status,
    created_datetime,
    completed_datetime)
VALUES
    (257401, 2574, 'Auto Payments In', 'COMPLETED', '2026-07-01 10:00:00', '2026-07-01 10:30:00'),
    (257402, 2575, 'Auto Payments In', 'COMPLETED', '2026-07-01 11:00:00', '2026-07-01 11:30:00'),
    (257403, 2574, 'Auto Payments In', 'FAILED', '2026-07-01 12:00:00', '2026-07-01 12:30:00'),
    (257404, 2575, 'Manual Payments In', 'COMPLETED', '2026-07-01 11:05:00', '2026-07-01 11:35:00');

INSERT INTO interface_files (
    interface_file_id,
    interface_job_id,
    file_name,
    created_datetime,
    source,
    record_count)
VALUES
    (257411, 257401, 'auto-payments-in-1.dat', '2026-07-01 10:01:00', 'NATWEST'::t_interface_file_source_enum, 10),
    (257412, 257401, 'auto-payments-in-2.dat', '2026-07-01 10:02:00', 'NATWEST'::t_interface_file_source_enum, 20),
    (257413, 257402, 'cardiff-auto-payments-in.dat', '2026-07-01 11:01:00',
     'ALLPAY'::t_interface_file_source_enum, 30),
    (257414, 257403, 'failed-auto-payments-in.dat', '2026-07-01 12:01:00',
     'DWP'::t_interface_file_source_enum, 40),
    (257415, 257404, 'manual-payments-in.dat', '2026-07-01 11:06:00', 'DWP'::t_interface_file_source_enum, 50);
