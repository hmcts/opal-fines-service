INSERT INTO reports (
    report_id,
    report_title,
    report_group,
    audited_report,
    report_parameters,
    supports_multi_bu,
    is_bespoke_journey,
    shown_as_worklist,
    retention_period,
    permission,
    supported_file_types,
    can_manually_create
) VALUES (
    'cash_list',
    'Cash List',
    'Fines',
    true,
    NULL,
    false,
    false,
    false,
    NULL,
    NULL,
    NULL,
    false
);

INSERT INTO report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    requested_by,
    report_parameters,
    requested_at,
    generation_status,
    requested_by_name
) VALUES (
    99000000343000,
    'cash_list',
    ARRAY[1777]::smallint[],
    1,
    12345678,
    '{"till_id":999999999}'::json,
    '2026-05-27 09:00:00',
    CAST('REQUESTED' AS ri_generation_status_enum),
    'opal-test'
);
