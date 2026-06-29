/**
* OPAL Program
*
* MODULE      : insert_cash_till_report.sql
*
* DESCRIPTION : Insert the cash till report into the REPORTS table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 12/06/2026    C Cho       1.0         PO-2607 Insert the cash till report into the REPORTS table.
*
**/

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
    'cash_till',
    'Cash till report',
    'Cash input',
    '[{ "name":"till_number", "prompt":"Till Number", "type":"integer", "min":1, "max":999 }]',
    false,
    false,
    false,
    false,
    '14',
    NULL,
    '{CSV,PDF}',
    false
);
