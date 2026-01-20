/**
* OPAL Program
*
* MODULE      : insert_operational_reports.sql
*
* DESCRIPTION : Insert operational report definitions for enforcement reporting.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 08/01/2026    C Cho       1.0         PO-2274 Insert operational reports for enforcement and payments.
*
**/

INSERT INTO reports (
    report_id,
    report_title,
    report_group,
    report_parameters,
    audited_report,
    supports_multi_bu,
    is_bespoke_journey,
    shown_as_worklist,
    retention_period,
    permission,
    supported_file_types,
    can_manually_create
) VALUES
(
    'operational_report_enforcement',
    'Operational report (by enforcement)',
    'Operational Reports',
    NULL,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    '14',
    NULL,
    ARRAY['CSV', 'PDF']::r_supported_file_type_enum[],
    TRUE
),
(
    'operational_report_payment',
    'Operational report (by payment)',
    'Operational Reports',
    NULL,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    '14',
    NULL,
    ARRAY['CSV', 'PDF']::r_supported_file_type_enum[],
    TRUE
);
