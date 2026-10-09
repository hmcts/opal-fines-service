/**
* OPAL Program
*
* MODULE      : p_insert_note_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_note.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 05/10/2026    C Cho       1.0         PO-3472 Unit tests for p_insert_note
*
**/
\timing

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM notes
     WHERE posted_by = 'P3472UT';

    COMMIT;

    RAISE NOTICE 'Cleanup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Insert a note with the supplied values and the fixed AA note type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_posted_before  TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_note           notes%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: Insert a note ===';

    CALL p_insert_note(
        pi_associated_record_type := 'suspense_items'::t_associated_record_type_enum,
        pi_associated_record_id := 'P3472-UNIT-TEST-1'::VARCHAR,
        pi_note_text := 'Suspense item unit test note'::TEXT,
        pi_posted_by := 'P3472UT'::VARCHAR,
        pi_posted_by_name := 'PO-3472 Unit Test'::VARCHAR
    );

    SELECT *
      INTO v_note
      FROM notes
     WHERE associated_record_type = 'suspense_items'::t_associated_record_type_enum
       AND associated_record_id = 'P3472-UNIT-TEST-1'
       AND posted_by = 'P3472UT';

    ASSERT v_note.note_id IS NOT NULL, 'A note should have been inserted';
    ASSERT v_note.note_type = 'AA'::t_note_type_enum, 'note_type should be AA';
    ASSERT v_note.associated_record_type = 'suspense_items'::t_associated_record_type_enum,
        'associated_record_type should match';
    ASSERT v_note.associated_record_id = 'P3472-UNIT-TEST-1', 'associated_record_id should match';
    ASSERT v_note.note_text = 'Suspense item unit test note', 'note_text should match';
    ASSERT v_note.posted_by = 'P3472UT', 'posted_by should match';
    ASSERT v_note.posted_by_name = 'PO-3472 Unit Test', 'posted_by_name should match';
    ASSERT v_note.posted_date >= v_posted_before, 'posted_date should be on or after the call start time';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Reject a NULL associated record type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
BEGIN
    RAISE NOTICE '=== TEST 2: Reject a NULL associated record type ===';

    BEGIN
        CALL p_insert_note(
            pi_associated_record_type := NULL::t_associated_record_type_enum,
            pi_associated_record_id := 'P3472-UNIT-TEST-2'::VARCHAR,
            pi_note_text := 'This note should not be inserted'::TEXT,
            pi_posted_by := 'P3472UT'::VARCHAR,
            pi_posted_by_name := 'PO-3472 Unit Test'::VARCHAR
        );
    EXCEPTION
        WHEN SQLSTATE 'P3306' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_associated_record_type must be provided',
                'P3306 should have the expected error message';
            ASSERT v_exception_detail = 'p_insert_note: pi_associated_record_type is required',
                'P3306 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3306 error should have been raised for a NULL associated record type';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Reject a NULL associated record ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
BEGIN
    RAISE NOTICE '=== TEST 3: Reject a NULL associated record ID ===';

    BEGIN
        CALL p_insert_note(
            pi_associated_record_type := 'suspense_items'::t_associated_record_type_enum,
            pi_associated_record_id := NULL::VARCHAR,
            pi_note_text := 'This note should not be inserted'::TEXT,
            pi_posted_by := 'P3472UT'::VARCHAR,
            pi_posted_by_name := 'PO-3472 Unit Test'::VARCHAR
        );
    EXCEPTION
        WHEN SQLSTATE 'P3307' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_associated_record_id must be provided',
                'P3307 should have the expected error message';
            ASSERT v_exception_detail = 'p_insert_note: pi_associated_record_id is required',
                'P3307 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3307 error should have been raised for a NULL associated record ID';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Reject empty note text
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
BEGIN
    RAISE NOTICE '=== TEST 4: Reject empty note text ===';

    BEGIN
        CALL p_insert_note(
            pi_associated_record_type := 'suspense_items'::t_associated_record_type_enum,
            pi_associated_record_id := 'P3472-UNIT-TEST-4'::VARCHAR,
            pi_note_text := ''::TEXT,
            pi_posted_by := 'P3472UT'::VARCHAR,
            pi_posted_by_name := 'PO-3472 Unit Test'::VARCHAR
        );
    EXCEPTION
        WHEN SQLSTATE 'P3308' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_note_text must be provided',
                'P3308 should have the expected error message';
            ASSERT v_exception_detail = 'p_insert_note: pi_note_text is required',
                'P3308 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3308 error should have been raised for empty note text';
    ASSERT (
        SELECT COUNT(*)
          FROM notes
         WHERE posted_by = 'P3472UT'
    ) = 1, 'Failed calls should not insert notes';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM notes
     WHERE posted_by = 'P3472UT';

    COMMIT;

    RAISE NOTICE 'Test data cleanup completed';
END $$;
