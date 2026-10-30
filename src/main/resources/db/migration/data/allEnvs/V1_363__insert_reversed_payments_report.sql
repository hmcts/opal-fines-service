
/**
* OPAL Program
*
* MODULE      : insert_reversed_payments_report_reference_data.sql
*
* DESCRIPTION : Insert Reversed Payments Report reference data
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------
* 21/10/2026    I Chandio   1.0         PO-10345 - Insert Reversed Payments Report reference data.
*
*/

INSERT INTO public.reports (
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
    'Reports, Daily Checks/ Month End',
    ARRAY['PDF']::public.r_supported_file_type_enum[],
    true
);

