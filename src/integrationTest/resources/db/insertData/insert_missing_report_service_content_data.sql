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
    'missing_service_report',
    'Missing Service Report',
    'Operational Reports',
    FALSE,
    '[]',
    FALSE,
    FALSE,
    FALSE,
    'P14D',
    'SEARCH_AND_VIEW_ACCOUNTS',
    '{CSV}',
    FALSE
);

UPDATE report_instances
   SET report_id = 'missing_service_report'
 WHERE report_instance_id = 99000000353000;
