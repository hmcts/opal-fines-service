--\timing

/**
* CGI Opal Program
*
* MODULE      : p_int_payment_card_requests_unit_tests
*
* DESCRIPTION : Unit tests for the stored procedure p_int_payment_card_requests.
*               These tests cover various scenarios to ensure the procedure correctly processes payment card requests.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------------
* 19/06/2025    ?           1.0         Basic Unit test for p_int_payment_card_requests.
* 11/11/2026    TMc         2.0         PO-11165 - Unit tests for p_int_payment_card_requests.
*                                       The unit tests validates the successful, empty, and error-processing paths of the p_int_payment_card_requests procedure.
*                                       Test 1 - Processes multiple requests and validates JSON output, data sanitisation, debtor selection, address compaction,
*                                                name-on-card rules, interface-job creation, and request tagging.
*                                       Test 2 - Confirms previously processed requests are ignored and that no eligible requests returns {} while still 
*                                                creating a PROCESSING interface job.
*                                       Test 3 - Confirms missing Allpay client-code configuration raises P4001 without tagging the request or 
*                                                creating an interface job.
*              
**/
BEGIN;

DO LANGUAGE plpgsql $$
DECLARE
    c_business_unit_id             CONSTANT SMALLINT := 5;
    c_account_standard             CONSTANT BIGINT := 99000000010001;
    c_account_first_fallback       CONSTANT BIGINT := 99000000010002;
    c_account_second_fallback      CONSTANT BIGINT := 99000000010003;
    c_account_organisation         CONSTANT BIGINT := 99000000010004;
    c_account_parent_guardian      CONSTANT BIGINT := 99000000010005;
    c_account_missing_config       CONSTANT BIGINT := 99000000010006;
    c_party_standard               CONSTANT BIGINT := 99000000020001;
    c_party_first_fallback         CONSTANT BIGINT := 99000000020002;
    c_party_second_fallback        CONSTANT BIGINT := 99000000020003;
    c_party_organisation           CONSTANT BIGINT := 99000000020004;
    c_party_defendant              CONSTANT BIGINT := 99000000020005;
    c_party_parent_guardian        CONSTANT BIGINT := 99000000020006;
    c_party_missing_config         CONSTANT BIGINT := 99000000020007;
    c_processed_job_id             CONSTANT BIGINT := 99000000040001;
    v_payload                      JSON;
    v_job_id                       BIGINT;
    v_item                         JSONB;
    v_count                        BIGINT;
    v_jobs_before                  BIGINT;
    v_error_caught                 BOOLEAN := FALSE;
    v_error_detail                 TEXT;
    v_error_state                  TEXT;
    v_error_message                TEXT;
    v_failed_test_count            INTEGER := 0;
BEGIN
    RAISE NOTICE '=== Setting up test data ===';

    ASSERT EXISTS (
        SELECT 1 FROM business_units WHERE business_unit_id = c_business_unit_id
    ), 'Existing business unit 5 is required by this test';

    ASSERT (
        SELECT COUNT(*)
        FROM configuration_items
        WHERE item_name = 'BUSINESS_UNIT_ALLPAY_CLIENT'
          AND business_unit_id = c_business_unit_id
          AND item_value = 'HMCA'
    ) = 1, 'Business unit 5 must have one BUSINESS_UNIT_ALLPAY_CLIENT configuration item with value HMCA';

    ASSERT NOT EXISTS (
        SELECT 1 FROM defendant_accounts
        WHERE defendant_account_id BETWEEN c_account_standard AND c_account_missing_config
    ), 'One or more test defendant_account_id values are already in use';

    ASSERT NOT EXISTS (
        SELECT 1 FROM parties
        WHERE party_id BETWEEN c_party_standard AND c_party_missing_config
    ), 'One or more test party_id values are already in use';

    ASSERT NOT EXISTS (
        SELECT 1 FROM defendant_account_parties
        WHERE defendant_account_party_id BETWEEN 99000000030001 AND 99000000030007
    ), 'One or more test defendant_account_party_id values are already in use';

    ASSERT NOT EXISTS (
        SELECT 1 FROM interface_jobs
        WHERE interface_job_id = c_processed_job_id
    ), 'Test interface_job_id is already in use';

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number,
        amount_imposed, amount_paid, account_balance, account_status, account_type
    ) VALUES
        (c_account_standard,        c_business_unit_id, 'HMCA0001', 100, 0, 100, 'L', 'Fine'),
        (c_account_first_fallback,  c_business_unit_id, 'HMCA0002', 100, 0, 100, 'L', 'Fine'),
        (c_account_second_fallback, c_business_unit_id, 'HMCA0003', 100, 0, 100, 'L', 'Fine'),
        (c_account_organisation,    c_business_unit_id, 'HMCA0004', 100, 0, 100, 'L', 'Fine'),
        (c_account_parent_guardian, c_business_unit_id, 'HMCA0005', 100, 0, 100, 'L', 'Fine'),
        (c_account_missing_config,  c_business_unit_id, 'HMCA0006', 100, 0, 100, 'L', 'Fine');

    INSERT INTO parties (
        party_id, organisation, organisation_name, surname, forenames, title,
        address_line_1, address_line_2, address_line_3, address_line_4, postcode
    ) VALUES
        (c_party_standard, FALSE, NULL, 'SmĀ[ith]', 'Alice    Beatrice', ' M?r ',
         '', E' 12\tHigh? Street ', '', ' Cambridge ', 'CB1? 1AA'),
        (c_party_first_fallback, FALSE, NULL, 'Longsurnameabcdefgh',
         'Alice Beatrice Catherine', 'Mrs', '1 First Fallback Road', NULL, NULL, NULL, 'AA1 1AA'),
        (c_party_second_fallback, FALSE, NULL, 'Longsurnameabcdefgh',
         'Alice Beatrice', 'Professor', '2 Second Fallback Road', NULL, NULL, NULL, 'BB2 2BB'),
        (c_party_organisation, TRUE, 'Acme? Corporation Limited', NULL, NULL, NULL,
         '3 Organisation Road', NULL, NULL, NULL, 'CC3 3CC'),
        (c_party_defendant, FALSE, NULL, 'NotTheDebtor', 'Daniel', 'Mr',
         '4 Defendant Road', NULL, NULL, NULL, 'DD4 4DD'),
        (c_party_parent_guardian, FALSE, NULL, 'Keeper', 'Paula    Guardian', 'Ms',
         '5 Parent Road', NULL, NULL, NULL, 'EE5 5EE'),
        (c_party_missing_config, FALSE, NULL, 'MissingCode', 'Michael', 'Mr',
         '6 Missing Code Road', NULL, NULL, NULL, 'FF6 6FF');

    INSERT INTO defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES
        (99000000030001, c_account_standard,        c_party_standard,        'Defendant',       TRUE),
        (99000000030002, c_account_first_fallback,  c_party_first_fallback,  'Defendant',       TRUE),
        (99000000030003, c_account_second_fallback, c_party_second_fallback, 'Defendant',       TRUE),
        (99000000030004, c_account_organisation,    c_party_organisation,    'Defendant',       TRUE),
        (99000000030005, c_account_parent_guardian, c_party_defendant,       'Defendant',       FALSE),
        (99000000030006, c_account_parent_guardian, c_party_parent_guardian, 'Parent/Guardian', TRUE),
        (99000000030007, c_account_missing_config,  c_party_missing_config,  'Defendant',       TRUE);

    -- Control the complete request batch for every test. The outer ROLLBACK
    -- restores any environment fixture requests after the script completes.
    DELETE FROM payment_card_requests;

    RAISE NOTICE '=== Test data setup completed ===';

    RAISE NOTICE '=== TEST 1: Successful batch and output transformations ===';
    BEGIN
        INSERT INTO payment_card_requests (defendant_account_id, interface_job_id)
        VALUES
            (c_account_standard, NULL),
            (c_account_first_fallback, NULL),
            (c_account_second_fallback, NULL),
            (c_account_organisation, NULL),
            (c_account_parent_guardian, NULL);

        CALL p_int_payment_card_requests(v_payload, v_job_id);
    
        ASSERT v_job_id IS NOT NULL, 'Successful call should return an interface_job_id';
        ASSERT jsonb_typeof(v_payload::JSONB) = 'array', 'Successful call should return a JSON array';
        ASSERT jsonb_array_length(v_payload::JSONB) = 5, 'Successful call should return five payment card requests';
    
        SELECT value INTO STRICT v_item
          FROM jsonb_array_elements(v_payload::JSONB) AS request(value)
         WHERE (value ->> 'defendant_account_id')::BIGINT = c_account_standard;
    
        ASSERT v_item ?& ARRAY[
            'defendant_account_id', 'business_unit_id', 'allpay_client_code', 'account_number',
            'debtor_party_id', 'debtor_association_type', 'debtor_title_initials', 'debtor_name',
            'name_on_card', 'address_line_1', 'address_line_2', 'address_line_3',
            'address_line_4', 'postcode'
        ], 'Each JSON item should contain all fourteen required fields';
        ASSERT (v_item ->> 'defendant_account_id')::BIGINT = c_account_standard, 'defendant_account_id should identify the requested account';
        ASSERT (v_item ->> 'business_unit_id')::SMALLINT = c_business_unit_id, 'business_unit_id should match the account';
        ASSERT v_item ->> 'allpay_client_code' = 'HMCA', 'allpay_client_code should come from configuration_items';
        ASSERT v_item ->> 'account_number' = 'HMCA0001', 'account_number should match the account';
        ASSERT (v_item ->> 'debtor_party_id')::BIGINT = c_party_standard, 'debtor_party_id should identify the debtor';
        ASSERT v_item ->> 'debtor_association_type' = 'Defendant', 'debtor association should be Defendant';
        ASSERT v_item ->> 'debtor_title_initials' = 'Mr A B', 'Title and initials should be cleaned and multiple spaces normalised';
        ASSERT v_item ->> 'debtor_name' = 'Smith', 'Debtor name should remove non-ASCII and excluded characters';
        ASSERT v_item ->> 'name_on_card' = 'Mr A B Smith', 'Short name_on_card should retain title and all initials';
        ASSERT v_item ->> 'address_line_1' = '12High Street', 'Address should remove control and excluded characters before compaction';
        ASSERT v_item ->> 'address_line_2' = 'Cambridge', 'Later populated address lines should move upward';
        ASSERT v_item -> 'address_line_3' = 'null'::JSONB, 'Missing third address line should be JSON null';
        ASSERT v_item -> 'address_line_4' = 'null'::JSONB, 'Missing fourth address line should be JSON null';
        ASSERT v_item ->> 'postcode' = 'CB1 1AA', 'Postcode should remove excluded characters';
    
        SELECT value INTO STRICT v_item
          FROM jsonb_array_elements(v_payload::JSONB) AS request(value)
         WHERE (value ->> 'defendant_account_id')::BIGINT = c_account_first_fallback;
    
        ASSERT v_item ->> 'debtor_title_initials' = 'Mrs A B C', 'All cleaned initials should be derived before name_on_card reduction';
        ASSERT v_item ->> 'debtor_name' = 'Longsurnameabcdefgh', 'A nineteen-character debtor name should be retained';
        ASSERT v_item ->> 'name_on_card' = 'Mrs A Longsurnameabcdefgh', 'First fallback should retain title and only the first initial';
        ASSERT length(v_item ->> 'name_on_card') <= 27, 'First-fallback name_on_card should not exceed 27 characters';
    
        SELECT value INTO STRICT v_item
          FROM jsonb_array_elements(v_payload::JSONB) AS request(value)
         WHERE (value ->> 'defendant_account_id')::BIGINT = c_account_second_fallback;
    
        ASSERT v_item ->> 'name_on_card' = 'A Longsurnameabcdefgh', 'Second fallback should remove the title and retain the first initial';
        ASSERT length(v_item ->> 'name_on_card') <= 27, 'Second-fallback name_on_card should not exceed 27 characters';
    
        SELECT value INTO STRICT v_item
          FROM jsonb_array_elements(v_payload::JSONB) AS request(value)
         WHERE (value ->> 'defendant_account_id')::BIGINT = c_account_organisation;
    
        ASSERT v_item -> 'debtor_title_initials' = 'null'::JSONB, 'Organisation debtor should have null title and initials';
        ASSERT v_item ->> 'debtor_name' = 'Acme Corporation Li', 'Organisation name should be cleaned and truncated to nineteen characters';
        ASSERT v_item ->> 'name_on_card' = 'Acme Corporation Li', 'Organisation name_on_card should contain the cleaned organisation name';
    
        SELECT value INTO STRICT v_item
          FROM jsonb_array_elements(v_payload::JSONB) AS request(value)
         WHERE (value ->> 'defendant_account_id')::BIGINT = c_account_parent_guardian;
    
        ASSERT (v_item ->> 'debtor_party_id')::BIGINT = c_party_parent_guardian, 'Parent/guardian debtor party should be selected';
        ASSERT v_item ->> 'debtor_association_type' = 'Parent/Guardian', 'Parent/guardian association should be returned';
        ASSERT v_item ->> 'debtor_title_initials' = 'Ms P G', 'Parent/guardian title and initials should be derived from their details';
        ASSERT v_item ->> 'debtor_name' = 'Keeper', 'Parent/guardian surname should be returned';
    
        SELECT COUNT(*) INTO v_count
          FROM payment_card_requests
         WHERE defendant_account_id BETWEEN c_account_standard AND c_account_parent_guardian
           AND interface_job_id = v_job_id;

        ASSERT v_count = 5, 'Every successfully processed request should be tagged with the returned job ID';
    
        SELECT COUNT(*) INTO v_count
        FROM interface_jobs
        WHERE interface_job_id = v_job_id
          AND business_unit_id IS NULL
          AND interface_name = 'payment_card_requests'
          AND status = 'PROCESSING'
          AND created_datetime IS NOT NULL
          AND started_datetime IS NOT NULL
          AND completed_datetime IS NULL;

        ASSERT v_count = 1, 'Successful call should create the required PROCESSING interface job';

        RAISE NOTICE 'TEST 1 PASSED';
    EXCEPTION
        -- OTHERS does not include PostgreSQL ASSERT_FAILURE (SQLSTATE P0004).
        WHEN ASSERT_FAILURE OR OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_state = RETURNED_SQLSTATE,
                v_error_message = MESSAGE_TEXT;
            v_failed_test_count := v_failed_test_count + 1;
            RAISE WARNING 'TEST 1 FAILED [%]: %', v_error_state, v_error_message;
    END;

    -- Ensure Test 2 does not depend on Test 1 succeeding.
    DELETE FROM payment_card_requests
     WHERE defendant_account_id BETWEEN c_account_standard AND c_account_parent_guardian;

    RAISE NOTICE '=== TEST 2: No eligible requests and already-processed exclusion ===';
    BEGIN
        INSERT INTO interface_jobs (
            interface_job_id, business_unit_id, interface_name, status,
            created_datetime, started_datetime, completed_datetime
        ) VALUES (
            c_processed_job_id, NULL, 'payment_card_requests', 'PROCESSING',
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL
        );

        INSERT INTO payment_card_requests (defendant_account_id, interface_job_id)
        VALUES (c_account_missing_config, c_processed_job_id);

        v_payload := NULL;
        v_job_id := NULL;

        CALL p_int_payment_card_requests(v_payload, v_job_id);
    
        ASSERT v_payload::TEXT = '{}', 'No-data call should return an empty JSON object';
        ASSERT v_job_id IS NOT NULL, 'No-data call should still return an interface_job_id';
    
        SELECT COUNT(*) INTO v_count
          FROM interface_jobs
         WHERE interface_job_id = v_job_id
           AND business_unit_id IS NULL
           AND interface_name = 'payment_card_requests'
           AND status = 'PROCESSING'
           AND created_datetime IS NOT NULL
           AND started_datetime IS NOT NULL
           AND completed_datetime IS NULL;

        ASSERT v_count = 1, 'No-data call should still create the required PROCESSING interface job';
    
        SELECT COUNT(*) INTO v_count
          FROM payment_card_requests
         WHERE defendant_account_id = c_account_missing_config
           AND interface_job_id IS NOT NULL;

        ASSERT v_count = 1, 'An already-processed request should be ignored and remain tagged';

        RAISE NOTICE 'TEST 2 PASSED';
    EXCEPTION
        -- OTHERS does not include PostgreSQL ASSERT_FAILURE (SQLSTATE P0004).
        WHEN ASSERT_FAILURE OR OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_state = RETURNED_SQLSTATE,
                v_error_message = MESSAGE_TEXT;
            v_failed_test_count := v_failed_test_count + 1;
            RAISE WARNING 'TEST 2 FAILED [%]: %', v_error_state, v_error_message;
    END;

    RAISE NOTICE '=== TEST 3: Missing Allpay client-code configuration error ===';
    BEGIN
        INSERT INTO payment_card_requests (defendant_account_id, interface_job_id)
        VALUES (c_account_missing_config, NULL)
        ON CONFLICT (defendant_account_id) DO UPDATE
        SET interface_job_id = EXCLUDED.interface_job_id;

        DELETE FROM configuration_items
         WHERE item_name = 'BUSINESS_UNIT_ALLPAY_CLIENT'
           AND business_unit_id = c_business_unit_id
           AND item_value = 'HMCA';

        SELECT COUNT(*) INTO v_jobs_before FROM interface_jobs;

        v_payload := NULL;
        v_job_id := NULL;

        BEGIN
            CALL p_int_payment_card_requests(v_payload, v_job_id);
            
            ASSERT FALSE, 'Missing client-code configuration should raise SQLSTATE P4001';
        EXCEPTION
            WHEN SQLSTATE 'P4001' THEN
                v_error_caught := TRUE;
                GET STACKED DIAGNOSTICS v_error_detail = PG_EXCEPTION_DETAIL;
                ASSERT SQLERRM = format(
                    'Allpay client code not found for business unit id %s', c_business_unit_id
                ), 'Missing configuration should return the expected error message';
                ASSERT v_error_detail =
                    'p_int_payment_card_requests: Allpay client code for business unit is missing from configuration_items',
                    'Missing configuration should return the expected error detail';
        END;
    
        ASSERT v_error_caught, 'Missing client-code configuration should raise SQLSTATE P4001';
    
        SELECT COUNT(*) INTO v_count
          FROM payment_card_requests
         WHERE defendant_account_id = c_account_missing_config
           AND interface_job_id IS NULL;
    
        ASSERT v_count = 1, 'Failed call should leave the payment card request untagged';
    
        SELECT COUNT(*) INTO v_count FROM interface_jobs;
        ASSERT v_count = v_jobs_before, 'Failed call should not create an interface job';
    
        RAISE NOTICE 'TEST 3 PASSED';
    EXCEPTION
        -- OTHERS does not include PostgreSQL ASSERT_FAILURE (SQLSTATE P0004).
        WHEN ASSERT_FAILURE OR OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_state = RETURNED_SQLSTATE,
                v_error_message = MESSAGE_TEXT;
            v_failed_test_count := v_failed_test_count + 1;
            RAISE WARNING 'TEST 3 FAILED [%]: %', v_error_state, v_error_message;
    END;

    IF v_failed_test_count > 0 THEN
        RAISE EXCEPTION '% p_int_payment_card_requests test(s) failed; see warnings above', v_failed_test_count;
    END IF;

    RAISE NOTICE 'All p_int_payment_card_requests unit tests passed';
END;
$$;

ROLLBACK;
