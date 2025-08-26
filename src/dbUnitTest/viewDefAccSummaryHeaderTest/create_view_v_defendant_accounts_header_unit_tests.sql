/**
* CGI OPAL Program
*
* MODULE      : v_defendant_accounts_header_tests.sql
*
* DESCRIPTION : Unit tests for v_defendant_accounts_header view
*               Tests verify that the view correctly retrieves defendant account header information.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 31/07/2025    C Cho       1.0         PO-1641 Unit tests for v_defendant_accounts_header view
*
**/

-- Start timing measurements for test execution
\timing

-- Test setup: Create test data
DO $$
DECLARE
    v_business_unit_id        smallint := 9999;
    v_defendant_account_id    bigint := 999901;
    v_party_id_defendant      bigint := 999901;
    v_party_id_guardian       bigint := 999902;
    v_payment_terms_id        bigint := 999901;
    
    -- Second test case variables
    v_business_unit_id_2      smallint := 9998;
    v_defendant_account_id_2  bigint := 999902;
    v_party_id_defendant_2    bigint := 999903;
BEGIN
    RAISE NOTICE '=== Setting up test data for v_defendant_accounts_header tests ===';
    
    -- Create test business units
    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_code,
        business_unit_type,
        welsh_language
    ) VALUES
    (
        v_business_unit_id,
        'Test Business Unit',
        'TBU',
        'Area',
        FALSE
    ),
    (
        v_business_unit_id_2,
        'Another Test BU',
        'ATB',
        'Area',
        FALSE
    )
    ON CONFLICT (business_unit_id) DO UPDATE 
    SET business_unit_name = EXCLUDED.business_unit_name,
        business_unit_code = EXCLUDED.business_unit_code,
        business_unit_type = EXCLUDED.business_unit_type,
        welsh_language = EXCLUDED.welsh_language;
    
    -- Create test parties
    INSERT INTO parties (
        party_id,
        title,
        forenames,
        surname,
        birth_date,
        organisation,
        organisation_name
    ) VALUES
    (
        v_party_id_defendant,
        'Mr',
        'Test',
        'Defendant',
        '1990-01-01',
        FALSE,
        NULL
    ),
    (
        v_party_id_guardian,
        'Mrs',
        'Test',
        'Guardian',
        '1965-01-01',
        FALSE,
        NULL
    ),
    (
        v_party_id_defendant_2,
        'Ms',
        'Another',
        'Person',
        '1980-05-05',
        FALSE,
        NULL
    )
    ON CONFLICT (party_id) DO UPDATE 
    SET title = EXCLUDED.title,
        forenames = EXCLUDED.forenames,
        surname = EXCLUDED.surname,
        birth_date = EXCLUDED.birth_date,
        organisation = EXCLUDED.organisation,
        organisation_name = EXCLUDED.organisation_name;
    
    -- Create test defendant accounts
    INSERT INTO defendant_accounts (
        defendant_account_id,
        business_unit_id,
        account_number,
        prosecutor_case_reference,
        account_status,
        account_type,
        amount_paid,
        account_balance,
        amount_imposed,
        version_number,
        allow_writeoffs,
        allow_cheques,
        payment_card_requested
    ) VALUES
    (
        v_defendant_account_id,
        v_business_unit_id,
        'TEST123456',
        'PCR12345',
        'A',
        'Fine',
        50.00,
        100.00,
        150.00,
        1,
        TRUE,
        TRUE,
        TRUE
    ),
    (
        v_defendant_account_id_2,
        v_business_unit_id_2,
        'TEST789012',
        'PCR78901',
        'A',
        'Fine',
        0.00,
        200.00,
        200.00,
        1,
        TRUE,
        TRUE,
        FALSE
    )
    ON CONFLICT (defendant_account_id) DO UPDATE 
    SET business_unit_id = EXCLUDED.business_unit_id,
        account_number = EXCLUDED.account_number,
        prosecutor_case_reference = EXCLUDED.prosecutor_case_reference,
        account_status = EXCLUDED.account_status,
        account_type = EXCLUDED.account_type,
        amount_paid = EXCLUDED.amount_paid,
        account_balance = EXCLUDED.account_balance,
        amount_imposed = EXCLUDED.amount_imposed,
        version_number = EXCLUDED.version_number,
        allow_writeoffs = EXCLUDED.allow_writeoffs,
        allow_cheques = EXCLUDED.allow_cheques,
        payment_card_requested = EXCLUDED.payment_card_requested;
    
    -- Create defendant associations
    INSERT INTO defendant_account_parties (
        defendant_account_party_id,
        defendant_account_id,
        party_id,
        association_type,
        debtor
    ) VALUES
    (
        999901, -- First defendant association
        v_defendant_account_id,
        v_party_id_defendant,
        'Defendant',
        TRUE
    ),
    (
        999902, -- Guardian association 
        v_defendant_account_id,
        v_party_id_guardian,
        'Parent/Guardian',
        TRUE
    ),
    (
        999903, -- Second defendant (without parent/guardian)
        v_defendant_account_id_2,
        v_party_id_defendant_2,
        'Defendant',
        TRUE
    )
    ON CONFLICT (defendant_account_party_id) DO UPDATE 
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        party_id = EXCLUDED.party_id,
        association_type = EXCLUDED.association_type,
        debtor = EXCLUDED.debtor;
    
    -- Create fixed penalty offence record
    INSERT INTO fixed_penalty_offences (
        defendant_account_id,
        ticket_number
    ) VALUES (
        v_defendant_account_id,
        'FPN12345'
    ) ON CONFLICT (defendant_account_id) DO UPDATE 
    SET ticket_number = EXCLUDED.ticket_number;
    
    -- Create payment terms for arrears calculation
    INSERT INTO payment_terms (
        payment_terms_id,
        defendant_account_id,
        terms_type_code,
        effective_date,
        instalment_period,
        instalment_amount,
        instalment_lump_sum,
        posted_date,
        posted_by,
        account_balance
    ) VALUES (
        v_payment_terms_id,
        v_defendant_account_id,
        'I',                           -- Instalment
        CURRENT_DATE - INTERVAL '3 months', -- 3 months ago
        'M',                           -- Monthly
        50.00,                         -- £50 per month
        20.00,                         -- £20 lump sum
        CURRENT_DATE - INTERVAL '3 months',
        'TEST001',                     -- Posted by (required)
        100.00                         -- Account balance at posting (required)
    ) ON CONFLICT (payment_terms_id) DO UPDATE 
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        terms_type_code = EXCLUDED.terms_type_code,
        effective_date = EXCLUDED.effective_date,
        instalment_period = EXCLUDED.instalment_period,
        instalment_amount = EXCLUDED.instalment_amount,
        instalment_lump_sum = EXCLUDED.instalment_lump_sum,
        posted_date = EXCLUDED.posted_date,
        posted_by = EXCLUDED.posted_by,
        account_balance = EXCLUDED.account_balance;
    
    RAISE NOTICE 'Test data setup completed: defendant_account_id = % and %', v_defendant_account_id, v_defendant_account_id_2;
END $$;

-- Test 1: Comprehensive test for account with parent/guardian
DO $$
DECLARE
    -- Account information variables
    v_account_number          varchar;
    v_prosecutor_case_ref     varchar;
    v_account_status          varchar;
    v_account_type            varchar;
    
    -- Party information variables
    v_party_id                bigint;
    v_forenames               varchar;
    v_surname                 varchar;
    v_title                   varchar;
    
    -- Financial information variables
    v_amount_paid             decimal(18,2);
    v_account_balance         decimal(18,2);
    v_amount_imposed          decimal(18,2);
    v_defendant_account_id    bigint := 999901;
    
    -- Business unit information variables
    v_business_unit_id        smallint;
    v_business_unit_name      varchar;
    v_business_unit_code      varchar;
    
    -- Parent/Guardian relationship variables
    v_has_parent_guardian     boolean;
    v_parent_guardian_debtor  varchar;
    
    -- Account party IDs
    v_defendant_account_party_id        bigint;
    v_parent_guardian_account_party_id  bigint;
    
    -- Fixed penalty information variables
    v_ticket_number           varchar;
BEGIN
    RAISE NOTICE '=== TEST 1: Comprehensive test for account with parent/guardian ===';
    
    -- Query the view for all information in one go
    SELECT 
        account_number,
        prosecutor_case_reference,
        account_status,
        account_type,
        party_id,
        forenames,
        surname,
        title,
        paid_written_off,
        account_balance,
        amount_imposed,
        business_unit_id,
        business_unit_name,
        business_unit_code,
        has_parent_guardian,
        parent_guardian_debtor_type,
        ticket_number,
        defendant_account_party_id,
        parent_guardian_account_party_id
    INTO 
        v_account_number,
        v_prosecutor_case_ref,
        v_account_status,
        v_account_type,
        v_party_id,
        v_forenames,
        v_surname,
        v_title,
        v_amount_paid,
        v_account_balance,
        v_amount_imposed,
        v_business_unit_id,
        v_business_unit_name,
        v_business_unit_code,
        v_has_parent_guardian,
        v_parent_guardian_debtor,
        v_ticket_number,
        v_defendant_account_party_id,
        v_parent_guardian_account_party_id
    FROM v_defendant_accounts_header
    WHERE account_number = 'TEST123456';
    
    -- Verify all results in groups of related data
    
    -- 1. Basic account information
    ASSERT v_account_number = 'TEST123456', 'Account number should match';
    ASSERT v_prosecutor_case_ref = 'PCR12345', 'Prosecutor case reference should match';
    ASSERT v_account_status = 'A', 'Account status should match';
    ASSERT v_account_type = 'Fine', 'Account type should match';
    
    -- 2. Party information
    ASSERT v_party_id = 999901, 'Party ID should match';
    ASSERT v_forenames = 'Test', 'Forenames should match';
    ASSERT v_surname = 'Defendant', 'Surname should match';
    ASSERT v_title = 'Mr', 'Title should match';
    
    -- 3. Financial information
    ASSERT v_amount_paid = 50.00, 'Amount paid should be 50.00';
    ASSERT v_account_balance = 100.00, 'Account balance should be 100.00';
    ASSERT v_amount_imposed = 150.00, 'Amount imposed should be 150.00';
    
    -- 4. Business unit information
    ASSERT v_business_unit_id = 9999, 'Business unit ID should be 9999';
    ASSERT v_business_unit_name = 'Test Business Unit', 'Business unit name should match';
    ASSERT v_business_unit_code = 'TBU', 'Business unit code should match';
    
    -- 5. Parent/Guardian relationship
    ASSERT v_has_parent_guardian = TRUE, 'has_parent_guardian should be TRUE';
    ASSERT v_parent_guardian_debtor = 'Parent/Guardian', 'parent_guardian_debtor_type should be Parent/Guardian';
    
    -- 6. Account party IDs
    ASSERT v_defendant_account_party_id = 999901, 'Defendant account party ID should be 999901';
    ASSERT v_parent_guardian_account_party_id = 999902, 'Parent/Guardian account party ID should be 999902';
    
    -- 7. Fixed penalty information
    ASSERT v_ticket_number = 'FPN12345', 'Ticket number should match';
    
    RAISE NOTICE 'TEST 1 PASSED: All information retrieved and validated successfully for account with parent/guardian';
END $$;

-- Test 2: Comprehensive test for account without parent/guardian
DO $$
DECLARE
    -- Account information variables
    v_account_number          varchar;
    v_prosecutor_case_ref     varchar;
    
    -- Party information variables
    v_forenames               varchar;
    v_surname                 varchar;
    v_title                   varchar;
    
    -- Financial information variables
    v_amount_paid             decimal(18,2);
    v_account_balance         decimal(18,2);
    v_amount_imposed          decimal(18,2);
    v_defendant_account_id    bigint := 999902;
    
    -- Parent/Guardian relationship variables
    v_has_parent_guardian     boolean;
    v_parent_guardian_debtor  varchar;
    
    -- Account party IDs
    v_defendant_account_party_id        bigint;
    v_parent_guardian_account_party_id  bigint;
    
    -- Fixed penalty information variables
    v_ticket_number           varchar;
BEGIN
    RAISE NOTICE '=== TEST 2: Comprehensive test for account without parent/guardian ===';
    
    -- Query the view for key information
    SELECT 
        account_number,
        prosecutor_case_reference,
        forenames,
        surname,
        title,
        paid_written_off,
        account_balance,
        amount_imposed,
        has_parent_guardian,
        parent_guardian_debtor_type,
        ticket_number,
        defendant_account_party_id,
        parent_guardian_account_party_id
    INTO 
        v_account_number,
        v_prosecutor_case_ref,
        v_forenames,
        v_surname,
        v_title,
        v_amount_paid,
        v_account_balance,
        v_amount_imposed,
        v_has_parent_guardian,
        v_parent_guardian_debtor,
        v_ticket_number,
        v_defendant_account_party_id,
        v_parent_guardian_account_party_id
    FROM v_defendant_accounts_header
    WHERE account_number = 'TEST789012';
    
    -- Verify key results
    
    -- 1. Basic account information
    ASSERT v_account_number = 'TEST789012', 'Account number should match';
    ASSERT v_prosecutor_case_ref = 'PCR78901', 'Prosecutor case reference should match';
    
    -- 2. Party information
    ASSERT v_forenames = 'Another', 'Forenames should match';
    ASSERT v_surname = 'Person', 'Surname should match';
    ASSERT v_title = 'Ms', 'Title should match';
    
    -- 3. Financial information
    ASSERT v_amount_paid = 0.00, 'Amount paid should be 0.00';
    ASSERT v_account_balance = 200.00, 'Account balance should be 200.00';
    ASSERT v_amount_imposed = 200.00, 'Amount imposed should be 200.00';
    
    -- 4. Parent/Guardian relationship
    ASSERT v_has_parent_guardian = FALSE, 'has_parent_guardian should be FALSE';
    ASSERT v_parent_guardian_debtor IS NULL, 'parent_guardian_debtor_type should be NULL';
    
    -- 5. Account party IDs
    ASSERT v_defendant_account_party_id = 999903, 'Defendant account party ID should be 999903';
    ASSERT v_parent_guardian_account_party_id IS NULL, 'Parent/Guardian account party ID should be NULL';
    
    -- 6. Fixed penalty information (should be NULL for this account)
    ASSERT v_ticket_number IS NULL, 'Ticket number should be NULL for this account';
    
    RAISE NOTICE 'TEST 2 PASSED: All information retrieved and validated successfully for account without parent/guardian';
END $$;

-- Clean up test data
DO $$
BEGIN
    RAISE NOTICE '=== Cleaning up test data ===';
    
    -- Delete test records in the correct order to avoid foreign key constraint issues
    DELETE FROM payment_terms WHERE payment_terms_id IN (999901);
    
    DELETE FROM fixed_penalty_offences WHERE defendant_account_id IN (999901, 999902);
    
    DELETE FROM defendant_account_parties WHERE defendant_account_party_id IN (999901, 999902, 999903);
    
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999901, 999902);
    
    DELETE FROM parties WHERE party_id IN (999901, 999902, 999903);
    
    DELETE FROM business_units WHERE business_unit_id IN (9999, 9998);
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing