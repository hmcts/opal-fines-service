/**
 * CGI OPAL Program
 *
 * MODULE      : insert_into_reports.sql
 *
 * DESCRIPTION : Insert test data into the REPORTS table for use by integration tests
 *
 * VERSION HISTORY:
 *
 * Date          Author      Version     Nature of Change
 * ----------    -------     --------    ------------------------------------------------------------------------------------------------
 * 01/06/2026    A REEVES    2.0         Insert test data into the REPORTS table for use by integration tests
 **/

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
) VALUES
(
    'it_report_full',
    'Integration Full Report',
    'Operational Reports',
    true,
    '{"filters":{"status":["ACTIVE","PENDING"]},"options":{"includeArchived":false}}',
    true,
    true,
    true,
    'P30D',
    'SEARCH_AND_VIEW_ACCOUNTS',
    '{CSV,PDF,XML}',
    false
),
(
    'it_report_optional',
    'Integration Optional',
    'Operational Reports',
    false,
    NULL,
    false,
    false,
    false,
    NULL,
    'SEARCH_AND_VIEW_ACCOUNTS',
    ARRAY[]::public.r_supported_file_type_enum[],
    true
),
(
    'it_report_order',
    'Integration File Order',
    'Operational Reports',
    false,
    '{"mode":"ordered"}',
    false,
    false,
    false,
    'P14D',
    'SEARCH_AND_VIEW_ACCOUNTS',
    '{XML,CSV,PDF}',
    true
),(
    'IT-report-1',
    'Single BU report',
    'group-1',
    false,
    null,
    false,
    false,
    false,
    '14',
    null,
    '{CSV,PDF}',
    true
),
(
    'IT-report-2',
    'Multi BU report',
    'group-2',
    false,
    null,
    true,
    false,
    false,
    '14',
    null,
    '{CSV,PDF}',
    true
),
(
    'IT-report-3',
    'No manual creation report',
    'group-3',
    false,
    null,
    true,
    false,
    false,
    '14',
    null,
    '{CSV,PDF}',
    false
);
