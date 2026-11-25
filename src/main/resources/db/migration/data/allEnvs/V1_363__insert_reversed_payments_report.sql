
/**
* OPAL Program
*
* MODULE      : insert_reversed_payments_report.sql
*
* DESCRIPTION : Insert Reversed Payments Report reference data
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* -----------   ----------  --------    --------------------------------------------------------
* 24/11/2026    I Chandio   1.0         PO-10345 - Insert Reversed Payments Report reference data.
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
) VALUES (
    'reversed_payments_report',
    'Reversed Payments Report',
    'Cash reports',
    NULL,
    false,
    false,
    false,
    false,
    NULL,
    'REVERSE_PAYMENT_REPORT',
    ARRAY['PDF']::r_supported_file_type_enum[],
    true
);
