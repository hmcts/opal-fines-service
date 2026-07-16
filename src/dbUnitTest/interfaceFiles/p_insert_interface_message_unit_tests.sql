/**
* OPAL Program
*
* MODULE      : p_insert_interface_message_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_interface_message.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 23/06/2026    C Cho       1.0         PO-2618 Unit tests for p_insert_interface_message SP
*
**/
\timing

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM interface_messages WHERE interface_job_id = 990003;
    DELETE FROM interface_files WHERE interface_file_id = 990003;
    DELETE FROM interface_jobs WHERE interface_job_id = 990003;

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
        990003,
        65,
        'p_insert_interface_message_unit_tests'
    );

    INSERT INTO interface_files (
        interface_file_id,
        interface_job_id,
        file_name,
        records
    ) VALUES (
        990003,
        990003,
        'p_insert_interface_message_unit_tests.json',
        '[]'
    );

    COMMIT;

    RAISE NOTICE 'Test data setup completed';
END $$;

DO $$
DECLARE
    v_message interface_messages%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: p_insert_interface_message stores required message values ===';

    CALL p_insert_interface_message(
        990003::bigint,
        'Info'::character varying,
        'required_only_message'::character varying,
        NULL::bigint,
        NULL::bigint,
        NULL::text,
        NULL::json
    );

    SELECT *
      INTO v_message
      FROM interface_messages
     WHERE interface_job_id = 990003
       AND message_text = 'required_only_message';

    ASSERT v_message.interface_message_id IS NOT NULL, 'interface_message_id should be populated';
    ASSERT v_message.interface_job_id = 990003, 'interface_job_id should match';
    ASSERT v_message.message_type = 'Info', 'message_type should match';
    ASSERT v_message.message_text = 'required_only_message', 'message_text should match';
    ASSERT v_message.interface_file_id IS NULL, 'interface_file_id should default to NULL';
    ASSERT v_message.record_index IS NULL, 'record_index should default to NULL';
    ASSERT v_message.record_detail IS NULL, 'record_detail should default to NULL';
    ASSERT v_message.message_data IS NULL, 'message_data should default to NULL';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

DO $$
DECLARE
    v_message interface_messages%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: p_insert_interface_message stores optional message values and message_data ===';

    CALL p_insert_interface_message(
        990003::bigint,
        'Warning'::character varying,
        'summary_key'::character varying,
        990003::bigint,
        12::bigint,
        'record detail'::text,
        json_build_object('count', 3, 'value', 12.34, 'display', 'Till summary')
    );

    SELECT *
      INTO v_message
      FROM interface_messages
     WHERE interface_job_id = 990003
       AND message_text = 'summary_key';

    ASSERT v_message.interface_message_id IS NOT NULL, 'interface_message_id should be populated';
    ASSERT v_message.interface_job_id = 990003, 'interface_job_id should match';
    ASSERT v_message.interface_file_id = 990003, 'interface_file_id should match';
    ASSERT v_message.record_index = 12, 'record_index should match';
    ASSERT v_message.record_detail = 'record detail', 'record_detail should match';
    ASSERT v_message.message_type = 'Warning', 'message_type should match';
    ASSERT v_message.message_text = 'summary_key', 'message_text should match';
    ASSERT (v_message.message_data->>'count')::integer = 3, 'message_data count should match';
    ASSERT (v_message.message_data->>'value')::numeric = 12.34, 'message_data value should match';
    ASSERT v_message.message_data->>'display' = 'Till summary', 'message_data display should match';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

DO $$
DECLARE
    v_message interface_messages%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 3: p_insert_interface_message truncates message_text to 500 characters ===';

    CALL p_insert_interface_message(
        990003::bigint,
        'Exception'::character varying,
        repeat('A', 600)::character varying,
        NULL::bigint,
        NULL::bigint,
        NULL::text,
        NULL::json
    );

    SELECT *
      INTO v_message
      FROM interface_messages
     WHERE interface_job_id = 990003
       AND message_type = 'Exception';

    ASSERT length(v_message.message_text) = 500, 'message_text should be truncated to 500 characters';
    ASSERT v_message.message_text = repeat('A', 500), 'message_text should contain the first 500 characters only';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM interface_messages WHERE interface_job_id = 990003;
    DELETE FROM interface_files WHERE interface_file_id = 990003;
    DELETE FROM interface_jobs WHERE interface_job_id = 990003;

    COMMIT;

    RAISE NOTICE 'Test data cleanup completed';
END $$;
