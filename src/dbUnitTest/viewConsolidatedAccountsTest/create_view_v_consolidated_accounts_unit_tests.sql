/**
* CGI OPAL Program
*
* MODULE      : create_view_v_consolidated_accounts_unit_tests_copilot.sql
*
* DESCRIPTION : Unit test for v_consolidated_accounts view
*               Tests verify that the view correctly retrieves consolidated account information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 26/02/2026    C Larkin    1.0         PO-2339 - Unit Test for v_consolidated_accounts view
*
**/

-- Start timing measurements for test execution
\timing

DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Clear down data
    DELETE FROM defendant_transactions
     WHERE defendant_transaction_id IN (9601, 9602, 9603, 9604, 9605);
    DELETE FROM defendant_account_parties
     WHERE defendant_account_party_id IN (9601, 9602, 9603);
    DELETE FROM defendant_accounts
     WHERE defendant_account_id IN (9101, 9102, 9201, 9202, 9203, 9204, 9205);
    DELETE FROM parties
     WHERE party_id IN (9301, 9302, 9303);
    DELETE FROM business_units
     WHERE business_unit_id = 91;

    RAISE NOTICE '=== Data cleanup before tests completed ===';

END $$;

DO LANGUAGE 'plpgsql' $$
DECLARE

    -- Declare local variables for each column in v_consolidated_accounts
    v_master_account_id      BIGINT;
    v_child_account_id       VARCHAR(30);
    v_child_account_number   VARCHAR(30);
    v_child_reference        VARCHAR(30);
    v_child_date_imposed     DATE;
    v_child_imposed_by       VARCHAR(40);
    v_child_first_name       VARCHAR(50);
    v_child_last_name        VARCHAR(50);
    v_row_count              BIGINT;

BEGIN

    RAISE NOTICE '=== Setting up test data for v_consolidated_accounts tests ===';

    -- Insert test data - Business Unit
    INSERT INTO business_units
            (
                business_unit_id,
                business_unit_name,
                business_unit_code,
                business_unit_type,
                welsh_language
            ) VALUES
            (
                91,
                'Consolidated Accounts Unit Test BU',
                'BU91',
                'Accounting',
                FALSE
            );

    -- Insert test data - Parties
    INSERT INTO parties 
            (
                party_id,
                organisation,
                title,
                forenames,
                surname,
                organisation_name,
                address_line_1,
                postcode,
                account_type
            ) VALUES
            (
                9301,
                FALSE,
                'Mr',
                'John',
                'Doe',
                NULL,
                '1 Test Street',
                'TS1 1AA',
                'Fine'
            ),
            (
                9302,
                FALSE,
                'Ms',
                'Jane',
                'Roe',
                NULL,
                '2 Test Street',
                'TS1 1AB',
                'Fine'
            ),
            (
                9303,
                FALSE,
                'Dr',
                'Alex',
                'Smith',
                NULL,
                '3 Test Street',
                'TS1 1AC',
                'Fine'
            );

    -- Insert test data - Defendant Accounts
    INSERT INTO defendant_accounts 
            (
                defendant_account_id,
                business_unit_id,
                account_number,
                amount_imposed,
                amount_paid,
                account_balance,
                account_status,
                account_type,
                prosecutor_case_reference,
                imposed_hearing_date,
                imposed_by_name
            ) VALUES
            (
                9101,
                91,
                'MA001',
                1000.00,
                0.00,
                1000.00,
                'L',
                'Fine',
                'MREF001',
                '2025-01-01',
                'Court A'
            ),
            (
                9102,
                91,
                'MA002',
                2000.00,
                0.00,
                2000.00,
                'L',
                'Fine',
                'MREF002',
                '2025-01-01',
                'Court B'
            ),
            (
                9201,
                91,
                'CA001',
                500.00,
                0.00,
                500.00,
                'L',
                'Fine',
                'CREF001',
                '2025-02-01',
                'Court B'
            ),
            (
                9202,
                91,
                'CA002',
                600.00,
                0.00,
                600.00,
                'L',
                'Fine',
                'CREF002',
                '2025-03-01',
                'Court C'
            ),
            (
                9203,
                91,
                'CA003',
                700.00,
                0.00,
                700.00,
                'L',
                'Fine',
                'CREF003',
                '2025-04-01',
                'Court D'
            ),
            (
                9204,
                91,
                'CA004',
                800.00,
                0.00,
                800.00,
                'L',
                'Fine',
                'CREF004',
                '2025-05-01',
                'Court E'
            ),
            (
                9205,
                91,
                'CA005',
                900.00,
                0.00,
                900.00,
                'L',
                'Fine',
                'CREF005',
                '2025-06-01',
                'Court F'
            );

    -- Insert test data - Defendant account parties
    -- Scenario 1 child account has Defendant association
    INSERT INTO defendant_account_parties
            (
                defendant_account_party_id,
                defendant_account_id,
                party_id,
                association_type,
                debtor
            ) VALUES
            (
                9601,
                9201,
                9301,
                'Defendant',
                TRUE
            );

    -- Scenario 3 child account has non-Defendant association only
    INSERT INTO defendant_account_parties
            (
                defendant_account_party_id,
                defendant_account_id,
                party_id,
                association_type,
                debtor
            ) VALUES
            (
                9602,
                9203,
                9302,
                'Parent/Guardian',
                FALSE
            );

    -- Additional non-related defendant association for Master Defendant Account to verify only correct child accounts are returned
    INSERT INTO defendant_account_parties
            (
                defendant_account_party_id,
                defendant_account_id,
                party_id,
                association_type,
                debtor
            ) VALUES
            (
                9603,
                9101,
                9303,
                'Defendant',
                TRUE
            );

    -- Insert test data - Defendant transactions
    -- Scenario 1: Valid CONSOL transaction
    INSERT INTO defendant_transactions
            (
                defendant_transaction_id,
                defendant_account_id,
                posted_date,
                transaction_type,
                transaction_amount,
                associated_record_type,
                associated_record_id
            ) VALUES
            (
                9601,
                9101,
                CURRENT_TIMESTAMP - INTERVAL '10 days',
                'CONSOL',
                500.00,
                'defendant_accounts',
                '9201'
            );

    -- Scenario 2: Valid CONSOL transaction, child has no party association
    INSERT INTO defendant_transactions
            (
                defendant_transaction_id,
                defendant_account_id,
                posted_date,
                transaction_type,
                transaction_amount,
                associated_record_type,
                associated_record_id
            ) VALUES
            (
                9602,
                9102,
                CURRENT_TIMESTAMP - INTERVAL '9 days',
                'CONSOL',
                600.00,
                'defendant_accounts',
                '9202'
            );

    -- Scenario 3: Valid CONSOL transaction, child has no Defendant association in defendant_account_parties
    INSERT INTO defendant_transactions
            (
                defendant_transaction_id,
                defendant_account_id,
                posted_date,
                transaction_type,
                transaction_amount,
                associated_record_type,
                associated_record_id
            ) VALUES
            (
                9603,
                9101,
                CURRENT_TIMESTAMP - INTERVAL '8 days',
                'CONSOL',
                700.00,
                'defendant_accounts',
                '9203'
            );

    -- Scenario 4: Excluded - transaction_type not CONSOL
    INSERT INTO defendant_transactions
            (
                defendant_transaction_id,
                defendant_account_id,
                posted_date,
                transaction_type,
                transaction_amount,
                associated_record_type,
                associated_record_id
            ) VALUES
            (
                9604,
                9101,
                CURRENT_TIMESTAMP - INTERVAL '7 days',
                'PAYMNT',
                800.00,
                'defendant_accounts',
                '9204'
            );

    -- Scenario 5: Excluded - associated_record_type not defendant_accounts
    INSERT INTO defendant_transactions
            (
                defendant_transaction_id,
                defendant_account_id,
                posted_date,
                transaction_type,
                transaction_amount,
                associated_record_type,
                associated_record_id
            ) VALUES
            (
                9605,
                9101,
                CURRENT_TIMESTAMP - INTERVAL '6 days',
                'CONSOL',
                900.00,
                'creditor_accounts',
                '9205'
            );

    RAISE NOTICE '=== Setting up test data for v_consolidated_accounts tests complete ===';

    -- Test scenarios

    -- Scenario 1: Verify the number of rows ibmn teh view
    RAISE NOTICE '--- Running test scenario 1 ---';

    SELECT  COUNT(*)
    INTO    v_row_count
    FROM    v_consolidated_accounts;

    ASSERT v_row_count = 3, 'There should 3 rows in the view';

    -- Scenario 2: Verify valid CONSOL transaction returns child account with Defendant party details
    RAISE NOTICE '--- Running test scenario 2 ---';

    SELECT  master_account_id,
            child_account_id,
            child_account_number,
            child_reference,
            child_date_imposed,
            child_imposed_by,
            child_first_name,
            child_last_name
    INTO 
            v_master_account_id,
            v_child_account_id,
            v_child_account_number,
            v_child_reference,
            v_child_date_imposed,
            v_child_imposed_by,
            v_child_first_name,
            v_child_last_name
    FROM    v_consolidated_accounts
    WHERE   master_account_id = 9101
    AND     child_account_id = '9201';

    ASSERT v_master_account_id = 9101, 'Master account ID should be 9101';
    ASSERT v_child_account_id = '9201', 'Child account ID should be 9201';
    ASSERT v_child_account_number = 'CA001', 'Child account number should be CA001';
    ASSERT v_child_reference = 'CREF001', 'Child reference should be CREF001';
    ASSERT v_child_date_imposed = '2025-02-01'::DATE, 'Child imposed date should be 2025-02-01';
    ASSERT v_child_imposed_by = 'Court B', 'Child imposed by should be Court B';
    ASSERT v_child_first_name = 'John', 'Child first name should be John';
    ASSERT v_child_last_name = 'Doe', 'Child last name should be Doe';

    -- Scenario 3: Verify valid CONSOL transaction returns child account with NULL party details when no party association exists
    RAISE NOTICE '--- Running test scenario 3 ---';

    SELECT  master_account_id,
            child_account_id,
            child_account_number,
            child_reference,
            child_date_imposed,
            child_imposed_by,
            child_first_name,
            child_last_name
    INTO 
            v_master_account_id,
            v_child_account_id,
            v_child_account_number,
            v_child_reference,
            v_child_date_imposed,
            v_child_imposed_by,
            v_child_first_name,
            v_child_last_name
    FROM    v_consolidated_accounts
    WHERE   master_account_id = 9102
    AND     child_account_id = '9202';

    ASSERT v_master_account_id = 9102, 'Master account ID should be 9102';
    ASSERT v_child_account_id = '9202', 'Child account ID should be 9202';
    ASSERT v_child_account_number = 'CA002', 'Child account number should be CA002';
    ASSERT v_child_reference = 'CREF002', 'Child reference should be CREF002';
    ASSERT v_child_date_imposed = '2025-03-01'::DATE, 'Child imposed date should be 2025-03-01';
    ASSERT v_child_imposed_by = 'Court C', 'Child imposed by should be Court C';
    ASSERT v_child_first_name IS NULL, 'Child first name should be NULL when no party association exists';
    ASSERT v_child_last_name IS NULL, 'Child last name should be NULL when no party association exists';

    -- Scenario 4: Verify valid CONSOL transaction returns child account with NULL party details when no Defendant association exists
    RAISE NOTICE '--- Running test scenario 4 ---';

    SELECT  master_account_id,
            child_account_id,
            child_account_number,
            child_reference,
            child_date_imposed,
            child_imposed_by,
            child_first_name,
            child_last_name
    INTO 
            v_master_account_id,
            v_child_account_id,
            v_child_account_number,
            v_child_reference,
            v_child_date_imposed,
            v_child_imposed_by,
            v_child_first_name,
            v_child_last_name
    FROM    v_consolidated_accounts
    WHERE   master_account_id = 9101
    AND     child_account_id = '9203';

    ASSERT v_master_account_id = 9101, 'Master account ID should be 9101';
    ASSERT v_child_account_id = '9203', 'Child account ID should be 9203';
    ASSERT v_child_account_number = 'CA003', 'Child account number should be CA003';
    ASSERT v_child_reference = 'CREF003', 'Child reference should be CREF003';
    ASSERT v_child_date_imposed = '2025-04-01'::DATE, 'Child imposed date should be 2025-04-01';
    ASSERT v_child_imposed_by = 'Court D', 'Child imposed by should be Court D';
    ASSERT v_child_first_name IS NULL, 'Child first name should be NULL when no Defendant association exists';
    ASSERT v_child_last_name IS NULL, 'Child last name should be NULL when no Defendant association exists';

    -- Scenario 5: Verify non-CONSOL transaction is excluded from the view
    RAISE NOTICE '--- Running test scenario 5 ---';

    SELECT  COUNT(*)
    INTO    v_row_count
    FROM    v_consolidated_accounts
    WHERE   master_account_id = 9101
    AND     child_account_id = '9204';

    ASSERT v_row_count = 0, 'Non-CONSOL transaction should not appear in the view';

    -- Scenario 6: Verify non-defendant_accounts associated_record_type is excluded from the view
    RAISE NOTICE '--- Running test scenario 6 ---';

    SELECT  COUNT(*)
    INTO    v_row_count
    FROM    v_consolidated_accounts
    WHERE   master_account_id = 9101
    AND     child_account_id = '9205';

    ASSERT v_row_count = 0, 'Non-defendant_accounts associated_record_type should not appear in the view';
    
    RAISE NOTICE '=== All tests completed and passed for v_consolidated_accounts view ===';

END
$$;

\timing
