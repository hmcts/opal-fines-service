/**
* OPAL Program
*
* MODULE      : insert_operational_report_instances_dev_test_data.sql
*
* DESCRIPTION : Insert operational report instance dev test data
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------------------------
* 08/07/2026    C Cho       1.0         PO-8301 - Add operational report instances for report summary list testing.
*
**/

INSERT INTO public.report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    created_timestamp,
    requested_by,
    report_parameters,
    location,
    requested_at,
    generation_status,
    scheduled_deletion_timestamp,
    report_name,
    no_of_records,
    errors,
    requested_by_name
) VALUES
    (
        99000000008001,
        'operational_report_enforcement',
        ARRAY[5,77]::smallint[],
        1,
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        12345678,
        '{"reportType":"SUMMARY","businessUnitIds":[5,77],"reportEnforcementMode":"ALL","includeAdult":true,"includeYouth":true,"includeCompany":true}',
        'reports/enforcement/99000000008001.json',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        'READY',
        CURRENT_TIMESTAMP + INTERVAL '13 days',
        'Operational report (by enforcement) - Camberwell Green',
        42,
        NULL,
        'opal-test'
    ),
    (
        99000000008002,
        'operational_report_enforcement',
        ARRAY[82]::smallint[],
        2,
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        12345678,
        '{"reportType":"SUMMARY","businessUnitIds":[82],"reportEnforcementMode":"ALL","includeAdult":true,"includeYouth":true,"includeCompany":true}',
        'reports/enforcement/99000000008002.json',
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        'READY',
        CURRENT_TIMESTAMP + INTERVAL '12 days',
        'Operational report (by enforcement) - Bolton',
        0,
        NULL,
        'opal-test'
    ),
    (
        99000000008003,
        'operational_report_enforcement',
        ARRAY[103]::smallint[],
        3,
        CURRENT_TIMESTAMP - INTERVAL '3 days',
        22345678,
        '{"reportType":"DETAILED","businessUnitIds":[103],"reportEnforcementMode":"LAST_ACTION","enforcementAction":"NOENF","lastActionDateFrom":"2026-01-01","includeAdult":true,"includeYouth":true,"includeCompany":true}',
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '3 days',
        'REQUESTED',
        NULL,
        'Operational report (by enforcement) - Pennine requested',
        NULL,
        NULL,
        'opal-requester'
    ),
    (
        99000000008004,
        'operational_report_enforcement',
        ARRAY[5,77]::smallint[],
        4,
        CURRENT_TIMESTAMP - INTERVAL '10 days',
        12345678,
        '{"reportType":"DETAILED","businessUnitIds":[5,77],"reportEnforcementMode":"REGF","regfDateFrom":"2026-01-01","includeAdult":true,"includeYouth":true,"includeCompany":true}',
        'reports/enforcement/99000000008004.json',
        CURRENT_TIMESTAMP - INTERVAL '10 days',
        'READY',
        CURRENT_TIMESTAMP + INTERVAL '4 days',
        'Operational report (by enforcement) - older than 7 days',
        18,
        NULL,
        'opal-test'
    ),
    (
        99000000008005,
        'operational_report_payment',
        ARRAY[77]::smallint[],
        5,
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        12345678,
        '{"reportType":"DETAILED","businessUnitIds":[77],"isPaymentMade":true,"reportMode":"SINCE_DATE","sinceDate":"2026-01-01","includeAdult":true,"includeYouth":true,"includeCompany":true}',
        'reports/payment/99000000008005.json',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        'READY',
        CURRENT_TIMESTAMP + INTERVAL '13 days',
        'Operational report (by payment) - Camberwell Green',
        64,
        NULL,
        'opal-test'
    ),
    (
        99000000008006,
        'operational_report_payment',
        ARRAY[82]::smallint[],
        6,
        CURRENT_TIMESTAMP - INTERVAL '4 days',
        12345678,
        '{"reportType":"DETAILED","businessUnitIds":[82],"isPaymentMade":false,"reportMode":"SINCE_DATE","sinceDate":"2026-01-01","includeAdult":true,"includeYouth":true,"includeCompany":true}',
        'reports/payment/99000000008006.json',
        CURRENT_TIMESTAMP - INTERVAL '4 days',
        'READY',
        CURRENT_TIMESTAMP + INTERVAL '10 days',
        'Operational report (by payment) - Bolton',
        0,
        NULL,
        'opal-test'
    ),
    (
        99000000008007,
        'operational_report_payment',
        ARRAY[103]::smallint[],
        7,
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        22345678,
        '{"reportType":"DETAILED","businessUnitIds":[103],"isPaymentMade":true,"reportMode":"WITH_REGF","includeAdult":true,"includeYouth":true,"includeCompany":true}',
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        'REQUESTED',
        NULL,
        'Operational report (by payment) - Pennine requested',
        NULL,
        NULL,
        'opal-requester'
    ),
    (
        99000000008008,
        'operational_report_payment',
        ARRAY[77]::smallint[],
        8,
        CURRENT_TIMESTAMP - INTERVAL '11 days',
        12345678,
        '{"reportType":"DETAILED","businessUnitIds":[77],"isPaymentMade":true,"reportMode":"SINCE_DATE","sinceDate":"2026-01-01","includeAdult":true,"includeYouth":true,"includeCompany":true}',
        'reports/payment/99000000008008.json',
        CURRENT_TIMESTAMP - INTERVAL '11 days',
        'READY',
        CURRENT_TIMESTAMP + INTERVAL '3 days',
        'Operational report (by payment) - older than 7 days',
        27,
        NULL,
        'opal-test'
    );

SELECT setval(
    'public.report_instance_id_seq',
    GREATEST(
        (SELECT COALESCE(MAX(report_instance_id), 0) FROM public.report_instances),
        99000000008008
    ),
    true
);
