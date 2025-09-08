/**
* CGI OPAL Program
*
* MODULE      : p_create_account_notes_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_create_account_notes.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------
* 03/09/2025    TMc         1.0         Unit tests for the stored procedure p_create_account_notes
*                                       Scenarios:
*                                       1. Valid account_notes JSON: 
*                                          3 AA notes (notes not in serial order in JSON, they should be ordered and first 2 inserted),
*                                          1 AC note (da.account_comments should be updated correctly),
*                                          1 AN note (it should be ignored)
*                                       2. Valid account notes JSON:
*                                          2 AA notes (notes not in serial order in JSON, they should be ordered and both inserted), 
*                                          0 AC note (da.account_comments should be NULL),
*                                          1 AN note (it should be ignored)
*                                       3. Invalid account notes JSON:
*                                          An element has an invalid note_type (i.e. not AA, AC or AN) - Exception raised = 'P2012 - Note_type <note_type> is not valid'
*                                       4. Invalid account notes JSON:
*                                          More than one AC elements in the JSON array - Exception raised = 'P2013 - Only one AC note type is expected. Number of AC entries = <number of AC entries passed>'
*
**/
\timing

-- Clear out tables
DO $$
DECLARE

BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Delete all test data created by these tests
    DELETE FROM notes WHERE associated_record_id IN ('999901','999902');
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999901, 999902);
    DELETE FROM business_units WHERE business_unit_id = 9999;

    COMMIT;

    RAISE NOTICE 'Data cleanup before tests completed';
END $$;

DO $$
DECLARE
    v_business_unit_id        SMALLINT  := 9999;
    v_defendant_account_id_1  BIGINT    := 999901;
    v_defendant_account_id_2  BIGINT    := 999902; 
    v_account_number_1        VARCHAR   := '99000001A';
    v_account_number_2        VARCHAR   := '99000002A';
BEGIN
    RAISE NOTICE '=== Setting up test data ===';
    
    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_type
    ) VALUES (
        v_business_unit_id,
        'Test BU',
        'Accounting Division'
    );

    INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number, amount_imposed, amount_paid, account_balance, account_status, account_type, account_comments)
    VALUES ( 
            v_defendant_account_id_1
          , v_business_unit_id
          , v_account_number_1
          , 0
          , 0
          , 0
          , 'L'
          , 'Fine'
          , NULL
    ),
    (
            v_defendant_account_id_2
          , v_business_unit_id
          , v_account_number_2
          , 0
          , 0
          , 0
          , 'L'
          , 'Fine'
          , NULL
    );
    
    COMMIT;

    RAISE NOTICE 'Test data setup completed: defendant_account_id = % and %', v_defendant_account_id_1, v_defendant_account_id_2;
END $$;

--Test 1 - Valid account_notes JSON (3 AA notes (not in serial order), 1 AC note, 1 AN note)
DO $$
DECLARE
    v_account_notes_json    JSON;
    v_defendant_account_id  BIGINT   := 999901;
    v_posted_by             VARCHAR  := 'L045EO';
    v_posted_by_name        VARCHAR  := 'Test Notes SP - Test 1';
    v_record                RECORD;
BEGIN
    
    v_account_notes_json := '[
            {
                "account_note_serial": 3,
                "account_note_text": "AA note with serial 3 - Should be second record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 2,
                "account_note_text": "AA note with serial 2 - Should be first record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 4,
                "account_note_text": "AA note with serial 4 - Should NOT be in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 1,
                "account_note_text": "AC note with serial 1",
                "note_type": "AC"
            },
            {
                "account_note_serial": 5,
                "account_note_text": "AN note with serial 5 - Should be ignored",
                "note_type": "AN"
            }
    ]';
    
    RAISE NOTICE '=== TEST 1: Valid account_notes JSON (3 AA notes (not in serial order), 1 AC note, 1 AN note) ===';
    
    CALL p_create_account_notes(
            v_defendant_account_id,
            v_posted_by,
            v_posted_by_name,
            v_account_notes_json
    );

    --Verify that only 2 AA records exist in the Notes table
    ASSERT (SELECT COUNT(*) FROM notes WHERE associated_record_id = v_defendant_account_id::VARCHAR) = 2, 'There should only be 2 AA notes records';

    --Verify fields ensuring they are in the correct order
    FOR v_record IN 
        SELECT ROW_NUMBER() OVER(ORDER BY posted_date) AS row_num, * 
          FROM notes WHERE associated_record_id = v_defendant_account_id::VARCHAR
    LOOP

        CASE v_record.row_num 
            WHEN 1 THEN 
                ASSERT v_record.note_text = 'AA note with serial 2 - Should be first record in Notes', FORMAT('Row %s - note_text should match', v_record.row_num);
            WHEN 2 THEN 
                ASSERT v_record.note_text = 'AA note with serial 3 - Should be second record in Notes', FORMAT('Row %s - note_text should match', v_record.row_num);
        END CASE;

        ASSERT v_record.note_type              = 'AA', FORMAT('Row %s - note_type should be "AA"', v_record.row_num);
        ASSERT v_record.associated_record_type = 'defendant_accounts', FORMAT('Row %s - associated_record_type should be "defendant_accounts"', v_record.row_num);
        ASSERT v_record.posted_date            IS NOT NULL, FORMAT('Row %s - posted_date should be set', v_record.row_num);
        ASSERT v_record.posted_by              = v_posted_by, FORMAT('Row %s - posted_by should match', v_record.row_num);
        ASSERT v_record.posted_by_name         = v_posted_by_name, FORMAT('Row %s - posted_by_name should match', v_record.row_num);
 
    END LOOP;

    --Check that DEFENDANT_ACCOUNTS.ACCOUNT_COMMENTS was updated correctly
    ASSERT (SELECT account_comments FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'AC note with serial 1', 'Account comments should match';

    RAISE NOTICE 'TEST 1 PASSED';
    
END $$;

--Test 2 - Valid account_notes JSON (2 AA notes (not in serial order), 0 AC notes, 1 AN note)
DO $$
DECLARE
    v_account_notes_json    JSON;
    v_defendant_account_id  BIGINT   := 999902;
    v_posted_by             VARCHAR  := 'L045EO';
    v_posted_by_name        VARCHAR  := 'Test Notes SP - Test 2';
    v_record                RECORD;
BEGIN
    
    v_account_notes_json := '[
            {
                "account_note_serial": 3,
                "account_note_text": "AA note with serial 3 - Should be second record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 2,
                "account_note_text": "AA note with serial 2 - Should be first record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 1,
                "account_note_text": "AN note with serial 1 - Should be ignored",
                "note_type": "AN"
            }
    ]';
    
    RAISE NOTICE '=== TEST 2: Valid account_notes JSON (2 AA notes (not in serial order), 0 AC notes, 1 AN note) ===';
    
    CALL p_create_account_notes(
            v_defendant_account_id,
            v_posted_by,
            v_posted_by_name,
            v_account_notes_json
    );

    --Verify that only 2 AA records exist in the Notes table
    ASSERT (SELECT COUNT(*) FROM notes WHERE associated_record_id = v_defendant_account_id::VARCHAR) = 2, 'There should be 2 AA notes records';

    --Verify fields ensuring they are in the correct order
    FOR v_record IN 
        SELECT ROW_NUMBER() OVER(ORDER BY posted_date) AS row_num, * 
          FROM notes WHERE associated_record_id = v_defendant_account_id::VARCHAR
    LOOP

        CASE v_record.row_num 
            WHEN 1 THEN 
                ASSERT v_record.note_text = 'AA note with serial 2 - Should be first record in Notes', FORMAT('Row %s - note_text should match', v_record.row_num);
            WHEN 2 THEN 
                ASSERT v_record.note_text = 'AA note with serial 3 - Should be second record in Notes', FORMAT('Row %s - note_text should match', v_record.row_num);
        END CASE;

        ASSERT v_record.note_type              = 'AA', FORMAT('Row %s - note_type should be "AA"', v_record.row_num);
        ASSERT v_record.associated_record_type = 'defendant_accounts', FORMAT('Row %s - associated_record_type should be "defendant_accounts"', v_record.row_num);
        ASSERT v_record.posted_date            IS NOT NULL, FORMAT('Row %s - posted_date should be set', v_record.row_num);
        ASSERT v_record.posted_by              = v_posted_by, FORMAT('Row %s - posted_by should match', v_record.row_num);
        ASSERT v_record.posted_by_name         = v_posted_by_name, FORMAT('Row %s - posted_by_name should match', v_record.row_num);
 
    END LOOP;

    --Check that DEFENDANT_ACCOUNTS.ACCOUNT_COMMENTS was updated with a NULL value
    ASSERT (SELECT account_comments FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'Account comments should be NULL';
    
    RAISE NOTICE 'TEST 2 PASSED';
    
END $$;

--Test 3 - Invalid account notes JSON: An element has an invalid note_type (i.e. not AA, AC or AN) - Exception raised = 'P2012 - Note_type <note_type> is not valid'
DO $$
DECLARE
    v_account_notes_json        JSON;
    v_defendant_account_id      BIGINT   := 999903;
    v_posted_by                 VARCHAR  := 'L045EO';
    v_posted_by_name            VARCHAR  := 'Test Notes SP - Test 3';
    v_error_caught              BOOLEAN  := FALSE;
    v_expected_errmsg_pattern   VARCHAR  := 'Note_type % is not valid';
BEGIN
    
    v_account_notes_json := '[
            {
                "account_note_serial": 3,
                "account_note_text": "AA note with serial 3 - Should be second record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 1,
                "account_note_text": "AC note with serial 1",
                "note_type": "AC"
            },
            {
                "account_note_serial": 4,
                "account_note_text": "AN note with serial 4 - Should be ignored",
                "note_type": "AN"
            },
            {
                "account_note_serial": 2,
                "account_note_text": "Invalid note type",
                "note_type": "ZZ"
            }
    ]';
    
    RAISE NOTICE '=== TEST 3: Invalid account notes JSON: An element has an invalid note_type (i.e. not AA, AC or AN) ===';
    
    -- Call the procedure - should throw a P2012 exception
    BEGIN
        CALL p_create_account_notes(
            v_defendant_account_id,
            v_posted_by,
            v_posted_by_name,
            v_account_notes_json
        );
    EXCEPTION
        WHEN SQLSTATE 'P2012' THEN 

            IF SQLERRM LIKE v_expected_errmsg_pattern THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'A P2012 error, with correct SQLERRM, should have been raised when the passed Notes Json contains an invalid note_type';
    
    RAISE NOTICE 'TEST 3 PASSED';
    
END $$;

--Test 4 - Invalid account notes JSON: More than one AC elements in the JSON array - Exception raised = 'P2013 - Only one AC note type is expected. Number of AC entries = <number of AC entries passed>'
DO $$
DECLARE
    v_account_notes_json        JSON;
    v_defendant_account_id      BIGINT   := 999904;
    v_posted_by                 VARCHAR  := 'L045EO';
    v_posted_by_name            VARCHAR  := 'Test Notes SP - Test 4';
    v_error_caught              BOOLEAN  := FALSE;
    v_expected_errmsg_pattern   VARCHAR  := 'Only one AC note type is expected. Number of AC entries = %';
BEGIN
    
    v_account_notes_json := '[
            {
                "account_note_serial": 3,
                "account_note_text": "AA note with serial 3 - Should be second record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 1,
                "account_note_text": "AC note with serial 1",
                "note_type": "AC"
            },
            {
                "account_note_serial": 2,
                "account_note_text": "AC note with serial 2",
                "note_type": "AC"
            },
            {
                "account_note_serial": 4,
                "account_note_text": "AN note with serial 4 - Should be ignored",
                "note_type": "AN"
            }
    ]';
    
    RAISE NOTICE '=== TEST 4: Invalid account notes JSON: More than one AC elements in the JSON array ===';
    
    -- Call the procedure - should throw a P2013 exception
    BEGIN
        CALL p_create_account_notes(
            v_defendant_account_id,
            v_posted_by,
            v_posted_by_name,
            v_account_notes_json
        );
    EXCEPTION
        WHEN SQLSTATE 'P2013' THEN 

            IF SQLERRM LIKE v_expected_errmsg_pattern THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'A P2013 error, with correct SQLERRM, should have been raised when the passed Notes Json contains more than 1 AC entries';
    
    RAISE NOTICE 'TEST 4 PASSED';
    
END $$;

-- Cleanup test data
DO $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';
    
    -- Delete all test data created by these tests
    DELETE FROM notes WHERE associated_record_id IN ('999901','999902');
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999901, 999902);
    DELETE FROM business_units WHERE business_unit_id = 9999;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing