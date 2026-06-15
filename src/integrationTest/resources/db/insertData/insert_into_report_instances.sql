INSERT INTO public.reports (
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
    'it_report_instances',
    'Integration Report Instances',
    'Operational Reports',
    false,
    NULL,
    true,
    false,
    false,
    'P30D',
    'SEARCH_AND_VIEW_ACCOUNTS',
    '{CSV,PDF}',
    true
);

INSERT INTO public.report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    created_timestamp,
    requested_by,
    requested_by_name,
    report_parameters,
    requested_at,
    generation_status,
    report_name,
    no_of_records
) VALUES
(
    9001,
    'it_report_instances',
    '{10,20}',
    1,
    '2026-01-01 11:00:00',
    42,
    'John Doe',
    NULL,
    '2026-01-01 10:00:00',
    'READY',
    'My Report',
    100
),
(
    9002,
    'it_report_instances',
    '{10}',
    2,
    '2026-02-01 11:00:00',
    43,
    'Jane Doe',
    NULL,
    '2026-02-01 10:00:00',
    'IN_PROGRESS',
    NULL,
    NULL
),
(
    9003,
    'it_report_instances',
    '{20}',
    3,
    '2026-03-01 11:00:00',
    42,
    'John Doe',
    NULL,
    '2026-03-01 10:00:00',
    'ERROR',
    'Error Report',
    NULL
);


