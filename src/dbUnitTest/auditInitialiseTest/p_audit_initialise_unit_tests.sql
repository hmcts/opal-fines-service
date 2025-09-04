\timing

/**
* CGI Opal Program
*
* MODULE      : p_audit_initialise_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_audit_initialise.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------
* 28/08/2025    C Cho       1.0         PO-1666 Unit tests for p_audit_initialise.
*
**/

-- Clear out test data before tests
DO LANGUAGE 'plpgsql' $$
DECLARE

BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Delete test data from related tables in correct order to avoid FK violations
    DELETE FROM aliases WHERE party_id IN (SELECT party_id FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent'));
    DELETE FROM debtor_detail WHERE party_id IN (SELECT party_id FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent'));
    DELETE FROM defendant_account_parties WHERE defendant_account_id IN (SELECT defendant_account_id FROM defendant_accounts WHERE account_number LIKE 'TEST%');
    DELETE FROM creditor_accounts WHERE account_number LIKE 'TEST%';
    DELETE FROM defendant_accounts WHERE account_number LIKE 'TEST%';
    DELETE FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent');

    -- Drop any existing temp tables from previous sessions
    DROP TABLE IF EXISTS temp_def_ac_amendment_list;
    DROP TABLE IF EXISTS temp_cred_ac_amendment_list;

    RAISE NOTICE 'Data cleanup before tests completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Test defendant_accounts record type with valid account ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_defendant_account_id       BIGINT := 90001;
    v_business_unit_id           SMALLINT := 65;
    v_party_id_def               BIGINT;
    v_party_id_pg                BIGINT;
    v_record_count               INTEGER;
    v_column_count               INTEGER;
    v_temp_table_exists          BOOLEAN;
BEGIN
    RAISE NOTICE '=== TEST 1: Test defendant_accounts record type with valid account ID ===';
    
    -- Setup test data - Create defendant party (satisfy all NOT NULL constraints from CSV)
    INSERT INTO parties (
        party_id, organisation, surname, forenames, title, 
        address_line_1, address_line_2, address_line_3, postcode,
        birth_date, national_insurance_number, telephone_home, 
        telephone_business, telephone_mobile, email_1, email_2
    ) VALUES (
        90001, FALSE, 'TestDefendant', 'John', 'Mr',
        '123 Test Street', 'Test Area', 'Test City', 'TE1 2ST',
        '1980-01-01'::DATE, 'AB123456C', '01234567890',
        '01234567891', '07123456789', 'john.test@example.com', 'j.test@example.com'
    ) RETURNING party_id INTO v_party_id_def;

    -- Create parent/guardian party
    INSERT INTO parties (
        party_id, organisation, surname, forenames, 
        address_line_1, address_line_2, address_line_3, postcode,
        birth_date, national_insurance_number
    ) VALUES (
        90002, FALSE, 'TestParent', 'Jane',
        '456 Parent Street', '', '', 'TE2 3ST',
        '1950-01-01'::DATE, 'CD123456E'
    ) RETURNING party_id INTO v_party_id_pg;

    -- Create defendant account
    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, account_type,
        imposed_hearing_date, amount_imposed, amount_paid, account_balance,
        account_status, cheque_clearance_period, allow_cheques,
        credit_trans_clearance_period, allow_writeoffs, enforcing_court_id,
        collection_order, suspended_committal_date, account_comments,
        account_note_1, account_note_2, account_note_3
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'TEST001', 'Fine',
        '2024-01-01'::DATE, 100.00, 50.00, 50.00,
        'L', 5, TRUE, 3, TRUE, 650000000045,
        TRUE, '2024-12-15'::DATE, 'Test account comments',
        'Test note 1', 'Test note 2', 'Test note 3'
    );

    -- Create defendant account parties associations
    INSERT INTO defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES 
        (90001, v_defendant_account_id, v_party_id_def, 'Defendant', FALSE),
        (90002, v_defendant_account_id, v_party_id_pg, 'Parent/Guardian', TRUE);

    -- Create debtor details
    INSERT INTO debtor_detail (
        party_id, vehicle_make, vehicle_registration, employer_name,
        employer_address_line_1, employer_address_line_2, employer_address_line_3,
        employer_address_line_4, employer_address_line_5, employer_postcode,
        employee_reference, employer_telephone, employer_email,
        document_language, document_language_date, hearing_language, hearing_language_date
    ) VALUES 
        (v_party_id_def, 'Honda', 'ABC123', 'Test Corp',
         '789 Business Rd', 'Floor 2', '', '', '', 'TE3 4ST',
         'EMP123', '01234567892', 'hr@testcorp.com',
         'EN', CURRENT_TIMESTAMP, 'EN', CURRENT_TIMESTAMP);

    -- Create aliases
    INSERT INTO aliases (alias_id, party_id, surname, forenames, sequence_number, organisation_name)
    VALUES 
        (90001, v_party_id_def, 'TestAlias1', 'Johnny', 1, ''),
        (90002, v_party_id_def, 'TestAlias2', 'Jon', 2, ''),
        (90003, v_party_id_pg, 'ParentAlias', 'Janie', 1, '');

    -- Call the procedure
    CALL p_audit_initialise(v_defendant_account_id, 'defendant_accounts');

    -- Verify the temporary table was created
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_name = 'temp_def_ac_amendment_list'
        AND table_type = 'LOCAL TEMPORARY'
    ) INTO v_temp_table_exists;
    
    ASSERT v_temp_table_exists = TRUE, 'Temporary table temp_def_ac_amendment_list should exist';

    -- Verify record count
    EXECUTE 'SELECT COUNT(*) FROM temp_def_ac_amendment_list' INTO v_record_count;
    ASSERT v_record_count = 1, 'Should have exactly 1 record in temp table';

    -- Verify specific field values are correctly populated
    PERFORM 1 FROM temp_def_ac_amendment_list 
    WHERE defendant_account_id = v_defendant_account_id
      AND name = 'Mr John TestDefendant'
      AND birth_date = '1980-01-01'::DATE
      AND address_line_1 = '123 Test Street'
      AND postcode = 'TE1 2ST'
      AND national_insurance_number = 'AB123456C'
      AND pname = 'Jane TestParent'
      AND paddr1 = '456 Parent Street'
      AND pninumber = 'CD123456E'
      AND alias1 = 'Johnny TestAlias1'
      AND alias2 = 'Jon TestAlias2'
      AND document_language = 'EN'
      AND vehicle_make = 'Honda'
      AND employer_name = 'Test Corp'
      AND cheque_clearance_period = 5
      AND allow_cheques = TRUE
      AND account_comments = 'Test account comments';

    ASSERT FOUND, 'Temp table should contain correct data from the view';

    RAISE NOTICE 'TEST 1 PASSED: Defendant accounts temporary table created successfully with correct data';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Test creditor_accounts record type with valid account ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_creditor_account_id        BIGINT := 90003;
    v_business_unit_id           SMALLINT := 65;
    v_party_id                   BIGINT;
    v_record_count               INTEGER;
    v_column_count               INTEGER;
    v_temp_table_exists          BOOLEAN;
BEGIN
    RAISE NOTICE '=== TEST 2: Test creditor_accounts record type with valid account ID ===';
    
    -- Setup test data
    INSERT INTO parties (
        party_id, organisation, surname, forenames, title, address_line_1, address_line_2, 
        address_line_3, postcode
    ) VALUES (
        90003, FALSE, 'TestCreditor', 'Jane', 'Ms', '789 Creditor St', 'Creditor Area', 
        'Creditor City', 'CR1 2ED'
    ) RETURNING party_id INTO v_party_id;

    -- Create creditor account
    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, 
        creditor_account_type, minor_creditor_party_id, prosecution_service,
        from_suspense, hold_payout, pay_by_bacs, bank_sort_code, 
        bank_account_type, bank_account_number, bank_account_name, bank_account_reference
    ) VALUES (
        v_creditor_account_id, v_business_unit_id, 'TESTCRED001',
        'MN', v_party_id, FALSE,
        FALSE, FALSE, TRUE, '123456',
        '1', '12345678', 'TestCredAcct', 'REF123'
    );

    -- Call the procedure
    CALL p_audit_initialise(v_creditor_account_id, 'creditor_accounts');

    -- Verify the temporary table was created
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_name = 'temp_cred_ac_amendment_list'
        AND table_type = 'LOCAL TEMPORARY'
    ) INTO v_temp_table_exists;
    
    ASSERT v_temp_table_exists = TRUE, 'Temporary table temp_cred_ac_amendment_list should exist';

    -- Verify record count
    EXECUTE 'SELECT COUNT(*) FROM temp_cred_ac_amendment_list' INTO v_record_count;
    ASSERT v_record_count = 1, 'Should have exactly 1 record in temp table';

    -- Verify specific field values are correctly populated
    PERFORM 1 FROM temp_cred_ac_amendment_list 
    WHERE creditor_account_id = v_creditor_account_id
      AND name = 'Ms Jane TestCreditor'
      AND address_line_1 = '789 Creditor St'
      AND address_line_2 = 'Creditor Area'
      AND address_line_3 = 'Creditor City'
      AND postcode = 'CR1 2ED'
      AND hold_payout = FALSE
      AND pay_by_bacs = TRUE
      AND bank_sort_code = '123456'
      AND bank_account_type = '1'
      AND bank_account_number = '12345678'
      AND bank_account_name = 'TestCredAcct'
      AND bank_account_reference = 'REF123';

    ASSERT FOUND, 'Temp table should contain correct data from the view';

    RAISE NOTICE 'TEST 2 PASSED: Creditor accounts temporary table created successfully with correct data';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Test error handling - NULL associated_account_id
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3001';
    v_expected_message           VARCHAR := 'Associated account ID cannot be null';
BEGIN
    RAISE NOTICE '=== TEST 3: Test error handling - NULL associated_account_id ===';
    
    -- Call the procedure with NULL account ID - should throw P3001 exception
    BEGIN
        CALL p_audit_initialise(NULL, 'defendant_accounts');
    EXCEPTION
        WHEN SQLSTATE 'P3001' THEN
            IF SQLERRM = v_expected_message THEN
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
    ASSERT v_error_caught = TRUE, 'A P3001 error should have been raised due to NULL associated_account_id';

    RAISE NOTICE 'TEST 3 PASSED: Error handling works correctly for NULL associated_account_id';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Test error handling - Invalid record type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3002';
    v_expected_message_pattern   VARCHAR := 'Invalid record type: invalid_type. Must be defendant_accounts or creditor_accounts';
BEGIN
    RAISE NOTICE '=== TEST 4: Test error handling - Invalid record type ===';
    
    -- Call the procedure with invalid record type - should throw P3002 exception
    BEGIN
        CALL p_audit_initialise(12345, 'invalid_type');
    EXCEPTION
        WHEN SQLSTATE 'P3002' THEN
            IF SQLERRM LIKE '%Invalid record type%' THEN
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
    ASSERT v_error_caught = TRUE, 'A P3002 error should have been raised due to invalid record type';

    RAISE NOTICE 'TEST 4 PASSED: Error handling works correctly for invalid record type';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Test error handling - NULL record type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3002';
BEGIN
    RAISE NOTICE '=== TEST 5: Test error handling - NULL record type ===';
    
    -- Call the procedure with NULL record type - should throw P3002 exception
    BEGIN
        CALL p_audit_initialise(12345, NULL);
    EXCEPTION
        WHEN SQLSTATE 'P3002' THEN
            IF SQLERRM LIKE '%Invalid record type%' THEN
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
    ASSERT v_error_caught = TRUE, 'A P3002 error should have been raised due to NULL record type';

    RAISE NOTICE 'TEST 5 PASSED: Error handling works correctly for NULL record type';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Test error handling - Non-existent defendant account ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3003';
    v_expected_message           VARCHAR := 'No defendant account found with ID: 999999';
    v_non_existent_id            BIGINT := 999999;
BEGIN
    RAISE NOTICE '=== TEST 6: Test error handling - Non-existent defendant account ID ===';
    
    -- Call the procedure with non-existent defendant account ID - should throw P3003 exception
    BEGIN
        CALL p_audit_initialise(v_non_existent_id, 'defendant_accounts');
    EXCEPTION
        WHEN SQLSTATE 'P3003' THEN
            IF SQLERRM = v_expected_message THEN
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
    ASSERT v_error_caught = TRUE, 'A P3003 error should have been raised due to non-existent defendant account ID';

    RAISE NOTICE 'TEST 6 PASSED: Error handling works correctly for non-existent defendant account ID';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Test error handling - Non-existent creditor account ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3004';
    v_expected_message           VARCHAR := 'No creditor account found with ID: 999999';
    v_non_existent_id            BIGINT := 999999;
BEGIN
    RAISE NOTICE '=== TEST 7: Test error handling - Non-existent creditor account ID ===';
    
    -- Call the procedure with non-existent creditor account ID - should throw P3004 exception
    BEGIN
        CALL p_audit_initialise(v_non_existent_id, 'creditor_accounts');
    EXCEPTION
        WHEN SQLSTATE 'P3004' THEN
            IF SQLERRM = v_expected_message THEN
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
    ASSERT v_error_caught = TRUE, 'A P3004 error should have been raised due to non-existent creditor account ID';

    RAISE NOTICE 'TEST 7 PASSED: Error handling works correctly for non-existent creditor account ID';
END $$;

-- Cleanup test data
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';
    
    -- Delete test data from related tables in correct order to avoid FK violations
    DELETE FROM aliases WHERE party_id IN (SELECT party_id FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent'));
    DELETE FROM debtor_detail WHERE party_id IN (SELECT party_id FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent'));
    DELETE FROM defendant_account_parties WHERE defendant_account_id IN (SELECT defendant_account_id FROM defendant_accounts WHERE account_number LIKE 'TEST%');
    DELETE FROM creditor_accounts WHERE account_number LIKE 'TEST%';
    DELETE FROM defendant_accounts WHERE account_number LIKE 'TEST%';
    DELETE FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent');

    -- Drop temporary tables
    DROP TABLE IF EXISTS temp_def_ac_amendment_list;
    DROP TABLE IF EXISTS temp_cred_ac_amendment_list;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing