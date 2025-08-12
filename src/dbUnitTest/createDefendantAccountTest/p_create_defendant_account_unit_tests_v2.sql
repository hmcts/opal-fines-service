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
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------
* 16/07/2025    C Cho       1.0         Unit tests for p_create_defendant_account.
* 30/07/2025    TMc         2.0         Added unit tests for p_create_defendant_parties, p_create_debtor_details, p_create_aliases, p_create_fp_offences
*                                       Aligned to version 3.0 of p_create_defendant_account
*
**/

-- Clear out tables - Added in v2.0
DO LANGUAGE 'plpgsql' $$
DECLARE

BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Delete all test accounts created by these tests
    DELETE FROM draft_accounts 
          WHERE draft_account_id BETWEEN 10001 AND 10010
             OR draft_account_id = 10022;
    
    DELETE FROM aliases;
    DELETE FROM debtor_detail;
    DELETE FROM fixed_penalty_offences;
    DELETE FROM defendant_account_parties;
    DELETE FROM parties;
    --DELETE FROM payment_card_requests;
    DELETE FROM defendant_accounts;
    DELETE FROM account_number_index;

    RAISE NOTICE 'Data cleanup before tests completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Basic account creation with minimum required fields - account_type NOT 'Fixed Penalty'
----------------------------------------------------------------------------------------------------------------------
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
    -- v2.0 - Added other required objects/fields: defendant_type (values: adultOrYouthOnly, pgToPay, company)
    --                                             defendant (company_flag, address_line_1), payment_card_request
    --                                             offences -> offence (date_of_sentence, offence_id, impositions (result_id, amount_imposed, amount_paid))
    --                                             payment_terms (payment_terms_type_code (values: B,P,I)) 
    v_account_json := '{
        "account_type": "Fine",
        "defendant_type": "adultOrYouthOnly",
        "originator_name": "LJS",
        "originator_id": "12345",
        "enforcement_court_id": 650000000045,
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "defendant": {
            "company_flag": true,
            "address_line_1": "789 Parent St"
        },
        "offences": [
            {
                "date_of_sentence": "2024-06-15",
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 50.00
                    }
                ]
            }
        ],
        "payment_terms": {
            "payment_terms_type_code": "B"
        }
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

    -- Verify results
    ASSERT v_defendant_account_id IS NOT NULL, 'Defendant account ID should not be NULL';
    ASSERT v_account_number IS NOT NULL, 'Account number should not be NULL';
  
    -- Verify account was created in the database
    ASSERT EXISTS (
        SELECT 1 FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id
    ), 'Defendant account should exist in the database';

    -- Verify fp_ticket_detail logic - originator_type should be 'TFO' when fp_ticket_detail do not exist   v2.0
    ASSERT (SELECT originator_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'TFO',
           'Originator type should be TFO when fp_ticket_detail do not exist';

    -- Verify account_number was created in the database  v2.0
    ASSERT EXISTS (
        SELECT 1 FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number AND associated_record_type = 'defendant_accounts'
    ), 'Defendant account_number should exist in the database';

    -- Verify the party record was created in the database  v2.0
    ASSERT EXISTS (
        SELECT 1 FROM parties WHERE organisation = TRUE AND address_line_1 = '789 Parent St'
    ), 'Defendant parties record should exist in the database';

    -- Verify the defendant_account_parties record for the defendant was created as expected in the database  v2.0
    ASSERT EXISTS (
        SELECT 1 FROM defendant_account_parties WHERE defendant_account_id = v_defendant_account_id AND association_type = 'Defendant' AND debtor = TRUE
    ), 'Defendant defendant_account_parties record should exist in the database';

    RAISE NOTICE 'TEST 1 PASSED: Created account % with ID %', v_account_number, v_defendant_account_id;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Basic account creation with minimum required fields when account_type = 'Fixed Penalty'  Added in v2.0
----------------------------------------------------------------------------------------------------------------------
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
    RAISE NOTICE '=== TEST 2: Basic account creation with minimum required fields and account_type = "Fixed Penalty"';
    
    v_account_json := '{
        "account_type": "Fixed Penalty",
        "defendant_type": "adultOrYouthOnly",
        "originator_name": "LJS",
        "originator_id": "12345",
        "enforcement_court_id": 650000000045,
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "defendant": {
            "company_flag": true,
            "address_line_1": "789 Parent St"
        },
        "offences": [
            {
                "date_of_sentence": "2024-06-15",
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 50.00
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345"
        },
        "payment_terms": {
            "payment_terms_type_code": "B"
        }
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

    -- Verify fp_ticket_detail logic - originator_type should be 'FP' when fp_ticket_detail exists
    ASSERT (SELECT originator_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'FP',
           'Originator type should be FP when fp_ticket_detail exists';

    -- Verify account_number was created in the database
    ASSERT EXISTS (
        SELECT 1 FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number AND associated_record_type = 'defendant_accounts'
    ), 'Defendant account_number should exist in the database';

    -- Verify the party record was created in the database
    ASSERT EXISTS (
        SELECT 1 FROM parties WHERE organisation = TRUE AND address_line_1 = '789 Parent St'
    ), 'Defendant parties record should exist in the database';

    -- Verify the defendant_account_parties record for the defendant was created in the database
    ASSERT EXISTS (
        SELECT 1 FROM defendant_account_parties WHERE defendant_account_id = v_defendant_account_id AND association_type = 'Defendant'
    ), 'Defendant defendant_account_parties record should exist in the database';

    -- Verify the fixed_penalty_offences record was created in the database
    ASSERT EXISTS (
        SELECT 1 FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id AND ticket_number = 'FP12345'
    ), 'fixed_penalty_offences record should exist in the database';

    RAISE NOTICE 'TEST 2 PASSED: Created account % with ID %', v_account_number, v_defendant_account_id;
END $$;

-------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 3: Full account creation with all fields including payment card request  -  account_type = 'Fixed Penalty', debtor is the parent/guardian
-------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       bigint            := 10003;
    v_business_unit_id       smallint          := 65;
    v_posted_by              character varying := 'L045EO';
    v_posted_by_name         character varying := 'Tester 1';
    v_account_json           json;
    v_account_number         character varying;
    v_defendant_account_id   bigint;

    v_party_id_def           bigint; 
    v_party_id_pg            bigint;
    v_is_debtor_def          boolean; 
    v_is_debtor_pg           boolean;
BEGIN
    RAISE NOTICE '=== TEST 3: Full account creation with all fields including payment card request - account_type = "Fixed Penalty", debtor is the parent/guardian ===';
    
    -- Use the comprehensive JSON with account_type as Fixed Penalty
    v_account_json := '{
        "account_type": "Fixed Penalty",
        "defendant_type": "pgToPay",
        "originator_name": "LJS",
        "originator_id": "12345",
        "prosecutor_case_reference": "ABC123",
        "enforcement_court_id": 650000000045,
        "collection_order_made": true,
        "collection_order_date": "2024-12-01",
        "suspended_committal_date": "2024-12-15",
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "defendant": {
            "company_flag": false,
            "title": "Mr",
            "surname": "Doe",
            "company_name": null,
            "forenames": "John",
            "dob": "1980-01-01",
            "address_line_1": "789 Some St",
            "address_line_2": null,
            "address_line_3": "",
            "address_line_4": "",
            "address_line_5": "",
            "post_code": "54321",
            "telephone_number_home": "555-4321",
            "telephone_number_business": "555-8765",
            "telephone_number_mobile": "555-5678",
            "email_address_1": "john.doe@example.com",
            "email_address_2": "j.doe@example.com",
            "national_insurance_number": "AB123456C",
            "debtor_detail": {
                "vehicle_make": "Honda",
                "vehicle_registration_mark": "ABC 9876",
                "document_language": "EN",
                "hearing_language": "EN",
                "employee_reference": "EMP456",
                "employer_company_name": "XYZ Corp",
                "employer_address_line_1": "123 Business Rd",
                "employer_address_line_2": "Floor 2",
                "employer_address_line_3": "",
                "employer_address_line_4": "",
                "employer_address_line_5": "",
                "employer_post_code": "98765",
                "employer_telephone_number": "555-6789",
                "employer_email_address": "hr@xyzcorp.com",
                "aliases": [
                    {
                        "alias_forenames": "Jonny",
                        "alias_surname": "Doe",
                        "alias_company_name": ""
                    },
                    {
                        "alias_forenames": "Jon",
                        "alias_surname": "ADoe",
                        "alias_company_name": null
                    }
                ]
            },
            "parent_guardian": {
                "company_flag": false,
                "company_name": "",
                "surname": "DoePG",
                "forenames": "Jane",
                "dob": "1950-01-01",
                "address_line_1": "789 Parent St",
                "address_line_2": "",
                "address_line_3": "",
                "address_line_4": "",
                "address_line_5": "",
                "post_code": "54321",
                "telephone_number_home": "555-4321",
                "telephone_number_business": "555-9876",
                "telephone_number_mobile": "555-6587",
                "email_address_1": "jane.doe@example.com",
                "email_address_2": "j.doe@example.com",
                "national_insurance_number": "AB654321C",
                "debtor_detail": {
                    "vehicle_make": "Honda",
                    "vehicle_registration_mark": "ABC 9876",
                    "document_language": "CY",
                    "hearing_language": "CY",
                    "employee_reference": "EMP123",
                    "employer_company_name": "ABC Corp",
                    "employer_address_line_1": "456 Business Rd",
                    "employer_address_line_2": "Unit 1",
                    "employer_address_line_3": "",
                    "employer_address_line_4": "",
                    "employer_address_line_5": "",
                    "employer_post_code": "AB1 2ZX",
                    "employer_telephone_number": "555-1234",
                    "employer_email_address": "work@xyzcorp.com",
                    "aliases": [
                        {
                            "alias_forenames": "Janie",
                            "alias_surname": "Doe",
                            "alias_company_name": ""
                        }
                    ]
                }
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-06-15",
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 50.00
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-06-15",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
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
        'Fixed Penalty',  -- Match what's in the JSON
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
    
    -------------------------------
    -- DEFENDANT_ACCOUNTS
    -------------------------------

    -- Basic field verification
    ASSERT (SELECT account_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'Fixed Penalty',
           'Account type should be Fixed Penalty';
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
    ASSERT (SELECT last_movement_date FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) IS NOT NULL,
           'Last movement date should be set';
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

        
    -------------------------------
    -- DEFENDANT_ACCOUNT_PARTIES
    -------------------------------

    -- Verify the correct record was created for the defendant 
    SELECT party_id, debtor INTO v_party_id_def, v_is_debtor_def
      FROM defendant_account_parties 
     WHERE defendant_account_id = v_defendant_account_id AND association_type = 'Defendant';

    ASSERT v_party_id_def IS NOT NULL, 'DEFENDANT_ACCOUNT_PARTIES record for the Defendant should exist';
    ASSERT v_is_debtor_def = FALSE, 'DEFENDANT_ACCOUNT_PARTIES - The debtor should NOT be the Defendant';

    -- Verify the correct record was created for the parent/guardian because defendant_type = 'pgToPay'
    SELECT party_id, debtor INTO v_party_id_pg, v_is_debtor_pg
      FROM defendant_account_parties 
     WHERE defendant_account_id = v_defendant_account_id AND association_type = 'Parent/Guardian';

    ASSERT v_party_id_pg IS NOT NULL, 'DEFENDANT_ACCOUNT_PARTIES record for the Parent/Guardian should exist';
    ASSERT v_is_debtor_pg = TRUE, 'DEFENDANT_ACCOUNT_PARTIES - The debtor should be the Parent/Guardian';

    -------------------------------
    -- PARTIES
    -------------------------------

    -- Verify fields for the Defendant party 
    ASSERT (SELECT account_type FROM parties WHERE party_id = v_party_id_def)               = 'Defendant',  'PARTIES - Defendant - account_type should match';
    ASSERT (SELECT organisation FROM parties WHERE party_id = v_party_id_def)               = FALSE,        'PARTIES - Defendant - organisation should match';
    ASSERT (SELECT organisation_name FROM parties WHERE party_id = v_party_id_def)          IS NULL,        'PARTIES - Defendant - organisation_name should match';
    ASSERT (SELECT surname FROM parties WHERE party_id = v_party_id_def)                    = 'Doe',        'PARTIES - Defendant - surname should match';
    ASSERT (SELECT forenames FROM parties WHERE party_id = v_party_id_def)                  = 'John',       'PARTIES - Defendant - forenames should match';
    ASSERT (SELECT title FROM parties WHERE party_id = v_party_id_def)                      = 'Mr',         'PARTIES - Defendant - title should match';
    ASSERT (SELECT address_line_1 FROM parties WHERE party_id = v_party_id_def)             = '789 Some St', 'PARTIES - Defendant - address_line_1 should match';
    ASSERT (SELECT address_line_2 FROM parties WHERE party_id = v_party_id_def)             IS NULL,        'PARTIES - Defendant - address_line_2 should match';
    ASSERT (SELECT address_line_3 FROM parties WHERE party_id = v_party_id_def)             = '',           'PARTIES - Defendant - address_line_3 should match';
    ASSERT (SELECT address_line_4 FROM parties WHERE party_id = v_party_id_def)             = '',           'PARTIES - Defendant - address_line_4 should match';
    ASSERT (SELECT address_line_5 FROM parties WHERE party_id = v_party_id_def)             = '',           'PARTIES - Defendant - address_line_5 should match';
    ASSERT (SELECT postcode FROM parties WHERE party_id = v_party_id_def)                   = '54321',      'PARTIES - Defendant - postcode should match';
    ASSERT (SELECT birth_date FROM parties WHERE party_id = v_party_id_def)                 = '1980-01-01', 'PARTIES - Defendant - birth_date should match';
    ASSERT (SELECT age FROM parties WHERE party_id = v_party_id_def)                        IS NULL,        'PARTIES - Defendant - age should be NULL';
    ASSERT (SELECT national_insurance_number FROM parties WHERE party_id = v_party_id_def)  = 'AB123456C',  'PARTIES - Defendant - national_insurance_number should match';
    ASSERT (SELECT telephone_home FROM parties WHERE party_id = v_party_id_def)             = '555-4321',   'PARTIES - Defendant - telephone_home should match';
    ASSERT (SELECT telephone_business FROM parties WHERE party_id = v_party_id_def)         = '555-8765',   'PARTIES - Defendant - telephone_business should match';
    ASSERT (SELECT telephone_mobile FROM parties WHERE party_id = v_party_id_def)           = '555-5678',   'PARTIES - Defendant - telephone_mobile should match';
    ASSERT (SELECT email_1 FROM parties WHERE party_id = v_party_id_def)                    = 'john.doe@example.com', 'PARTIES - Defendant - email_1 should match';
    ASSERT (SELECT email_2 FROM parties WHERE party_id = v_party_id_def)                    = 'j.doe@example.com',    'PARTIES - Defendant - email_2 should match';
    ASSERT (SELECT last_changed_date FROM parties WHERE party_id = v_party_id_def)          IS NULL,        'PARTIES - Defendant - last_changed_date should be NULL';

    -- Verify fields for the parent/guardian party
    ASSERT (SELECT account_type FROM parties WHERE party_id = v_party_id_pg)               = 'Defendant',  'PARTIES - Parent/Guardian - account_type should match';
    ASSERT (SELECT organisation FROM parties WHERE party_id = v_party_id_pg)               = FALSE,        'PARTIES - Parent/Guardian - organisation should match';
    ASSERT (SELECT organisation_name FROM parties WHERE party_id = v_party_id_pg)          = '',           'PARTIES - Parent/Guardian - organisation_name should match';
    ASSERT (SELECT surname FROM parties WHERE party_id = v_party_id_pg)                    = 'DoePG',      'PARTIES - Parent/Guardian - surname should match';
    ASSERT (SELECT forenames FROM parties WHERE party_id = v_party_id_pg)                  = 'Jane',       'PARTIES - Parent/Guardian - forenames should match';
    ASSERT (SELECT title FROM parties WHERE party_id = v_party_id_pg)                      IS NULL,        'PARTIES - Parent/Guardian - title should be NULL';
    ASSERT (SELECT address_line_1 FROM parties WHERE party_id = v_party_id_pg)             = '789 Parent St', 'PARTIES - Parent/Guardian - address_line_1 should match';
    ASSERT (SELECT address_line_2 FROM parties WHERE party_id = v_party_id_pg)             = '',           'PARTIES - Parent/Guardian - address_line_2 should match';
    ASSERT (SELECT address_line_3 FROM parties WHERE party_id = v_party_id_pg)             = '',           'PARTIES - Parent/Guardian - address_line_3 should match';
    ASSERT (SELECT address_line_4 FROM parties WHERE party_id = v_party_id_pg)             = '',           'PARTIES - Parent/Guardian - address_line_4 should match';
    ASSERT (SELECT address_line_5 FROM parties WHERE party_id = v_party_id_pg)             = '',           'PARTIES - Parent/Guardian - address_line_5 should match';
    ASSERT (SELECT postcode FROM parties WHERE party_id = v_party_id_pg)                   = '54321',      'PARTIES - Parent/Guardian - postcode should match';
    ASSERT (SELECT birth_date FROM parties WHERE party_id = v_party_id_pg)                 = '1950-01-01', 'PARTIES - Parent/Guardian - birth_date should match';
    ASSERT (SELECT age FROM parties WHERE party_id = v_party_id_pg)                        IS NULL,        'PARTIES - Parent/Guardian - age should be NULL';
    ASSERT (SELECT national_insurance_number FROM parties WHERE party_id = v_party_id_pg)  = 'AB654321C',  'PARTIES - Parent/Guardian - national_insurance_number should match';
    ASSERT (SELECT telephone_home FROM parties WHERE party_id = v_party_id_pg)             = '555-4321',   'PARTIES - Parent/Guardian - telephone_home should match';
    ASSERT (SELECT telephone_business FROM parties WHERE party_id = v_party_id_pg)         = '555-9876',   'PARTIES - Parent/Guardian - telephone_business should match';
    ASSERT (SELECT telephone_mobile FROM parties WHERE party_id = v_party_id_pg)           = '555-6587',   'PARTIES - Parent/Guardian - telephone_mobile should match';
    ASSERT (SELECT email_1 FROM parties WHERE party_id = v_party_id_pg)                    = 'jane.doe@example.com', 'PARTIES - Parent/Guardian - email_1 should match';
    ASSERT (SELECT email_2 FROM parties WHERE party_id = v_party_id_pg)                    = 'j.doe@example.com',    'PARTIES - Parent/Guardian - email_2 should match';
    ASSERT (SELECT last_changed_date FROM parties WHERE party_id = v_party_id_pg)          IS NULL,        'PARTIES - Parent/Guardian - last_changed_date should be NULL';

    -------------------------------
    -- DEBTOR_DETAILS
    -------------------------------
    
    -- Verify fields for the Defendant debtor_detail 
    ASSERT (SELECT vehicle_make FROM debtor_detail WHERE party_id = v_party_id_def)            = 'Honda',           'DEBTOR_DETAILS - Defendant - vehicle_make should match';
    ASSERT (SELECT vehicle_registration FROM debtor_detail WHERE party_id = v_party_id_def)    = 'ABC 9876',        'DEBTOR_DETAILS - Defendant - vehicle_registration should match';
    ASSERT (SELECT employer_name FROM debtor_detail WHERE party_id = v_party_id_def)           = 'XYZ Corp',        'DEBTOR_DETAILS - Defendant - employer_name should match';
    ASSERT (SELECT employer_address_line_1 FROM debtor_detail WHERE party_id = v_party_id_def) = '123 Business Rd', 'DEBTOR_DETAILS - Defendant - employer_address_line_1 should match';
    ASSERT (SELECT employer_address_line_2 FROM debtor_detail WHERE party_id = v_party_id_def) = 'Floor 2',         'DEBTOR_DETAILS - Defendant - employer_address_line_2 should match';
    ASSERT (SELECT employer_address_line_3 FROM debtor_detail WHERE party_id = v_party_id_def) = '',                'DEBTOR_DETAILS - Defendant - employer_address_line_3 should match';
    ASSERT (SELECT employer_address_line_4 FROM debtor_detail WHERE party_id = v_party_id_def) = '',                'DEBTOR_DETAILS - Defendant - employer_address_line_4 should match';
    ASSERT (SELECT employer_address_line_5 FROM debtor_detail WHERE party_id = v_party_id_def) = '',                'DEBTOR_DETAILS - Defendant - employer_address_line_5 should match';
    ASSERT (SELECT employer_postcode FROM debtor_detail WHERE party_id = v_party_id_def)       = '98765',           'DEBTOR_DETAILS - Defendant - employer_postcode should match';
    ASSERT (SELECT employee_reference FROM debtor_detail WHERE party_id = v_party_id_def)      = 'EMP456',          'DEBTOR_DETAILS - Defendant - employee_reference should match';
    ASSERT (SELECT employer_telephone FROM debtor_detail WHERE party_id = v_party_id_def)      = '555-6789',        'DEBTOR_DETAILS - Defendant - employer_telephone should match';
    ASSERT (SELECT employer_email FROM debtor_detail WHERE party_id = v_party_id_def)          = 'hr@xyzcorp.com',  'DEBTOR_DETAILS - Defendant - employer_email should match';
    ASSERT (SELECT document_language FROM debtor_detail WHERE party_id = v_party_id_def)       = 'EN',              'DEBTOR_DETAILS - Defendant - document_language should match';
    ASSERT (SELECT document_language_date FROM debtor_detail WHERE party_id = v_party_id_def)  IS NOT NULL,         'DEBTOR_DETAILS - Defendant - document_language_date should be set';
    ASSERT (SELECT hearing_language FROM debtor_detail WHERE party_id = v_party_id_def)        = 'EN',              'DEBTOR_DETAILS - Defendant - hearing_language should match';
    ASSERT (SELECT hearing_language_date FROM debtor_detail WHERE party_id = v_party_id_def)   IS NOT NULL,         'DEBTOR_DETAILS - Defendant - hearing_language_date should be set';


    -- Verify fields for the parent/guardian debtor_detail 
    ASSERT (SELECT vehicle_make FROM debtor_detail WHERE party_id = v_party_id_pg)            = 'Honda',           'DEBTOR_DETAILS - Parent/Guardian - vehicle_make should match';
    ASSERT (SELECT vehicle_registration FROM debtor_detail WHERE party_id = v_party_id_pg)    = 'ABC 9876',        'DEBTOR_DETAILS - Parent/Guardian - vehicle_registration should match';
    ASSERT (SELECT employer_name FROM debtor_detail WHERE party_id = v_party_id_pg)           = 'ABC Corp',        'DEBTOR_DETAILS - Parent/Guardian - employer_name should match';
    ASSERT (SELECT employer_address_line_1 FROM debtor_detail WHERE party_id = v_party_id_pg) = '456 Business Rd', 'DEBTOR_DETAILS - Parent/Guardian - employer_address_line_1 should match';
    ASSERT (SELECT employer_address_line_2 FROM debtor_detail WHERE party_id = v_party_id_pg) = 'Unit 1',          'DEBTOR_DETAILS - Parent/Guardian - employer_address_line_2 should match';
    ASSERT (SELECT employer_address_line_3 FROM debtor_detail WHERE party_id = v_party_id_pg) = '',                'DEBTOR_DETAILS - Parent/Guardian - employer_address_line_3 should match';
    ASSERT (SELECT employer_address_line_4 FROM debtor_detail WHERE party_id = v_party_id_pg) = '',                'DEBTOR_DETAILS - Parent/Guardian - employer_address_line_4 should match';
    ASSERT (SELECT employer_address_line_5 FROM debtor_detail WHERE party_id = v_party_id_pg) = '',                'DEBTOR_DETAILS - Parent/Guardian - employer_address_line_5 should match';
    ASSERT (SELECT employer_postcode FROM debtor_detail WHERE party_id = v_party_id_pg)       = 'AB1 2ZX',         'DEBTOR_DETAILS - Parent/Guardian - employer_postcode should match';
    ASSERT (SELECT employee_reference FROM debtor_detail WHERE party_id = v_party_id_pg)      = 'EMP123',          'DEBTOR_DETAILS - Parent/Guardian - employee_reference should match';
    ASSERT (SELECT employer_telephone FROM debtor_detail WHERE party_id = v_party_id_pg)      = '555-1234',        'DEBTOR_DETAILS - Parent/Guardian - employer_telephone should match';
    ASSERT (SELECT employer_email FROM debtor_detail WHERE party_id = v_party_id_pg)          = 'work@xyzcorp.com','DEBTOR_DETAILS - Parent/Guardian - employer_email should match';
    ASSERT (SELECT document_language FROM debtor_detail WHERE party_id = v_party_id_pg)       = 'CY',              'DEBTOR_DETAILS - Parent/Guardian - document_language should match';
    ASSERT (SELECT document_language_date FROM debtor_detail WHERE party_id = v_party_id_pg)  IS NOT NULL,         'DEBTOR_DETAILS - Parent/Guardian - document_language_date should be set';
    ASSERT (SELECT hearing_language FROM debtor_detail WHERE party_id = v_party_id_pg)        = 'CY',              'DEBTOR_DETAILS - Parent/Guardian - hearing_language should match';
    ASSERT (SELECT hearing_language_date FROM debtor_detail WHERE party_id = v_party_id_pg)   IS NOT NULL,         'DEBTOR_DETAILS - Parent/Guardian - hearing_language_date should be set';

    -------------------------------
    -- ALIASES
    -------------------------------

    -- Verify fields for the Defendant debtor_detail - aliases
    ASSERT (SELECT COUNT(1) FROM aliases WHERE party_id = v_party_id_def) = 2,  'ALIASES - Defendant - There should be 2 records';
    ASSERT (SELECT surname FROM aliases WHERE party_id = v_party_id_def AND sequence_number = 1)      = 'Doe',   'ALIASES[1] - Defendant - surname should match';
    ASSERT (SELECT forenames FROM aliases WHERE party_id = v_party_id_def AND sequence_number = 1)    = 'Jonny', 'ALIASES[1] - Defendant - forenames should match';
    ASSERT (SELECT organisation_name FROM aliases WHERE party_id = v_party_id_def AND sequence_number = 1) = '', 'ALIASES[1] - Defendant - organisation_name should match';
    ASSERT (SELECT surname FROM aliases WHERE party_id = v_party_id_def AND sequence_number = 2)      = 'ADoe',  'ALIASES[2] - Defendant - surname should match';
    ASSERT (SELECT forenames FROM aliases WHERE party_id = v_party_id_def AND sequence_number = 2)    = 'Jon',   'ALIASES[2] - Defendant - forenames should match';
    ASSERT (SELECT organisation_name FROM aliases WHERE party_id = v_party_id_def AND sequence_number = 2) IS NULL, 'ALIASES[2] - Defendant - organisation_name should match';


    -- Verify fields for the parent/guardian debtor_detail - aliases
    ASSERT (SELECT COUNT(1) FROM aliases WHERE party_id = v_party_id_pg) = 1,  'ALIASES - Parent/Guardian - There should be 1 record';
    ASSERT (SELECT surname FROM aliases WHERE party_id = v_party_id_pg AND sequence_number = 1)      = 'Doe',   'ALIASES[1] - Parent/Guardian - surname should match';
    ASSERT (SELECT forenames FROM aliases WHERE party_id = v_party_id_pg AND sequence_number = 1)    = 'Janie', 'ALIASES[1] - Parent/Guardian - forenames should match';
    ASSERT (SELECT organisation_name FROM aliases WHERE party_id = v_party_id_pg AND sequence_number = 1) = '', 'ALIASES[1] - Parent/Guardian - organisation_name should match';

    -------------------------------
    -- FIXED_PENALTY_OFFENCES
    -------------------------------

    -- Verify fields
    ASSERT (SELECT ticket_number FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = 'FP12345', 'FIXED_PENALTY_OFFENCES - ticket_number should match';
    ASSERT (SELECT vehicle_registration FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = 'AB12XYZ', 'FIXED_PENALTY_OFFENCES - vehicle_registration should match';
    ASSERT (SELECT offence_location FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = 'Main Street', 'FIXED_PENALTY_OFFENCES - offence_location should match';
    ASSERT (SELECT notice_number FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = 'NOTICE1', 'FIXED_PENALTY_OFFENCES - notice_to_owner_hirer should match';
    ASSERT (SELECT issued_date FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = '2024-06-15', 'FIXED_PENALTY_OFFENCES - issued_date should match';
    ASSERT (SELECT licence_number FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = 'ABCD12345Z', 'FIXED_PENALTY_OFFENCES - licence_number should match';
    ASSERT (SELECT vehicle_fixed_penalty FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = TRUE, 'FIXED_PENALTY_OFFENCES - vehicle_fixed_penalty should be TRUE';
    ASSERT (SELECT offence_date FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'FIXED_PENALTY_OFFENCES - offence_date should be NULL';
    ASSERT (SELECT offence_time FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = '15:38', 'FIXED_PENALTY_OFFENCES -  should match';
    

    RAISE NOTICE 'TEST 3 PASSED: Created account % with ID % and payment card request', v_account_number, v_defendant_account_id;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Verify originator_type logic when fp_ticket_detail is absent
----------------------------------------------------------------------------------------------------------------------
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
    RAISE NOTICE '=== TEST 4: Verify originator_type logic when fp_ticket_detail is absent ===';
    
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

    RAISE NOTICE 'TEST 4 PASSED: Verified originator_type logic when fp_ticket_detail is absent';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Test handling of missing required fields   -- Untouched in v2.0
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       bigint            := 10005;
    v_business_unit_id       smallint          := 65;
    v_posted_by              character varying := 'L045EO';
    v_posted_by_name         character varying := 'Tester 1';
    v_account_json           json;
    v_account_number         character varying;
    v_defendant_account_id   bigint;
    v_error_caught           boolean := FALSE;
BEGIN
    RAISE NOTICE '=== TEST 5: Test handling of missing required fields ===';
    
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

    RAISE NOTICE 'TEST 5 PASSED: Error handling works correctly for missing required fields';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Test handling da_account_type_cc check constraint violation
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT      := 10006;
    v_business_unit_id          SMALLINT    := 60;
    v_posted_by                 VARCHAR     := 'L045EO';
    v_posted_by_name            VARCHAR     := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN     := FALSE;
    v_pg_exception_detail       TEXT;
    v_row_count                 INTEGER;
BEGIN
    RAISE NOTICE '=== TEST 6: Test handling da_account_type_cc check constraint violation ===';
    
    -- Prepare JSON with invalid account_type (i.e. fine (all in lowercase). Valid values are: 'Fixed Penalty', 'Fine', 'Conditional Caution', 'Confiscation')
    v_account_json := '{
        "account_type": "fine",
        "defendant_type": "adultOrYouthOnly",
        "originator_name": "LJS",
        "originator_id": "12345",
        "enforcement_court_id": 650000000045,
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "defendant": {
            "company_flag": true,
            "address_line_1": "789 Parent St"
        },
        "offences": [
            {
                "date_of_sentence": "2024-06-15",
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 50.00
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345"
        },
        "payment_terms": {
            "payment_terms_type_code": "B"
        }
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    BEGIN
        DELETE FROM account_number_index WHERE business_unit_id = v_business_unit_id;
        COMMIT;
    END;

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
            --Check for specific FK violations (i.e. imposing_court_id, offence_id, result_id)
            GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;

            IF SQLERRM ILIKE '%da_account_type_cc%' THEN 
                v_error_caught := TRUE;
            ELSE
                v_error_caught := FALSE;
                RAISE WARNING 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
                RAISE WARNING 'Error detail: %', v_pg_exception_detail;
            END IF;
    END;

    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'An error should have been raised due to da_account_type_cc check constraint violation';

    -- Verify rollback happened
    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 6 PASSED: Error handling works correctly for da_account_type_cc check constraint violation.';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Test handling when defendant_type = 'pgToPay' AND parent_guardian Json is missing
--         Expected exception: P2002 - Missing parent/guardian
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       bigint            := 10007;
    v_business_unit_id       smallint          := 65;
    v_posted_by              character varying := 'L045EO';
    v_posted_by_name         character varying := 'Tester 1';
    v_account_json           json;
    v_account_number         character varying;
    v_defendant_account_id   bigint;
    v_error_caught           boolean := FALSE;
    v_expected_errmsg        varchar    := 'Missing parent/guardian';
BEGIN
    RAISE NOTICE '=== TEST 7: Test handling when defendant_type = "pgToPay" AND parent_guardian Json is missing = P2002 ===';
    
    -- Prepare JSON with missing parent_guardian Json object
    v_account_json := '{
        "account_type": "Fine",
        "defendant_type": "pgToPay",
        "originator_name": "LJS",
        "originator_id": "12345",
        "prosecutor_case_reference": "ABC123",
        "enforcement_court_id": 650000000045,
        "collection_order_made": true,
        "collection_order_date": "2024-12-01",
        "suspended_committal_date": "2024-12-15",
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "defendant": {
            "company_flag": false,
            "title": "Mr",
            "surname": "Doe",
            "company_name": null,
            "forenames": "John",
            "dob": "1980-01-01",
            "address_line_1": "789 Some St",
            "address_line_2": null,
            "address_line_3": "",
            "address_line_4": "",
            "address_line_5": "",
            "post_code": "54321",
            "telephone_number_home": "555-4321",
            "telephone_number_business": "555-8765",
            "telephone_number_mobile": "555-5678",
            "email_address_1": "john.doe@example.com",
            "email_address_2": "j.doe@example.com",
            "national_insurance_number": "AB123456C",
            "debtor_detail": {
                "vehicle_make": "Honda",
                "vehicle_registration_mark": "ABC 9876",
                "document_language": "EN",
                "hearing_language": "EN",
                "employee_reference": "EMP456",
                "employer_company_name": "XYZ Corp",
                "employer_address_line_1": "123 Business Rd",
                "employer_address_line_2": "Floor 2",
                "employer_address_line_3": "",
                "employer_address_line_4": "",
                "employer_address_line_5": "",
                "employer_post_code": "98765",
                "employer_telephone_number": "555-6789",
                "employer_email_address": "hr@xyzcorp.com",
                "aliases": [
                    {
                        "alias_forenames": "Jonny",
                        "alias_surname": "Doe",
                        "alias_company_name": ""
                    },
                    {
                        "alias_forenames": "Jon",
                        "alias_surname": "ADoe",
                        "alias_company_name": null
                    }
                ]
            }
        }
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
        'Fine', -- table has a value but JSON doesn't
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2002 exception
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
        WHEN SQLSTATE 'P2002' THEN
            IF SQLERRM = v_expected_errmsg THEN
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
    ASSERT v_error_caught = TRUE, 'A P2002 error should have been raised due to missing parent_guardian details';

    RAISE NOTICE 'TEST 7 PASSED: Error handling works correctly for missing parent_guardian details';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 8: Test handling when account_type = 'Fixed Penalty' but notice_number field is missing (Json null)
--         Expected exception: P2011 - Missing ticket number
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       bigint     := 10008;
    v_business_unit_id       smallint   := 65;
    v_posted_by              varchar    := 'L045EO';
    v_posted_by_name         varchar    := 'Tester 1';
    v_account_json           json;
    v_account_number         varchar;
    v_defendant_account_id   bigint;
    v_error_caught           boolean    := FALSE;
    v_expected_errmsg        varchar    := 'Missing ticket number';
BEGIN
    RAISE NOTICE '=== TEST 8: Test handling when account_type = "Fixed Penalty" but notice_number field is missing (Json null) = P2011 ===';
    
    -- Prepare JSON with missing parent_guardian Json object
    v_account_json := '{
        "account_type": "Fixed Penalty",
        "defendant_type": "adultOrYouthOnly",
        "originator_name": "LJS",
        "originator_id": "12345",
        "prosecutor_case_reference": "ABC123",
        "enforcement_court_id": 650000000045,
        "collection_order_made": true,
        "collection_order_date": "2024-12-01",
        "suspended_committal_date": "2024-12-15",
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "defendant": {
            "company_flag": false,
            "title": "Mr",
            "surname": "Doe",
            "company_name": null,
            "forenames": "John",
            "dob": "1980-01-01",
            "address_line_1": "789 Some St",
            "address_line_2": null,
            "address_line_3": "",
            "address_line_4": "",
            "address_line_5": "",
            "post_code": "54321",
            "telephone_number_home": "555-4321",
            "telephone_number_business": "555-8765",
            "telephone_number_mobile": "555-5678",
            "email_address_1": "john.doe@example.com",
            "email_address_2": "j.doe@example.com",
            "national_insurance_number": "AB123456C",
            "debtor_detail": {
                "vehicle_make": "Honda",
                "vehicle_registration_mark": "ABC 9876",
                "document_language": "EN",
                "hearing_language": "EN",
                "employee_reference": "EMP456",
                "employer_company_name": "XYZ Corp",
                "employer_address_line_1": "123 Business Rd",
                "employer_address_line_2": "Floor 2",
                "employer_address_line_3": "",
                "employer_address_line_4": "",
                "employer_address_line_5": "",
                "employer_post_code": "98765",
                "employer_telephone_number": "555-6789",
                "employer_email_address": "hr@xyzcorp.com",
                "aliases": [
                    {
                        "alias_forenames": "Jonny",
                        "alias_surname": "Doe",
                        "alias_company_name": ""
                    },
                    {
                        "alias_forenames": "Jon",
                        "alias_surname": "ADoe",
                        "alias_company_name": null
                    }
                ]
            }
        },
        "fp_ticket_detail": {
            "notice_number": null,
            "date_of_issue": "2024-06-15",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        }
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
        'Fine', -- table has a value but JSON doesn't
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2002 exception
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
        WHEN SQLSTATE 'P2011' THEN 

            IF SQLERRM = v_expected_errmsg THEN
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
    ASSERT v_error_caught = TRUE, 'A P2011 error, with correct SQLERRM, should have been raised due to missing notice_number field';

    RAISE NOTICE 'TEST 8 PASSED: Error handling works correctly for missing notice_number field';
END $$;

-- Cleanup test data
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';
    
    -- Delete all test accounts created by these tests
    DELETE FROM draft_accounts 
    WHERE draft_account_id BETWEEN 10001 AND 10010
    OR draft_account_id = 10022;

    DELETE FROM aliases;
    DELETE FROM debtor_detail;
    DELETE FROM fixed_penalty_offences;
    DELETE FROM defendant_account_parties;
    DELETE FROM parties;
    --DELETE FROM payment_card_requests;
    DELETE FROM defendant_accounts;
    DELETE FROM account_number_index;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing