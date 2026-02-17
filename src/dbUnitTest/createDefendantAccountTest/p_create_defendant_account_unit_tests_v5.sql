\timing

/**
* CGI Opal Program
*
* MODULE      : p_create_defendant_account_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_create_defendant_account.
*               These tests cover various scenarios to ensure the procedure
*               correctly processes defendant account creation.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------
* 16/07/2025    C Cho       1.0         Unit tests for p_create_defendant_account.
* 30/07/2025    TMc         2.0         Added unit tests for p_create_defendant_parties, p_create_debtor_details, p_create_aliases, p_create_fp_offences
*                                       Aligned to version 3.0 of p_create_defendant_account
* 04/08/2025    TMc         3.0         Amended unit tests for p_create_impositions, p_get_creditor_account, p_create_minor_creditor, p_create_enforcemenets,
*                                                              p_create_account_notes, p_create_payment_terms
*                                       Added to Test 3 (Full account details)
*                                       Removed test 4 (Verify originator_type logic when fp_ticket_detail is absent). Already checked in test 1.
*                                       Aligned to version 4.0 of p_create_defendant_account
* 26/08/2025    TMc         3.1         Amended tests 2, 3 and 8B after amended payment terms logic in p_create_payment_terms
* 27/08/2025    TMc         3.2         PO-2084 - Amended test 3. FIXED_PENALTY_OFFENCES.OFFENCE_DATE is now being populated.
* 28/08/2025    TMc         3.3         PO-2096 - Amended tests 1, 2 and 3. Included checks for new columns IMPOSITIONS.ORIGINATOR_NAME and ORIGINAL_IMPOSITION_ID
*                                       Aligned to version 4.1 of p_create_defendant_account
* 28/08/2025    TMc         3.4         PO-2099 - Amended tests 1, 2 and 3. 
*                                       Added checks for VERSION_NUMBER column on DEFENDANT_ACCOUNTS and CREDITOR_ACCOUNTS. Should be set to 1
*                                       Aligned to version 4.2 of p_create_defendant_account
* 02/09/2025    TMc         3.5         PO-2115 - Amended tests 2 and 3. PAYMENT_TERMS.EFFECTIVE_DATE no longer has time component (i.e. should be 00:00)
* 03/09/2025    TMc         3.6         PO-1044, PO-2118 - Amended tests 1,2 and 3. 
*                                       DEFENDANT_ACCOUNTS.CHEQUE_CLEARANCE_PERIOD, CREDIT_TRANS_CLEARANCE_PERIOD and PAYMENT_CARD_REQUESTED_BY_NAME are being populated.
*                                       Alligned to version 4.3 of p_create_defendant_account
* 13/10/2025    CL          4.0         PO-2291 - The originator_name column has been removed from the impositions table, so removed related checks from tests 1, 2 and 3.
* 03/02/2026    TMc         5.0         PO-2751 - Amended tests 1, 2 and 3  
*                                                 Add checks for new column DEFENDANT_ACCOUNTS.IMPOSED_BY_NAME. 
*                                                 Logic for originator_type and report_id has changed, so updated checks and test data (enforcement_court_id and associated BU) 
*                                                 so it returns data when joined to LOCAL_JUSTICE_AREAS.
**/

-- Clear out tables - Added in v2.0
DO LANGUAGE 'plpgsql' $$
DECLARE

BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Delete all test accounts created by these tests
    DELETE FROM draft_accounts WHERE draft_account_id BETWEEN 10001 AND 10020;
    
    DELETE FROM control_totals WHERE control_total_id >= 60000000000000;
    DELETE FROM allocations WHERE allocation_id >= 60000000000000;
    DELETE FROM impositions WHERE imposition_id >= 60000000000000;
    DELETE FROM creditor_transactions WHERE creditor_account_id >= 60000000000000;
    DELETE FROM creditor_accounts WHERE creditor_account_id >= 60000000000000;
    DELETE FROM defendant_transactions WHERE defendant_transaction_id >= 60000000000000;
    DELETE FROM document_instances WHERE document_instance_id >= 60000000000000;
    DELETE FROM payment_terms WHERE payment_terms_id >= 60000000000000;
    DELETE FROM enforcements WHERE enforcement_id >= 60000000000000;
    DELETE FROM notes WHERE note_id >= 60000000000000;
    DELETE FROM report_entries WHERE report_entry_id >= 60000000000000;
    DELETE FROM aliases WHERE alias_id >= 60000000000000;
    DELETE FROM debtor_detail WHERE party_id >= 60000000000000;
    DELETE FROM fixed_penalty_offences WHERE defendant_account_id >= 60000000000000;
    DELETE FROM defendant_account_parties WHERE defendant_account_party_id >= 60000000000000;
    DELETE FROM parties WHERE party_id >= 60000000000000;
    DELETE FROM payment_card_requests WHERE defendant_account_id >= 60000000000000;
    DELETE FROM defendant_accounts WHERE defendant_account_id >= 60000000000000;
    DELETE FROM account_number_index WHERE account_number_index_id >= 60000000000000;

    --Set sequence for tables that already contain data
    PERFORM setval('creditor_account_id_seq', 60000000000000, false);

    COMMIT;

    RAISE NOTICE 'Data cleanup before tests completed';
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 1: Basic account creation with minimum required fields - account_type NOT 'Fixed Penalty'
--         Version 3.3: Updated IMPOSITIONS record check
--         Version 3.4: Added check for DEFENDANT_ACCOUNTS.VERSION_NUMBER
--         Verison 3.6: Added checks for DEFENDANT_ACCOUNTS.CHEQUE_CLEARANCE_PERIOD, CREDIT_TRANS_CLEARANCE_PERIOD and PAYMENT_CARD_REQUESTED_BY_NAME
--         Version 5.0: Added checks for DEFENDANT_ACCOUNTS.IMPOSED_BY_NAME and ORIGINATOR_TYPE (Now taken from new JSON field)
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       BIGINT     := 10001;
    v_business_unit_id       SMALLINT   := 65;
    v_posted_by              VARCHAR    := 'L045EO';
    v_posted_by_name         VARCHAR    := 'Tester 1';
    v_account_json           JSON; 
    v_account_number         VARCHAR;
    v_defendant_account_id   BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 1: Basic account creation with minimum required fields ===';
    
    -- Prepare minimal test JSON
    -- v2.0 - Added other required objects/fields: defendant_type (values: adultOrYouthOnly, pgToPay, company)
    --                                             defendant (company_flag, address_line_1), payment_card_request
    --                                             offences -> offence (date_of_sentence, offence_id, impositions (result_id, amount_imposed, amount_paid))
    --                                             payment_terms (payment_terms_type_code (values: B,P,I)) 
    -- v3.0 - Added debtor_detail, minimum fields, for the defendant.
    -- v5.0 - Added new field originator_type
    v_account_json := '{
        "account_type": "Fine",
        "defendant_type": "adultOrYouthOnly",
        "originator_name": "LJS",
        "originator_id": "12345",
        "originator_type": "TFO",
        "enforcement_court_id": 650000000045,
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "defendant": {
            "company_flag": true,
            "address_line_1": "789 Parent St",
            "debtor_detail": {
                "document_language": "EN"
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
        "payment_terms": {
            "payment_terms_type_code": "P"
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

    -- Verify defendant account columns (amount_imposed, amount_paid, account_balance) were updated correctly by p_create_impositions
    ASSERT EXISTS (
        SELECT 1 FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id
                                           AND amount_imposed = -100 AND amount_paid = 50 AND account_balance = -50
    ), 'Defendant account values (amount_imposed, amount_paid, account_balance) were NOT updated correctly';
    
    -- Verify version_number column on defendant_accounts was set correctly
    ASSERT (SELECT version_number FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 1,
           'Version number should be set to 1';  --v3.4

    -- Verify originator_type column on defendant_accounts was set correctly   v5.0
    ASSERT (SELECT originator_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'TFO',
           'Originator type should match';
    
    -- Verify originator_name column on defendant_accounts was set correctly   v5.0
    ASSERT (SELECT originator_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'LJS',
           'Originator name should match';
    
    -- Verify imposed_by_name column on defendant_accounts was set correctly, as not FP it should match originator_name   v5.0
    ASSERT (SELECT imposed_by_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'LJS',
           'Imposed by name should match';
    
    -- Verify defendant_accounts.cheque_clearance_period, credit_trans_clearance_period and payment_card_requested_by_name columns have been populated correctly  v3.6
    ASSERT (SELECT cheque_clearance_period FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = (SELECT item_value::int2 FROM configuration_items WHERE item_name = 'DEFAULT_CHEQUE_CLEARANCE_PERIOD'),
           'Cheque clearance period should match value on CONFIGURATION_ITEMS for DEFAULT_CHEQUE_CLEARANCE_PERIOD';
    
    ASSERT (SELECT credit_trans_clearance_period FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = (SELECT item_value::int2 FROM configuration_items WHERE item_name = 'DEFAULT_CREDIT_TRANS_CLEARANCE_PERIOD'),
           'Credit trans clearance period should match value on CONFIGURATION_ITEMS for DEFAULT_CREDIT_TRANS_CLEARANCE_PERIOD';
    
    ASSERT (SELECT payment_card_requested_by_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = v_posted_by_name,
           'Payment card requested by name should match';

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

    -- Verify the impositions record for the defendant was created as expected and the correct creditor account was found  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM impositions WHERE defendant_account_id = v_defendant_account_id 
                                    AND result_id = 'FO' AND offence_id = 30000 AND creditor_account_id = 65 AND imposed_amount = -100.00 AND paid_amount = 50.00
                                    AND imposed_date = '2024-06-15' AND offence_code = 'AA60005' AND offence_title = 'Person having charge abandoning animal'
                                    AND original_imposition_id IS NULL --v4.0
    ), 'Defendant impositions record should exist in the database';

    -- Verify the impositions record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM control_totals ct
                 JOIN impositions i
                   ON ct.associated_record_id = i.imposition_id::VARCHAR
                WHERE i.defendant_account_id = v_defendant_account_id 
                  AND ct.business_unit_id = v_business_unit_id AND ct.associated_record_type = 'impositions' AND ct.item_number = 208 AND ct.amount = -50
    ), 'Control_totals record, related to the imposition, should exist in the database';

    -- Verify the defendant_transactions record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id 
                                               AND transaction_type = 'TFO IN' AND transaction_amount = 50
    ), 'Defendant defendant_transactions record should exist in the database';

    -- Verify the allocations record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM allocations a
                 JOIN impositions i
                   ON a.imposition_id = i.imposition_id
                 JOIN defendant_transactions dt 
                   ON a.defendant_transaction_id = dt.defendant_transaction_id
                WHERE i.defendant_account_id = v_defendant_account_id AND dt.defendant_account_id = v_defendant_account_id 
                  AND a.allocated_amount = 50 AND a.transaction_type = 'TFO IN' AND a.allocation_function = 'MAC'
    ), 'Allocations record, related to the imposition and defendant_transaction, should exist in the database';


    -- Verify the payment_card_requests record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM payment_card_requests WHERE defendant_account_id = v_defendant_account_id 
    ), 'Defendant payment_card_requests record should exist in the database';

    -- Verify the payment_terms record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM payment_terms WHERE defendant_account_id = v_defendant_account_id 
                                      AND terms_type_code = 'P' AND effective_date IS NULL AND active = TRUE 
                                      AND account_balance = -50
    ), 'Defendant payment_terms record should exist in the database';


    -- Verify the document_instances record for 'TFO Order' for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM document_instances
                WHERE associated_record_id = v_defendant_account_id::VARCHAR AND business_unit_id = v_business_unit_id
                  AND associated_record_type = 'defendant_account'  AND status = 'New'
                  AND document_id = 'FINOT'
    ), 'Defendant document_instances records for "TFO Order" should exist in the database';

    -- Verify the document_instances records for 'TFO Letter' for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM document_instances
                WHERE associated_record_id = v_defendant_account_id::VARCHAR AND business_unit_id = v_business_unit_id
                  AND associated_record_type = 'defendant_account'  AND status = 'New'
                  AND document_id = 'FINOTA'
    ), 'Defendant document_instances records for "TFO Letter" should exist in the database';

    -- Verify NO document_instances records for 'compensation notice'/'COMPLETT' were created, as expected  v3.0
    ASSERT (
        SELECT COUNT(*) FROM document_instances
                WHERE business_unit_id = v_business_unit_id
                  AND associated_record_type = 'impositions'
                  AND document_id = 'COMPLETT'
    ) = 0, 'Defendant document_instances records for "COMPLETT" should NOT exist in the database';


    -- Verify the report_entries record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM report_entries WHERE associated_record_id = v_defendant_account_id::VARCHAR AND business_unit_id = v_business_unit_id
                                       AND associated_record_type = 'defendant_accounts' AND report_id = 'tfo_in_register'
    ), 'Defendant report_entries record should exist in the database';

    RAISE NOTICE 'TEST 1 PASSED: Created account % with ID %', v_account_number, v_defendant_account_id;

END $$;

-----------------------------------------------------------------------------------------------------------------------------------------
-- Test 2: Basic account creation with minimum required fields when account_type = 'Fixed Penalty'  Added in v2.0
--         Expanded in v3.0 similar to test 1 but with payment_terms logic relating to 'Fixed Penalty' (i.e. payment_terms_type_code = B)
--         and payment_terms default_days_in_jail has a value (test update to the defendant_accounts table)
--         Version 3.1: Set effective_date to null (must be NULL when account_type = 'Fixed Penalty' and payment_terms = 'B')
--         Version 3.3: Updated IMPOSITIONS record check
--         Version 3.4: Added check for DEFENDANT_ACCOUNTS.VERSION_NUMBER
--         Version 3.5: PAYMENT_TERMS.EFFECTIVE_DATE no longer has time component (i.e. should be 00:00)
--         Verison 3.6: Added checks for DEFENDANT_ACCOUNTS.CHEQUE_CLEARANCE_PERIOD, CREDIT_TRANS_CLEARANCE_PERIOD and PAYMENT_CARD_REQUESTED_BY_NAME
--         Version 5.0: Added checks for DEFENDANT_ACCOUNTS.IMPOSED_BY_NAME and ORIGINATOR_TYPE (Now taken from new JSON field).
-----------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       BIGINT     := 10002;
    v_business_unit_id       SMALLINT   := 47;
    v_posted_by              VARCHAR    := 'L045EO';
    v_posted_by_name         VARCHAR    := 'Tester 2';
    v_account_json           JSON;
    v_account_number         VARCHAR;
    v_defendant_account_id   BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 2: Basic account creation with minimum required fields and account_type = "Fixed Penalty", payment_terms "B" with effective_date = NULL and jail_days';
    
    v_account_json := '{
        "account_type": "Fixed Penalty",
        "defendant_type": "adultOrYouthOnly",
        "originator_name": "LJS",
        "originator_id": "12345",
        "originator_type": "FP",
        "enforcement_court_id": 470000000013,
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "defendant": {
            "company_flag": true,
            "address_line_1": "789 Parent St",
            "debtor_detail": {
                "document_language": "EN"
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
            "notice_number": "FP12345"
        },
        "payment_terms": {
            "payment_terms_type_code": "B",
            "effective_date": null,
            "default_days_in_jail": 14
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

    -- Verify defendant account columns (amount_imposed, amount_paid, account_balance) were updated correctly by p_create_impositions
    ASSERT EXISTS (
        SELECT 1 FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id
                                           AND amount_imposed = -100 AND amount_paid = 50 AND account_balance = -50
    ), 'Defendant account values (amount_imposed, amount_paid, account_balance) were NOT updated correctly';
    
    -- Verify version_number column on defendant_accounts was set correctly
    ASSERT (SELECT version_number FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 1,
           'Version number should be set to 1';  --v3.4

    -- Verify defendant account jail_days column was updated correctly by p_create_payment_terms
    ASSERT EXISTS (
        SELECT 1 FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id
                                           AND jail_days = 14
    ), 'Defendant account jail_days value was NOT updated correctly';
    
    -- Verify originator_type column on defendant_accounts was set correctly   v5.0
    ASSERT (SELECT originator_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'FP',
           'Originator type should match';
    
    -- Verify originator_name column on defendant_accounts was set correctly   v5.0
    ASSERT (SELECT originator_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'LJS',
           'Originator name should match';
    
    -- Verify imposed_by_name column on defendant_accounts was set correctly   v5.0
    ASSERT (SELECT imposed_by_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = (SELECT lja.name 
                                                                                                                     FROM courts c 
                                                                                                                     JOIN local_justice_areas lja ON lja.local_justice_area_id = c.local_justice_area_id
                                                                                                                    WHERE c.court_id = 470000000013),
           'Imposed by name should match local_justice_areas.name for the related enforcement_court_id';
    
    -- Verify defendant_accounts.cheque_clearance_period, credit_trans_clearance_period and payment_card_requested_by_name columns have been populated correctly  v3.6
    ASSERT (SELECT cheque_clearance_period FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = (SELECT item_value::int2 FROM configuration_items WHERE item_name = 'DEFAULT_CHEQUE_CLEARANCE_PERIOD'),
           'Cheque clearance period should match value on CONFIGURATION_ITEMS for DEFAULT_CHEQUE_CLEARANCE_PERIOD';
    
    ASSERT (SELECT credit_trans_clearance_period FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = (SELECT item_value::int2 FROM configuration_items WHERE item_name = 'DEFAULT_CREDIT_TRANS_CLEARANCE_PERIOD'),
           'Credit trans clearance period should match value on CONFIGURATION_ITEMS for DEFAULT_CREDIT_TRANS_CLEARANCE_PERIOD';
    
    ASSERT (SELECT payment_card_requested_by_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = v_posted_by_name,
           'Payment card requested by name should match';

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

    
    -- Verify the impositions record for the defendant was created as expected and the correct creditor account was found  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM impositions WHERE defendant_account_id = v_defendant_account_id 
                                    AND result_id = 'FO' AND offence_id = 30000 AND creditor_account_id = 47 AND imposed_amount = -100.00 AND paid_amount = 50.00
                                    AND imposed_date = '2024-06-15' AND offence_code = 'AA60005' AND offence_title = 'Person having charge abandoning animal'
                                    AND original_imposition_id IS NULL --v4.0
    ), 'Defendant impositions record should exist in the database';

    -- Verify the control_totals record for the imposition was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM control_totals ct
                 JOIN impositions i
                   ON ct.associated_record_id = i.imposition_id::VARCHAR
                WHERE i.defendant_account_id = v_defendant_account_id 
                  AND ct.business_unit_id = v_business_unit_id AND ct.associated_record_type = 'impositions' AND ct.item_number = 208 AND ct.amount = -50
    ), 'Control_totals record, related to the imposition, should exist in the database';

    -- Verify the defendant_transactions record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id 
                                               AND transaction_type = 'TFO IN' AND transaction_amount = 50
    ), 'Defendant defendant_transactions record should exist in the database';

    -- Verify the allocations record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM allocations a
                 JOIN impositions i
                   ON a.imposition_id = i.imposition_id
                 JOIN defendant_transactions dt 
                   ON a.defendant_transaction_id = dt.defendant_transaction_id
                WHERE i.defendant_account_id = v_defendant_account_id AND dt.defendant_account_id = v_defendant_account_id 
                  AND a.allocated_amount = 50 AND a.transaction_type = 'TFO IN' AND a.allocation_function = 'MAC'
    ), 'Allocations record, related to the imposition and defendant_transaction, should exist in the database';


    -- Verify the payment_card_requests record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM payment_card_requests WHERE defendant_account_id = v_defendant_account_id 
    ), 'Defendant payment_card_requests record should exist in the database';

    -- Verify the payment_terms record for the defendant was created as expected   v3.0
    ASSERT EXISTS (
        SELECT 1 FROM payment_terms WHERE defendant_account_id = v_defendant_account_id 
                                      --AND terms_type_code = 'B' AND effective_date = '2025-01-29' AND active = TRUE   --Commented out in v3.1
                                      --AND terms_type_code = 'B' AND effective_date::date = (CURRENT_TIMESTAMP + INTERVAL '28 days')::date AND active = TRUE  --v3.1  Commented out in v3.5
                                      AND terms_type_code = 'B' AND effective_date::date = (CURRENT_DATE + INTERVAL '28 days') AND active = TRUE  --v3.5
                                      AND account_balance = -50
    ), 'Defendant payment_terms record should exist in the database';


    -- Verify the document_instances record for 'TFO Order' for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM document_instances
                WHERE associated_record_id = v_defendant_account_id::VARCHAR AND business_unit_id = v_business_unit_id
                  AND associated_record_type = 'defendant_account'  AND status = 'New'
                  AND document_id = 'FINOR'
    ), 'Defendant document_instances records for "TFO Order" should exist in the database';

    -- Verify the document_instances records for 'TFO Letter' for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM document_instances
                WHERE associated_record_id = v_defendant_account_id::VARCHAR AND business_unit_id = v_business_unit_id
                  AND associated_record_type = 'defendant_account'  AND status = 'New'
                  AND document_id = 'FINOTA'
    ), 'Defendant document_instances records for "TFO Letter" should exist in the database';

    -- Verify NO document_instances records for 'compensation notice'/'COMPLETT' were created, as expected  v3.0
    ASSERT (
        SELECT COUNT(*) FROM document_instances
                WHERE business_unit_id = v_business_unit_id
                  AND associated_record_type = 'impositions'
                  AND document_id = 'COMPLETT'
    ) = 0, 'Defendant document_instances records for "COMPLETT" should NOT exist in the database';


    -- Verify the report_entries record for the defendant was created as expected  v3.0
    ASSERT EXISTS (
        SELECT 1 FROM report_entries WHERE associated_record_id = v_defendant_account_id::VARCHAR AND business_unit_id = v_business_unit_id
                                       AND associated_record_type = 'defendant_accounts' AND report_id = 'fp_register'
    ), 'Defendant report_entries record should exist in the database';

    RAISE NOTICE 'TEST 2 PASSED: Created account % with ID %', v_account_number, v_defendant_account_id;
END $$;

-------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 3: Full account creation with all fields including payment card request  -  account_type = 'Fixed Penalty', debtor is the parent/guardian
--         Version 3.1: Changed payment_terms from 'I' to 'B' (because account_type = FP) and set effective_date to null (must be NULL when account_type = 'Fixed Penalty' and payment_terms = 'B')
--         Version 3.3: Updated IMPOSITIONS record checks for new columns
--         Version 3.4: Added check for VERSION_NUMBER on DEFENDANT_ACCOUNTS and CREDITOR_ACCOUNTS
--         Version 3.5: PAYMENT_TERMS.EFFECTIVE_DATE no longer has time component (i.e. should be 00:00)
--         Verison 3.6: Added checks for DEFENDANT_ACCOUNTS.CHEQUE_CLEARANCE_PERIOD, CREDIT_TRANS_CLEARANCE_PERIOD and PAYMENT_CARD_REQUESTED_BY_NAME
--         Version 5.0: Added checks for DEFENDANT_ACCOUNTS.IMPOSED_BY_NAME and ORIGINATOR_TYPE (Now taken from new JSON field).
-------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id              BIGINT      := 10003;
    v_business_unit_id              SMALLINT    := 47;
    v_posted_by                     VARCHAR     := 'L045EO';
    v_posted_by_name                VARCHAR     := 'Tester 3';
    v_account_json                  JSON;
    v_account_number                VARCHAR;
    v_defendant_account_id          BIGINT;

    v_party_id_def                  BIGINT; 
    v_party_id_pg                   BIGINT;
    v_is_debtor_def                 BOOLEAN; 
    v_is_debtor_pg                  BOOLEAN;

    v_creditor_account_id           BIGINT;
    v_party_id_minor_creditor       BIGINT;
    v_minor_creditor_account_number VARCHAR;
    v_imposition_id_FO              BIGINT;
    v_imposition_id_FCOMP           BIGINT;
    v_defendant_transaction_id      BIGINT;
    v_record                        RECORD;
BEGIN 
    RAISE NOTICE '=== TEST 3: Full account creation with all fields including payment card request - account_type = "Fixed Penalty", debtor is the parent/guardian ===';
    
    -- Use the comprehensive JSON with account_type as Fixed Penalty
    v_account_json := '{
        "account_type": "Fixed Penalty",
        "defendant_type": "pgToPay",
        "originator_name": "LJS",
        "originator_id": "12345", 
        "originator_type": "FP",
        "prosecutor_case_reference": "ABC123",
        "enforcement_court_id": 470000000013,
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
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 40.00,
                        "major_creditor_id": null,
                        "minor_creditor": {
                            "company_flag": false,
                            "title": "Mr",
                            "company_name": null,
                            "surname": "MC-Surname",
                            "forenames": "MC-Forename",
                            "dob": "1980-01-01",
                            "address_line_1": "MC Address Line 1",
                            "address_line_2": "MC Address Line 2",
                            "address_line_3": "MC Address Line 3",
                            "address_line_4": null,
                            "address_line_5": null,
                            "post_code": "XY1 2AB",
                            "telephone": "555-9999",
                            "email_address": "mc@sample.com",
                            "payout_hold": false,
                            "pay_by_bacs": true,
                            "bank_account_type": "1",
                            "bank_sort_code": "102030",
                            "bank_account_number": "12345678",
                            "bank_account_name": "National Bank",
                            "bank_account_ref": "BankRef"
                        }
                    },
                    {
                        "result_id": "FCOMP",
                        "amount_imposed": 50.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null,
                        "minor_creditor": {
                            "company_flag": false,
                            "title": "Mr",
                            "company_name": null,
                            "surname": "MC-Surname2",
                            "forenames": "MC-Forename2",
                            "dob": "1980-01-01",
                            "address_line_1": "MC2 Address Line 1",
                            "address_line_2": "MC2 Address Line 2",
                            "address_line_3": "MC2 Address Line 3",
                            "address_line_4": null,
                            "address_line_5": null,
                            "post_code": "ZX2 3CD",
                            "telephone": "555-8888",
                            "email_address": "mc2@sample.com",
                            "payout_hold": false,
                            "pay_by_bacs": true,
                            "bank_account_type": "1",
                            "bank_sort_code": "112233",
                            "bank_account_number": "87654321",
                            "bank_account_name": "National Bank",
                            "bank_account_ref": "BankRef2"
                        }
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        },
        "payment_terms": {
            "payment_terms_type_code": "B",
            "effective_date": null,
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 20.00,
            "default_days_in_jail": 14,
            "enforcements": [
                {
                    "result_id": "PRIS",
                    "enforcement_result_responses": [
                        {
                            "parameter_name": "PRIS parm name 1",
                            "response": "PRIS response 1"
                        },
                        {
                            "parameter_name": "PRIS parm name 2",
                            "response": "PRIS response 2"
                        }
                    ]
                },
                {
                    "result_id": "NOENF",
                    "enforcement_result_responses": [
                        {
                            "parameter_name": "NOENF parm name 1",
                            "response": "NOENF response 1"
                        },
                        {
                            "parameter_name": "NOENF parm name 2",
                            "response": "NOENF response 2"
                        }
                    ]
                },
                {
                    "result_id": "COLLO",
                    "enforcement_result_responses": [
                        {
                            "parameter_name": "COLLO parm name 1",
                            "response": "COLLO response 1"
                        }
                    ]
                }
            ]
        },
        "account_notes": [
            {
                "account_note_serial": 1,
                "account_note_text": "AN note with serial 1",
                "note_type": "AN"
            },
            {
                "account_note_serial": 2,
                "account_note_text": "AN note with serial 2",
                "note_type": "AN"
            },
            {
                "account_note_serial": 3,
                "account_note_text": "AN note with serial 3",
                "note_type": "AN"
            },
            {
                "account_note_serial": 4,
                "account_note_text": "AC note with serial 4",
                "note_type": "AC"
            },
            {
                "account_note_serial": 6,
                "account_note_text": "AA note with serial 6 - Should be second record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 5,
                "account_note_text": "AA note with serial 5 - Should be first record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 7,
                "account_note_text": "AA note with serial 7 - Should NOT be in Notes",
                "note_type": "AA"
            }
        ]
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

    COMMIT;  --Commit here so data is retained and can be checked

    -- Verify results
    ASSERT v_defendant_account_id IS NOT NULL, 'Defendant account ID should not be NULL';
    ASSERT v_account_number IS NOT NULL, 'Account number should not be NULL';
    
    -------------------------------
    -- ACCOUNT_NUMBER_INDEX
    -------------------------------

    --Verify the account_number (associated_record_type = 'defendant_accounts') exists and matches the value returned from p_create_defendant_account
    ASSERT EXISTS (SELECT 1 FROM account_number_index 
             WHERE associated_record_type = 'defendant_accounts'
               AND business_unit_id = v_business_unit_id 
               AND account_number = v_account_number)
         , 'Returned account_number should match with ACCOUNT_NUMBER_INDEX';

    --Store the account_number for 'creditor_accounts' to check later 
    SELECT account_number INTO v_minor_creditor_account_number 
      FROM account_number_index WHERE associated_record_type = 'creditor_accounts' AND business_unit_id = v_business_unit_id;

    -------------------------------
    -- DEFENDANT_ACCOUNTS
    -------------------------------

    -- Basic field verification
    ASSERT (SELECT business_unit_id FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = v_business_unit_id,
           'business_unit_id should match';
    ASSERT (SELECT account_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'Fixed Penalty',
           'Account type should be Fixed Penalty';
    ASSERT (SELECT imposed_hearing_date FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id)::date = '2024-12-12'::date,
           'Account sentence date should match';
    ASSERT (SELECT enforcing_court_id FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id)::text = '470000000013',
           'Enforcement court ID should match';
    
    -- Verify originator fields
    ASSERT (SELECT originator_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'LJS',
           'Originator name should match';
    ASSERT (SELECT originator_id FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = '12345',
           'Originator ID should match';
    ASSERT (SELECT originator_type FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'FP',
           'Originator type should match';
    
    -- Verify imposed_by_name field
    ASSERT (SELECT imposed_by_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = (SELECT lja.name
                                                                                                                     FROM courts c
                                                                                                                     JOIN local_justice_areas lja ON lja.local_justice_area_id = c.local_justice_area_id
                                                                                                                    WHERE c.court_id = 470000000013),
           'Imposed by name should match local_justice_areas.name for the related enforcement_court_id';
           
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
    ASSERT (SELECT payment_card_requested_by_name FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = v_posted_by_name,
           'Payment card requested by name should match';  --v3.6
           
    -- Verify columns where the values come from CONFIGURATION_ITEMS (DEFENDANT_ACCOUNTS.CHEQUE_CLEARANCE_PERIOD, CREDIT_TRANS_CLEARANCE_PERIOD)
    ASSERT (SELECT cheque_clearance_period FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = (SELECT item_value::int2 FROM configuration_items WHERE item_name = 'DEFAULT_CHEQUE_CLEARANCE_PERIOD'),
           'Cheque clearance period should match value on CONFIGURATION_ITEMS for DEFAULT_CHEQUE_CLEARANCE_PERIOD'; --v3.6
    
    ASSERT (SELECT credit_trans_clearance_period FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = (SELECT item_value::int2 FROM configuration_items WHERE item_name = 'DEFAULT_CREDIT_TRANS_CLEARANCE_PERIOD'),
           'Credit trans clearance period should match value on CONFIGURATION_ITEMS for DEFAULT_CREDIT_TRANS_CLEARANCE_PERIOD'; --v3.6
           
    -- Verify default values are set correctly
    ASSERT (SELECT account_status FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'L',
           'Account status should be set to L for Live';
    ASSERT (SELECT last_movement_date FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) IS NOT NULL,
           'Last movement date should be set';
    ASSERT (SELECT allow_writeoffs FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = TRUE,
           'Allow writeoffs should be set to TRUE';
    ASSERT (SELECT allow_cheques FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = TRUE,
           'Allow cheques should be set to TRUE';
    ASSERT (SELECT version_number FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 1,
           'Version number should be set to 1';  --v3.4

    -- Verify columns that were updated
    ASSERT (SELECT amount_imposed FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = -150.00,
           'Amount imposed should match';
    ASSERT (SELECT amount_paid FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 40.00,
           'Amount paid should match';
    ASSERT (SELECT account_balance FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = -110.00,
           'Account balance should match';
    ASSERT (SELECT jail_days FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 14,
           'Jail days should match';
    ASSERT (SELECT account_comments FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'AC note with serial 4',
           'Account comments should match';
    ASSERT (SELECT account_note_1 FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) IS NULL,
           'Account note 1 should be NULL';
    ASSERT (SELECT account_note_2 FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) IS NULL,
           'Account note 2 should be NULL';
    ASSERT (SELECT account_note_3 FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) IS NULL,
           'Account note 3 should be NULL';
    ASSERT (SELECT last_enforcement FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 'NOENF',
           'Last enforcement should match';
        
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

    --Verify the minor creditor party record exists 
    ASSERT (SELECT COUNT(*) FROM parties WHERE account_type = 'Creditor') = 1, 'PARTIES - There should be one minor creditor record';
    
    SELECT party_id INTO v_party_id_minor_creditor
      FROM parties 
     WHERE account_type = 'Creditor';

    -- Verify fields for the minor creditor (related to the impositions where result_id = 'FCOMP')
    ASSERT (SELECT account_type FROM parties WHERE party_id = v_party_id_minor_creditor)               = 'Creditor',     'PARTIES - minor creditor - account_type should match';
    ASSERT (SELECT organisation FROM parties WHERE party_id = v_party_id_minor_creditor)               = FALSE,          'PARTIES - minor creditor - organisation should match';
    ASSERT (SELECT organisation_name FROM parties WHERE party_id = v_party_id_minor_creditor)          IS NULL,          'PARTIES - minor creditor - organisation_name should match';
    ASSERT (SELECT surname FROM parties WHERE party_id = v_party_id_minor_creditor)                    = 'MC-Surname2',  'PARTIES - minor creditor - surname should match';
    ASSERT (SELECT forenames FROM parties WHERE party_id = v_party_id_minor_creditor)                  = 'MC-Forename2', 'PARTIES - minor creditor - forenames should match';
    ASSERT (SELECT title FROM parties WHERE party_id = v_party_id_minor_creditor)                      = 'Mr',           'PARTIES - minor creditor - title should match';
    ASSERT (SELECT address_line_1 FROM parties WHERE party_id = v_party_id_minor_creditor)             = 'MC2 Address Line 1', 'PARTIES - minor creditor - address_line_1 should match';
    ASSERT (SELECT address_line_2 FROM parties WHERE party_id = v_party_id_minor_creditor)             = 'MC2 Address Line 2', 'PARTIES - minor creditor - address_line_2 should match';
    ASSERT (SELECT address_line_3 FROM parties WHERE party_id = v_party_id_minor_creditor)             = 'MC2 Address Line 3', 'PARTIES - minor creditor - address_line_3 should match';
    ASSERT (SELECT address_line_4 FROM parties WHERE party_id = v_party_id_minor_creditor)             IS NULL,        'PARTIES - minor creditor - address_line_4 should match';
    ASSERT (SELECT address_line_5 FROM parties WHERE party_id = v_party_id_minor_creditor)             IS NULL,        'PARTIES - minor creditor - address_line_5 should match';
    ASSERT (SELECT postcode FROM parties WHERE party_id = v_party_id_minor_creditor)                   = 'ZX2 3CD',    'PARTIES - minor creditor - postcode should match';
    ASSERT (SELECT birth_date FROM parties WHERE party_id = v_party_id_minor_creditor)                 = '1980-01-01', 'PARTIES - minor creditor - birth_date should match';
    ASSERT (SELECT age FROM parties WHERE party_id = v_party_id_minor_creditor)                        IS NULL,        'PARTIES - minor creditor - age should be NULL';
    ASSERT (SELECT national_insurance_number FROM parties WHERE party_id = v_party_id_minor_creditor)  IS NULL,        'PARTIES - minor creditor - national_insurance_number should be NULL';
    ASSERT (SELECT telephone_home FROM parties WHERE party_id = v_party_id_minor_creditor)             = '555-8888',   'PARTIES - minor creditor - telephone_home should match';
    ASSERT (SELECT telephone_business FROM parties WHERE party_id = v_party_id_minor_creditor)         IS NULL,        'PARTIES - minor creditor - telephone_business should be NULL';
    ASSERT (SELECT telephone_mobile FROM parties WHERE party_id = v_party_id_minor_creditor)           IS NULL,        'PARTIES - minor creditor - telephone_mobile should be NULL';
    ASSERT (SELECT email_1 FROM parties WHERE party_id = v_party_id_minor_creditor)                    = 'mc2@sample.com', 'PARTIES - minor creditor - email_1 should match';
    ASSERT (SELECT email_2 FROM parties WHERE party_id = v_party_id_minor_creditor)                    IS NULL,        'PARTIES - minor creditor - email_2 should be NULL';
    ASSERT (SELECT last_changed_date FROM parties WHERE party_id = v_party_id_minor_creditor)          IS NULL,        'PARTIES - minor creditor - last_changed_date should be NULL';

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
    ASSERT (SELECT issued_date FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = '2024-12-01', 'FIXED_PENALTY_OFFENCES - issued_date should match';
    ASSERT (SELECT licence_number FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = 'ABCD12345Z', 'FIXED_PENALTY_OFFENCES - licence_number should match';
    ASSERT (SELECT vehicle_fixed_penalty FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = TRUE, 'FIXED_PENALTY_OFFENCES - vehicle_fixed_penalty should be TRUE';
    --ASSERT (SELECT offence_date FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'FIXED_PENALTY_OFFENCES - offence_date should be NULL';    --v3.2
    ASSERT (SELECT offence_date FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = '2024-12-01', 'FIXED_PENALTY_OFFENCES - offence_date should match'; --v3.2
    ASSERT (SELECT offence_time FROM fixed_penalty_offences WHERE defendant_account_id = v_defendant_account_id) = '15:38', 'FIXED_PENALTY_OFFENCES - offence_time should match';
    
    -------------------------------
    -- CREDITOR_ACCOUNTS 
    -------------------------------
    
    --Related to the impositions where result_id = 'FCOMP'
    SELECT creditor_account_id INTO v_creditor_account_id
      FROM creditor_accounts
     WHERE minor_creditor_party_id = v_party_id_minor_creditor;

    -- Verify fields 
    ASSERT (SELECT business_unit_id FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = v_business_unit_id
         , 'CREDITOR_ACCOUNTS -  should match';
    ASSERT (SELECT account_number FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = v_minor_creditor_account_number
         , 'CREDITOR_ACCOUNTS - account_number should match with the value in ACCOUNT_NUMBER_INDEX';
    ASSERT (SELECT creditor_account_type FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = 'MN'
         , 'CREDITOR_ACCOUNTS - creditor_account_type should be MN';
    ASSERT (SELECT prosecution_service FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = FALSE
         , 'CREDITOR_ACCOUNTS - prosecution_service should be FALSE';
    ASSERT (SELECT major_creditor_id FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) IS NULL
         , 'CREDITOR_ACCOUNTS - major_creditor_id should be NULL';
    --ASSERT (SELECT minor_creditor_party_id FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = '', 'CREDITOR_ACCOUNTS - minor_creditor_party_id should match';
    ASSERT (SELECT from_suspense FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = FALSE
         , 'CREDITOR_ACCOUNTS - from_suspense should be FALSE';
    ASSERT (SELECT hold_payout FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = FALSE
         , 'CREDITOR_ACCOUNTS - hold_payout should match';
    ASSERT (SELECT pay_by_bacs FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = TRUE
         , 'CREDITOR_ACCOUNTS - pay_by_bacs should match';
    ASSERT (SELECT bank_sort_code FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = '112233'
         , 'CREDITOR_ACCOUNTS - bank_sort_code should match';
    ASSERT (SELECT bank_account_number FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = '87654321'
         , 'CREDITOR_ACCOUNTS - bank_account_number should match';
    ASSERT (SELECT bank_account_name FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = 'National Bank'
         , 'CREDITOR_ACCOUNTS - bank_account_name should match';
    ASSERT (SELECT bank_account_reference FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = 'BankRef2'
         , 'CREDITOR_ACCOUNTS - bank_account_reference should match';
    ASSERT (SELECT bank_account_type FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = '1'
         , 'CREDITOR_ACCOUNTS - bank_account_type should match';
    ASSERT (SELECT last_changed_date FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) IS NULL
         , 'CREDITOR_ACCOUNTS - last_changed_date should be NULL';
    ASSERT (SELECT version_number FROM creditor_accounts WHERE minor_creditor_party_id = v_party_id_minor_creditor) = 1
         , 'CREDITOR_ACCOUNTS - version_number should be 1';  --v3.4

    -------------------------------
    -- IMPOSITIONS
    -------------------------------

    SELECT imposition_id INTO v_imposition_id_FO    FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO';
    SELECT imposition_id INTO v_imposition_id_FCOMP FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP';

    -- Verify fields for result_id = 'FO'  (imposition_creditor = 'CF' --> creditor_accounts.creditor_account_id = 47)

    ASSERT (SELECT posted_date FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') IS NOT NULL
         , 'IMPOSITIONS - FO - posted_date should be set';
    ASSERT (SELECT posted_by FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = v_posted_by
         , 'IMPOSITIONS - FO - posted_by should match';
    ASSERT (SELECT posted_by_name FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = v_posted_by_name
         , 'IMPOSITIONS - FO - posted_by_name should match';
    ASSERT (SELECT original_posted_date FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') IS NOT NULL
         , 'IMPOSITIONS - FO - original_posted_date should be set';
    --ASSERT (SELECT result_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = 'FO', 'IMPOSITIONS - FO - result_id should match';
    ASSERT (SELECT imposing_court_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = '650000000001'
         , 'IMPOSITIONS - FO - imposing_court_id should match';
    ASSERT (SELECT imposed_date FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = '2024-12-12'
         , 'IMPOSITIONS - FO - imposed_date should match';
    ASSERT (SELECT imposed_amount FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = -100.00
         , 'IMPOSITIONS - FO - imposed_amount should match';
    ASSERT (SELECT paid_amount FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = 40.00
         , 'IMPOSITIONS - FO - paid_amount should match';
    ASSERT (SELECT offence_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = 30000
         , 'IMPOSITIONS - FO - offence_id should match';
    ASSERT (SELECT offence_title FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = 'Person having charge abandoning animal'
         , 'IMPOSITIONS - FO - offence_title should match';
    ASSERT (SELECT offence_code FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = 'AA60005'
         , 'IMPOSITIONS - FO - offence_code should match';
    ASSERT (SELECT creditor_account_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = 47
         , 'IMPOSITIONS - FO - creditor_account_id should match';
    ASSERT (SELECT unit_fine_adjusted FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') IS NULL
         , 'IMPOSITIONS - FO - unit_fine_adjusted should be NULL';
    ASSERT (SELECT unit_fine_units FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') IS NULL
         , 'IMPOSITIONS - FO - unit_fine_units should be NULL';
    ASSERT (SELECT completed FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') = FALSE
         , 'IMPOSITIONS - FO - completed should be FALSE';
    ASSERT (SELECT original_imposition_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FO') IS NULL
         , 'IMPOSITIONS - FO - original_imposition_id should be NULL'; --v3.3

    -- Verify fields for result_id = 'FCOMP'  (imposition_creditor = 'Any' --> creditor_accounts.creditor_account_id = <newly created record> = v_creditor_account_id)

    ASSERT (SELECT posted_date FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') IS NOT NULL
         , 'IMPOSITIONS - FCOMP - posted_date should be set';
    ASSERT (SELECT posted_by FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = v_posted_by
         , 'IMPOSITIONS - FCOMP - posted_by should match';
    ASSERT (SELECT posted_by_name FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = v_posted_by_name
         , 'IMPOSITIONS - FCOMP - posted_by_name should match';
    ASSERT (SELECT original_posted_date FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') IS NOT NULL
         , 'IMPOSITIONS - FCOMP - original_posted_date should be set';
    --ASSERT (SELECT result_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = 'FCOMP', 'IMPOSITIONS - FCOMP - result_id should match';
    ASSERT (SELECT imposing_court_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = '650000000001'
         , 'IMPOSITIONS - FCOMP - imposing_court_id should match';
    ASSERT (SELECT imposed_date FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = '2024-12-12'
         , 'IMPOSITIONS - FCOMP - imposed_date should match';
    ASSERT (SELECT imposed_amount FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = -50.00
         , 'IMPOSITIONS - FCOMP - imposed_amount should match';
    ASSERT (SELECT paid_amount FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = 0.00
         , 'IMPOSITIONS - FCOMP - paid_amount should match';
    ASSERT (SELECT offence_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = 30000
         , 'IMPOSITIONS - FCOMP - offence_id should match';
    ASSERT (SELECT offence_title FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = 'Person having charge abandoning animal'
         , 'IMPOSITIONS - FCOMP - offence_title should match';
    ASSERT (SELECT offence_code FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = 'AA60005'
         , 'IMPOSITIONS - FCOMP - offence_code should match';
    ASSERT (SELECT creditor_account_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = v_creditor_account_id
         , 'IMPOSITIONS - FCOMP - creditor_account_id should match';
    ASSERT (SELECT unit_fine_adjusted FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') IS NULL
         , 'IMPOSITIONS - FCOMP - unit_fine_adjusted should be NULL';
    ASSERT (SELECT unit_fine_units FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') IS NULL
         , 'IMPOSITIONS - FCOMP - unit_fine_units should be NULL';
    ASSERT (SELECT completed FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') = FALSE
         , 'IMPOSITIONS - FCOMP - completed should be FALSE';
    ASSERT (SELECT original_imposition_id FROM impositions WHERE defendant_account_id = v_defendant_account_id AND result_id = 'FCOMP') IS NULL
         , 'IMPOSITIONS - FCOMP - original_imposition_id should be NULL'; --v3.3

    -------------------------------
    -- CONTROL_TOTALS
    -------------------------------

    --Verify fields for v_imposition_id_FO
    ASSERT (SELECT business_unit_id FROM control_totals WHERE associated_record_id = v_imposition_id_FO::VARCHAR) = v_business_unit_id, 'CONTROL_TOTALS - FO - business_unit_id should match';
    ASSERT (SELECT item_number FROM control_totals WHERE associated_record_id = v_imposition_id_FO::VARCHAR) = 208, 'CONTROL_TOTALS - FO - item_number should match';
    ASSERT (SELECT amount FROM control_totals WHERE associated_record_id = v_imposition_id_FO::VARCHAR) = -60.00, 'CONTROL_TOTALS - FO - amount should match';
    ASSERT (SELECT associated_record_type FROM control_totals WHERE associated_record_id = v_imposition_id_FO::VARCHAR) = 'impositions', 'CONTROL_TOTALS - FO - associated_record_type should be "impositions"';
    ASSERT (SELECT associated_record_id FROM control_totals WHERE associated_record_id = v_imposition_id_FO::VARCHAR) = v_imposition_id_FO::VARCHAR, 'CONTROL_TOTALS - FO - associated_record_id should match';
    ASSERT (SELECT ct_report_instance_id FROM control_totals WHERE associated_record_id = v_imposition_id_FO::VARCHAR) IS NULL, 'CONTROL_TOTALS - FO - ct_report_instance_id should be NULL';
    ASSERT (SELECT qe_report_instance_id FROM control_totals WHERE associated_record_id = v_imposition_id_FO::VARCHAR) IS NULL, 'CONTROL_TOTALS - FO - qe_report_instance_id should be NULL';
    
    --Verify fields for v_imposition_id_FCOMP
    ASSERT (SELECT business_unit_id FROM control_totals WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR) = v_business_unit_id, 'CONTROL_TOTALS - FCOMP - business_unit_id should match';
    ASSERT (SELECT item_number FROM control_totals WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR) = 214, 'CONTROL_TOTALS - FCOMP - item_number should match';
    ASSERT (SELECT amount FROM control_totals WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR) = -50.00, 'CONTROL_TOTALS - FCOMP - amount should match';
    ASSERT (SELECT associated_record_type FROM control_totals WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR) = 'impositions', 'CONTROL_TOTALS - FCOMP - associated_record_type should be "impositions"';
    ASSERT (SELECT associated_record_id FROM control_totals WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR) = v_imposition_id_FCOMP::VARCHAR, 'CONTROL_TOTALS - FCOMP - associated_record_id should match';
    ASSERT (SELECT ct_report_instance_id FROM control_totals WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR) IS NULL, 'CONTROL_TOTALS - FCOMP - ct_report_instance_id should be NULL';
    ASSERT (SELECT qe_report_instance_id FROM control_totals WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR) IS NULL, 'CONTROL_TOTALS - FCOMP - qe_report_instance_id should be NULL';
    
    -------------------------------
    -- DEFENDANT_TRANSACTIONS
    -------------------------------

    --Verify fields  (should only be 1 record. Data: related to impositions - result_id FO (only one with a paid amount))

    ASSERT (SELECT COUNT(*) FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) = 1, 'DEFENDANT_TRANSACTIONS - There should only be 1 record';

    SELECT defendant_transaction_id INTO v_defendant_transaction_id FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id;

    ASSERT (SELECT posted_date FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NOT NULL, 'DEFENDANT_TRANSACTIONS - posted_date should be set';
    ASSERT (SELECT posted_by FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) = v_posted_by, 'DEFENDANT_TRANSACTIONS - posted_by should match';
    ASSERT (SELECT transaction_type FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) = 'TFO IN', 'DEFENDANT_TRANSACTIONS - transaction_type should be "TFO IN"';
    ASSERT (SELECT transaction_amount FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) = 40.00, 'DEFENDANT_TRANSACTIONS - transaction_amount should match';
    ASSERT (SELECT payment_method FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - payment_method should be NULL';
    ASSERT (SELECT payment_reference FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - payment_reference should be NULL';
    ASSERT (SELECT text FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - text should be NULL';
    ASSERT (SELECT status FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - status should be NULL';
    ASSERT (SELECT status_date FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - status_date should be NULL';
    ASSERT (SELECT status_amount FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - status_amount should be NULL';
    ASSERT (SELECT write_off_code FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - write_off_code should be NULL';
    ASSERT (SELECT associated_record_type FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - associated_record_type should be NULL';
    ASSERT (SELECT associated_record_id FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - associated_record_id should be NULL';
    ASSERT (SELECT imposed_amount FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) IS NULL, 'DEFENDANT_TRANSACTIONS - imposed_amount should be NULL';
    ASSERT (SELECT posted_by_name FROM defendant_transactions WHERE defendant_account_id = v_defendant_account_id) = v_posted_by_name, 'DEFENDANT_TRANSACTIONS - posted_by_name should match';

    -------------------------------
    -- ALLOCATIONS
    -------------------------------

    --Verify fields  (Data: related to impositions - result_id FO (only one with a paid amount))
    
    --ASSERT (SELECT imposition_id FROM allocations WHERE imposition_id = v_imposition_id_FO) = '', 'ALLOCATIONS -  should match';
    ASSERT (SELECT allocated_date FROM allocations WHERE imposition_id = v_imposition_id_FO) IS NOT NULL, 'ALLOCATIONS - allocated_date should be set';
    ASSERT (SELECT allocated_amount FROM allocations WHERE imposition_id = v_imposition_id_FO) = 40.00, 'ALLOCATIONS - allocated_amount should match';
    ASSERT (SELECT transaction_type FROM allocations WHERE imposition_id = v_imposition_id_FO) = 'TFO IN', 'ALLOCATIONS - transaction_type should be "TFO IN"';
    ASSERT (SELECT allocation_function FROM allocations WHERE imposition_id = v_imposition_id_FO) = 'MAC', 'ALLOCATIONS - allocation_function should be "MAC"';
    ASSERT (SELECT defendant_transaction_id FROM allocations WHERE imposition_id = v_imposition_id_FO) = v_defendant_transaction_id, 'ALLOCATIONS - defendant_transaction_id should match';

    -------------------------------
    -- PAYMENT_CARD_REQUESTS
    -------------------------------

    --Verify that one record exists 
    ASSERT EXISTS (SELECT 1 FROM payment_card_requests WHERE defendant_account_id = v_defendant_account_id), 'Defendant payment_card_requests record should exist in the database';
    
    -------------------------------
    -- PAYMENT_TERMS
    -------------------------------

    --Verify fields
    --ASSERT (SELECT defendant_account_id FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = '', 'PAYMENT_TERMS - defendant_account_id should match';
    ASSERT (SELECT posted_date FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) IS NOT NULL, 'PAYMENT_TERMS - posted_date should be set';
    ASSERT (SELECT posted_by FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = v_posted_by, 'PAYMENT_TERMS - posted_by should match';
    --ASSERT (SELECT terms_type_code FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = 'I', 'PAYMENT_TERMS - terms_type_code should match';  --Commented out in v3.1
    --ASSERT (SELECT effective_date FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = '2025-01-29', 'PAYMENT_TERMS - effective_date should match';  --Commented out in v3.1
    ASSERT (SELECT terms_type_code FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = 'B', 'PAYMENT_TERMS - terms_type_code should match';  --v3.1
    ASSERT (SELECT effective_date FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = (CURRENT_DATE + INTERVAL '28 days')::timestamp, 'PAYMENT_TERMS - effective_date should match';  --v3.5
    ASSERT (SELECT instalment_period FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = 'M', 'PAYMENT_TERMS - instalment_period should match';
    ASSERT (SELECT instalment_amount FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = 50.00, 'PAYMENT_TERMS - instalment_amount should match';
    ASSERT (SELECT instalment_lump_sum FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = 20.00, 'PAYMENT_TERMS - instalment_lump_sum should match';
    ASSERT (SELECT jail_days FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = 14, 'PAYMENT_TERMS - jail_days should match';
    ASSERT (SELECT "extension" FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = FALSE, 'PAYMENT_TERMS - extension should be FALSE';
    ASSERT (SELECT account_balance FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = -110.00, 'PAYMENT_TERMS - account_balance should match';
    ASSERT (SELECT posted_by_name FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = v_posted_by_name, 'PAYMENT_TERMS - posted_by_name should match';
    ASSERT (SELECT active FROM payment_terms WHERE defendant_account_id = v_defendant_account_id) = TRUE, 'PAYMENT_TERMS - active should be TRUE';

    -------------------------------
    -- ENFORCEMENTS
    -------------------------------

    --Verify 3 records exist
    ASSERT (SELECT COUNT(*) FROM enforcements WHERE defendant_account_id = v_defendant_account_id) = 3, 'ENFORCEMENTS - There should be 3 records';

    --Verify fields ensuring they are in the correct order (COLLO, PRIS, NOENF)
    FOR v_record IN 
        SELECT ROW_NUMBER() OVER(ORDER BY posted_date) AS row_num, * 
          FROM enforcements WHERE defendant_account_id = v_defendant_account_id
    LOOP

        CASE v_record.row_num 
            WHEN 1 THEN
                ASSERT v_record.result_id = 'COLLO', FORMAT('ENFORCEMENTS - Row %s - result_id should be "COLLO"', v_record.row_num);
                ASSERT jsonb_array_length(v_record.result_responses::jsonb) = 1, FORMAT('ENFORCEMENTS - Row %s - result_responses array length should be 1', v_record.row_num);
            WHEN 2 THEN
                ASSERT v_record.result_id = 'PRIS', FORMAT('ENFORCEMENTS - Row %s - result_id should be "PRIS"', v_record.row_num);
                ASSERT jsonb_array_length(v_record.result_responses::jsonb) = 2, FORMAT('ENFORCEMENTS - Row %s - result_responses array length should be 2', v_record.row_num);
            WHEN 3 THEN
                ASSERT v_record.result_id = 'NOENF', FORMAT('ENFORCEMENTS - Row %s - result_id should be "NOENF"', v_record.row_num);
                ASSERT jsonb_array_length(v_record.result_responses::jsonb) = 2, FORMAT('ENFORCEMENTS - Row %s - result_responses array length should be 2', v_record.row_num);
        END CASE; 

        ASSERT v_record.posted_date       IS NOT NULL, FORMAT('ENFORCEMENTS - Row %s - posted_date should be set', v_record.row_num);
        ASSERT v_record.posted_by         = v_posted_by, FORMAT('ENFORCEMENTS - Row %s - posted_by should match', v_record.row_num);
        ASSERT v_record.reason            IS NULL, FORMAT('ENFORCEMENTS - Row %s - reason should be NULL', v_record.row_num);
        ASSERT v_record.enforcer_id       IS NULL, FORMAT('ENFORCEMENTS - Row %s - enforcer_id should be NULL', v_record.row_num);
        ASSERT v_record.jail_days         IS NULL, FORMAT('ENFORCEMENTS - Row %s - jail_days should be NULL', v_record.row_num);
        ASSERT v_record.warrant_reference IS NULL, FORMAT('ENFORCEMENTS - Row %s - warrant_reference should be NULL', v_record.row_num);
        ASSERT v_record.case_reference    IS NULL, FORMAT('ENFORCEMENTS - Row %s - case_reference should be NULL', v_record.row_num);
        ASSERT v_record.hearing_date      IS NULL, FORMAT('ENFORCEMENTS - Row %s - hearing_date should be NULL', v_record.row_num);
        ASSERT v_record.hearing_court_id  IS NULL, FORMAT('ENFORCEMENTS - Row %s - hearing_court_id should be NULL', v_record.row_num);
        ASSERT v_record.account_type      IS NULL, FORMAT('ENFORCEMENTS - Row %s - account_type should be NULL', v_record.row_num);
        ASSERT v_record.posted_by_name    = v_posted_by_name, FORMAT('ENFORCEMENTS - Row %s - posted_by_name should match', v_record.row_num);

    END LOOP;

    -------------------------------
    -- NOTES
    -------------------------------

    --Verify that only 2 AA records exist in the Notes table
    ASSERT (SELECT COUNT(*) FROM notes WHERE associated_record_id = v_defendant_account_id::VARCHAR) = 2, 'NOTES - There should only be 2 records';

    --Verify fields ensuring they are in the correct order
    FOR v_record IN 
        SELECT ROW_NUMBER() OVER(ORDER BY posted_date) AS row_num, * 
          FROM notes WHERE associated_record_id = v_defendant_account_id::VARCHAR
    LOOP

        CASE v_record.row_num 
            WHEN 1 THEN 
                ASSERT v_record.note_text = 'AA note with serial 5 - Should be first record in Notes', FORMAT('NOTES - Row %s - note_text should match', v_record.row_num);
            WHEN 2 THEN 
                ASSERT v_record.note_text = 'AA note with serial 6 - Should be second record in Notes', FORMAT('NOTES - Row %s - note_text should match', v_record.row_num);
        END CASE;

        ASSERT v_record.note_type              = 'AA', FORMAT('NOTES - Row %s - note_type should be "AA"', v_record.row_num);
        ASSERT v_record.associated_record_type = 'defendant_accounts', FORMAT('NOTES - Row %s - associated_record_type should be "defendant_accounts"', v_record.row_num);
        ASSERT v_record.posted_date            IS NOT NULL, FORMAT('NOTES - Row %s - posted_date should be set', v_record.row_num);
        ASSERT v_record.posted_by              = v_posted_by, FORMAT('NOTES - Row %s - posted_by should match', v_record.row_num);
        ASSERT v_record.posted_by_name         = v_posted_by_name, FORMAT('NOTES - Row %s - posted_by_name should match', v_record.row_num);
 
    END LOOP;

    -------------------------------
    -- DOCUMENT_INSTANCES
    -------------------------------

    --Verify that the 'TFO Order' record exists
    ASSERT EXISTS (SELECT 1 FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR')
        , 'Defendant document_instance record for "TFO Order" should exist in the database';

    ASSERT (SELECT document_id FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR') = 'CY_FINOR'
         , 'DOCUMENT_INSTANCES - TFO Order - document_id should be "CY_FINOR"';
    ASSERT (SELECT business_unit_id FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR') = v_business_unit_id
         , 'DOCUMENT_INSTANCES - TFO Order - business_unit_id should match';
    ASSERT (SELECT generated_date FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR') IS NOT NULL
         , 'DOCUMENT_INSTANCES - TFO Order - generated_date should be set';
    ASSERT (SELECT generated_by FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR') = v_posted_by
         , 'DOCUMENT_INSTANCES - TFO Order - generated_by should match';
    ASSERT (SELECT associated_record_type FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR') = 'defendant_account'
         , 'DOCUMENT_INSTANCES - TFO Order - associated_record_type should be "defendant_account"';
    ASSERT (SELECT associated_record_id FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR') = v_defendant_account_id::VARCHAR
         , 'DOCUMENT_INSTANCES - TFO Order - associated_record_id should match';
    ASSERT (SELECT status FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR') = 'New'
         , 'DOCUMENT_INSTANCES - TFO Order - status should be "New"';
    ASSERT (SELECT printed_date FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR') IS NULL
         , 'DOCUMENT_INSTANCES - TFO Order - printed_date should be NULL';
    ASSERT (SELECT document_content FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'CY_FINOR') IS NULL
         , 'DOCUMENT_INSTANCES - TFO Order - document_content should be NULL';

    --Verify that the 'TFO Letter' record exists
    ASSERT EXISTS (SELECT 1 FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA')
        , 'Defendant document_instance record for "TFO Letter" should exist in the database';

    ASSERT (SELECT document_id FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA') = 'FINOTA'
         , 'DOCUMENT_INSTANCES - TFO Letter - document_id should be "FINOTA"';
    ASSERT (SELECT business_unit_id FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA') = v_business_unit_id
         , 'DOCUMENT_INSTANCES - TFO Letter - business_unit_id should match';
    ASSERT (SELECT generated_date FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA') IS NOT NULL
         , 'DOCUMENT_INSTANCES - TFO Letter - generated_date should be set';
    ASSERT (SELECT generated_by FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA') = v_posted_by
         , 'DOCUMENT_INSTANCES - TFO Letter - generated_by should match';
    ASSERT (SELECT associated_record_type FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA') = 'defendant_account'
         , 'DOCUMENT_INSTANCES - TFO Letter - associated_record_type should be "defendant_account"';
    ASSERT (SELECT associated_record_id FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA') = v_defendant_account_id::VARCHAR
         , 'DOCUMENT_INSTANCES - TFO Letter - associated_record_id should match';
    ASSERT (SELECT status FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA') = 'New'
         , 'DOCUMENT_INSTANCES - TFO Letter - status should be "New"';
    ASSERT (SELECT printed_date FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA') IS NULL
         , 'DOCUMENT_INSTANCES - TFO Letter - printed_date should be NULL';
    ASSERT (SELECT document_content FROM document_instances WHERE associated_record_id = v_defendant_account_id::VARCHAR AND document_id = 'FINOTA') IS NULL
         , 'DOCUMENT_INSTANCES - TFO Letter - document_content should be NULL';

    --Verify that the 'COMPLETT' record exists  (related to impositions result_id = 'FCOMP' and is minor creditor)
    ASSERT EXISTS (SELECT 1 FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT')
        , 'Defendant document_instance record for "COMPLETT" should exist in the database';

    ASSERT (SELECT document_id FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT') = 'COMPLETT'
         , 'DOCUMENT_INSTANCES - COMPLETT - document_id should be "COMPLETT"';
    ASSERT (SELECT business_unit_id FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT') = v_business_unit_id
         , 'DOCUMENT_INSTANCES - COMPLETT - business_unit_id should match';
    ASSERT (SELECT generated_date FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT') IS NOT NULL
         , 'DOCUMENT_INSTANCES - COMPLETT - generated_date should be set';
    ASSERT (SELECT generated_by FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT') = v_posted_by
         , 'DOCUMENT_INSTANCES - COMPLETT - generated_by should match';
    ASSERT (SELECT associated_record_type FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT') = 'impositions'
         , 'DOCUMENT_INSTANCES - COMPLETT - associated_record_type should be "defendant_account"';
    ASSERT (SELECT associated_record_id FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT') = v_imposition_id_FCOMP::VARCHAR
         , 'DOCUMENT_INSTANCES - COMPLETT - associated_record_id should match';
    ASSERT (SELECT status FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT') = 'New'
         , 'DOCUMENT_INSTANCES - COMPLETT - status should be "New"';
    ASSERT (SELECT printed_date FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT') IS NULL
         , 'DOCUMENT_INSTANCES - COMPLETT - printed_date should be NULL';
    ASSERT (SELECT document_content FROM document_instances WHERE associated_record_id = v_imposition_id_FCOMP::VARCHAR AND document_id = 'COMPLETT') IS NULL
         , 'DOCUMENT_INSTANCES - COMPLETT - document_content should be NULL';

    -------------------------------
    -- REPORT_ENTRIES
    -------------------------------

    --Verify fields
    ASSERT (SELECT business_unit_id FROM report_entries WHERE associated_record_id = v_defendant_account_id::VARCHAR) = v_business_unit_id, 'REPORT_ENTRIES - business_unit_id should match';
    ASSERT (SELECT report_id FROM report_entries WHERE associated_record_id = v_defendant_account_id::VARCHAR) = 'fp_register', 'REPORT_ENTRIES - report_id should be "fp_register"';
    ASSERT (SELECT entry_timestamp FROM report_entries WHERE associated_record_id = v_defendant_account_id::VARCHAR) IS NOT NULL, 'REPORT_ENTRIES - entry_timestamp should be set';
    ASSERT (SELECT reported_timestamp FROM report_entries WHERE associated_record_id = v_defendant_account_id::VARCHAR) IS NULL, 'REPORT_ENTRIES - reported_timestamp should be NULL';
    ASSERT (SELECT associated_record_type FROM report_entries WHERE associated_record_id = v_defendant_account_id::VARCHAR) = 'defendant_accounts', 'REPORT_ENTRIES - associated_record_type should be "defendant_accounts"';
    ASSERT (SELECT associated_record_id FROM report_entries WHERE associated_record_id = v_defendant_account_id::VARCHAR) = v_defendant_account_id::VARCHAR, 'REPORT_ENTRIES - associated_record_id should match';
    ASSERT (SELECT report_instance_id FROM report_entries WHERE associated_record_id = v_defendant_account_id::VARCHAR) IS NULL, 'REPORT_ENTRIES - report_instance_id should be NULL';


    RAISE NOTICE 'TEST 3 PASSED: Created account % with ID % and payment card request', v_account_number, v_defendant_account_id;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Verify originator_type logic when fp_ticket_detail is absent  -  Removed in v3.0, Already checked in test 1
----------------------------------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Test handling of missing required fields
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       BIGINT     := 10004;
    v_business_unit_id       SMALLINT   := 65;
    v_posted_by              VARCHAR    := 'L045EO';
    v_posted_by_name         VARCHAR    := 'Tester 1';
    v_account_json           JSON;
    v_account_number         VARCHAR;
    v_defendant_account_id   BIGINT;
    v_error_caught           BOOLEAN    := FALSE;
    v_row_count              INTEGER;
BEGIN
    RAISE NOTICE '=== TEST 4: Test handling of missing required fields ===';
    
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

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 4 PASSED: Error handling works correctly for missing required fields';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Test handling da_account_type_cc check constraint violation
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT      := 10005;
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
    RAISE NOTICE '=== TEST 5: Test handling da_account_type_cc check constraint violation ===';
    
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
            "company_flag": false,
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

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'An error should have been raised due to da_account_type_cc check constraint violation';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 5 PASSED: Error handling works correctly for da_account_type_cc check constraint violation.';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Test handling when defendant_type = 'pgToPay' AND parent_guardian Json is missing
--         Expected exception: P2002 - Missing parent/guardian
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       BIGINT     := 10006;
    v_business_unit_id       SMALLINT   := 65;
    v_posted_by              VARCHAR    := 'L045EO';
    v_posted_by_name         VARCHAR    := 'Tester 1';
    v_account_json           JSON;
    v_account_number         VARCHAR;
    v_defendant_account_id   BIGINT;
    v_error_caught           BOOLEAN    := FALSE;
    v_row_count              INTEGER;
    v_expected_errmsg        VARCHAR    := 'Missing parent/guardian';
BEGIN
    RAISE NOTICE '=== TEST 6: Test handling when defendant_type = "pgToPay" AND parent_guardian Json is missing = P2002 ===';
    
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
        'Fine',
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

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 6 PASSED: Error handling works correctly for missing parent_guardian details';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Test handling when account_type = 'Fixed Penalty' but notice_number field is missing (Json null)
--         Expected exception: P2011 - Missing ticket number
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id       BIGINT     := 10007;
    v_business_unit_id       SMALLINT   := 65;
    v_posted_by              VARCHAR    := 'L045EO';
    v_posted_by_name         VARCHAR    := 'Tester 1';
    v_account_json           JSON;
    v_account_number         VARCHAR;
    v_defendant_account_id   BIGINT;
    v_error_caught           BOOLEAN    := FALSE;
    v_row_count              INTEGER;
    v_expected_errmsg        VARCHAR    := 'Missing ticket number';
BEGIN
    RAISE NOTICE '=== TEST 7: Test handling when account_type = "Fixed Penalty" but notice_number field is missing (Json null) = P2011 ===';
    
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
        'Fine',
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

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 7 PASSED: Error handling works correctly for missing notice_number field';
END $$;

-------------------------------------------------------------------------------------------------------------------
-- Test 8: Test handling when the passed payment terms are invalid - Testing 3 variations:
--         8A: When payment_terms_type_code is not B, I or P
--         8B: When payment_terms_type_code = B, effective_date IS NULL but account_type is not 'Fixed Penalty'
--         8C: When payment_terms_type_code = I, effective_date IS NOT NULL but instalment_amount is missing
--         Expected exception: P2003 Invalid payment terms
-------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id_A        BIGINT     := 10008;
    v_draft_account_id_B        BIGINT     := 10009;
    v_draft_account_id_C        BIGINT     := 10010;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_common_json_str   VARCHAR;
    v_payment_terms_json_str_A  VARCHAR;
    v_payment_terms_json_str_B  VARCHAR;
    v_payment_terms_json_str_C  VARCHAR;
    v_account_json_A            JSON;
    v_account_json_B            JSON;
    v_account_json_C            JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg           VARCHAR    := 'Invalid payment terms';
BEGIN
    
    -- Prepare JSON with invalid payment terms
    v_payment_terms_json_str_A := '
        "payment_terms": {
            "payment_terms_type_code": "Z",
            "effective_date": "2025-01-01",
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 20.00,
            "default_days_in_jail": 14
        }';

    v_payment_terms_json_str_B := '
        "payment_terms": {
            "payment_terms_type_code": "B",
            "effective_date": null,
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 20.00,
            "default_days_in_jail": 14
        }';

    v_payment_terms_json_str_C := '
        "payment_terms": {
            "payment_terms_type_code": "I",
            "effective_date": "2025-01-01",
            "instalment_period": "M",
            "instalment_amount": null,
            "lump_sum_amount": 20.00,
            "default_days_in_jail": 14
        }';


    v_account_common_json_str := '{
        "account_type": "Fine",
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        },
        <PLACEHOLDER>
    }';

    v_account_json_A := REPLACE(v_account_common_json_str, '<PLACEHOLDER>', v_payment_terms_json_str_A)::JSON;
    v_account_json_B := REPLACE(v_account_common_json_str, '<PLACEHOLDER>', v_payment_terms_json_str_B)::JSON;
    v_account_json_C := REPLACE(v_account_common_json_str, '<PLACEHOLDER>', v_payment_terms_json_str_C)::JSON;

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id IN (v_draft_account_id_A, v_draft_account_id_B, v_draft_account_id_C);

    -- Insert test draft accounts
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES (
        v_draft_account_id_A,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json_A,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    ),
    (   v_draft_account_id_B,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json_B,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    ),
    (   v_draft_account_id_C,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json_C,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    RAISE NOTICE '=== TEST 8A: Test handling when the passed payment terms are invalid = P2003 ===';
    RAISE NOTICE '===          When payment_terms_type_code is not B, I or P                   ===';

    -- Call the procedure - should throw a P2003 exception
    BEGIN
        CALL p_create_defendant_account(
            v_draft_account_id_A,
            v_business_unit_id,
            v_posted_by,
            v_posted_by_name,
            v_account_number,
            v_defendant_account_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P2003' THEN 

            IF SQLERRM = v_expected_errmsg THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Test 8A: Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Test 8A: Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Test 8A: Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'Test 8A: A P2003 error, with correct SQLERRM, should have been raised due to invalid payment terms';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'Test 8A: ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'Test 8A: An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 8A PASSED: Error handling works correctly when the passed payment terms are invalid';


    RAISE NOTICE '=== TEST 8B: Test handling when the passed payment terms are invalid = P2003                           ===';
    RAISE NOTICE '===    When payment_terms_type_code = B, effective_date IS NOT NULL, account_type is NOT Fixed Penalty ===';

    v_error_caught := FALSE;

    -- Call the procedure - should throw a P2003 exception
    BEGIN
        CALL p_create_defendant_account(
            v_draft_account_id_B,
            v_business_unit_id,
            v_posted_by,
            v_posted_by_name,
            v_account_number,
            v_defendant_account_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P2003' THEN 

            IF SQLERRM = v_expected_errmsg THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Test 8B: Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Test 8B: Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Test 8B: Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'Test 8B: A P2003 error, with correct SQLERRM, should have been raised due to invalid payment terms';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'Test 8B: ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'Test 8B: An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 8B PASSED: Error handling works correctly when the passed payment terms are invalid';


    RAISE NOTICE '=== TEST 8C: Test handling when the passed payment terms are invalid = P2003                         ===';
    RAISE NOTICE '===    When payment_terms_type_code = I, effective_date IS NOT NULL but instalment_amount is missing ===';

    v_error_caught := FALSE;

    -- Call the procedure - should throw a P2003 exception
    BEGIN
        CALL p_create_defendant_account(
            v_draft_account_id_C,
            v_business_unit_id,
            v_posted_by,
            v_posted_by_name,
            v_account_number,
            v_defendant_account_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P2003' THEN 

            IF SQLERRM = v_expected_errmsg THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Test 8C: Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Test 8C: Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Test 8C: Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'Test 8C: A P2003 error, with correct SQLERRM, should have been raised due to invalid payment terms';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'Test 8C: ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'Test 8C: An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 8C PASSED: Error handling works correctly when the passed payment terms are invalid';
END $$;

-------------------------------------------------------------------------------------------------------------------
-- Test 9: Test handling when the passed imposition result ID is invalid
--         Expected exception: P2004 Result <ID> is not valid
-------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT     := 10011;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg_pattern   VARCHAR    := 'Result% is not valid';
BEGIN
    RAISE NOTICE '=== TEST 9: Test handling when the passed imposition result ID is invalid = P2004 ===';
    
    -- Prepare JSON with invalid result_id
    v_account_json := '{
        "account_type": "Fine",
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "ZZZZZZ",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        }
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft accounts
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES (
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2004 exception
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
        WHEN SQLSTATE 'P2004' THEN 

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
    ASSERT v_error_caught = TRUE, 'A P2004 error, with correct SQLERRM, should have been raised due to invalid imposition result_id';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 9 PASSED: Error handling works correctly when the passed imposition result ID is invalid';

END $$;

-------------------------------------------------------------------------------------------------------------------
-- Test 10: Test handling when the passed creditor information is missing
--          Expected exception: P2005 Missing creditor
-------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT     := 10012;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg_pattern   VARCHAR    := 'Missing creditor';
BEGIN
    RAISE NOTICE '=== TEST 10: Test handling when the passed creditor information is missing = P2005 ===';
    
    -- Prepare JSON with creditor information missing
    v_account_json := '{
        "account_type": "Fine",
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FCOMP",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        }
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft accounts
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES (
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2005 exception
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
        WHEN SQLSTATE 'P2005' THEN 

            IF SQLERRM = v_expected_errmsg_pattern THEN
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
    ASSERT v_error_caught = TRUE, 'A P2005 error, with correct SQLERRM, should have been raised due to missing creditor information';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 10 PASSED: Error handling works correctly when the passed creditor information is missing';

END $$;

-------------------------------------------------------------------------------------------------------------------
-- Test 11: Test handling when the passed major_creditor_id cannot be found
--          Expected exception: P2006 Creditor <ID> not found
-------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT     := 10013;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg_pattern   VARCHAR    := 'Creditor% not found';
BEGIN
    RAISE NOTICE '=== TEST 11: Test handling when the passed major_creditor_id cannot be found = P2006 ===';
    
    -- Prepare JSON with an invalid major_creditor_id
    v_account_json := '{
        "account_type": "Fine",
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FCOMP",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": 0
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        }
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft accounts
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES (
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2006 exception
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
        WHEN SQLSTATE 'P2006' THEN 

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
    ASSERT v_error_caught = TRUE, 'A P2006 error, with correct SQLERRM, should have been raised when the passed major_creditor_id cannot be found';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 11 PASSED: Error handling works correctly when the passed major_creditor_id cannot be found';

END $$;

-------------------------------------------------------------------------------------------------------------------
-- Test 12: Test handling when the passed enforcement court cannot be found
--          Expected exception: P2007 Enforcement court <ID> not found
-------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT     := 10014;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg_pattern   VARCHAR    := 'Enforcement court % not found';
BEGIN
    RAISE NOTICE '=== TEST 12: Test handling when the passed enforcement court cannot be found = P2007 ===';
    
    -- Prepare JSON with an invalid major_creditor_id
    v_account_json := '{
        "account_type": "Fine",
        "defendant_type": "adultOrYouthOnly",
        "originator_name": "LJS",
        "originator_id": "12345",
        "prosecutor_case_reference": "ABC123",
        "enforcement_court_id": 0,
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FCOMP",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        }
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft accounts
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES (
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2007 exception
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
        WHEN SQLSTATE 'P2007' THEN 

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
    ASSERT v_error_caught = TRUE, 'A P2007 error, with correct SQLERRM, should have been raised when the passed enforcement court cannot be found';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 12 PASSED: Error handling works correctly when the passed enforcement court cannot be found';

END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 13: Test handling when the passed imposing court is not found
--          Expected exception: P2008 Imposing court <imposing court id> not found
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT     := 10015;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg_pattern   varchar    := 'Imposing court % not found';
BEGIN
    RAISE NOTICE '=== TEST 13: Test handling when the passed imposing court is not found = P2008 ===';
    
    -- Prepare JSON with invalid imposing court
    v_account_json := '{
        "account_type": "Fine",
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 0,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
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
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2008 exception
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
        WHEN SQLSTATE 'P2008' THEN 

            IF SQLERRM ILIKE v_expected_errmsg_pattern THEN
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
    ASSERT v_error_caught = TRUE, 'A P2008 error, with correct SQLERRM, should have been raised due to imposing court not being found';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 13 PASSED: Error handling works correctly when imposing court is not found';
END $$;

-------------------------------------------------------------------------------------------------------------------
-- Test 14: Test handling when the passed offence cannot be found
--          Expected exception: P2009 Offence <ID> not found
-------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT     := 10016;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg_pattern   VARCHAR    := 'Offence% not found';
BEGIN
    RAISE NOTICE '=== TEST 14: Test handling when the passed offence cannot be found = P2009 ===';
    
    -- Prepare JSON with an invalid offence_id
    v_account_json := '{
        "account_type": "Fine",
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 0,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        }
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft accounts
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES (
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2009 exception
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
        WHEN SQLSTATE 'P2009' THEN 

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
    ASSERT v_error_caught = TRUE, 'A P2009 error, with correct SQLERRM, should have been raised when the passed offence cannot be found';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 14 PASSED: Error handling works correctly when the passed offence cannot be found';

END $$;

-------------------------------------------------------------------------------------------------------------------
-- Test 15: Test handling when the passed minor creditor bank details are missing
--          Expected exception: P2010 Missing bank details
-------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT     := 10017;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg_pattern   VARCHAR    := 'Missing bank detail';
BEGIN
    RAISE NOTICE '=== TEST 15: Test handling when the passed minor creditor bank details are missing = P2010 ===';
    
    -- Prepare JSON with an invalid offence_id
    v_account_json := '{
        "account_type": "Fine",
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FCOMP",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null,
                        "minor_creditor": {
                            "company_flag": false,
                            "title": "Mr",
                            "company_name": null,
                            "surname": "MC-Surname",
                            "forenames": "MC-Forename",
                            "dob": "1980-01-01",
                            "address_line_1": "MC Address Line 1",
                            "address_line_2": "MC Address Line 2",
                            "address_line_3": "MC Address Line 3",
                            "address_line_4": null,
                            "address_line_5": null,
                            "post_code": "XY1 2AB",
                            "telephone": "555-9999",
                            "email_address": "mc@sample.com",
                            "payout_hold": false,
                            "pay_by_bacs": true
                        }
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        }
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft accounts
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES (
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2010 exception
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
        WHEN SQLSTATE 'P2010' THEN 

            IF SQLERRM = v_expected_errmsg_pattern THEN
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
    ASSERT v_error_caught = TRUE, 'A P2010 error, with correct SQLERRM, should have been raised when the passed minor creditor bank details are missing';

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 15 PASSED: Error handling works correctly when the passed minor creditor bank details are missing';

END $$;


-------------------------------------------------------------------------------------------------------------------
-- Test 16: Test handling when the passed Notes Json contains an invalid note_type
--          Expected exception: P2012 Note_type <note_type> is not valid
-------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT     := 10018;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg_pattern   VARCHAR    := 'Note_type % is not valid';
BEGIN
    RAISE NOTICE '=== TEST 16: Test handling when the passed Notes Json contains an invalid note_type = P2012 ===';
    
    -- Prepare JSON with an invalid note_type
    v_account_json := '{
        "account_type": "Fine",
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 40.00,
                        "major_creditor_id": null,
                        "minor_creditor": {
                            "company_flag": false,
                            "title": "Mr",
                            "company_name": null,
                            "surname": "MC-Surname",
                            "forenames": "MC-Forename",
                            "dob": "1980-01-01",
                            "address_line_1": "MC Address Line 1",
                            "address_line_2": "MC Address Line 2",
                            "address_line_3": "MC Address Line 3",
                            "address_line_4": null,
                            "address_line_5": null,
                            "post_code": "XY1 2AB",
                            "telephone": "555-9999",
                            "email_address": "mc@sample.com",
                            "payout_hold": false,
                            "pay_by_bacs": true,
                            "bank_account_type": "1",
                            "bank_sort_code": "102030",
                            "bank_account_number": "12345678",
                            "bank_account_name": "National Bank",
                            "bank_account_ref": "BankRef"
                        }
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        },
        "payment_terms": {
            "payment_terms_type_code": "I",
            "effective_date": "2025-01-01",
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 20.00,
            "default_days_in_jail": 14,
            "enforcements": null
        },
        "account_notes": [
            {
                "account_note_serial": 1,
                "account_note_text": "AN note with serial 1",
                "note_type": "AN"
            },
            {
                "account_note_serial": 2,
                "account_note_text": "AN note with serial 2",
                "note_type": "AN"
            },
            {
                "account_note_serial": 3,
                "account_note_text": "ZZ note with serial 3 - INVALID",
                "note_type": "ZZ"
            },
            {
                "account_note_serial": 4,
                "account_note_text": "AC note with serial 4",
                "note_type": "AC"
            },
            {
                "account_note_serial": 6,
                "account_note_text": "AA note with serial 6 - Should be second record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 5,
                "account_note_text": "AA note with serial 5 - Should be first record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 7,
                "account_note_text": "AA note with serial 7 - Should NOT be in Notes",
                "note_type": "AA"
            }
        ]
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft accounts
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES (
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2012 exception
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

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 16 PASSED: Error handling works correctly when the passed Notes Json contains an invalid note_type';

END $$;

-----------------------------------------------------------------------------------------------------------------------------
-- Test 17: Test handling when the passed Notes Json contains more than 1 AC entries
--          Expected exception: P2013 Only one AC note type is expected. Number of AC entries = <number of AC entries passed>
-----------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_draft_account_id          BIGINT     := 10019;
    v_business_unit_id          SMALLINT   := 65;
    v_posted_by                 VARCHAR    := 'L045EO';
    v_posted_by_name            VARCHAR    := 'Tester 1';
    v_account_json              JSON;
    v_account_number            VARCHAR;
    v_defendant_account_id      BIGINT;
    v_error_caught              BOOLEAN    := FALSE;
    v_row_count                 INTEGER;
    v_expected_errmsg_pattern   VARCHAR    := 'Only one AC note type is expected. Number of AC entries = %';
BEGIN
    RAISE NOTICE '=== TEST 17: Test handling when the passed Notes Json contains more than 1 AC entries = P2013 ===';
    
    -- Prepare JSON with an invalid note_type
    v_account_json := '{
        "account_type": "Fine",
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
                "employer_email_address": "hr@xyzcorp.com"
            }
        },
        "offences": [
            {
                "date_of_sentence": "2024-12-12",
                "imposing_court_id": 650000000001,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 100.00,
                        "amount_paid": 40.00,
                        "major_creditor_id": null,
                        "minor_creditor": {
                            "company_flag": false,
                            "title": "Mr",
                            "company_name": null,
                            "surname": "MC-Surname",
                            "forenames": "MC-Forename",
                            "dob": "1980-01-01",
                            "address_line_1": "MC Address Line 1",
                            "address_line_2": "MC Address Line 2",
                            "address_line_3": "MC Address Line 3",
                            "address_line_4": null,
                            "address_line_5": null,
                            "post_code": "XY1 2AB",
                            "telephone": "555-9999",
                            "email_address": "mc@sample.com",
                            "payout_hold": false,
                            "pay_by_bacs": true,
                            "bank_account_type": "1",
                            "bank_sort_code": "102030",
                            "bank_account_number": "12345678",
                            "bank_account_name": "National Bank",
                            "bank_account_ref": "BankRef"
                        }
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2024-12-01",
            "time_of_issue": "15:38",
            "fp_registration_number": "AB12XYZ",
            "notice_to_owner_hirer": "NOTICE1",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "ABCD12345Z"
        },
        "payment_terms": {
            "payment_terms_type_code": "I",
            "effective_date": "2025-01-01",
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 20.00,
            "default_days_in_jail": 14,
            "enforcements": null
        },
        "account_notes": [
            {
                "account_note_serial": 1,
                "account_note_text": "AN note with serial 1",
                "note_type": "AN"
            },
            {
                "account_note_serial": 2,
                "account_note_text": "AN note with serial 2",
                "note_type": "AN"
            },
            {
                "account_note_serial": 3,
                "account_note_text": "AN note with serial 3",
                "note_type": "AN"
            },
            {
                "account_note_serial": 4,
                "account_note_text": "AC note with serial 4",
                "note_type": "AC"
            },
            {
                "account_note_serial": 6,
                "account_note_text": "AA note with serial 6 - Should be second record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 5,
                "account_note_text": "AA note with serial 5 - Should be first record in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 7,
                "account_note_text": "AA note with serial 7 - Should NOT be in Notes",
                "note_type": "AA"
            },
            {
                "account_note_serial": 8,
                "account_note_text": "Second AC note with serial 8",
                "note_type": "AC"
            }
        ]
    }';

    -- Clean up existing test data
    DELETE FROM draft_accounts WHERE draft_account_id = v_draft_account_id;

    -- Insert test draft accounts
    INSERT INTO draft_accounts(
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        submitted_by_name,
        account_status_date
    ) VALUES (
        v_draft_account_id,
        v_business_unit_id,
        CURRENT_TIMESTAMP,
        v_posted_by,
        v_account_json,
        'Fine', 
        v_posted_by_name,
        CURRENT_TIMESTAMP
    );

    -- Call the procedure - should throw a P2013 exception
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

    -- Verify rollback happened
    SELECT COUNT(*) INTO v_row_count FROM account_number_index WHERE business_unit_id = v_business_unit_id AND account_number = v_account_number;
    RAISE NOTICE 'ACCOUNT_NUMBER_INDEX row count = %', v_row_count;

    ASSERT v_row_count = 0, 'An error was caught but rows remained on table ACCOUNT_NUMBER_INDEX';

    RAISE NOTICE 'TEST 17 PASSED: Error handling works correctly when the passed Notes Json contains more than 1 AC entries';

    RAISE NOTICE '=== All tests completed and passed for p_create_defendant_account ===';
    RAISE NOTICE '=== All tests completed and passed for p_create_defendant_account ===';

END $$;


-- Cleanup test data
/*
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';
    
    -- Delete all test accounts created by these tests
    DELETE FROM draft_accounts WHERE draft_account_id BETWEEN 10001 AND 10020;

    DELETE FROM control_totals WHERE control_total_id >= 60000000000000;
    DELETE FROM allocations WHERE allocation_id >= 60000000000000;
    DELETE FROM impositions WHERE imposition_id >= 60000000000000;
    DELETE FROM creditor_transactions WHERE creditor_account_id >= 60000000000000;
    DELETE FROM creditor_accounts WHERE creditor_account_id >= 60000000000000;
    DELETE FROM defendant_transactions WHERE defendant_transaction_id >= 60000000000000;
    DELETE FROM document_instances WHERE document_instance_id >= 60000000000000;
    DELETE FROM payment_terms WHERE payment_terms_id >= 60000000000000;
    DELETE FROM enforcements WHERE enforcement_id >= 60000000000000;
    DELETE FROM notes WHERE note_id >= 60000000000000;
    DELETE FROM report_entries WHERE report_entry_id >= 60000000000000;
    DELETE FROM aliases WHERE alias_id >= 60000000000000;
    DELETE FROM debtor_detail WHERE party_id >= 60000000000000;
    DELETE FROM fixed_penalty_offences WHERE defendant_account_id >= 60000000000000;
    DELETE FROM defendant_account_parties WHERE defendant_account_party_id >= 60000000000000;
    DELETE FROM parties WHERE party_id >= 60000000000000;
    DELETE FROM payment_card_requests WHERE defendant_account_id >= 60000000000000;
    DELETE FROM defendant_accounts WHERE defendant_account_id >= 60000000000000;
    DELETE FROM account_number_index WHERE account_number_index_id >= 60000000000000;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;
*/

\timing