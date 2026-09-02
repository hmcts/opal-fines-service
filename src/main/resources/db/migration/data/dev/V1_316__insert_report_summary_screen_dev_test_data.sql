/**
* OPAL Program
*
* MODULE      : insert_report_summary_screen_dev_test_data.sql
*
* DESCRIPTION : Insert report summary screen dev test data.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------------------------
* 26/08/2026    C Cho       1.0         PO-9738 - Add report summary screen seed data for operational enforcement reports.
*
**/

INSERT INTO public.report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    created_timestamp,
    requested_by,
    requested_by_name,
    report_parameters,
    requested_at,
    generation_status,
    scheduled_deletion_timestamp,
    report_name,
    no_of_records,
    errors)
VALUES 
   (
    99000000355000,
    'operational_report_enforcement',
    ARRAY[77,82]::smallint[],
    1,
    NULL,
    12345678,
    'Report Summary User',
    '{"reportType":"SUMMARY","businessUnitIds":[77,82],"reportEnforcementMode":"ALL","enforcementDateFrom":"2026-08-01","enforcementDateTo":"2026-08-10","includeAdult":true,"includeYouth":true,"includeCompany":true,"onlyAccountsWithParentGuardian":true,"accountStatus":"LIVE","collectionOrderChoice":"ALL","minBalance":100.50,"maxBalance":5000.00,"firstPaymentOrPayByInNext7Days":true,"lowerNameRange":"A","upperNameRange":"M"}',
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    CAST('REQUESTED' AS ri_generation_status_enum),
    CURRENT_TIMESTAMP + INTERVAL '14 days',
    'Requested report with full criteria',
    NULL,
    NULL
    )
  ,(
    99000000355001,
    'operational_report_enforcement',
    ARRAY[77]::smallint[],
    2,
    NULL,
    12345678,
    'Report Summary User',
    '{"reportType":"DETAILED","businessUnitIds":[77],"reportEnforcementMode":"LAST_ACTION","enforcementAction":"REGF","lastActionDateFrom":"2026-08-01","includeYouth":true,"includeCompany":true,"accountStatus":"CLOSED","collectionOrderChoice":"WITH"}',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CAST('IN_PROGRESS' AS ri_generation_status_enum),
    CURRENT_TIMESTAMP + INTERVAL '14 days',
    'In-progress report with alternative criteria',
    NULL,
    NULL
    )
  ,(
    99000000355002,
    'operational_report_enforcement',
    ARRAY[77]::smallint[],
    3,
    NULL,
    12345678,
    'Report Summary User',
    '{"reportType":"SUMMARY","businessUnitIds":[77],"reportEnforcementMode":"REGF","regfDateTo":"2026-08-10","includeAdult":true,"accountStatus":"LIVE","collectionOrderChoice":"WITHOUT"}',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CAST('ERROR' AS ri_generation_status_enum),
    CURRENT_TIMESTAMP + INTERVAL '14 days',
    'Error report with ordered errors',
    NULL,
    '{"operationId":"REPORT-GENERATION-ERROR","error":"[{\"name\":\"Report query failed.\",\"value\":\"The report could not be generated for QA testing.\"},{\"name\":\"Account data unavailable.\",\"value\":\"One or more account records could not be read.\"}]"}'
    )
  ,(
    99000000355003,
    'operational_report_enforcement',
    ARRAY[77]::smallint[],
    4,
    CURRENT_TIMESTAMP - INTERVAL '20 minutes',
    12345678,
    'Report Summary User',
    '{"reportType":"DETAILED","businessUnitIds":[77],"reportEnforcementMode":"NOT_UNDER_ENFORCEMENT","includeAdult":true,"accountStatus":"LIVE","collectionOrderChoice":"ALL"}',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    CAST('READY' AS ri_generation_status_enum),
    CURRENT_TIMESTAMP + INTERVAL '14 days',
    'Ready report with no content',
    0,
    NULL
  );

SELECT setval(
    'public.report_instance_id_seq',
    GREATEST(
        (SELECT COALESCE(MAX(report_instance_id), 0) FROM public.report_instances),
        99000000355003
    ),
    true
);
