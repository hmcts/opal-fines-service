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
    '[{"name":"filters", "type":"menu-radio", "options":["ACTIVE","PENDING"],"mandatory":false}]',
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
    '[]',
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
    '[{"name":"date-param","prompt":"date-param","type":"date","mandatory":true,"min":null,"max":null,"hint":null,"options":null,"apidata":null,"language_dependent":null},{"name":"decimal-param","prompt":"decimal-param","type":"decimal-2dp","mandatory":true,"min":1.0,"max":10.0,"hint":null,"options":null,"apidata":null,"language_dependent":null},{"name":"integer-param","prompt":"integer-param","type":"integer","mandatory":true,"min":1,"max":10,"hint":null,"options":null,"apidata":null,"language_dependent":null},{"name":"radio-param","prompt":"radio-param","type":"menu-radio","mandatory":true,"min":1,"max":1,"hint":null,"options":["one","two"],"apidata":null,"language_dependent":null},{"name":"checkbox-param","prompt":"checkbox-param","type":"menu-checkbox","mandatory":true,"min":1,"max":2,"hint":null,"options":["one","two"],"apidata":null,"language_dependent":null},{"name":"text-60-param","prompt":"text-60-param","type":"text-60","mandatory":true,"min":1,"max":60,"hint":null,"options":null,"apidata":null,"language_dependent":null},{"name":"text-100-param","prompt":"text-100-param","type":"text-100","mandatory":true,"min":1,"max":100,"hint":null,"options":null,"apidata":null,"language_dependent":null},{"name":"text-1000-param","prompt":"text-1000-param","type":"text-1000","mandatory":true,"min":1,"max":1000,"hint":null,"options":null,"apidata":null,"language_dependent":null}]',
    false,
    false,
    false,
    'P14D',
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
    'P14D',
    null,
    '{CSV,PDF}',
    true
);