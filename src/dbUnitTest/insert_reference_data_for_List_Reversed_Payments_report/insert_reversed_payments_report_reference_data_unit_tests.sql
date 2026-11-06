
/**
* OPAL Program
*
* MODULE      : insert_reversed_payments_report_reference_data_unit_tests.sql
*
* DESCRIPTION : Unit tests for Reversed Payments Report reference data.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 21/10/2026    I Chandio   1.0         PO-10345 - Unit tests for Reversed Payments Report reference data.
*
**/
\timing

DO $$
DECLARE
    v_record_count INTEGER;
BEGIN
    RAISE NOTICE '=== TEST 1: Verify Reversed Payments Report record exists ===';

    SELECT COUNT(*)
    INTO v_record_count
    FROM public.reports
    WHERE report_id = 'reversed_payments_report';

    ASSERT v_record_count = 1,
        'Expected exactly one Reversed Payments Report record';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;


DO $$
DECLARE
    v_report public.reports%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: Verify Reversed Payments Report details ===';

    SELECT *
    INTO STRICT v_report
    FROM public.reports
    WHERE report_id = 'reversed_payments_report';

    ASSERT v_report.report_id = 'reversed_payments_report',
        'report_id should match';

    ASSERT v_report.report_title = 'Reversed Payments Report',
        'report_title should match';

    ASSERT v_report.report_group = 'Cash reports',
        'report_group should match';

    ASSERT v_report.audited_report = false,
        'audited_report should be false';

    ASSERT v_report.report_parameters IS NULL,
        'report_parameters should be NULL';

    ASSERT v_report.supports_multi_bu = false,
        'supports_multi_bu should be false';

    ASSERT v_report.is_bespoke_journey = false,
        'is_bespoke_journey should be false';

    ASSERT v_report.shown_as_worklist = false,
        'shown_as_worklist should be false';

    ASSERT v_report.retention_period IS NULL,
        'retention_period should be NULL';

    ASSERT v_report.permission = 'Reports, Daily Checks/ Month End',
        'permission should match';

    ASSERT v_report.supported_file_types =
        ARRAY['PDF']::public.r_supported_file_type_enum[],
        'supported_file_types should contain PDF only';

    ASSERT v_report.can_manually_create = true,
        'can_manually_create should be true';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;


DO $$
DECLARE
    v_report public.reports%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 3: Verify report configuration and supported file types ===';

    SELECT *
    INTO STRICT v_report
    FROM public.reports
    WHERE report_id = 'reversed_payments_report';

    ASSERT cardinality(v_report.supported_file_types) = 1,
        'Only one supported file type should exist';

    ASSERT v_report.supported_file_types[1] = 'PDF',
        'Supported file type should be PDF';

    ASSERT v_report.report_parameters IS NULL,
        'Report should not require additional parameters';

    ASSERT v_report.can_manually_create = true,
        'Report should support manual creation';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;


DO $$
DECLARE
    v_record_count INTEGER;
BEGIN
    RAISE NOTICE '=== TEST 4: Verify no duplicate report reference data exists ===';

    SELECT COUNT(*)
    INTO v_record_count
    FROM public.reports
    WHERE report_id = 'reversed_payments_report';

    ASSERT v_record_count = 1,
        'Duplicate Reversed Payments Report records found';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;


DO $$
BEGIN
    RAISE NOTICE '=== PO-10345: All Reversed Payments Report unit tests completed successfully ===';
END $$;

