/**
* CGI OPAL Program
*
* MODULE      : create_view_v_search_minor_creditor_accounts_unit_tests.sql
*
* DESCRIPTION : Unit test for v_search_minor_creditor_accounts view
*               Tests verify that the view correctly retrieves minor creditor account information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 02/09/2025    C Larkin    1.0         PO-2114 -  Unit Test for search_minor_creditor_accounts view
*
**/

-- Start timing measurements for test execution
\timing

DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Clear down data
    DELETE FROM cheques;
    DELETE FROM creditor_transactions;
    DELETE FROM creditor_accounts;
    DELETE FROM parties;
    DELETE FROM courts;
    DELETE FROM enforcers;
    DELETE FROM prisons;
    DELETE FROM major_creditors;
    DELETE FROM configuration_items;
    DELETE FROM business_units;      

    RAISE NOTICE '=== Data cleanup before tests completed ===';

END $$;

DO LANGUAGE 'plpgsql' $$
DECLARE

    -- Declare local variables for each column in v_search_minor_creditor_accounts
    v_creditor_account_id         BIGINT;
    v_account_number              VARCHAR(20);
    v_business_unit_id            SMALLINT;
    v_business_unit_name          VARCHAR(200);
    v_party_id                    BIGINT;
    v_organisation                BOOLEAN;
    v_organisation_name           VARCHAR(80);
    v_address_line_1              VARCHAR(35);
    v_postcode                    VARCHAR(10);    
    v_forenames                   VARCHAR(50);
    v_surname                     VARCHAR(50);
    v_defendant_account_id        BIGINT;
    v_defendant_organisation_name VARCHAR(80);
    v_defendant_forenames         VARCHAR(50);
    v_defendant_surname           VARCHAR(50);
    v_creditor_account_balance    NUMERIC(18,2);

BEGIN

    RAISE NOTICE '=== Setting up test data for v_search_minor_creditor_accounts tests ===';

    -- Insert test data - Business Units
    FOR i IN 1..3 LOOP
        INSERT INTO business_units 
                (
                    business_unit_id,
                    business_unit_name,
                    business_unit_code,
                    business_unit_type,
                    welsh_language
                ) VALUES (
                    i,
                    'Test Audit Business Unit',
                    'TBU' || i,
                    'Accounting',
                    FALSE
                );
    END LOOP;

    -- Insert test data - Parties
    -- Person
    INSERT INTO parties 
            (
                party_id,
                organisation,
                title,
                forenames,
                surname,
                organisation_name,
                address_line_1,
                address_line_2,
                address_line_3,
                postcode,
                account_type
            ) VALUES
            (
                1,
                FALSE,
                'Mr',
                'John',
                'Smith',
                NULL,
                '123 Main Street',
                'Apartment 4B',
                'Downtown',
                'SW1A 1AA',
                'Minor Creditor'
            );

    -- Organisation
    INSERT INTO parties 
            (
                party_id,
                organisation,
                title,
                forenames,
                surname,
                organisation_name,
                address_line_1,
                address_line_2,
                address_line_3,
                postcode,
                account_type
            ) VALUES
            (
                2,
                TRUE,
                NULL,
                NULL,
                NULL,
                'Acme Corporation',
                '456 Business Road',
                NULL,
                'Industrial Park',
                'EC2A 4AA',
                'Minor Creditor'
            );

    -- Insert test data - Creditor Accounts - Scenario 1
    INSERT INTO creditor_accounts 
            (
                creditor_account_id,
                business_unit_id,
                account_number,
                creditor_account_type,
                prosecution_service,
                minor_creditor_party_id,
                from_suspense,
                hold_payout,
                pay_by_bacs,
                bank_sort_code,
                bank_account_number,
                bank_account_name,
                bank_account_reference,
                bank_account_type
            ) VALUES
            (
                1,
                1,
                'CA001',
                'MN',
                FALSE,
                1,
                FALSE,
                TRUE,
                TRUE,
                '123456',
                '12345678',
                'John Smith Account',
                'REF001',
                '1'
            );

    -- Insert test data - Creditor Transactions
    -- Scenario 1 - creditor transaction 1
    INSERT INTO creditor_transactions
            (
                creditor_transaction_id,
                creditor_account_id,
                posted_date,
                posted_by,
                posted_by_name,
                transaction_type,
                transaction_amount,
                imposition_result_id,
                payment_processed,
                payment_reference,
                status,
                status_date,
                associated_record_type,
                associated_record_id
            ) VALUES 
            (
                1,
                1,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                'DRJ',
                NULL,
                'PAYMNT',
                5000,
                1,
                FALSE,
                976,
                NULL,
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                NULL,
                NULL    
            );

    -- Scenario 1 - Cheque 1
    INSERT INTO cheques 
            (
                cheque_id,
                business_unit_id,
                cheque_number,
                issue_date,
                creditor_transaction_id,
                defendant_transaction_id,
                amount,
                allocation_type,
                reminder_date,
                status
            ) VALUES 
            (
                1,
                3,
                8888,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                1,
                NULL,
                5000,
                'A',
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                'U'
            );

    -- Scenario 1 - creditor transaction 2
    INSERT INTO creditor_transactions 
            (
                creditor_transaction_id,
                creditor_account_id,
                posted_date,
                posted_by,
                posted_by_name,
                transaction_type,
                transaction_amount,
                imposition_result_id,
                payment_processed,
                payment_reference,
                status,
                status_date,
                associated_record_type,
                associated_record_id
            ) VALUES 
            (
                2,
                1,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                'DRJ2',
                NULL,
                'PAYMNT',
                1050,
                1,
                FALSE,
                976,
                NULL,
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                NULL,
                NULL
            );

    -- Scenario 1 - Cheque 2
    INSERT INTO cheques 
            (
                cheque_id,
                business_unit_id,
                cheque_number,
                issue_date,
                creditor_transaction_id,
                defendant_transaction_id,
                amount,
                allocation_type,
                reminder_date,
                status
            ) VALUES 
            (
                2,
                2,
                7777,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                2,
                NULL,
                1050,
                'A',
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                'U'
            );

    -- Insert test data - Creditor Accounts - Scenario 2
    INSERT INTO creditor_accounts 
            (
                creditor_account_id,
                business_unit_id,
                account_number,
                creditor_account_type,
                prosecution_service,
                minor_creditor_party_id,
                from_suspense,
                hold_payout,
                pay_by_bacs,
                bank_sort_code,
                bank_account_number,
                bank_account_name,
                bank_account_reference,
                bank_account_type
            ) VALUES
            (
                2,
                1,
                'CA002',
                'MN',
                FALSE,
                2,
                FALSE,
                FALSE,
                TRUE,
                '654321',
                '87654321',
                'Acme Corp Account',
                'REF002',
                '2'
            );

    INSERT INTO creditor_transactions 
            (
                creditor_transaction_id,
                creditor_account_id,
                posted_date,
                posted_by,
                posted_by_name,
                transaction_type,
                transaction_amount,
                imposition_result_id,
                payment_processed,
                payment_reference,
                status,
                status_date,
                associated_record_type,
                associated_record_id
            ) VALUES
                (3, 2, (CURRENT_TIMESTAMP - INTERVAL '30 days'), 'DRJ3', NULL, 'PAYMNT', 2000, 2, TRUE, 977, NULL, (CURRENT_TIMESTAMP + INTERVAL '30 days'), NULL, NULL),
                (4, 2, (CURRENT_TIMESTAMP - INTERVAL '20 days'), 'DRJ4', NULL, 'PAYMNT', 1500, 2, TRUE, 978, NULL, (CURRENT_TIMESTAMP + INTERVAL '20 days'), NULL, NULL),
                (5, 2, (CURRENT_TIMESTAMP - INTERVAL '10 days'), 'DRJ5', NULL, 'PAYMNT', 2500, 2, FALSE, 979, NULL, (CURRENT_TIMESTAMP + INTERVAL '10 days'), NULL, NULL);

    INSERT INTO cheques 
            (
                cheque_id,
                business_unit_id,
                cheque_number,
                issue_date,
                creditor_transaction_id,
                defendant_transaction_id,
                amount,
                allocation_type,
                reminder_date,
                status
            ) VALUES
                (3, 1, 6666, (CURRENT_TIMESTAMP - INTERVAL '30 days'), 3, NULL, 2000, 'A', (CURRENT_TIMESTAMP + INTERVAL '30 days'), 'U'),
                (4, 2, 5555, (CURRENT_TIMESTAMP - INTERVAL '20 days'), 4, NULL, 1500, 'A', (CURRENT_TIMESTAMP + INTERVAL '20 days'), 'U'),
                (5, 3, 4444, (CURRENT_TIMESTAMP - INTERVAL '10 days'), 5, NULL, 2500, 'A', (CURRENT_TIMESTAMP + INTERVAL '10 days'), 'U');

    -- Insert test data - Creditor Accounts - Scenario 3
    INSERT INTO creditor_accounts 
            (
                creditor_account_id,
                business_unit_id,
                account_number,
                creditor_account_type,
                prosecution_service,
                minor_creditor_party_id,
                from_suspense,
                hold_payout,
                pay_by_bacs,
                bank_sort_code,
                bank_account_number,
                bank_account_name,
                bank_account_reference,
                bank_account_type
            ) VALUES
            (
                3,
                2,
                'CA003',
                'MN',
                FALSE,
                1,
                FALSE,
                FALSE,
                TRUE,
                '111222',
                '11223344',
                'John Smith Acct 2',
                'REF003',
                '1'
            );

    -- Insert processed PAYMNT transactions for Scenario 3
    INSERT INTO creditor_transactions 
            (
                creditor_transaction_id,
                creditor_account_id,
                posted_date,
                posted_by,
                posted_by_name,
                transaction_type,
                transaction_amount,
                imposition_result_id,
                payment_processed,
                payment_reference,
                status,
                status_date,
                associated_record_type,
                associated_record_id
            ) VALUES
                (6, 3, (CURRENT_TIMESTAMP - INTERVAL '15 days'), 'DRJ6', NULL, 'PAYMNT', 1200, 3, TRUE, 980, NULL, (CURRENT_TIMESTAMP + INTERVAL '15 days'), NULL, NULL),
                (7, 3, (CURRENT_TIMESTAMP - INTERVAL '5 days'), 'DRJ7', NULL, 'PAYMNT', 800, 3, TRUE, 981, NULL, (CURRENT_TIMESTAMP + INTERVAL '5 days'), NULL, NULL);

    -- Insert cheques for Scenario 3
    INSERT INTO cheques 
            (
                cheque_id,
                business_unit_id,
                cheque_number,
                issue_date,
                creditor_transaction_id,
                defendant_transaction_id,
                amount,
                allocation_type,
                reminder_date,
                status
            ) VALUES
                (6, 2, 3333, (CURRENT_TIMESTAMP - INTERVAL '15 days'), 6, NULL, 1200, 'A', (CURRENT_TIMESTAMP + INTERVAL '15 days'), 'U'),
                (7, 2, 2222, (CURRENT_TIMESTAMP - INTERVAL '5 days'), 7, NULL, 800, 'A', (CURRENT_TIMESTAMP + INTERVAL '5 days'), 'U');

    -- Insert test data - Creditor Accounts - Scenario 4 (no PAYMNT transactions, but has 3 cheques)
    INSERT INTO creditor_accounts 
            (
                creditor_account_id,
                business_unit_id,
                account_number,
                creditor_account_type,
                prosecution_service,
                minor_creditor_party_id,
                from_suspense,
                hold_payout,
                pay_by_bacs,
                bank_sort_code,
                bank_account_number,
                bank_account_name,
                bank_account_reference,
                bank_account_type
            ) VALUES
            (
                4,
                3,
                'CA004',
                'MN',
                FALSE,
                2,
                FALSE,
                FALSE,
                TRUE,
                '333444',
                '33445566',
                'Acme Corp Acct 2',
                'REF004',
                '2'
            );

    -- Insert creditor_transactions for Scenario 4 with transaction_type OTHER
    INSERT INTO creditor_transactions 
        (
            creditor_transaction_id,
            creditor_account_id,
            posted_date,
            posted_by,
            posted_by_name,
            transaction_type,
            transaction_amount,
            imposition_result_id,
            payment_processed,
            payment_reference,
            status,
            status_date,
            associated_record_type,
            associated_record_id
        ) VALUES
            (8, 4, (CURRENT_TIMESTAMP - INTERVAL '7 days'), 'DRJ8', NULL, 'OTHER', 1000, NULL, NULL, NULL, NULL, (CURRENT_TIMESTAMP + INTERVAL '7 days'), NULL, NULL),
            (9, 4, (CURRENT_TIMESTAMP - INTERVAL '5 days'), 'DRJ9', NULL, 'OTHER', 2000, NULL, NULL, NULL, NULL, (CURRENT_TIMESTAMP + INTERVAL '5 days'), NULL, NULL),
            (10, 4, (CURRENT_TIMESTAMP - INTERVAL '3 days'), 'DRJ10', NULL, 'OTHER', 3000, NULL, NULL, NULL, NULL, (CURRENT_TIMESTAMP + INTERVAL '3 days'), NULL, NULL);
                    
    -- Insert cheques for Scenario 4
    INSERT INTO cheques 
            (
                cheque_id,
                business_unit_id,
                cheque_number,
                issue_date,
                creditor_transaction_id,
                defendant_transaction_id,
                amount,
                allocation_type,
                reminder_date,
                status
            ) VALUES
                (8, 3, 1111, (CURRENT_TIMESTAMP - INTERVAL '7 days'), NULL, NULL, 1000, 'A', (CURRENT_TIMESTAMP + INTERVAL '7 days'), 'U'),
                (9, 3, 2223, (CURRENT_TIMESTAMP - INTERVAL '5 days'), NULL, NULL, 2000, 'A', (CURRENT_TIMESTAMP + INTERVAL '5 days'), 'U'),
                (10, 3, 3335, (CURRENT_TIMESTAMP - INTERVAL '3 days'), NULL, NULL, 3000, 'A', (CURRENT_TIMESTAMP + INTERVAL '3 days'), 'U');

    -- Insert test data - Creditor Accounts - Scenario 5 (no transactions at all)
    INSERT INTO creditor_accounts 
            (
                creditor_account_id,
                business_unit_id,
                account_number,
                creditor_account_type,
                prosecution_service,
                minor_creditor_party_id,
                from_suspense,
                hold_payout,
                pay_by_bacs,
                bank_sort_code,
                bank_account_number,
                bank_account_name,
                bank_account_reference,
                bank_account_type
            ) VALUES
            (
                5,
                2,
                'CA005',
                'MN',
                FALSE,
                2,
                FALSE,
                FALSE,
                TRUE,
                '555666',
                '55667788',
                'Acme Corp Acct 3',
                'REF005',
                '2'
            );

    RAISE NOTICE '=== Setting up test data for v_search_minor_creditor_accounts tests complete ===';

    -- Test scenarios
    -- Scenario 1: Verify creditor_account_balance calculation for account with multiple PAYMNT transactions, all unprocessed
    -- Scenario 2: Verify creditor_account_balance calculation for account with multiple PAYMNT transactions, some unprocessed
    -- Scenario 3: Verify creditor_account_balance calculation for account with multiple PAYMNT transactions, all processed
    -- Scenario 4: Verify creditor_account_balance calculation for account with no PAYMNT transactions
    -- Scenario 5: Verify creditor_account_balance calculation for account with no transactions at all

    FOR i IN 1..5 LOOP
        
        RAISE NOTICE '--- Running test scenario % ---', i;
        -- Select from the view and assign to variables
        SELECT  creditor_account_id,
                account_number,
                business_unit_id,
                business_unit_name,
                party_id,
                organisation,
                organisation_name,
                address_line_1,
                postcode,
                forenames,
                surname,
                defendant_account_id,
                defendant_organisation_name,
                defendant_forenames,
                defendant_surname,
                creditor_account_balance
        INTO 
                v_creditor_account_id,
                v_account_number,
                v_business_unit_id,
                v_business_unit_name,
                v_party_id,
                v_organisation,
                v_organisation_name,
                v_address_line_1,
                v_postcode,
                v_forenames,
                v_surname,
                v_defendant_account_id,
                v_defendant_organisation_name,
                v_defendant_forenames,
                v_defendant_surname,
                v_creditor_account_balance
        FROM    v_search_minor_creditor_accounts
        WHERE   creditor_account_id = i;

        CASE i
            WHEN 1 THEN
                -- Scenario 1: Expect balance to be sum of unprocessed PAYMNT transactions (5000 + 1050 = 6050)
                ASSERT v_creditor_account_balance = 6050, 'Creditor account balance should be 6050';
            WHEN 2 THEN
                -- Scenario 2: Expect balance to be sum of unprocessed PAYMNT transactions (2500)
                ASSERT v_creditor_account_balance = 2500, 'Creditor account balance should be 2500';
            WHEN 3 THEN
                -- Scenario 3: Expect balance to be zero as all PAYMNT transactions are processed
                ASSERT v_creditor_account_balance = 0, 'Creditor account balance should be 0';
            WHEN 4 THEN
                -- Scenario 4: Expect balance to be zero as there are no PAYMNT transactions
                ASSERT v_creditor_account_balance = 0, 'Creditor account balance should be 0';
            WHEN 5 THEN
                -- Scenario 5: Expect balance to be zero as there are no transactions at all
                ASSERT v_creditor_account_balance = 0, 'Creditor account balance should be 0';            
        END CASE;
         
    END LOOP;       

    RAISE NOTICE '=== All tests completed and passed for v_search_minor_creditor_accounts view ===';

END
$$;

\timing
