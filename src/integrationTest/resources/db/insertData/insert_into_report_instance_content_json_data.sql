UPDATE reports
SET permission = 'SEARCH_AND_VIEW_ACCOUNTS'
WHERE report_id = 'fp_register';

INSERT INTO report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    requested_by,
    report_parameters,
    location,
    requested_at,
    generation_status,
    requested_by_name
) VALUES (
    99000000354000,
    'fp_register',
    ARRAY[77]::smallint[],
    1,
    12345678,
    '{"from":"auto"}'::json,
    'stored-report-location',
    '2026-05-27 09:00:00',
    CAST('READY' AS ri_generation_status_enum),
    'opal-test'
);
