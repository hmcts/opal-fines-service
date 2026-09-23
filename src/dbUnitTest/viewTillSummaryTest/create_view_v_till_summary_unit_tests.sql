/**
* OPAL Program
*
* MODULE      : create_view_v_till_summary_unit_tests.sql
*
* DESCRIPTION : Unit tests for v_till_summary view.
*               Tests verify till summary values, file joins, and error counts returned by the view.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 17/08/2026    C Cho       1.0         PO-2594 Unit tests for v_till_summary view
*
**/
\timing

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM interface_messages
     WHERE interface_message_id BETWEEN 315001 AND 315009
        OR interface_file_id IN (315001, 315002, 315003, 315004)
        OR interface_job_id IN (315001, 315002, 315003);

    DELETE FROM tills
     WHERE till_id IN (315001, 315002, 315003, 315004)
        OR interface_file_id IN (315001, 315002, 315003, 315004);

    DELETE FROM interface_files
     WHERE interface_file_id IN (315001, 315002, 315003, 315004)
        OR interface_job_id IN (315001, 315002, 315003);

    DELETE FROM interface_jobs
     WHERE interface_job_id IN (315001, 315002, 315003);

    DELETE FROM business_units
     WHERE business_unit_id IN (3150, 3151);

    COMMIT;

    RAISE NOTICE 'Cleanup completed';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Setting up test data for v_till_summary tests ===';

    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_code,
        business_unit_type
    ) VALUES
    (
        3150,
        'V1 315 Test Business Unit A',
        'T315',
        'Area'
    ),
    (
        3151,
        'V1 315 Test Business Unit B',
        'U315',
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
        315001,
        3150,
        'Auto Payments In',
        'COMPLETED',
        '2026-08-02 09:00:00',
        '2026-08-02 09:30:00'
    ),
    (
        315002,
        3151,
        'Auto Payments In',
        'COMPLETED',
        '2026-08-02 10:00:00',
        '2026-08-02 10:20:00'
    ),
    (
        315003,
        3150,
        'Manual Payments In',
        'FAILED',
        '2026-08-02 11:00:00',
        '2026-08-02 11:10:00'
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
        315001,
        315001,
        'v1-315-natwest.dat',
        '2026-08-02 09:01:00',
        '[]',
        'ALLPAY'::t_interface_file_source_enum,
        99,
        9999.99
    ),
    (
        315002,
        315001,
        'v1-315-allpay-dd.dat',
        '2026-08-02 09:02:00',
        '[]',
        'ALLPAY_DD'::t_interface_file_source_enum,
        4,
        44.44
    ),
    (
        315003,
        315002,
        'v1-315-orphan-file.dat',
        '2026-08-02 10:01:00',
        '[]',
        'DWP'::t_interface_file_source_enum,
        8,
        88.88
    ),
    (
        315004,
        315003,
        'v1-315-failed-till.dat',
        '2026-08-02 11:01:00',
        '[]',
        'BARCLAYCARD'::t_interface_file_source_enum,
        3,
        33.33
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
        315001,
        3150,
        5310,
        'TST315A',
        'NATWEST'::t_interface_file_source_enum,
        'Allocated'::t_till_status_enum,
        1234.56,
        315001,
        12,
        'V1 315 Till Owner A',
        TRUE,
        '2026-08-02 09:03:00'
    ),
    (
        315002,
        3151,
        5311,
        'TST315B',
        'OTHER'::t_interface_file_source_enum,
        'Created'::t_till_status_enum,
        0.00,
        NULL,
        0,
        'V1 315 Till Owner B',
        FALSE,
        '2026-08-02 10:03:00'
    ),
    (
        315003,
        3151,
        5312,
        NULL,
        'ALLPAY_DD'::t_interface_file_source_enum,
        'Processing'::t_till_status_enum,
        NULL,
        315002,
        NULL,
        NULL,
        TRUE,
        '2026-08-02 10:13:00'
    ),
    (
        315004,
        3150,
        5313,
        'TST315D',
        'BARCLAYCARD'::t_interface_file_source_enum,
        'Failed'::t_till_status_enum,
        -12.34,
        315004,
        3,
        'V1 315 Till Owner D',
        FALSE,
        '2026-08-02 11:03:00'
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
        315001,
        315001,
        315001,
        'Exception',
        'Rejected payment',
        1,
        'exception record'
    ),
    (
        315002,
        315001,
        315001,
        'Warning',
        'Inhibit overridden',
        2,
        'warning record'
    ),
    (
        315003,
        315001,
        315001,
        'Info',
        'Accepted payment',
        3,
        'info record'
    ),
    (
        315004,
        315001,
        315001,
        'Error',
        'Technical error',
        4,
        'error record'
    ),
    (
        315005,
        315001,
        315002,
        'Warning',
        'Direct debit warning',
        1,
        'warning record'
    ),
    (
        315006,
        315002,
        315003,
        'Exception',
        'Orphan file exception',
        1,
        'orphan file'
    ),
    (
        315007,
        315003,
        315004,
        'Error',
        'Failed till technical error',
        1,
        'failed till error'
    ),
    (
        315008,
        315003,
        315004,
        'Info',
        'Failed till info',
        2,
        'failed till info'
    ),
    (
        315009,
        315001,
        NULL,
        'Warning',
        'Job level warning without file',
        NULL,
        'job warning'
    );

    COMMIT;

    RAISE NOTICE 'Test data setup completed';
END $$;

DO $$
DECLARE
    v_summary v_till_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: Till linked to file returns expected values and counts only warning and exception messages ===';

    SELECT *
      INTO STRICT v_summary
      FROM v_till_summary
     WHERE till_id = 315001;

    ASSERT v_summary.till_id = 315001, 'till_id should match';
    ASSERT v_summary.till_number = 5310, 'till_number should match';
    ASSERT v_summary.errors = 2, 'errors should count Exception and Warning messages only';
    ASSERT v_summary.interface_file_id = 315001, 'interface_file_id should match';
    ASSERT v_summary.file_name = 'v1-315-natwest.dat', 'file_name should come from interface_files';
    ASSERT v_summary.source::TEXT = 'NATWEST', 'source should come from tills, not interface_files';
    ASSERT v_summary.amount = 1234.56, 'amount should come from tills.total_amount';
    ASSERT v_summary.business_unit_id = 3150, 'business_unit_id should match';
    ASSERT v_summary.business_unit_name = 'V1 315 Test Business Unit A', 'business_unit_name should match';
    ASSERT v_summary.processed_by = 'TST315A', 'processed_by should come from tills.owned_by';
    ASSERT v_summary.date_processed = '2026-08-02 09:03:00', 'date_processed should match tills.created_date';
    ASSERT v_summary.auto_payment IS TRUE, 'auto_payment should match';
    ASSERT v_summary.status::TEXT = 'Allocated', 'status should match';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

DO $$
DECLARE
    v_summary v_till_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: Till without interface file is returned with null file name and zero errors ===';

    SELECT *
      INTO STRICT v_summary
      FROM v_till_summary
     WHERE till_id = 315002;

    ASSERT v_summary.till_id = 315002, 'till_id should match';
    ASSERT v_summary.till_number = 5311, 'till_number should match';
    ASSERT v_summary.errors = 0, 'errors should default to zero when interface_file_id is NULL';
    ASSERT v_summary.interface_file_id IS NULL, 'interface_file_id should be NULL';
    ASSERT v_summary.file_name IS NULL, 'file_name should be NULL when no interface file exists';
    ASSERT v_summary.source::TEXT = 'OTHER', 'source should match';
    ASSERT v_summary.amount = 0.00, 'amount should allow zero values';
    ASSERT v_summary.business_unit_id = 3151, 'business_unit_id should match';
    ASSERT v_summary.business_unit_name = 'V1 315 Test Business Unit B', 'business_unit_name should match';
    ASSERT v_summary.processed_by = 'TST315B', 'processed_by should match';
    ASSERT v_summary.date_processed = '2026-08-02 10:03:00', 'date_processed should match';
    ASSERT v_summary.auto_payment IS FALSE, 'auto_payment should match';
    ASSERT v_summary.status::TEXT = 'Created', 'status should match';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

DO $$
DECLARE
    v_summary v_till_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 3: Nullable till values are preserved when a file exists ===';

    SELECT *
      INTO STRICT v_summary
      FROM v_till_summary
     WHERE till_id = 315003;

    ASSERT v_summary.till_id = 315003, 'till_id should match';
    ASSERT v_summary.till_number = 5312, 'till_number should match';
    ASSERT v_summary.errors = 1, 'errors should count the warning linked to the file';
    ASSERT v_summary.interface_file_id = 315002, 'interface_file_id should match';
    ASSERT v_summary.file_name = 'v1-315-allpay-dd.dat', 'file_name should match';
    ASSERT v_summary.source::TEXT = 'ALLPAY_DD', 'source should match';
    ASSERT v_summary.amount IS NULL, 'amount should remain NULL when tills.total_amount is NULL';
    ASSERT v_summary.business_unit_id = 3151, 'business_unit_id should match';
    ASSERT v_summary.business_unit_name = 'V1 315 Test Business Unit B', 'business_unit_name should match';
    ASSERT v_summary.processed_by IS NULL, 'processed_by should remain NULL when tills.owned_by is NULL';
    ASSERT v_summary.date_processed = '2026-08-02 10:13:00', 'date_processed should match';
    ASSERT v_summary.auto_payment IS TRUE, 'auto_payment should match';
    ASSERT v_summary.status::TEXT = 'Processing', 'status should match';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

DO $$
DECLARE
    v_summary v_till_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 4: Failed till is included and Error or Info messages are not counted ===';

    SELECT *
      INTO STRICT v_summary
      FROM v_till_summary
     WHERE till_id = 315004;

    ASSERT v_summary.till_id = 315004, 'till_id should match';
    ASSERT v_summary.errors = 0, 'Error and Info messages should not be counted';
    ASSERT v_summary.file_name = 'v1-315-failed-till.dat', 'file_name should match';
    ASSERT v_summary.amount = -12.34, 'amount should preserve the value stored on the till';
    ASSERT v_summary.source::TEXT = 'BARCLAYCARD', 'source should match';
    ASSERT v_summary.business_unit_name = 'V1 315 Test Business Unit A', 'business_unit_name should match';
    ASSERT v_summary.processed_by = 'TST315D', 'processed_by should match';
    ASSERT v_summary.auto_payment IS FALSE, 'auto_payment should match';
    ASSERT v_summary.status::TEXT = 'Failed', 'status should match';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

DO $$
DECLARE
    v_row_count integer;
BEGIN
    RAISE NOTICE '=== TEST 5: Negative test - interface file without a till is excluded ===';

    SELECT COUNT(*)
      INTO v_row_count
      FROM v_till_summary
     WHERE interface_file_id = 315003;

    ASSERT v_row_count = 0, 'Interface files should not be returned unless a till references them';

    RAISE NOTICE 'TEST 5 PASSED';
END $$;

DO $$
DECLARE
    v_row_count integer;
    v_total_errors bigint;
BEGIN
    RAISE NOTICE '=== TEST 6: Negative test - unrelated job-level messages do not affect till rows ===';

    SELECT COUNT(*),
           SUM(errors)
      INTO v_row_count,
           v_total_errors
      FROM v_till_summary
     WHERE till_id IN (315001, 315002, 315003, 315004);

    ASSERT v_row_count = 4, 'Each test till should return exactly one row';
    ASSERT v_total_errors = 3, 'Only file-level Exception and Warning messages should contribute to errors';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data after tests ===';

    DELETE FROM interface_messages
     WHERE interface_message_id BETWEEN 315001 AND 315009
        OR interface_file_id IN (315001, 315002, 315003, 315004)
        OR interface_job_id IN (315001, 315002, 315003);

    DELETE FROM tills
     WHERE till_id IN (315001, 315002, 315003, 315004)
        OR interface_file_id IN (315001, 315002, 315003, 315004);

    DELETE FROM interface_files
     WHERE interface_file_id IN (315001, 315002, 315003, 315004)
        OR interface_job_id IN (315001, 315002, 315003);

    DELETE FROM interface_jobs
     WHERE interface_job_id IN (315001, 315002, 315003);

    DELETE FROM business_units
     WHERE business_unit_id IN (3150, 3151);

    COMMIT;

    RAISE NOTICE 'Cleanup completed';
END $$;
