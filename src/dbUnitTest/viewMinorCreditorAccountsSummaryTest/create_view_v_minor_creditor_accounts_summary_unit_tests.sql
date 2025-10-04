/**
* CGI OPAL Program
*
* MODULE      : create_view_v_minor_creditor_accounts_summary_unit_tests.sql
*
* DESCRIPTION : Unit test for v_minor_creditor_accounts_summary view
*               Tests verify that the view correctly retrieves minor creditor accounts summary information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 25/09/2025    C Larkin    1.0         PO-1927 -  Unit Test for v_minor_creditor_accounts_summary view
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
    DELETE FROM defendant_account_parties;
    DELETE FROM defendant_accounts;
    DELETE FROM parties;    
    
    RAISE NOTICE '=== Data cleanup before tests completed ===';

END $$;

DO LANGUAGE 'plpgsql' $$
DECLARE

    -- Declare local variables for each column in v_minor_creditor_accounts_summary
    v_creditor_account_id         BIGINT;
    v_creditor_account_number     VARCHAR(20);
    v_pay_by_bacs                 BOOLEAN;
    v_version_number              BIGINT;
    v_hold_payout                 BOOLEAN;
    v_party_id                    BIGINT;
    v_creditor_title              VARCHAR(20);
    v_creditor_forenames          VARCHAR(50);
    v_creditor_surname            VARCHAR(50);
    v_creditor_organisation       BOOLEAN;
    v_creditor_organisation_name  VARCHAR(80);
    v_creditor_address_line_1     VARCHAR(35);
    v_creditor_address_line_2     VARCHAR(35);
    v_creditor_address_line_3     VARCHAR(35);
    v_creditor_address_line_4     VARCHAR(35);
    v_creditor_address_line_5     VARCHAR(35);
    v_creditor_postcode           VARCHAR(10);
    v_defendant_account_id        BIGINT;
    v_defendant_account_number    VARCHAR(20);
    v_defendant_title             VARCHAR(20);
    v_defendant_forenames         VARCHAR(50);
    v_defendant_surname           VARCHAR(50);


BEGIN

    RAISE NOTICE '=== Setting up test data for v_minor_creditor_accounts_summary tests ===';

    -- Scenario 1 - Individual - Impositions and creditor transactions, no payments fully processed
    -- Scenario 2 - Organisation - No impositions
    -- Scenario 3 - Individual - Impositions and creditor transactions, partial payment 
    -- Scenario 4 - Organisation - Impositions and creditor transactions, full payment
    -- Scenario 5 - Individual - Impositions, but no creditor transactions
    -- Scenario 6 - Organisation - Defendant Account, but no impositions

    -- Extra data created for Other Creditor, to prove that only Minor Creditors are not retrieved

    -- Consolidated parties inserts (each row annotated with original scenario comments)
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
                address_line_4,
                address_line_5,
                postcode,
                account_type
            ) VALUES
            -- Individual (Scenario 1)
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
                'Swansea',
                'Swansea',
                'SW1A 1AA',
                'Minor Creditor'
            ),
            -- Individual (Other Creditor - Scenario 2)
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
                'Caerdydd',
                'Caerdydd',
                'CF1A 1BB',
                'Other Creditor'
            ),
            -- Organisation (Scenario 2 - no impositions)
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
                NULL,
                NULL,
                'EC2A 4AA',
                'Minor Creditor'
            ),
            -- Individual (Scenario 3)
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
                'Leeds',
                'West Yorkshire',
                'LS1 2AB',
                'Minor Creditor'
            ),
            -- Additional Organisation (Scenario 4)
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
                NULL,
                NULL,
                'BN1 4ZZ',
                'Minor Creditor'
            ),
            -- Additional Individual (Scenario 5)
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
                NULL,
                NULL,
                'GT2 9XY',
                'Minor Creditor'
            ),
            -- Insert test data - Parties - Defendant (Scenario 1)
            (
                7,
                FALSE,
                'Mr',
                'David',
                'Roberts',
                NULL,
                '321 Elm Street',
                'Flat 3A',
                'Midtown',
                NULL,
                NULL,
                'WD1 3AA',
                'Defendant'
            ),
            -- Scenario 3 - Defendant
            (
                8,
                FALSE,
                'Ms',
                'Sarah',
                'Williams',
                NULL,
                '654 Pine Road',
                NULL,
                'Suburbia',
                NULL,
                NULL,
                'HG1 4BB',
                'Defendant'
            ),
            -- Scenario 4 - Defendant
            (
                9,
                FALSE,
                'Mr',
                'Michael',
                'Johnson',
                NULL,
                '987 Cedar Lane',
                NULL,
                'Countryside',
                NULL,
                NULL,
                'NR1 5CC',
                'Defendant'
            ),
            -- Scenario 5 - Defendant
            (
                10,
                FALSE,
                'Mr',
                'James',
                'Miller',
                NULL,
                '159 Spruce Street',
                NULL,
                'Lakeside',
                NULL,
                NULL,
                'BB1 6DD',
                'Defendant'
            ),
            -- Scenario 6 - Minor Creditor (Organisation)
            (
                11,
                TRUE,
                NULL,
                NULL,
                NULL,
                'Gamma Ventures',
                '88 Startup Way',
                'Floor 3',
                'Tech Park',
                NULL,
                NULL,
                'TE1 9GH',
                'Minor Creditor'
            ),
            -- Scenario 6 - Defendant (Individual)
            (
                12,
                FALSE,
                'Mr',
                'Peter',
                'Clark',
                NULL,
                '44 River Road',
                NULL,
                'Riverside',
                NULL,
                NULL,
                'RV3 2LM',
                'Defendant'
            );
    
    -- Insert test data - Creditor Accounts
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
            -- Scenario 1 - Individual
            (
                1,
                1040,
                'CA001',
                'MN',
                FALSE,
                1,
                FALSE,
                FALSE,
                TRUE,
                '123456',
                '12345678',
                'John Smith Account',
                'REF001',
                '1'
            ),
            -- Scenario 2 - Organisation (no impositions)
            (
                2,
                1034,
                'CA002',
                'MN',
                FALSE,
                3,
                FALSE,
                TRUE,
                TRUE,
                '654321',
                '87654321',
                'Acme Corp Account',
                'REF002',
                '2'
            ),
            -- Scenario 3 - Individual (partial payment)
            (
                3,
                1024,
                'CA003',
                'MN',
                FALSE,
                4,
                FALSE,
                FALSE,
                FALSE,
                '124589',
                '15987426',
                'E BrownAccount',
                'REF003',
                '2'
            ),
            -- Scenario 4 - Organisation (full payment)
            (
                4,
                1018,
                'CA004',
                'MN',
                FALSE,
                5,
                FALSE,
                FALSE,
                FALSE,
                '354526',
                '11291344',
                'Beta Limited Acct',
                'REF004',
                '1'
            ),
            -- Scenario 5 - Individual (no creditor transactions)
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
            ),
            -- Scenario 6 - Organisation (Defendant Account, but no impositions)            
            (
                6,
                1042,
                'CA006',
                'MN',
                FALSE,
                11,
                FALSE,
                FALSE,
                TRUE,
                '112233',
                '99887766',
                'Gamma Ventures',
                'REF006',
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
        -- Scenario 1
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
        ),
        -- Scenario 3
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
        ),
        -- Scenario 4
        (
            3,
            1018,
            'EMP345745',
            'A',
            'Fine',
            30000.00,
            0.00,
            30000.00,
            1,
            'DW',
            TRUE,
            300000000035,
            'Norwich'
        ),
        -- Scenario 5
        (
            4,
            110,
            'EMP378392',
            'A',
            'Fine',
            0.00,
            45000.00,
            45000.00,
            1,
            'DW',
            TRUE,
            1030000000011,
            'Blackburn'
        ),
        -- Scenario 6 - Defendant Account with no impositions
        (
            5,
            1042,
            'EMP456789',
            'A',
            'Fine',
            0.00,
            5000.00,
            5000.00,
            1,
            'DW',
            TRUE,
            360000000016,
            'Cardiff'
        );

    -- Link defendant accounts to parties for scenarios 1, 3, 4 and 5
    INSERT INTO defendant_account_parties 
            (
                defendant_account_party_id,
                defendant_account_id, 
                party_id,
                association_type,
                debtor
            ) VALUES
            (   
                nextval('defendant_account_party_id_seq'), 1, 7, 'Defendant', TRUE
            ),
            (
                nextval('defendant_account_party_id_seq'), 2, 8, 'Defendant', TRUE
            ),
            (
                nextval('defendant_account_party_id_seq'), 3, 9, 'Defendant', TRUE
            ),
            (
                nextval('defendant_account_party_id_seq'), 4, 10, 'Defendant', TRUE
            ),
            (
                nextval('defendant_account_party_id_seq'), 5, 12, 'Defendant', TRUE
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
            -- Scenario 1
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
            ),
            -- Scenario 1
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
            ),
            -- Scenario 3
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
            ),
            -- Scenario 4
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
            ),
            -- Scenario 4
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
            ),
            -- Scenario 4
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
            ),
            -- Scenario 5
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
            ),
            -- Scenario 5
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
    
    -- Insert test data - Creditor Transactions (combined inserts)
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
            -- Scenario 1 - creditor transaction 1
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
            ),
            -- Scenario 1 - creditor transaction 2
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
            ),
            -- Scenario 3
            (
                3,
                3,
                (CURRENT_TIMESTAMP - INTERVAL '30 days'),
                'DRJ3',
                NULL,
                'PAYMNT',
                11000,
                2,
                TRUE,
                977,
                NULL,
                (CURRENT_TIMESTAMP + INTERVAL '30 days'),
                NULL,
                NULL
            ),
            -- Scenario 4 - creditor transaction 4
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
            -- Scenario 4 - creditor transaction 5
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
            -- Scenario 4 - creditor transaction 6
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
            )
        VALUES
            -- Scenario 1 - Cheque 1
            (nextval('cheque_id_seq'), 1040, 8888, (CURRENT_TIMESTAMP - INTERVAL '60 days'), 1, NULL, 6000, 'A', (CURRENT_TIMESTAMP + INTERVAL '60 days'), 'N'),
            -- Scenario 1 - Cheque 2
            (nextval('cheque_id_seq'), 1040, 7777, (CURRENT_TIMESTAMP - INTERVAL '60 days'), 2, NULL, 4000, 'A', (CURRENT_TIMESTAMP + INTERVAL '60 days'), 'N'),
            -- Scenario 3 - Cheque 3
            (nextval('cheque_id_seq'), 1024, 6666, (CURRENT_TIMESTAMP - INTERVAL '30 days'), 3, NULL, 11000, 'A', (CURRENT_TIMESTAMP + INTERVAL '30 days'), 'U'),
            -- Scenario 4 - Cheques 4, 5 and 6
            (nextval('cheque_id_seq'), 1018, 4444, (CURRENT_TIMESTAMP - INTERVAL '60 days'), 4, NULL, 12000, 'A', (CURRENT_TIMESTAMP + INTERVAL '60 days'), 'U'),
            (nextval('cheque_id_seq'), 1018, 5555, (CURRENT_TIMESTAMP - INTERVAL '60 days'), 5, NULL, 10000, 'A', (CURRENT_TIMESTAMP + INTERVAL '60 days'), 'U'),
            (nextval('cheque_id_seq'), 1018, 6666, (CURRENT_TIMESTAMP - INTERVAL '60 days'), 6, NULL, 8000, 'A', (CURRENT_TIMESTAMP + INTERVAL '60 days'), 'U');

    RAISE NOTICE '=== Setting up test data for v_minor_creditor_accounts_summary tests complete ===';

commit;

    -- Test Scenarios
    -- Scenario 1 - Individual - Impositions and creditor transactions, no payments fully processed
    -- Scenario 2 - Organisation - No impositions
    -- Scenario 3 - Individual - Impositions and creditor transactions, partial payment 
    -- Scenario 4 - Organisation - Impositions and creditor transactions, full payment
    -- Scenario 5 - Individual - Impositions, but no creditor transactions
    -- Scenario 6 - Organisation - Defendant Account, but no impositions

    FOR i IN 1..6 LOOP

        RAISE NOTICE '--- Running test scenario % ---', i;

        -- Select from the view and assign to variables
        SELECT creditor_account_id,    
               creditor_account_number,
               pay_by_bacs,
               version_number,
               hold_payout,
               party_id,
               creditor_title,
               creditor_forenames,
               creditor_surname,
               creditor_organisation,
               creditor_organisation_name,
               creditor_address_line_1,
               creditor_address_line_2,
               creditor_address_line_3,
               creditor_address_line_4,
               creditor_address_line_5,
               creditor_postcode,
               defendant_account_id,
               defendant_account_number,
               defendant_title,
               defendant_forenames,
               defendant_surname    
          INTO                
               v_creditor_account_id,
               v_creditor_account_number,
               v_pay_by_bacs,
               v_version_number,
               v_hold_payout,
               v_party_id,
               v_creditor_title,
               v_creditor_forenames,
               v_creditor_surname,
               v_creditor_organisation,
               v_creditor_organisation_name,
               v_creditor_address_line_1,
               v_creditor_address_line_2,
               v_creditor_address_line_3,
               v_creditor_address_line_4,
               v_creditor_address_line_5,
               v_creditor_postcode,
               v_defendant_account_id,
               v_defendant_account_number,
               v_defendant_title,
               v_defendant_forenames,
               v_defendant_surname
          FROM v_minor_creditor_accounts_summary
         WHERE creditor_account_id = i;

        -- Perform assertions based on scenarios
        CASE i
            WHEN 1 THEN
                -- Scenario 1: Individual - Impositions and creditor transactions, but no payments processed
                ASSERT v_creditor_surname = 'Smith', 'Surname should be Smith';
                ASSERT v_pay_by_bacs = TRUE, 'Pay by BACS should be TRUE';
                ASSERT v_hold_payout = FALSE, 'Hold payout should be FALSE';
                ASSERT v_defendant_surname = 'Roberts', 'Defendant surname should be Roberts';               

            WHEN 2 THEN
                -- Scenario 2 - Organisation - No impositions
                ASSERT v_creditor_organisation_name = 'Acme Corporation', 'Organisation name should be Acme Corporation';
                ASSERT v_pay_by_bacs = TRUE, 'Pay by BACS should be TRUE';
                ASSERT v_hold_payout = TRUE, 'Hold payout should be TRUE';
                ASSERT v_defendant_surname IS NULL, 'Defendant surname should be NULL';

            WHEN 3 THEN
                -- Scenario 3: Individual - Impositions and creditor transactions, partial payment
                ASSERT v_creditor_surname = 'Brown', 'Surname should be Brown';
                ASSERT v_pay_by_bacs = FALSE, 'Pay by BACS should be FALSE';
                ASSERT v_hold_payout = FALSE, 'Hold payout should be FALSE';
                ASSERT v_defendant_surname = 'Williams', 'Defendant surname should be Williams';
               
            WHEN 4 THEN
                -- Scenario 4: Organisation - Impositions and creditor transactions, full payment
                ASSERT v_creditor_organisation_name =  'Beta Limited', 'Organisation name should be Beta Limited';
                ASSERT v_pay_by_bacs = FALSE, 'Pay by BACS should be FALSE';
                ASSERT v_hold_payout = FALSE, 'Hold payout should be FALSE';
                ASSERT v_defendant_surname = 'Johnson', 'Defendant surname should be Johnson';

            WHEN 5 THEN
                -- Scenario 5: Individual - Impositions, but no creditor transactions
                ASSERT v_creditor_surname = 'Green', 'Surname should be Green';
                ASSERT v_pay_by_bacs = TRUE, 'Pay by BACS should be TRUE';
                ASSERT v_hold_payout = FALSE, 'Hold payout should be FALSE';
                ASSERT v_defendant_surname = 'Miller', 'Defendant surname should be Miller';

            WHEN 6 THEN
                -- Scenario 6 - Organisation - Defendant Account, but no impositions - Needs defendant account
                ASSERT v_creditor_organisation_name = 'Gamma Ventures', 'Organisation name should be Gamma Ventures';
                ASSERT v_pay_by_bacs = TRUE, 'Pay by BACS should be TRUE';
                ASSERT v_hold_payout = FALSE, 'Hold payout should be FALSE';
                ASSERT v_defendant_surname IS NULL, 'Defendant surname should be NULL';
                
        END CASE;
         
    END LOOP;       

    RAISE NOTICE '=== All tests completed and passed for v_minor_creditor_accounts_summary view ===';

END
$$;

\timing
