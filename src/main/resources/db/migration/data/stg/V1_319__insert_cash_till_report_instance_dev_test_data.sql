/**
* OPAL Program
*
* MODULE      : insert_cash_till_report_instance_dev_test_data.sql
*
* DESCRIPTION : Insert cash till report instance dev test data.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------------------------
* 26/08/2026    C Cho       1.0         PO-8856 - Add cash till report instance data for E2E report download testing.
*
**/

INSERT INTO public.report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    requested_by,
    report_parameters,
    location,
    requested_at,
    generation_status,
    requested_by_name
) VALUES (
    99000000353000,
    'cash_till',
    ARRAY[77]::smallint[],
    1,
    12345678,
    '{"till_id":99000000353100,"allocated_report":false}'::json,
    'stored-cash-till-report-location',
    '2026-05-27 09:00:00',
    CAST('READY' AS ri_generation_status_enum),
    'opal-test'
);

SELECT setval(
    'public.report_instance_id_seq',
    GREATEST(
        (SELECT COALESCE(MAX(report_instance_id), 0) FROM public.report_instances),
        99000000353000
    ),
    true
);
