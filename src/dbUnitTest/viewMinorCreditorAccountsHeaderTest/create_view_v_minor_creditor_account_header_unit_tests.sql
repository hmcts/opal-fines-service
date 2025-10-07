/**
* CGI OPAL Program
*
* MODULE      : create_view_v_minor_creditor_account_header_unit_tests.sql
*
* DESCRIPTION : Unit test for v_minor_creditor_account_header view
*               Tests verify that the view correctly retrieves minor creditor accounts header information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 19/09/2025    C Larkin    1.0         PO-1926 -  Unit Test for v_minor_creditor_account_header view
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
    DELETE FROM impositions;
    DELETE FROM creditor_accounts;
    DELETE FROM defendant_accounts;
    DELETE FROM parties;    
    
    RAISE NOTICE '=== Data cleanup before tests completed ===';

END $$;

DO LANGUAGE 'plpgsql' $$
DECLARE

    -- Declare local variables for each column in v_minor_creditor_accounts_header
    v_creditor_account_id         BIGINT;
    v_creditor_account_number     VARCHAR(20);
    v_creditor_account_type       VARCHAR(2);
    v_version_number              BIGINT;
    v_party_id                    BIGINT;
    v_title                       VARCHAR(20);
    v_forenames                   VARCHAR(50);
    v_surname                     VARCHAR(50);
    v_organisation                BOOLEAN;
    v_organisation_name           VARCHAR(80);
    v_business_unit_id            SMALLINT;
    v_business_unit_name          VARCHAR(200);
    v_welsh_language              BOOLEAN;
    v_awarded                     NUMERIC(18,2);
    v_paid_out                    NUMERIC(18,2);
    v_awaiting_payment            NUMERIC(18,2);
    v_outstanding                 NUMERIC(18,2);

BEGIN

    RAISE NOTICE '=== Setting up test data for v_minor_creditor_accounts_header tests ===';

    -- Scenario 1 - Individual - Impositions and creditor transactions, no payments fully processed
    -- Scenario 2 - Organisation - No impositions
    -- Scenario 3 - Individual - Impositions and creditor transactions, partial payment 
    -- Scenario 4 - Organisation - Impositions and creditor transactions, full payment
    -- Scenario 5 - Individual - Impositions, but no creditor transactions
    -- Extra data created for Major Creditor, to prove that only Minor Creditors are not retrieved

    -- Insert test data - Parties
    -- Individual
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

    -- Individual
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
                FALSE,
                'Mr',
                'Jack',
                'Jones',
                NULL,
                '123 Old Street',
                'Apartment 2B',
                'Oldtown',
                'CF1A 1BB',
                'Other Creditor'
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
                3,
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

    -- Individual
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
                4,
                FALSE,
                'Ms',
                'Emily',
                'Brown',
                NULL,
                '789 High Street',
                'Suite 5',
                'Uptown',
                'LS1 2AB',
                'Minor Creditor'
            );
    
    -- Additional Organisation
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
                5,
                TRUE,
                NULL,
                NULL,
                NULL,
                'Beta Limited',
                '12 Commerce Way',
                'Suite 200',
                'Business Park',
                'BN1 4ZZ',
                'Minor Creditor'
            );

    -- Additional Individual
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
                6,
                FALSE,
                'Mrs',
                'Laura',
                'Green',
                NULL,
                '21 Oak Avenue',
                NULL,
                'Greenfield',
                'GT2 9XY',
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
                1040,
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

    -- Insert test data - Defendant Accounts
    INSERT INTO defendant_accounts 
            (
                defendant_account_id,
                business_unit_id,
                account_number,
                account_status,
                account_type,
                amount_paid,
                account_balance,
                amount_imposed,
                version_number,
                last_enforcement,
                collection_order,
                imposing_court_id,
                originator_name
            ) VALUES
            (
                1,
                1040,
                'EMP123456',
                'A',
                'Fine',
                0.00,
                10000.00,
                10000.00,
                1,
                'DW',
                TRUE,
                260000000048,
                'Watford'
            );        

    -- Insert test data - Impositions
    INSERT INTO impositions 
            (
                imposition_id,
                defendant_account_id,                
                posted_date,
                posted_by,
                posted_by_name,
                original_posted_date,
                result_id,
                imposing_court_id,
                imposed_date,
                imposed_amount,
                paid_amount,
                offence_id,
                offence_title,
                offence_code,
                creditor_account_id,
                unit_fine_adjusted,
                unit_fine_units,
                completed,
                originator_name,
                original_imposition_id
            ) VALUES 
            (
                1,
                1,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'DRJ',
                NULL,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'FCOMP',
                260000000048,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                7500,
                0,
                35781,
                NULL,
                NULL,
                1,
                NULL,
                NULL,
                FALSE,
                'Watford',
                NULL
            );

INSERT INTO impositions 
            (
                imposition_id,
                defendant_account_id,                
                posted_date,
                posted_by,
                posted_by_name,
                original_posted_date,
                result_id,
                imposing_court_id,
                imposed_date,
                imposed_amount,
                paid_amount,
                offence_id,
                offence_title,
                offence_code,
                creditor_account_id,
                unit_fine_adjusted,
                unit_fine_units,
                completed,
                originator_name,
                original_imposition_id
            ) VALUES 
            (
                2,
                1,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'DRJ',
                NULL,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'FCOMP',
                260000000048,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                2500,
                0,
                35781,
                NULL,
                NULL,
                1,
                NULL,
                NULL,
                FALSE,
                'Watford',
                NULL
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
                6000,
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
                1040,
                8888,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                1,
                NULL,
                6000,
                'A',
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                'N'
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
                4000,
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
                1040,
                7777,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                2,
                NULL,
                4000,
                'A',
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                'N'
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
                1034,
                'CA002',
                'MN',
                FALSE,
                3,
                FALSE,
                FALSE,
                TRUE,
                '654321',
                '87654321',
                'Acme Corp Account',
                'REF002',
                '2'
            );

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
                1024,
                'CA003',
                'MN',
                FALSE,
                4,
                FALSE,
                FALSE,
                TRUE,
                '124589',
                '15987426',
                'E BrownAccount',
                'REF003',
                '2'
            );

    -- Insert test data - Defendant Accounts
    INSERT INTO defendant_accounts 
            (
                defendant_account_id,
                business_unit_id,
                account_number,
                account_status,
                account_type,
                amount_paid,
                account_balance,
                amount_imposed,
                version_number,
                last_enforcement,
                collection_order,
                imposing_court_id,
                originator_name
            ) VALUES
            (
                2,
                1024,
                'EMP234567',
                'A',
                'Fine',
                11000.00,
                14000.00,
                25000.00,
                1,
                'DW',
                TRUE,
                260000000061,
                'HertfordCC'
            );              

    INSERT INTO impositions 
            (
                imposition_id,
                defendant_account_id,                
                posted_date,
                posted_by,
                posted_by_name,
                original_posted_date,
                result_id,
                imposing_court_id,
                imposed_date,
                imposed_amount,
                paid_amount,
                offence_id,
                offence_title,
                offence_code,
                creditor_account_id,
                unit_fine_adjusted,
                unit_fine_units,
                completed,
                originator_name,
                original_imposition_id
            ) VALUES 
            (
                4,
                2,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'DRJ',
                NULL,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'FCOMP',
                260000000061,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                25000,
                11000,
                45144,
                NULL,
                NULL,
                3,
                NULL,
                NULL,
                FALSE,
                'HertfordCC',
                NULL
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
                (3, 3, (CURRENT_TIMESTAMP - INTERVAL '30 days'), 'DRJ3', NULL, 'PAYMNT', 11000, 2, TRUE, 977, NULL, (CURRENT_TIMESTAMP + INTERVAL '30 days'), NULL, NULL);
    
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
                (3, 1024, 6666, (CURRENT_TIMESTAMP - INTERVAL '30 days'), 3, NULL,11000, 'A', (CURRENT_TIMESTAMP + INTERVAL '30 days'), 'U');


    -- Insert test data - Creditor Accounts - Scenario 4
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
                1018,
                'CA004',
                'MN',
                FALSE,
                5,
                FALSE,
                FALSE,
                TRUE,
                '354526',
                '11291344',
                'Beta Limited Acct',
                'REF004',
                '1'
            );

    -- Insert test data - Defendant Accounts
    INSERT INTO defendant_accounts 
            (
                defendant_account_id,
                business_unit_id,
                account_number,
                account_status,
                account_type,
                amount_paid,
                account_balance,
                amount_imposed,
                version_number,
                last_enforcement,
                collection_order,
                imposing_court_id,
                originator_name
            ) VALUES
            (
                3,
                1018,
                'EMP345745',
                'A',
                'Fine',
                30000.00,
                0,
                30000.00,
                1,
                'DW',
                TRUE,
                300000000035,
                'Norwich'
            );     

    INSERT INTO impositions 
            (
                imposition_id,
                defendant_account_id,                
                posted_date,
                posted_by,
                posted_by_name,
                original_posted_date,
                result_id,
                imposing_court_id,
                imposed_date,
                imposed_amount,
                paid_amount,
                offence_id,
                offence_title,
                offence_code,
                creditor_account_id,
                unit_fine_adjusted,
                unit_fine_units,
                completed,
                originator_name,
                original_imposition_id
            ) VALUES 
            (
                5,
                3,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'DRJ',
                NULL,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'FCOMP',
                300000000035,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                12000,
                12000,
                35800,
                NULL,
                NULL,
                4,
                NULL,
                NULL,
                FALSE,
                'Norwich',
                NULL
            );     

    INSERT INTO impositions 
            (
                imposition_id,
                defendant_account_id,                
                posted_date,
                posted_by,
                posted_by_name,
                original_posted_date,
                result_id,
                imposing_court_id,
                imposed_date,
                imposed_amount,
                paid_amount,
                offence_id,
                offence_title,
                offence_code,
                creditor_account_id,
                unit_fine_adjusted,
                unit_fine_units,
                completed,
                originator_name,
                original_imposition_id
            ) VALUES 
            (
                6,
                3,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'DRJ',
                NULL,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'FCOMP',
                300000000035,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                10000,
                10000,
                54350,
                NULL,
                NULL,
                4,
                NULL,
                NULL,
                FALSE,
                'Norwich',
                NULL
            );     

    INSERT INTO impositions 
            (
                imposition_id,
                defendant_account_id,                
                posted_date,
                posted_by,
                posted_by_name,
                original_posted_date,
                result_id,
                imposing_court_id,
                imposed_date,
                imposed_amount,
                paid_amount,
                offence_id,
                offence_title,
                offence_code,
                creditor_account_id,
                unit_fine_adjusted,
                unit_fine_units,
                completed,
                originator_name,
                original_imposition_id
            ) VALUES 
            (
                7,
                3,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'DRJ',
                NULL,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'FCOMP',
                300000000035,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                8000,
                8000,
                54660,
                NULL,
                NULL,
                4,
                NULL,
                NULL,
                FALSE,
                'Norwich',
                NULL
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
            (
                4,
                4,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                'DRJ4',
                NULL,
                'PAYMNT',
                12000,
                5,
                TRUE,
                990,
                NULL,
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                NULL,
                NULL
            ),
            (
                5,
                4,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                'DRJ5',
                NULL,
                'PAYMNT',
                10000,
                6,
                TRUE,
                991,
                NULL,
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                NULL,
                NULL
            ),
            (
                6,
                4,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                'DRJ6',
                NULL,
                'PAYMNT',
                8000,
                7,
                TRUE,
                992,
                NULL,
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                NULL,
                NULL
            );

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
                4,
                1018,
                4444,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                4,
                NULL,
                12000,
                'A',
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                'U'
            ),
            (
                5,
                1018,
                5555,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                5,
                NULL,
                10000,
                'A',
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                'U'
            ),
            (
                6,
                1018,
                6666,
                (CURRENT_TIMESTAMP - INTERVAL '60 days'),
                6,
                NULL,
                8000,
                'A',
                (CURRENT_TIMESTAMP + INTERVAL '60 days'),
                'U'
            );
    
    -- Insert test data - Creditor Accounts - Scenario 5
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
                110,
                'CA005',
                'MN',
                FALSE,
                6,
                FALSE,
                FALSE,
                TRUE,
                '353567',
                '80081351',
                'L Green',
                'REF005',
                '1'
            );

    -- Insert test data - Defendant Accounts
    INSERT INTO defendant_accounts 
            (
                defendant_account_id,
                business_unit_id,
                account_number,
                account_status,
                account_type,
                amount_paid,
                account_balance,
                amount_imposed,
                version_number,
                last_enforcement,
                collection_order,
                imposing_court_id,
                originator_name
            ) VALUES
            (
                4,
                110,
                'EMP378392',
                'A',
                'Fine',
                0,
                45000.00,
                45000.00,
                1,
                'DW',
                TRUE,
                1030000000011,
                'Blackburn'
            );     

    INSERT INTO impositions 
            (
                imposition_id,
                defendant_account_id,                
                posted_date,
                posted_by,
                posted_by_name,
                original_posted_date,
                result_id,
                imposing_court_id,
                imposed_date,
                imposed_amount,
                paid_amount,
                offence_id,
                offence_title,
                offence_code,
                creditor_account_id,
                unit_fine_adjusted,
                unit_fine_units,
                completed,
                originator_name,
                original_imposition_id
            ) VALUES 
            (
                8,
                4,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'DRJ',
                NULL,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'FCOMP',
                1030000000011,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                15000,
                0,
                41073,
                NULL,
                NULL,
                5,
                NULL,
                NULL,
                FALSE,
                'Blackburn',
                NULL
            );     

    INSERT INTO impositions 
            (
                imposition_id,
                defendant_account_id,                
                posted_date,
                posted_by,
                posted_by_name,
                original_posted_date,
                result_id,
                imposing_court_id,
                imposed_date,
                imposed_amount,
                paid_amount,
                offence_id,
                offence_title,
                offence_code,
                creditor_account_id,
                unit_fine_adjusted,
                unit_fine_units,
                completed,
                originator_name,
                original_imposition_id
            ) VALUES 
            (
                9,
                4,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'DRJ',
                NULL,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                'FCOMP',
                1030000000011,
                (CURRENT_TIMESTAMP - INTERVAL '90 days'),
                30000,
                0,
                47176,
                NULL,
                NULL,
                5,
                NULL,
                NULL,
                FALSE,
                'Blackburn',
                NULL
            );  

    RAISE NOTICE '=== Setting up test data for v_minor_creditor_accounts_header tests complete ===';

    -- Test Scenarios
    -- Scenario 1 - Individual - Impositions and creditor transactions, no payments fully processed
    -- Scenario 2 - Organisation - No impositions
    -- Scenario 3 - Individual - Impositions and creditor transactions, partial payment 
    -- Scenario 4 - Organisation - Impositions and creditor transactions, full payment
    -- Scenario 5 - Individual - Impositions, but no creditor transactions

    FOR i IN 1..5 LOOP
        
        RAISE NOTICE '--- Running test scenario % ---', i;

        -- Select from the view and assign to variables
        SELECT creditor_account_id,    
               creditor_account_number,
               creditor_account_type,            
               version_number,
               party_id,
               title,
               forenames,
               surname,
               organisation,
               organisation_name,
               business_unit_id,
               business_unit_name,
               welsh_language,
               awarded,
               paid_out,
               awaiting_payment,
               outstanding            
          INTO                
               v_creditor_account_id,
               v_creditor_account_number,
               v_creditor_account_type,
               v_version_number,
               v_party_id,
               v_title,
               v_forenames,
               v_surname,
               v_organisation,
               v_organisation_name,
               v_business_unit_id,
               v_business_unit_name,
               v_welsh_language,
               v_awarded,
               v_paid_out,
               v_awaiting_payment,
               v_outstanding                
         FROM  v_minor_creditor_account_header
        WHERE  creditor_account_id = i;


        CASE i
            WHEN 1 THEN
                -- Scenario 1: Individual - Impositions and creditor transactions, but no payments processed
                ASSERT v_surname = 'Smith', 'Surname should be Smith';
                ASSERT v_awarded = 10000.00, 'Awarded amount should be 10000.00';
                ASSERT v_paid_out = 0, 'Paid out amount should be 0';
                ASSERT v_awaiting_payment = 10000.00, 'Awaiting payment amount should be 10000.00';
                ASSERT v_outstanding = 10000.00, 'Outstanding amount should be 10000.00';

            WHEN 2 THEN
                -- Scenario 2 - Organisation - No impositions
                ASSERT v_organisation_name = 'Acme Corporation', 'Organisation name should be Acme Corporation';
                ASSERT v_awarded = 0, 'Awarded amount should be 0';
                ASSERT v_paid_out = 0, 'Paid out amount should be 0';
                ASSERT v_awaiting_payment = 0, 'Awaiting payment amount should be 0';
                ASSERT v_outstanding = 0, 'Outstanding amount should be 0';

            WHEN 3 THEN
                -- Scenario 3: Individual - Impositions and creditor transactions, partial payment
                ASSERT v_surname = 'Brown', 'Surname should be Brown';
                ASSERT v_awarded = 25000.00, 'Awarded amount should be 25000.00';
                ASSERT v_paid_out = 11000.00, 'Paid out amount should be 11000.00';
                ASSERT v_awaiting_payment = 0, 'Awaiting payment amount should be 0';
                ASSERT v_outstanding = 14000.00, 'Outstanding amount should be 14000.00';

            WHEN 4 THEN
                -- Scenario 4: Organisation - Impositions and creditor transactions, full payment
                ASSERT v_organisation_name =  'Beta Limited', 'Organisation name should be Beta Limited';
                ASSERT v_awarded = 30000, 'Awarded amount should be 30000';
                ASSERT v_paid_out = 30000, 'Paid out amount should be 30000';
                ASSERT v_awaiting_payment = 0, 'Awaiting payment amount should be 0';
                ASSERT v_outstanding = 0, 'Outstanding amount should be 0';

            WHEN 5 THEN
                -- Scenario 5: Individual - Impositions, but no creditor transactions
                ASSERT v_surname = 'Green', 'Surname should be Green';
                ASSERT v_awarded = 45000.00, 'Awarded amount should be 45000.00';
                ASSERT v_paid_out = 0, 'Paid out amount should be 0';
                ASSERT v_awaiting_payment = 0, 'Awaiting payment amount should be 00';
                ASSERT v_outstanding = 45000.00, 'Outstanding amount should be 45000.00';

        END CASE;
         
    END LOOP;       

    RAISE NOTICE '=== All tests completed and passed for v_minor_creditor_accounts_header view ===';

END
$$;

\timing
