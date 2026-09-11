/**
* OPAL Program
*
* DESCRIPTION : Inserts till summary rows for PO-2575 integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 28/08/2026  R DODD      1.0      Insert till summary test data.
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
    (25750, 'Luton', 'LUTN', 'Area', 'LU', 'Fines', true),
    (25751, 'Cardiff', 'CARD', 'Area', 'CA', 'Fines', true),
    (25752, 'Swansea', 'SWAN', 'Area', 'SW', 'Fines', true);

INSERT INTO interface_jobs (
    interface_job_id,
    business_unit_id,
    interface_name,
    status,
    created_datetime,
    completed_datetime)
VALUES
    (257501, 25750, 'Auto Payments In', 'COMPLETED', '2026-08-27 09:00:00', '2026-08-27 09:10:00'),
    (257502, 25751, 'Auto Payments In', 'COMPLETED', '2026-08-27 10:00:00', '2026-08-27 10:10:00'),
    (257503, 25752, 'Auto Payments In', 'COMPLETED', '2026-08-27 11:00:00', '2026-08-27 11:10:00');

INSERT INTO interface_files (
    interface_file_id,
    interface_job_id,
    file_name,
    created_datetime,
    records,
    source,
    record_count,
    total_amount)
VALUES
    (257501, 257501, 'luton-allocated.dat', '2026-08-27 09:01:00', '[]', 'NATWEST', 10, 1234.56),
    (257502, 257501, 'luton-created.dat', '2026-08-27 09:02:00', '[]', 'ALLPAY', 11, 2345.67),
    (257503, 257502, 'cardiff-allocated.dat', '2026-08-27 10:01:00', '[]', 'DWP', 12, 3456.78),
    (257504, 257503, 'swansea-allocated.dat', '2026-08-27 11:01:00', '[]', 'OTHER', 13, 4567.89);

INSERT INTO tills (
    till_id,
    business_unit_id,
    till_number,
    owned_by,
    source,
    status,
    total_amount,
    interface_file_id,
    payments_count,
    owned_by_name,
    auto_payment,
    created_date)
VALUES
    (257501, 25750, 501, 'L25750', 'NATWEST', 'Allocated', 1234.56, 257501, 10, 'Luton User', true,
     '2026-08-27 09:15:00'),
    (257502, 25750, 502, 'L25750', 'ALLPAY', 'Created', 2345.67, 257502, 11, 'Luton User', false,
     '2026-08-27 09:30:00'),
    (257503, 25751, 503, 'C25751', 'DWP', 'Allocated', 3456.78, 257503, 12, 'Cardiff User', true,
     '2026-08-27 10:15:00'),
    (257504, 25752, 504, 'S25752', 'OTHER', 'Allocated', 4567.89, 257504, 13, 'Swansea User', true,
     '2026-08-27 11:15:00');

INSERT INTO interface_messages (
    interface_message_id,
    interface_job_id,
    interface_file_id,
    message_type,
    message_text,
    record_index,
    record_detail)
VALUES
    (257501, 257501, 257501, 'Exception', 'Rejected payment', 1, 'exception record'),
    (257502, 257501, 257501, 'Warning', 'Inhibit overridden', 2, 'warning record'),
    (257503, 257501, 257501, 'Info', 'Accepted payment', 3, 'info record'),
    (257504, 257501, 257502, 'Warning', 'Created till warning', 1, 'warning record'),
    (257505, 257502, 257503, 'Exception', 'Cardiff rejected payment', 1, 'exception record');
