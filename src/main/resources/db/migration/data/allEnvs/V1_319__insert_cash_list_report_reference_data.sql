/**
* OPAL Program
*
* MODULE      : insert_cash_list_report_reference_data.sql
*
* DESCRIPTION : Insert Cash List report reference data
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 10/09/2026    C Cho       1.0         PO-3408 - Insert Cash List report reference data.
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
    'cash_list',
    'Cash List report',
    'Finance',
    '[{"name":"till_number","prompt":"Till Number","type":"integer","min":1,"max":999}]'::json,
    false,
    false,
    false,
    false,
    'P14D',
    'PROCESS_AND_ALLOCATE_PAYMENTS',
    ARRAY['CSV','PDF']::public.r_supported_file_type_enum[],
    false
);
