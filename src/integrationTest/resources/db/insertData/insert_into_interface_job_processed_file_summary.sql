INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    account_number_prefix,
    opal_domain,
    welsh_language
) VALUES (
    2576, 'Processed Summary BU', 'PSBU', 'Area', 'PS', 'Fines', true
);

INSERT INTO interface_jobs (
    interface_job_id,
    business_unit_id,
    interface_name,
    status,
    created_datetime,
    completed_datetime
) VALUES (
    257601, 2576, 'Auto Payments In', 'COMPLETED',
    '2026-07-01 10:00:00', '2026-07-01 10:30:00'
);

INSERT INTO interface_jobs (
    interface_job_id,
    business_unit_id,
    interface_name,
    status,
    created_datetime,
    completed_datetime
) VALUES (
    257602, 2576, 'Auto Payments In', 'COMPLETED',
    '2026-07-01 11:00:00', '2026-07-01 11:30:00'
);

INSERT INTO interface_jobs (
    interface_job_id,
    business_unit_id,
    interface_name,
    status,
    created_datetime,
    completed_datetime
) VALUES (
    257603, 2576, 'Auto Payments In', 'COMPLETED',
    '2026-07-01 12:00:00', '2026-07-01 12:30:00'
);

INSERT INTO interface_files (
    interface_file_id,
    interface_job_id,
    file_name,
    created_datetime,
    source,
    record_count,
    total_amount
) VALUES (
    257611, 257601, 'processed-summary.dat', '2026-07-01 10:01:00',
    'NATWEST'::t_interface_file_source_enum, 3, 123.45
);

INSERT INTO interface_files (
    interface_file_id,
    interface_job_id,
    file_name,
    created_datetime,
    source,
    record_count,
    total_amount
) VALUES (
    257612, 257602, 'ignored-summary.dat', '2026-07-01 11:01:00',
    'NATWEST'::t_interface_file_source_enum, 2, 50.00
);

INSERT INTO tills (
    till_id,
    business_unit_id,
    till_number,
    owned_by,
    interface_file_id,
    source,
    total_amount,
    payments_count,
    owned_by_name,
    auto_payment,
    created_date
) VALUES (
    257621, 2576, 901, 'test-user', 257611,
    'NATWEST'::t_interface_file_source_enum, 123.45, 3, 'Test User', true,
    '2026-07-01 10:30:00'
);

INSERT INTO interface_messages (
    interface_message_id,
    interface_job_id,
    interface_file_id,
    message_type,
    message_text,
    message_data
) VALUES
    (257631, 257601, 257611, 'Info', 'records_read', '{"count": 3}'),
    (257632, 257601, 257611, 'Warning', 'records_rejected', '{"count": 1}'),
    (257633, 257601, 257611, 'Info', 'records_read', '{"count": 4}'),
    (257634, 257602, 257612, 'Exception', 'records_ignored', '{"count": 2}');
