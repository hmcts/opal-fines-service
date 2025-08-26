\timing

/**
* CGI Opal Program
*
* MODULE      : p_create_defendant_account_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_create_defendant_account.
*              These tests cover various scenarios to ensure the procedure
*              correctly processes defendant account creation.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------
* 16/07/2025    C Cho       1.0         Unit tests for p_create_defendant_account.
*
**/

-- Test 1: Basic account creation with minimum required fields
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       bigint            := 10001;
    v_business_unit_id       smallint          := 65;
    v_posted_by              character varying := 'L045EO';
    v_posted_by_name         character varying := 'Tester 1';
    v_account_json           json;
    v_account_number         character varying;
    v_defendant_account_id   bigint;
BEGIN
    RAISE NOTICE '=== TEST 1: Basic account creation with minimum required fields ===';
    
    -- Prepare minimal test JSON
    v_account_json := '{
        "account_type": "Fixed Penalty",
        "originator_name": "LJS",
        "originator_id": "12345",
        "enforcement_court_id": 650000000045,
        "account_sentence_date": "2024-12-12"
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft account
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES(
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fixed Penalty',
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure
    CALL p_create_defendant_account(
        v_draft_account_id,
        v_business_unit_id,
        v_posted_by,
        v_posted_by_name,
        v_account_number,
        v_defendant_account_id
    );

    -- Verify results
    ASSERT v_defendant_account_id IS NOT NULL, 'Defendant account ID should not be NULL';
    ASSERT v_account_number IS NOT NULL, 'Account number should not be NULL';
  
    -- Verify account was created in the database
    ASSERT EXISTS (
        SELECT 1 FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id
    ), 'Defendant account should exist in the database';

    RAISE NOTICE 'TEST 1 PASSED: Created account % with ID %', v_account_number, v_defendant_account_id;
END $$;

-- Test 2: Full account creation with all fields including payment card request
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       bigint            := 10002;
    v_business_unit_id       smallint          := 65;
    v_posted_by              character varying := 'L045EO';
    v_posted_by_name         character varying := 'Tester 1';
    v_account_json           json;
    v_account_number         character varying;
    v_defendant_account_id   bigint;
BEGIN
    RAISE NOTICE '=== TEST 2: Full account creation with all fields including payment card request ===';
    
    -- Use the comprehensive JSON with account_type as Fine
    v_account_json := '{
        "account_type": "Fine",
        "defendant_type": "A",
        "originator_name": "LJS",
        "originator_id": "12345",
        "enforcement_court_id": 650000000045,
        "prosecutor_case_reference": "ABC123",
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "collection_order_made": true,
        "collection_order_date": "2024-12-01",
        "suspended_committal_date": "2024-12-15",
        "offences": [
            {
                "offence_code": "OFF001",
                "offence_date": "2024-06-15",
                "description": "Test Offence 1",
                "amount": 100.00,
                "legislation_code": "LEG001"
            },
            {
                "offence_code": "OFF002",
                "offence_date": "2024-06-15",
                "description": "Test Offence 2",
                "amount": 150.00,
                "legislation_code": "LEG002"
            }
        ],
        "fp_ticket_detail": {
            "ticket_number": "FP12345",
            "issue_date": "2024-06-15",
            "officer_id": "OFF789",
            "location": "Main Street"
        },
        "payment_terms": {
            "payment_terms_type_code": "I",
            "effective_date": "2024-07-25",
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 20.00,
            "jail_days": 14
        }
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft account with Fine as the account_type
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES(
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine',  -- Match what's in the JSON
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Add better error handling to identify any issues
    BEGIN
        -- Call the procedure
        CALL p_create_defendant_account(
            v_draft_account_id,
            v_business_unit_id,
            v_posted_by,
            v_posted_by_name,
            v_account_number,
            v_defendant_account_id
        );
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Error occurred with JSON: %', v_account_json;
            RAISE NOTICE 'Error details: % - %', SQLERRM, SQLSTATE;
            RAISE; -- Re-throw the error
    END;

    -- Verify results
    ASSERT v_defendant_account_id IS NOT NULL, 'Defendant account ID should not be NULL';
    ASSERT v_account_number IS NOT NULL, 'Account number should not be NULL';
    
    -- Basic field verification
    ASSERT (SELECT account_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'Fine',
           'Account type should be Fine';
    ASSERT (SELECT imposed_hearing_date FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id)::date = '2024-12-12'::date,
           'Account sentence date should match';
    ASSERT (SELECT enforcing_court_id FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id)::text = '650000000045',
           'Enforcement court ID should match';
    
    -- Verify originator fields
    ASSERT (SELECT originator_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'LJS',
           'Originator name should match';
    ASSERT (SELECT originator_id FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = '12345',
           'Originator ID should match';
           
    -- Verify fp_ticket_detail logic - originator_type should be 'FP' when fp_ticket_detail exists
    ASSERT (SELECT originator_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'FP',
           'Originator type should be FP when fp_ticket_detail exists';
           
    -- Verify collection order fields
    ASSERT (SELECT collection_order FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = TRUE,
           'Collection order should be TRUE';
    ASSERT (SELECT collection_order_date FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id)::date = '2024-12-01'::date,
           'Collection order date should match';
           
    -- Verify suspended committal date
    ASSERT (SELECT suspended_committal_date FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id)::date = '2024-12-15'::date,
           'Suspended committal date should match';
           
    -- Verify prosecutor case reference
    ASSERT (SELECT prosecutor_case_reference FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'ABC123',
           'Prosecutor case reference should match';
    
    -- Verify payment card request fields and logic
    ASSERT (SELECT payment_card_requested FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = TRUE,
           'Payment card requested should be TRUE';
    ASSERT (SELECT payment_card_requested_date FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) IS NOT NULL,
           'Payment card requested date should be set when payment_card_request is TRUE';
    ASSERT (SELECT payment_card_requested_by FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = v_posted_by,
           'Payment card requested by should be set to posted_by when payment_card_request is TRUE';
           
    -- Verify default values are set correctly
    ASSERT (SELECT account_status FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'L',
           'Account status should be set to L for Live';
    ASSERT (SELECT amount_imposed FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 0.00,
           'Amount imposed should be initialized to 0.00';
    ASSERT (SELECT amount_paid FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 0.00,
           'Amount paid should be initialized to 0.00';
    ASSERT (SELECT account_balance FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 0.00,
           'Account balance should be initialized to 0.00';
    ASSERT (SELECT allow_writeoffs FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = TRUE,
           'Allow writeoffs should be set to TRUE';
    ASSERT (SELECT allow_cheques FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = TRUE,
           'Allow cheques should be set to TRUE';

    RAISE NOTICE 'TEST 2 PASSED: Created account % with ID % and payment card request', v_account_number, v_defendant_account_id;
END $$;

-- Test 2B: Verify originator_type logic when fp_ticket_detail is absent
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       bigint            := 10022;
    v_business_unit_id       smallint          := 65;
    v_posted_by              character varying := 'L045EO';
    v_posted_by_name         character varying := 'Tester 1';
    v_account_json           json;
    v_account_number         character varying;
    v_defendant_account_id   bigint;
BEGIN
    RAISE NOTICE '=== TEST 2B: Verify originator_type logic when fp_ticket_detail is absent ===';
    
    -- Use JSON without fp_ticket_detail
    v_account_json := '{
        "account_type": "Fine",
        "defendant_type": "A",
        "originator_name": "LJS",
        "originator_id": "12345",
        "enforcement_court_id": 650000000045,
        "prosecutor_case_reference": "ABC123",
        "payment_card_request": false,
        "account_sentence_date": "2024-12-12",
        "collection_order_made": true,
        "collection_order_date": "2024-12-01"
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft account
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES(
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine',
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure
    CALL p_create_defendant_account(
        v_draft_account_id,
        v_business_unit_id,
        v_posted_by,
        v_posted_by_name,
        v_account_number,
        v_defendant_account_id
    );

    -- Verify originator_type logic - should be 'TFO' when fp_ticket_detail is absent
    ASSERT (SELECT originator_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'TFO',
           'Originator type should be TFO when fp_ticket_detail is absent';
           
    -- Verify payment card requested fields when payment_card_request is false
    ASSERT (SELECT payment_card_requested FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = FALSE,
           'Payment card requested should be FALSE';
    ASSERT (SELECT payment_card_requested_date FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) IS NULL,
           'Payment card requested date should be NULL when payment_card_request is FALSE';
    ASSERT (SELECT payment_card_requested_by FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) IS NULL,
           'Payment card requested by should be NULL when payment_card_request is FALSE';

    RAISE NOTICE 'TEST 2B PASSED: Verified originator_type logic when fp_ticket_detail is absent';
END $$;

-- Test 3: Test handling of missing required fields
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       bigint            := 10003;
    v_business_unit_id       smallint          := 65;
    v_posted_by              character varying := 'L045EO';
    v_posted_by_name         character varying := 'Tester 1';
    v_account_json           json;
    v_account_number         character varying;
    v_defendant_account_id   bigint;
    v_error_caught           boolean := FALSE;
BEGIN
    RAISE NOTICE '=== TEST 3: Test handling of missing required fields ===';
    
    -- Prepare JSON with missing required fields (no account_type)
    v_account_json := '{
        "originator_name": "LJS",
        "originator_id": "12345",
        "enforcement_court_id": 650000000045
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft account
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES(
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fixed Penalty', -- table has a value but JSON doesn't
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw an exception
    BEGIN
        CALL p_create_defendant_account(
            v_draft_account_id,
            v_business_unit_id,
            v_posted_by,
            v_posted_by_name,
            v_account_number,
            v_defendant_account_id
        );
    EXCEPTION
        WHEN OTHERS THEN
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: %', SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'An error should have been raised due to missing required fields';

    RAISE NOTICE 'TEST 3 PASSED: Error handling works correctly for missing required fields';
END $$;

-- Cleanup test data
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';
    
    -- Delete all test accounts created by these tests
    DELETE FROM draft_accounts 
    WHERE draft_account_id BETWEEN 10001 AND 10006
    OR draft_account_id = 10022;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing