/**
* OPAL Program
*
* MODULE      : create_view_v_interface_jobs_processed_file_summary_unit_tests.sql
*
* DESCRIPTION : Unit tests for v_interface_jobs_processed_file_summary view.
*               Tests verify the processed interface file summary values returned by the view.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 14/08/2026    C Cho       1.0         PO-2615 Unit tests for v_interface_jobs_processed_file_summary view
*
**/
\timing

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM interface_messages
     WHERE interface_file_id IN (310001, 310002, 310003, 310004, 310005, 310006)
        OR interface_job_id IN (310001, 310002, 310003, 310004, 310005);

    DELETE FROM tills
     WHERE till_id IN (310001, 310002, 310003)
        OR interface_file_id IN (310001, 310002, 310003, 310004, 310005, 310006);

    DELETE FROM interface_files
     WHERE interface_file_id IN (310001, 310002, 310003, 310004, 310005, 310006)
        OR interface_job_id IN (310001, 310002, 310003, 310004, 310005);

    DELETE FROM interface_jobs
     WHERE interface_job_id IN (310001, 310002, 310003, 310004, 310005);

    DELETE FROM business_units
     WHERE business_unit_id IN (3100, 3101);

    COMMIT;

    RAISE NOTICE 'Cleanup completed';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Setting up test data for v_interface_jobs_processed_file_summary tests ===';

    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_code,
        business_unit_type
    ) VALUES
    (
        3100,
        'V1 310 Test Business Unit A',
        'T310',
        'Area'
    ),
    (
        3101,
        'V1 310 Test Business Unit B',
        'U310',
        'Accounting Division'
    );

    INSERT INTO interface_jobs (
        interface_job_id,
        business_unit_id,
        interface_name,
        status,
        created_datetime,
        completed_datetime
    ) VALUES
    (
        310001,
        3100,
        'Auto Payments In',
        'COMPLETED',
        '2026-08-01 09:00:00',
        '2026-08-01 09:30:00'
    ),
    (
        310002,
        3101,
        'Auto Payments In',
        'COMPLETED',
        '2026-08-01 10:00:00',
        '2026-08-01 10:25:00'
    ),
    (
        310003,
        3100,
        'Auto Payments In',
        'FAILED',
        '2026-08-01 11:00:00',
        '2026-08-01 11:10:00'
    ),
    (
        310004,
        3100,
        'Auto Payments In',
        'CREATED',
        '2026-08-01 12:00:00',
        NULL
    ),
    (
        310005,
        3101,
        'Manual Payments In',
        'COMPLETED',
        '2026-08-01 13:00:00',
        '2026-08-01 13:15:00'
    );

    INSERT INTO interface_files (
        interface_file_id,
        interface_job_id,
        file_name,
        created_datetime,
        records,
        source,
        record_count,
        total_amount
    ) VALUES
    (
        310001,
        310001,
        'v1-310-natwest.dat',
        '2026-08-01 09:01:00',
        '[]',
        'NATWEST'::t_interface_file_source_enum,
        12,
        1234.56
    ),
    (
        310002,
        310001,
        'v1-310-allpay.dat',
        '2026-08-01 09:02:00',
        '[]',
        'ALLPAY'::t_interface_file_source_enum,
        8,
        80.00
    ),
    (
        310003,
        310002,
        'v1-310-no-till.dat',
        '2026-08-01 10:01:00',
        '[]',
        'DWP'::t_interface_file_source_enum,
        0,
        0.00
    ),
    (
        310004,
        310003,
        'v1-310-failed-job.dat',
        '2026-08-01 11:01:00',
        '[]',
        'BARCLAYCARD'::t_interface_file_source_enum,
        99,
        999.99
    ),
    (
        310005,
        310004,
        'v1-310-created-job.dat',
        '2026-08-01 12:01:00',
        '[]',
        'ALLPAY_DD'::t_interface_file_source_enum,
        77,
        777.77
    ),
    (
        310006,
        310005,
        'v1-310-manual-completed.dat',
        '2026-08-01 13:01:00',
        '[]',
        'OTHER'::t_interface_file_source_enum,
        6,
        66.66
    );

    INSERT INTO tills (
        till_id,
        business_unit_id,
        till_number,
        owned_by,
        source,
        status,
        total_amount,
        interface_file_id,
        payments_count,
        owned_by_name,
        auto_payment,
        created_date
    ) VALUES
    (
        310001,
        3100,
        4310,
        'TST310A',
        'NATWEST'::t_interface_file_source_enum,
        'Allocated'::t_till_status_enum,
        1234.56,
        310001,
        12,
        'V1 310 Till Owner A',
        TRUE,
        '2026-08-01 09:03:00'
    ),
    (
        310002,
        3101,
        4311,
        'TST310B',
        'OTHER'::t_interface_file_source_enum,
        'Allocated'::t_till_status_enum,
        66.66,
        310006,
        6,
        'V1 310 Till Owner B',
        FALSE,
        '2026-08-01 13:03:00'
    ),
    (
        310003,
        3100,
        4312,
        'TST310C',
        'BARCLAYCARD'::t_interface_file_source_enum,
        'Failed'::t_till_status_enum,
        999.99,
        310004,
        99,
        'V1 310 Till Owner C',
        TRUE,
        '2026-08-01 11:03:00'
    );

    INSERT INTO interface_messages (
        interface_message_id,
        interface_job_id,
        interface_file_id,
        message_type,
        message_text,
        record_index,
        record_detail
    ) VALUES
    (
        310001,
        310001,
        310001,
        'Exception',
        'Rejected account',
        1,
        'exception record'
    ),
    (
        310002,
        310001,
        310001,
        'Warning',
        'Inhibit overridden',
        2,
        'warning record'
    ),
    (
        310003,
        310001,
        310001,
        'Info',
        'Accepted payment',
        3,
        'info record'
    ),
    (
        310004,
        310001,
        310001,
        'Error',
        'Technical error',
        4,
        'error record'
    ),
    (
        310005,
        310001,
        310002,
        'Info',
        'File processed',
        1,
        'info only file'
    ),
    (
        310006,
        310003,
        310004,
        'Exception',
        'Failed job exception',
        1,
        'failed job'
    ),
    (
        310007,
        310004,
        310005,
        'Warning',
        'Created job warning',
        1,
        'created job'
    ),
    (
        310008,
        310005,
        310006,
        'Warning',
        'Manual completed warning',
        1,
        'manual job'
    );

    COMMIT;

    RAISE NOTICE 'Test data setup completed';
END $$;

DO $$
DECLARE
    v_summary v_interface_jobs_processed_file_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: Completed file with till and mixed message types returns expected values ===';

    SELECT *
      INTO STRICT v_summary
      FROM v_interface_jobs_processed_file_summary
     WHERE interface_file_id = 310001;

    ASSERT v_summary.interface_file_id = 310001, 'interface_file_id should match';
    ASSERT v_summary.interface_file_name = 'v1-310-natwest.dat', 'interface file name should match';
    ASSERT v_summary.interface_job_id = 310001, 'interface_job_id should match';
    ASSERT v_summary.source::TEXT = 'NATWEST', 'source should match';
    ASSERT v_summary.business_unit_id = 3100, 'business_unit_id should come from the till';
    ASSERT v_summary.business_unit_name = 'V1 310 Test Business Unit A', 'business_unit_name should come from the till business unit';
    ASSERT v_summary.total_amount = 1234.56, 'total_amount should match the interface file';
    ASSERT v_summary.total_records = 12, 'total_records should match interface_files.record_count';
    ASSERT v_summary.total_errors = 2, 'total_errors should count Exception and Warning messages only';
    ASSERT v_summary.till_id = 310001, 'till_id should match';
    ASSERT v_summary.till_number = 4310, 'till_number should match';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

DO $$
DECLARE
    v_summary v_interface_jobs_processed_file_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: Completed file with only Info messages returns zero total errors ===';

    SELECT *
      INTO STRICT v_summary
      FROM v_interface_jobs_processed_file_summary
     WHERE interface_file_id = 310002;

    ASSERT v_summary.interface_file_name = 'v1-310-allpay.dat', 'interface file name should match';
    ASSERT v_summary.source::TEXT = 'ALLPAY', 'source should match';
    ASSERT v_summary.total_amount = 80.00, 'total_amount should match';
    ASSERT v_summary.total_records = 8, 'total_records should match';
    ASSERT v_summary.total_errors = 0, 'Info messages should not be counted as total errors';
    ASSERT v_summary.till_id IS NULL, 'till_id should be NULL when no till exists for the file';
    ASSERT v_summary.till_number IS NULL, 'till_number should be NULL when no till exists for the file';
    ASSERT v_summary.business_unit_id IS NULL, 'business_unit_id should be NULL when no till exists for the file';
    ASSERT v_summary.business_unit_name IS NULL, 'business_unit_name should be NULL when no till exists for the file';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

DO $$
DECLARE
    v_summary v_interface_jobs_processed_file_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 3: Completed file with no messages and no till returns summary with null join columns ===';

    SELECT *
      INTO STRICT v_summary
      FROM v_interface_jobs_processed_file_summary
     WHERE interface_file_id = 310003;

    ASSERT v_summary.interface_job_id = 310002, 'interface_job_id should match';
    ASSERT v_summary.interface_file_name = 'v1-310-no-till.dat', 'interface file name should match';
    ASSERT v_summary.source::TEXT = 'DWP', 'source should match';
    ASSERT v_summary.total_amount = 0.00, 'total_amount should allow zero values';
    ASSERT v_summary.total_records = 0, 'total_records should allow zero values';
    ASSERT v_summary.total_errors = 0, 'total_errors should default to zero when there are no messages';
    ASSERT v_summary.business_unit_id IS NULL, 'business_unit_id should be NULL without a till';
    ASSERT v_summary.business_unit_name IS NULL, 'business_unit_name should be NULL without a till';
    ASSERT v_summary.till_id IS NULL, 'till_id should be NULL without a till';
    ASSERT v_summary.till_number IS NULL, 'till_number should be NULL without a till';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

DO $$
DECLARE
    v_summary v_interface_jobs_processed_file_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 4: Completed manual interface file is included and populated from its till ===';

    SELECT *
      INTO STRICT v_summary
      FROM v_interface_jobs_processed_file_summary
     WHERE interface_file_id = 310006;

    ASSERT v_summary.interface_job_id = 310005, 'completed manual job interface_job_id should match';
    ASSERT v_summary.interface_file_name = 'v1-310-manual-completed.dat', 'interface file name should match';
    ASSERT v_summary.source::TEXT = 'OTHER', 'source should match';
    ASSERT v_summary.business_unit_id = 3101, 'business_unit_id should come from the till';
    ASSERT v_summary.business_unit_name = 'V1 310 Test Business Unit B', 'business_unit_name should come from the till business unit';
    ASSERT v_summary.total_amount = 66.66, 'total_amount should match';
    ASSERT v_summary.total_records = 6, 'total_records should match';
    ASSERT v_summary.total_errors = 1, 'Warning messages should be counted';
    ASSERT v_summary.till_id = 310002, 'till_id should match';
    ASSERT v_summary.till_number = 4311, 'till_number should match';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

DO $$
DECLARE
    v_row_count integer;
BEGIN
    RAISE NOTICE '=== TEST 5: Negative test - non-completed jobs are excluded from the view ===';

    SELECT COUNT(*)
      INTO v_row_count
      FROM v_interface_jobs_processed_file_summary
     WHERE interface_file_id IN (310004, 310005);

    ASSERT v_row_count = 0, 'FAILED and CREATED jobs should not be returned by the view';

    RAISE NOTICE 'TEST 5 PASSED';
END $$;

DO $$
DECLARE
    v_row_count integer;
BEGIN
    RAISE NOTICE '=== TEST 6: Completed jobs return one row per interface file ===';

    SELECT COUNT(*)
      INTO v_row_count
      FROM v_interface_jobs_processed_file_summary
     WHERE interface_job_id = 310001;

    ASSERT v_row_count = 2, 'Completed job 310001 should return both associated interface files';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data after tests ===';

    DELETE FROM interface_messages
     WHERE interface_file_id IN (310001, 310002, 310003, 310004, 310005, 310006)
        OR interface_job_id IN (310001, 310002, 310003, 310004, 310005);

    DELETE FROM tills
     WHERE till_id IN (310001, 310002, 310003)
        OR interface_file_id IN (310001, 310002, 310003, 310004, 310005, 310006);

    DELETE FROM interface_files
     WHERE interface_file_id IN (310001, 310002, 310003, 310004, 310005, 310006)
        OR interface_job_id IN (310001, 310002, 310003, 310004, 310005);

    DELETE FROM interface_jobs
     WHERE interface_job_id IN (310001, 310002, 310003, 310004, 310005);

    DELETE FROM business_units
     WHERE business_unit_id IN (3100, 3101);

    COMMIT;

    RAISE NOTICE 'Cleanup completed';
END $$;
