\timing

/**
* CGI Opal Program
*
* MODULE      : p_add_defendant_account_enforcement_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_add_defendant_account_enforcement.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------
* 12/09/2025    C Cho       1.0         PO-1790 Unit tests for p_add_defendant_account_enforcement.
*
**/

-- Clear out test data before tests
DO LANGUAGE 'plpgsql' $$
DECLARE

BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Delete test data from related tables in correct order to avoid FK violations
    DELETE FROM report_entries WHERE associated_record_type = 'Enforcement' AND associated_record_id IN (SELECT enforcement_id::VARCHAR FROM enforcements WHERE defendant_account_id IN (90001, 90002, 90003, 90004));
    DELETE FROM document_instances WHERE associated_record_type = 'Enforcement' AND associated_record_id IN (SELECT enforcement_id::VARCHAR FROM enforcements WHERE defendant_account_id IN (90001, 90002, 90003, 90004));
    DELETE FROM warrant_register WHERE enforcement_id IN (SELECT enforcement_id FROM enforcements WHERE defendant_account_id IN (90001, 90002, 90003, 90004));
    DELETE FROM enforcements WHERE defendant_account_id IN (90001, 90002, 90003, 90004);
    DELETE FROM aliases WHERE party_id IN (90001, 90002, 90003, 90004);
    DELETE FROM debtor_detail WHERE party_id IN (90001, 90002, 90003, 90004);
    DELETE FROM defendant_account_parties WHERE defendant_account_id IN (90001, 90002, 90003, 90004);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (90001, 90002, 90003, 90004);
    DELETE FROM parties WHERE party_id IN (90001, 90002, 90003, 90004);
    DELETE FROM enforcers WHERE enforcer_id = 90001;

    -- Drop any existing temp tables from previous sessions
    DROP TABLE IF EXISTS temp_def_ac_amendment_list;
    DROP TABLE IF EXISTS temp_cred_ac_amendment_list;

    RAISE NOTICE 'Data cleanup before tests completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Test successful enforcement creation with warrant generation
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_defendant_account_id       BIGINT := 90001;
    v_business_unit_id           SMALLINT := 65;
    v_result_id                  VARCHAR(6);
    v_enforcer_id                BIGINT := 90001;
    v_party_id_def               BIGINT;
    v_enforcement_id             BIGINT;
    v_record_count               INTEGER;
    v_warrant_ref                VARCHAR(20);
    v_jail_days                  INTEGER := 30;
    v_expected_doc_count         INTEGER;
    v_current_year               VARCHAR(2);
BEGIN
    RAISE NOTICE '=== TEST 1: Test successful enforcement creation with warrant generation ===';
    
    -- Setup test data - Create defendant party
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

    -- Create defendant account
    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, account_type,
        imposed_hearing_date, amount_imposed, amount_paid, account_balance,
        account_status, cheque_clearance_period, allow_cheques,
        credit_trans_clearance_period, allow_writeoffs, enforcing_court_id,
        collection_order, suspended_committal_date, account_comments,
        account_note_1, account_note_2, account_note_3, jail_days, version_number
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'TEST001', 'Fine',
        '2024-01-01'::DATE, 100.00, 50.00, 50.00,
        'L', 5, TRUE, 3, TRUE, 650000000045,
        FALSE, NULL, 'Test account comments',
        'Test note 1', 'Test note 2', 'Test note 3', 0, 1
    );

    -- Create defendant account parties association
    INSERT INTO defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES (90001, v_defendant_account_id, v_party_id_def, 'Defendant', FALSE);

    -- Get a result that generates warrant from existing data
    SELECT result_id INTO v_result_id
    FROM results
    WHERE generates_warrant = TRUE
      AND enforcement = TRUE
      AND active = TRUE
    LIMIT 1;
    
    IF v_result_id IS NULL THEN
        RAISE EXCEPTION 'No warrant-generating result found in test data';
    END IF;
    
    RAISE NOTICE 'Using result_id: %', v_result_id;

    -- Create enforcer (all fields are non-nullable except warrant_reference_sequence and warrant_register_sequence)
    INSERT INTO enforcers (
        enforcer_id, business_unit_id, enforcer_code, name,
        warrant_reference_sequence
    ) VALUES (
        v_enforcer_id, v_business_unit_id, 101, 'Test Enforcer',
        '101/24/00007'
    );

    -- Call the procedure
    CALL p_add_defendant_account_enforcement(
        pi_result_id := v_result_id,
        pi_defendant_account_id := v_defendant_account_id,
        pi_business_unit_id := v_business_unit_id,
        pi_record_type := 'defendant_accounts',
        pi_case_reference := 'CASE123',
        pi_function_code := 'ACCOUNT_ENQUIRY',
        pi_jail_days := v_jail_days,
        pi_posted_by := 'USER123',
        pi_posted_by_name := 'Test User',
        pi_reason := 'Test enforcement',
        pi_enforcer_id := v_enforcer_id,
        pi_result_responses := '{"test": "data"}',
        pi_earliest_release_date := NULL,
        pi_version_number := 1,
        po_enforcement_id := v_enforcement_id
    );

    -- Verify enforcement was created
    ASSERT v_enforcement_id IS NOT NULL, 'Enforcement ID should be returned';

    SELECT COUNT(*) INTO v_record_count FROM enforcements WHERE enforcement_id = v_enforcement_id;
    ASSERT v_record_count = 1, 'Should have exactly 1 enforcement record';

    -- Verify enforcement data
    PERFORM 1 FROM enforcements 
    WHERE enforcement_id = v_enforcement_id
      AND defendant_account_id = v_defendant_account_id
      AND result_id = v_result_id
      AND posted_by = 'USER123'
      AND posted_by_name = 'Test User'
      AND reason = 'Test enforcement'
      AND enforcer_id = v_enforcer_id
      AND jail_days = v_jail_days;

    ASSERT FOUND, 'Enforcement should contain correct data';

    -- Verify defendant account was updated
    PERFORM 1 FROM defendant_accounts 
    WHERE defendant_account_id = v_defendant_account_id
      AND last_enforcement = v_result_id
      AND jail_days = v_jail_days
      AND last_movement_date IS NOT NULL;

    ASSERT FOUND, 'Defendant account should be updated with enforcement data';

    -- Verify warrant reference was generated and updated
    v_current_year := RIGHT(EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::TEXT, 2);
    SELECT warrant_reference INTO v_warrant_ref FROM enforcements WHERE enforcement_id = v_enforcement_id;
    ASSERT v_warrant_ref = format('101/%s/00001', v_current_year),
       format('Warrant reference should be set to 101/%s/00001', v_current_year);

    -- Verify enforcer warrant sequence was updated
    PERFORM 1 FROM enforcers 
    WHERE enforcer_id = v_enforcer_id 
      AND warrant_reference_sequence = v_warrant_ref;

    ASSERT FOUND, 'Enforcer warrant sequence should be updated';

    -- Verify warrant register entry
    SELECT COUNT(*) INTO v_record_count FROM warrant_register WHERE enforcement_id = v_enforcement_id;
    ASSERT v_record_count = 1, 'Should have exactly 1 warrant register entry';

    -- Get expected document count for this result
    SELECT COUNT(*) INTO v_expected_doc_count 
    FROM result_documents 
    WHERE result_id = v_result_id;

    -- Verify document instances were created (should match result_documents count)
    SELECT COUNT(*) INTO v_record_count FROM document_instances 
    WHERE associated_record_type = 'Enforcement' AND associated_record_id = v_enforcement_id::VARCHAR;
    ASSERT v_record_count = v_expected_doc_count, FORMAT('Should have exactly %s document instance(s) based on result_documents', v_expected_doc_count);

    -- Verify report entry was created
    SELECT COUNT(*) INTO v_record_count FROM report_entries 
    WHERE associated_record_type = 'Enforcement' AND associated_record_id = v_enforcement_id::VARCHAR;
    ASSERT v_record_count = 1, 'Should have exactly 1 report entry';

    RAISE NOTICE 'TEST 1 PASSED: Enforcement created successfully with warrant generation';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Test COLLO enforcement action updates collection order
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_defendant_account_id       BIGINT := 90002;
    v_business_unit_id           SMALLINT := 65;
    v_result_id                  VARCHAR(6) := 'COLLO';
    v_party_id_def               BIGINT;
    v_enforcement_id             BIGINT;
    v_expected_doc_count         INTEGER;
    v_record_count               INTEGER;
BEGIN
    RAISE NOTICE '=== TEST 2: Test COLLO enforcement action updates collection order ===';
    
    -- Setup test data
    INSERT INTO parties (
        party_id, organisation, surname, forenames, title, 
        address_line_1, postcode, birth_date
    ) VALUES (
        90002, FALSE, 'TestDefendant2', 'Jane', 'Ms',
        '456 Test Street', 'TE2 3ST', '1985-05-15'::DATE
    ) RETURNING party_id INTO v_party_id_def;

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, account_type,
        imposed_hearing_date, amount_imposed, amount_paid, account_balance,
        account_status, collection_order, collection_order_date, version_number
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'TEST002', 'Fine',
        '2024-01-01'::DATE, 200.00, 0.00, 200.00,
        'L', FALSE, NULL, 1
    );

    INSERT INTO defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES (90002, v_defendant_account_id, v_party_id_def, 'Defendant', FALSE);

    -- Verify COLLO result exists
    PERFORM 1 FROM results WHERE result_id = v_result_id AND enforcement = TRUE AND active = TRUE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'COLLO result not found in test data';
    END IF;

    -- Call the procedure
    CALL p_add_defendant_account_enforcement(
        pi_result_id := v_result_id,
        pi_defendant_account_id := v_defendant_account_id,
        pi_business_unit_id := v_business_unit_id,
        pi_record_type := 'defendant_accounts',
        pi_case_reference := NULL,
        pi_function_code := 'ACCOUNT_ENQUIRY',
        pi_jail_days := NULL,
        pi_posted_by := 'USER123',
        pi_posted_by_name := 'Test User',
        pi_reason := NULL,
        pi_enforcer_id := NULL,
        pi_result_responses := NULL,
        pi_earliest_release_date := NULL,
        pi_version_number := 1,
        po_enforcement_id := v_enforcement_id
    );

    -- Verify collection order was set
    PERFORM 1 FROM defendant_accounts 
    WHERE defendant_account_id = v_defendant_account_id
      AND collection_order = TRUE
      AND collection_order_date IS NOT NULL;

    ASSERT FOUND, 'Collection order should be set to TRUE with timestamp';

    -- Get expected document count for this result
    SELECT COUNT(*) INTO v_expected_doc_count 
    FROM result_documents 
    WHERE result_id = v_result_id;

    -- Verify document instances were created (should match result_documents count)
    SELECT COUNT(*) INTO v_record_count FROM document_instances 
    WHERE associated_record_type = 'Enforcement' AND associated_record_id = v_enforcement_id::VARCHAR;
    ASSERT v_record_count = v_expected_doc_count, FORMAT('Should have exactly %s document instance(s) based on result_documents', v_expected_doc_count);

    RAISE NOTICE 'TEST 2 PASSED: COLLO enforcement correctly updates collection order';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Test FSN enforcement action sets further steps notice date only if NULL
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_defendant_account_id       BIGINT := 90003;
    v_business_unit_id           SMALLINT := 65;
    v_result_id                  VARCHAR(6) := 'FSN';
    v_party_id_def               BIGINT;
    v_enforcement_id             BIGINT;
    v_original_date              TIMESTAMP := '2024-01-01 10:00:00';
    BEGIN
    RAISE NOTICE '=== TEST 3: Test FSN enforcement action sets further steps notice date only if NULL ===';
    
    -- Setup test data with existing FSN date
    INSERT INTO parties (
        party_id, organisation, surname, forenames, address_line_1, postcode, birth_date
    ) VALUES (
        90003, FALSE, 'TestDefendant3', 'Bob', '789 Test Street', 'TE3 4ST', '1990-03-20'::DATE
    ) RETURNING party_id INTO v_party_id_def;

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, account_type,
        imposed_hearing_date, amount_imposed, amount_paid, account_balance,
        account_status, further_steps_notice_date, version_number
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'TEST003', 'Fine',
        '2024-01-01'::DATE, 150.00, 25.00, 125.00,
        'L', v_original_date, 1
    );

    INSERT INTO defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES (90003, v_defendant_account_id, v_party_id_def, 'Defendant', FALSE);

    -- Verify FSN result exists
    PERFORM 1 FROM results WHERE result_id = v_result_id AND enforcement = TRUE AND active = TRUE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'FSN result not found in test data';
    END IF;

    -- Call the procedure
    CALL p_add_defendant_account_enforcement(
        pi_result_id := v_result_id,
        pi_defendant_account_id := v_defendant_account_id,
        pi_business_unit_id := v_business_unit_id,
        pi_record_type := 'defendant_accounts',
        pi_case_reference := NULL,
        pi_function_code := 'ACCOUNT_ENQUIRY',
        pi_jail_days := NULL,
        pi_posted_by := 'USER123',
        pi_posted_by_name := 'Test User',
        pi_reason := NULL,
        pi_enforcer_id := NULL,
        pi_result_responses := NULL,
        pi_earliest_release_date := NULL,
        pi_version_number := 1,
        po_enforcement_id := v_enforcement_id
    );

    -- Verify FSN date was NOT changed (should remain original)
    PERFORM 1 FROM defendant_accounts 
    WHERE defendant_account_id = v_defendant_account_id
      AND further_steps_notice_date = v_original_date;

    ASSERT FOUND, 'Further steps notice date should not be overwritten when already exists';

    RAISE NOTICE 'TEST 3 PASSED: FSN enforcement correctly preserves existing further steps notice date';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Test error handling - NULL result_id
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P4001';
    v_expected_message           VARCHAR := 'Result ID cannot be null';
    v_enforcement_id             BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 4: Test error handling - NULL result_id ===';
    
    -- Call the procedure with NULL result_id - should throw P4001 exception
    BEGIN
        CALL p_add_defendant_account_enforcement(
            pi_result_id := NULL,
            pi_defendant_account_id := 90001::BIGINT,
            pi_business_unit_id := 65::SMALLINT,
            pi_record_type := 'defendant_accounts',
            pi_case_reference := NULL,
            pi_function_code := 'ACCOUNT_ENQUIRY',
            pi_jail_days := NULL,
            pi_posted_by := 'USER123',
            pi_posted_by_name := 'Test User',
            pi_reason := NULL,
            pi_enforcer_id := NULL,
            pi_result_responses := NULL,
            pi_earliest_release_date := NULL,
            pi_version_number := 1,
            po_enforcement_id := v_enforcement_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P4001' THEN
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
    ASSERT v_error_caught = TRUE, 'A P4001 error should have been raised due to NULL result_id';

    RAISE NOTICE 'TEST 4 PASSED: Error handling works correctly for NULL result_id';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Test error handling - NULL defendant_account_id
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P4002';
    v_expected_message           VARCHAR := 'Defendant account ID cannot be null';
    v_enforcement_id             BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 5: Test error handling - NULL defendant_account_id ===';
    
    -- Call the procedure with NULL defendant_account_id - should throw P4002 exception
    BEGIN
        CALL p_add_defendant_account_enforcement(
            pi_result_id := 'SUMM',
            pi_defendant_account_id := NULL::BIGINT,
            pi_business_unit_id := 65::SMALLINT,
            pi_record_type := 'defendant_accounts',
            pi_case_reference := NULL,
            pi_function_code := 'ACCOUNT_ENQUIRY',
            pi_jail_days := NULL,
            pi_posted_by := 'USER123',
            pi_posted_by_name := 'Test User',
            pi_reason := NULL,
            pi_enforcer_id := NULL,
            pi_result_responses := NULL,
            pi_earliest_release_date := NULL,
            pi_version_number := 1,
            po_enforcement_id := v_enforcement_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P4002' THEN
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
    ASSERT v_error_caught = TRUE, 'A P4002 error should have been raised due to NULL defendant_account_id';

    RAISE NOTICE 'TEST 5 PASSED: Error handling works correctly for NULL defendant_account_id';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Test error handling - Invalid record type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P4004';
    v_enforcement_id             BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 6: Test error handling - Invalid record type ===';
    
    -- Call the procedure with invalid record type - should throw P4004 exception
    BEGIN
        CALL p_add_defendant_account_enforcement(
            pi_result_id := 'SUMM',
            pi_defendant_account_id := 90001::BIGINT,
            pi_business_unit_id := 65::SMALLINT,
            pi_record_type := 'invalid_type',
            pi_case_reference := NULL,
            pi_function_code := 'ACCOUNT_ENQUIRY',
            pi_jail_days := NULL,
            pi_posted_by := 'USER123',
            pi_posted_by_name := 'Test User',
            pi_reason := NULL,
            pi_enforcer_id := NULL,
            pi_result_responses := NULL,
            pi_earliest_release_date := NULL,
            pi_version_number := 1,
            po_enforcement_id := v_enforcement_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P4004' THEN
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
    ASSERT v_error_caught = TRUE, 'A P4004 error should have been raised due to invalid record type';

    RAISE NOTICE 'TEST 6 PASSED: Error handling works correctly for invalid record type';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Test error handling - Non-existent enforcer_id
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P4006';
    v_expected_message           VARCHAR := 'Enforcer not found with ID: 999999';
    v_non_existent_id            BIGINT := 999999;
    v_enforcement_id             BIGINT;
    v_result_id                  VARCHAR(6);
BEGIN
    RAISE NOTICE '=== TEST 7: Test error handling - Non-existent enforcer_id ===';
    
    -- Get a result that generates warrant from existing data
    SELECT result_id INTO v_result_id
    FROM results
    WHERE generates_warrant = TRUE
      AND enforcement = TRUE
      AND active = TRUE
    LIMIT 1;
    
    IF v_result_id IS NULL THEN
        RAISE EXCEPTION 'No warrant-generating result found in test data';
    END IF;
    
    -- Call the procedure with non-existent enforcer_id for warrant-generating result - should throw P4006 exception
    BEGIN
        CALL p_add_defendant_account_enforcement(
            pi_result_id := v_result_id,
            pi_defendant_account_id := 90001::BIGINT,
            pi_business_unit_id := 65::SMALLINT,
            pi_record_type := 'defendant_accounts',
            pi_case_reference := NULL,
            pi_function_code := 'ACCOUNT_ENQUIRY',
            pi_jail_days := NULL,
            pi_posted_by := 'USER123',
            pi_posted_by_name := 'Test User',
            pi_reason := NULL,
            pi_enforcer_id := v_non_existent_id,
            pi_result_responses := NULL,
            pi_earliest_release_date := NULL,
            pi_version_number := 1,
            po_enforcement_id := v_enforcement_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P4006' THEN
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
    ASSERT v_error_caught = TRUE, 'A P4006 error should have been raised due to non-existent enforcer_id';

    RAISE NOTICE 'TEST 7 PASSED: Error handling works correctly for non-existent enforcer_id';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 8: Test error handling - NULL version_number
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P4007';
    v_expected_message           VARCHAR := 'Version number cannot be null';
    v_enforcement_id             BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 8: Test error handling - NULL version_number ===';
    
    -- Call the procedure with NULL version_number - should throw P4007 exception
    BEGIN
        CALL p_add_defendant_account_enforcement(
            pi_result_id := 'SUMM',
            pi_defendant_account_id := 90001::BIGINT,
            pi_business_unit_id := 65::SMALLINT,
            pi_record_type := 'defendant_accounts',
            pi_case_reference := NULL,
            pi_function_code := 'ACCOUNT_ENQUIRY',
            pi_jail_days := NULL,
            pi_posted_by := 'USER123',
            pi_posted_by_name := 'Test User',
            pi_reason := NULL,
            pi_enforcer_id := NULL,
            pi_result_responses := NULL,
            pi_earliest_release_date := NULL,
            pi_version_number := NULL,
            po_enforcement_id := v_enforcement_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P4007' THEN
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
    ASSERT v_error_caught = TRUE, 'A P4007 error should have been raised due to NULL version_number';

    RAISE NOTICE 'TEST 8 PASSED: Error handling works correctly for NULL version_number';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 9: Test error handling - Version number mismatch (concurrency check)
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_defendant_account_id       BIGINT := 90004;
    v_business_unit_id           SMALLINT := 65;
    v_party_id_def               BIGINT;
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P4008';
    v_expected_message           VARCHAR := 'Some information on this page may be out of date.';
    v_enforcement_id             BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 9: Test error handling - Version number mismatch (concurrency check) ===';
    
    -- Setup test data for concurrency test
    INSERT INTO parties (
        party_id, organisation, surname, forenames, address_line_1, postcode, birth_date
    ) VALUES (
        90004, FALSE, 'TestDefendant4', 'Alice', '101 Test Street', 'TE4 5ST', '1995-12-10'::DATE
    ) RETURNING party_id INTO v_party_id_def;

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, account_type,
        imposed_hearing_date, amount_imposed, amount_paid, account_balance,
        account_status, version_number
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'TEST004', 'Fine',
        '2024-01-01'::DATE, 300.00, 0.00, 300.00,
        'L', 1
    );

    INSERT INTO defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES (90004, v_defendant_account_id, v_party_id_def, 'Defendant', FALSE);

    -- Call the procedure with incorrect version number (should be 1, passing 5) - should throw P4008 exception
    BEGIN
        CALL p_add_defendant_account_enforcement(
            pi_result_id := 'SUMM',
            pi_defendant_account_id := v_defendant_account_id,
            pi_business_unit_id := v_business_unit_id,
            pi_record_type := 'defendant_accounts',
            pi_case_reference := NULL,
            pi_function_code := 'ACCOUNT_ENQUIRY',
            pi_jail_days := NULL,
            pi_posted_by := 'USER123',
            pi_posted_by_name := 'Test User',
            pi_reason := 'Test concurrency failure',
            pi_enforcer_id := NULL,
            pi_result_responses := NULL,
            pi_earliest_release_date := NULL,
            pi_version_number := 5,
            po_enforcement_id := v_enforcement_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P4008' THEN
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
    ASSERT v_error_caught = TRUE, 'A P4008 error should have been raised due to version number mismatch';

    -- Verify no enforcement was created due to concurrency failure
    PERFORM 1 FROM enforcements WHERE defendant_account_id = v_defendant_account_id;
    ASSERT NOT FOUND, 'No enforcement should be created when concurrency check fails';

    -- Verify defendant account version number remains unchanged
    PERFORM 1 FROM defendant_accounts 
    WHERE defendant_account_id = v_defendant_account_id AND version_number = 1;
    ASSERT FOUND, 'Defendant account version number should remain unchanged when concurrency check fails';

    RAISE NOTICE 'TEST 9 PASSED: Error handling works correctly for version number mismatch (concurrency check)';
END $$;

-- Cleanup test data
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';
    
    -- Delete test data from related tables in correct order to avoid FK violations
    DELETE FROM report_entries WHERE associated_record_type = 'Enforcement' AND associated_record_id IN (SELECT enforcement_id::VARCHAR FROM enforcements WHERE defendant_account_id IN (90001, 90002, 90003, 90004));
    DELETE FROM document_instances WHERE associated_record_type = 'Enforcement' AND associated_record_id IN (SELECT enforcement_id::VARCHAR FROM enforcements WHERE defendant_account_id IN (90001, 90002, 90003, 90004));
    DELETE FROM warrant_register WHERE enforcement_id IN (SELECT enforcement_id FROM enforcements WHERE defendant_account_id IN (90001, 90002, 90003, 90004));
    DELETE FROM enforcements WHERE defendant_account_id IN (90001, 90002, 90003, 90004);
    DELETE FROM aliases WHERE party_id IN (90001, 90002, 90003, 90004);
    DELETE FROM debtor_detail WHERE party_id IN (90001, 90002, 90003, 90004);
    DELETE FROM defendant_account_parties WHERE defendant_account_id IN (90001, 90002, 90003, 90004);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (90001, 90002, 90003, 90004);
    DELETE FROM parties WHERE party_id IN (90001, 90002, 90003, 90004);
    DELETE FROM enforcers WHERE enforcer_id = 90001;

    -- Drop temporary tables
    DROP TABLE IF EXISTS temp_def_ac_amendment_list;
    DROP TABLE IF EXISTS temp_cred_ac_amendment_list;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing