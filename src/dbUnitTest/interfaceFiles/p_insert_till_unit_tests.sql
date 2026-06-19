/**
* OPAL Program
*
* MODULE      : p_insert_till_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_till.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 18/06/2026    C Cho       1.0         PO-2617 Unit tests for p_insert_till SP
*
**/
\timing

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM tills WHERE interface_file_id IN (990001, 990002);
    DELETE FROM interface_files WHERE interface_file_id IN (990001, 990002);
    DELETE FROM interface_jobs WHERE interface_job_id = 990001;

    COMMIT;

    RAISE NOTICE 'Cleanup completed';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Setting up test data ===';

    INSERT INTO interface_jobs (
        interface_job_id,
        business_unit_id,
        interface_name
    ) VALUES (
        990001,
        65,
        'p_insert_till_unit_tests'
    );

    INSERT INTO interface_files (
        interface_file_id,
        interface_job_id,
        file_name,
        records
    ) VALUES
    (
        990001,
        990001,
        'p_insert_till_unit_test_file_1.json',
        '[]'
    ),
    (
        990002,
        990001,
        'p_insert_till_unit_test_file_2.json',
        '[]'
    );

    COMMIT;

    RAISE NOTICE 'Test data setup completed';
END $$;

DO $$
DECLARE
    v_till_id            tills.till_id%TYPE;
    v_till_number        tills.till_number%TYPE;
    v_sequence_value      bigint;
    v_created_before     timestamp without time zone := CURRENT_TIMESTAMP;
    v_till               tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: p_insert_till populates till metadata ===';

    CALL p_insert_till(
        v_till_id,
        v_till_number,
        65::smallint,
        'NATWEST'::t_interface_file_source_enum,
        990001::bigint,
        'L065UT'::character varying,
        'Till Unit Test 1'::character varying
    );

    SELECT last_value
      INTO v_sequence_value
      FROM till_number_65_seq;

    SELECT *
      INTO v_till
      FROM tills
     WHERE till_id = v_till_id;

    ASSERT v_till_id IS NOT NULL, 'Till ID should be returned';
    ASSERT v_till_number IS NOT NULL, 'Till number should be returned';
    ASSERT v_till_number = v_sequence_value, 'Till number should match the current value of till_number_65_seq';
    ASSERT v_till.till_id = v_till_id, 'Inserted till_id should match the returned till_id';
    ASSERT v_till.till_number = v_till_number, 'Inserted till_number should match the returned till_number';
    ASSERT v_till.business_unit_id = 65, 'business_unit_id should be 65';
    ASSERT v_till.owned_by = 'L065UT', 'owned_by should match';
    ASSERT v_till.owned_by_name = 'Till Unit Test 1', 'owned_by_name should match';
    ASSERT v_till.status::TEXT = 'Created', 'status should be Created';
    ASSERT v_till.source::TEXT = 'NATWEST', 'source should match';
    ASSERT v_till.interface_file_id = 990001, 'interface_file_id should match';
    ASSERT v_till.total_amount IS NULL, 'total_amount should not be populated by p_insert_till';
    ASSERT v_till.payments_count IS NULL, 'payments_count should not be populated by p_insert_till';
    ASSERT v_till.auto_payment = TRUE, 'auto_payment should be TRUE';
    ASSERT v_till.created_date IS NOT NULL, 'created_date should be set';
    ASSERT v_till.created_date >= v_created_before, 'created_date should be on or after the call start time';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

DO $$
DECLARE
    v_till_id_1          tills.till_id%TYPE;
    v_till_number_1      tills.till_number%TYPE;
    v_till_id_2          tills.till_id%TYPE;
    v_till_number_2      tills.till_number%TYPE;
    v_sequence_value_1    bigint;
    v_sequence_value_2    bigint;
    v_till_2             tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: p_insert_till allocates the next till number for the same business unit ===';

    CALL p_insert_till(
        v_till_id_1,
        v_till_number_1,
        65::smallint,
        'NATWEST'::t_interface_file_source_enum,
        990001::bigint,
        'L065U1'::character varying,
        'Till Unit Test 2A'::character varying
    );

    SELECT last_value
      INTO v_sequence_value_1
      FROM till_number_65_seq;

    CALL p_insert_till(
        v_till_id_2,
        v_till_number_2,
        65::smallint,
        'ALLPAY'::t_interface_file_source_enum,
        990002::bigint,
        'L065U2'::character varying,
        'Till Unit Test 2B'::character varying
    );

    SELECT last_value
      INTO v_sequence_value_2
      FROM till_number_65_seq;

    SELECT *
      INTO v_till_2
      FROM tills
     WHERE till_id = v_till_id_2;

    ASSERT v_till_id_1 <> v_till_id_2, 'Each call should create a distinct till';
    ASSERT v_till_number_1 = v_sequence_value_1, 'First till number should match till_number_65_seq after the first call';
    ASSERT v_till_number_2 = v_sequence_value_2, 'Second till number should match till_number_65_seq after the second call';
    ASSERT v_sequence_value_2 = v_sequence_value_1 + 1, 'till_number_65_seq should increment by 1 between calls';
    ASSERT v_till_2.source::TEXT = 'ALLPAY', 'Second till source should match';
    ASSERT v_till_2.interface_file_id = 990002, 'Second till interface_file_id should match';
    ASSERT v_till_2.owned_by = 'L065U2', 'Second till owned_by should match';
    ASSERT v_till_2.owned_by_name = 'Till Unit Test 2B', 'Second till owned_by_name should match';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM tills WHERE interface_file_id IN (990001, 990002);
    DELETE FROM interface_files WHERE interface_file_id IN (990001, 990002);
    DELETE FROM interface_jobs WHERE interface_job_id = 990001;

    COMMIT;

    RAISE NOTICE 'Test data cleanup completed';
END $$;
