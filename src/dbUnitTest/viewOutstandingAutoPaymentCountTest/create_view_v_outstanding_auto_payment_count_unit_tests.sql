/**
* OPAL Program
*
* MODULE      : create_view_v_outstanding_auto_payment_count_unit_tests.sql
*
* DESCRIPTION : Unit tests for v_outstanding_auto_payment_count view.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 27/07/2026    C Cho       1.0         PO-2591 Unit tests for v_outstanding_auto_payment_count view
*
**/

\timing

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM tills
     WHERE business_unit_id BETWEEN 24201 AND 24205;

    DELETE FROM interface_jobs
     WHERE interface_job_id IN (
        2420101, 2420102, 2420103, 2420104, 2420105,
        2420301, 2420302,
        2420501, 2420502,
        2429999
    );

    DELETE FROM business_units
     WHERE business_unit_id BETWEEN 24201 AND 24205;

    RAISE NOTICE 'Cleanup completed';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Setting up test data for v_outstanding_auto_payment_count tests ===';

    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_code,
        business_unit_type
    ) VALUES
    (
        24201,
        'Auto Payment Outstanding All',
        'A201',
        'Area'
    ),
    (
        24202,
        'Auto Payment Tills Only',
        'A202',
        'Area'
    ),
    (
        24203,
        'Auto Payment Files Only',
        'A203',
        'Area'
    ),
    (
        24204,
        'Auto Payment No Work',
        'A204',
        'Area'
    ),
    (
        24205,
        'Auto Payment Excluded Work',
        'A205',
        'Area'
    );

    INSERT INTO interface_jobs (
        interface_job_id,
        business_unit_id,
        interface_name,
        status
    ) VALUES
    (
        2420101,
        24201,
        'p_int_payments_in',
        'CREATED'
    ),
    (
        2420102,
        24201,
        'p_int_payments_in',
        'FAILED'
    ),
    (
        2420103,
        24201,
        'p_int_payments_in',
        'PROCESSING'
    ),
    (
        2420104,
        24201,
        'p_int_payments_in',
        'IGNORED'
    ),
    (
        2420105,
        24201,
        'p_int_payments_in',
        'COMPLETED'
    ),
    (
        2420301,
        24203,
        'p_int_payments_in',
        'CREATED'
    ),
    (
        2420302,
        24203,
        'p_int_payments_in',
        'FAILED'
    ),
    (
        2420501,
        24205,
        'p_int_payments_in',
        'PROCESSING'
    ),
    (
        2420502,
        24205,
        'p_int_payments_in',
        'IGNORED'
    ),
    (
        2429999,
        NULL,
        'p_int_payments_in',
        'CREATED'
    );

    INSERT INTO tills (
        till_id,
        business_unit_id,
        till_number,
        owned_by,
        status,
        auto_payment
    ) VALUES
    (
        2420101,
        24201,
        20101,
        'V242T1',
        'Created',
        TRUE
    ),
    (
        2420102,
        24201,
        20102,
        'V242T2',
        'Failed',
        TRUE
    ),
    (
        2420103,
        24201,
        20103,
        'V242T3',
        'Created',
        FALSE
    ),
    (
        2420104,
        24201,
        20104,
        'V242T4',
        'Failed',
        FALSE
    ),
    (
        2420105,
        24201,
        20105,
        'V242T5',
        'Processing',
        TRUE
    ),
    (
        2420106,
        24201,
        20106,
        'V242T6',
        'Allocated',
        TRUE
    ),
    (
        2420201,
        24202,
        20201,
        'V242T7',
        'Created',
        TRUE
    ),
    (
        2420202,
        24202,
        20202,
        'V242T8',
        'Failed',
        TRUE
    ),
    (
        2420301,
        24203,
        20301,
        'V242T9',
        'Created',
        FALSE
    ),
    (
        2420302,
        24203,
        20302,
        'V242TA',
        'Allocated',
        TRUE
    ),
    (
        2420501,
        24205,
        20501,
        'V242TB',
        'Created',
        FALSE
    ),
    (
        2420502,
        24205,
        20502,
        'V242TC',
        'Failed',
        FALSE
    ),
    (
        2420503,
        24205,
        20503,
        'V242TD',
        'Processing',
        TRUE
    ),
    (
        2420504,
        24205,
        20504,
        'V242TE',
        'Allocated',
        TRUE
    );

    RAISE NOTICE 'Test data setup completed';
END $$;

DO $$
DECLARE
    v_row v_outstanding_auto_payment_count%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: Outstanding file and auto-payment till counts are returned ===';

    SELECT *
      INTO v_row
      FROM v_outstanding_auto_payment_count
     WHERE business_unit_id = 24201;

    ASSERT FOUND, 'Business unit 24201 should be returned by the view';
    ASSERT v_row.business_unit_name = 'Auto Payment Outstanding All', 'Business unit name should match';
    ASSERT v_row.files_to_process_count = 2, 'Only CREATED and FAILED interface jobs should be counted';
    ASSERT v_row.tills_to_allocate_count = 2, 'Only Created and Failed auto-payment tills should be counted';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

DO $$
DECLARE
    v_row v_outstanding_auto_payment_count%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: Business unit with tills but no files returns zero file count ===';

    SELECT *
      INTO v_row
      FROM v_outstanding_auto_payment_count
     WHERE business_unit_id = 24202;

    ASSERT FOUND, 'Business unit 24202 should be returned by the view';
    ASSERT v_row.files_to_process_count = 0, 'File count should be zero when there are no interface jobs';
    ASSERT v_row.tills_to_allocate_count = 2, 'Two Created or Failed auto-payment tills should be counted';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

DO $$
DECLARE
    v_row v_outstanding_auto_payment_count%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 3: Business unit with files but no eligible tills returns zero till count ===';

    SELECT *
      INTO v_row
      FROM v_outstanding_auto_payment_count
     WHERE business_unit_id = 24203;

    ASSERT FOUND, 'Business unit 24203 should be returned by the view';
    ASSERT v_row.files_to_process_count = 2, 'Only CREATED and FAILED interface jobs should be counted';
    ASSERT v_row.tills_to_allocate_count = 0, 'Non-auto-payment and Allocated tills should not be counted';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

DO $$
DECLARE
    v_row v_outstanding_auto_payment_count%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 4: Business unit with no matching work is returned with zero counts ===';

    SELECT *
      INTO v_row
      FROM v_outstanding_auto_payment_count
     WHERE business_unit_id = 24204;

    ASSERT FOUND, 'Business unit 24204 should be returned by the view';
    ASSERT v_row.files_to_process_count = 0, 'File count should default to zero';
    ASSERT v_row.tills_to_allocate_count = 0, 'Till count should default to zero';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

DO $$
DECLARE
    v_row v_outstanding_auto_payment_count%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 5: Negative scenario excludes non-outstanding jobs and ineligible tills ===';

    SELECT *
      INTO v_row
      FROM v_outstanding_auto_payment_count
     WHERE business_unit_id = 24205;

    ASSERT FOUND, 'Business unit 24205 should be returned by the view';
    ASSERT v_row.files_to_process_count = 0, 'PROCESSING and IGNORED interface jobs should not be counted';
    ASSERT v_row.tills_to_allocate_count = 0, 'Non-auto-payment, Processing, and Allocated tills should not be counted';

    RAISE NOTICE 'TEST 5 PASSED';
END $$;

DO $$
DECLARE
    v_test_business_unit_rows bigint;
    v_null_business_unit_rows bigint;
BEGIN
    RAISE NOTICE '=== TEST 6: View is anchored on business_units only ===';

    SELECT COUNT(*)
      INTO v_test_business_unit_rows
      FROM v_outstanding_auto_payment_count
     WHERE business_unit_id BETWEEN 24201 AND 24205;

    SELECT COUNT(*)
      INTO v_null_business_unit_rows
      FROM v_outstanding_auto_payment_count
     WHERE business_unit_id IS NULL;

    ASSERT v_test_business_unit_rows = 5, 'All five test business units should be returned';
    ASSERT v_null_business_unit_rows = 0, 'Interface jobs without a business unit should not create a view row';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM tills
     WHERE business_unit_id BETWEEN 24201 AND 24205;

    DELETE FROM interface_jobs
     WHERE interface_job_id IN (
        2420101, 2420102, 2420103, 2420104, 2420105,
        2420301, 2420302,
        2420501, 2420502,
        2429999
    );

    DELETE FROM business_units
     WHERE business_unit_id BETWEEN 24201 AND 24205;

    RAISE NOTICE 'Cleanup completed';
END $$;
