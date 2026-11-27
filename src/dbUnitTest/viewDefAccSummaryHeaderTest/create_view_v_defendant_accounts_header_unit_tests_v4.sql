/**
* CGI OPAL Program
*
* MODULE      : create_view_v_defendant_accounts_header_unit_tests_v4.sql
*
* DESCRIPTION : Unit tests for v_defendant_accounts_header view
*               Tests verify that the view correctly retrieves defendant account header information.
*
*               PO-11608 appends business_units.welsh_language as welsh_language. These tests verify the
*               source value is preserved for TRUE, FALSE and NULL, that existing columns retain their
*               names, types and order, and that each account still returns exactly one header row.
*
*               This test script assumes V1_381__alter_v_defendant_accounts_header_add_welsh_language.sql
*               has already been applied before the script is run.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------------------------------
* 31/07/2025    C Cho       1.0         PO-1641 Unit tests for v_defendant_accounts_header view
* 01/12/2025    C Cho       1.1         PO-2338 Added checks for has_consolidated_accounts flag
* 17/03/2026    TMc         2.0         PO-2868 Update columns on DEFENDANT_ACCOUNTS table to use postgresql enum
*                                               Amended INSERT statement into DEFENDANT_ACCOUNTS to use valid ENUM values for ACCOUNT_STATUS
* 17/07/2026    C Cho       3.0         PO-2976 Added checks for originator_type and originator_name fields
* 24/11/2026    S Abbasi    4.0         PO-11608 Added checks for welsh_language (TRUE, FALSE and NULL), a third
*                                                account on a business unit with NULL welsh_language, a column
*                                                structure check and a one-row-per-account check.
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
    v_defendant_transaction_id bigint := 999901;
    
    -- Second test case variables
    v_business_unit_id_2      smallint := 9998;
    v_defendant_account_id_2  bigint := 999902;
    v_party_id_defendant_2    bigint := 999903;

    -- PO-11608: Third test case (business unit with NULL welsh_language)
    v_business_unit_id_3      smallint := 9997;
    v_defendant_account_id_3  bigint := 999903;
    v_party_id_defendant_3    bigint := 999904;
BEGIN
    RAISE NOTICE '=== Setting up test data for v_defendant_accounts_header tests ===';

    -- Create test business units
    -- PO-11608: welsh_language is TRUE for 9999, FALSE for 9998 and NULL for 9997
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
        TRUE
    ),
    (
        v_business_unit_id_2,
        'Another Test BU',
        'ATB',
        'Area',
        FALSE
    ),
    (
        v_business_unit_id_3,
        'Null Welsh Test BU',
        'NWB',
        'Area',
        NULL
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
    ),
    (
        v_party_id_defendant_3,
        'Dr',
        'Null',
        'Welsh',
        '1975-03-03',
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
        payment_card_requested,
        originator_type,
        originator_name,
        collection_order
    ) VALUES
    (
        v_defendant_account_id,
        v_business_unit_id,
        'TEST123456',
        'PCR12345',
        'L',
        'Fine',
        50.00,
        100.00,
        150.00,
        1,
        TRUE,
        TRUE,
        TRUE,
        'TFO',
        'Test Originator Court',
        TRUE
    ),
    (
        v_defendant_account_id_2,
        v_business_unit_id_2,
        'TEST789012',
        'PCR78901',
        'L',
        'Fine',
        0.00,
        200.00,
        200.00,
        1,
        TRUE,
        TRUE,
        FALSE,
        'FP',
        'Test Fixed Penalty Office',
        FALSE
    ),
    (
        v_defendant_account_id_3,
        v_business_unit_id_3,
        'TEST345678',
        'PCR34567',
        'L',
        'Fine',
        10.00,
        90.00,
        100.00,
        1,
        TRUE,
        TRUE,
        FALSE,
        'TFO',
        'Test Null Welsh Court',
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
        payment_card_requested = EXCLUDED.payment_card_requested,
        originator_type = EXCLUDED.originator_type,
        originator_name = EXCLUDED.originator_name,
        collection_order = EXCLUDED.collection_order;
    
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
    ),
    (
        999904, -- PO-11608: Third defendant (business unit with NULL welsh_language)
        v_defendant_account_id_3,
        v_party_id_defendant_3,
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
    
    -- Create defendant transaction indicating consolidation for first account
    INSERT INTO defendant_transactions (
        defendant_transaction_id,
        defendant_account_id,
        posted_date,
        transaction_type,
        transaction_amount,
        associated_record_type,
        associated_record_id
    ) VALUES (
        v_defendant_transaction_id,
        v_defendant_account_id,
        CURRENT_TIMESTAMP,
        'CONSOL',
        0.00,
        'defendant_accounts',
        v_defendant_account_id::varchar
    )
    ON CONFLICT (defendant_transaction_id) DO NOTHING;
    
    RAISE NOTICE 'Test data setup completed: defendant_account_id = %, % and %', v_defendant_account_id, v_defendant_account_id_2, v_defendant_account_id_3;
END $$;

-- Test 1: Comprehensive test for account with parent/guardian
DO $$
DECLARE
    -- Account information variables
    v_account_number          varchar;
    v_prosecutor_case_ref     varchar;
    v_account_status          varchar;
    v_account_type            varchar;
    v_originator_type         varchar;
    v_originator_name         varchar;
    v_collection_order        boolean;
    
    -- Party information variables
    v_party_id                bigint;
    v_forenames               varchar;
    v_surname                 varchar;
    v_title                   varchar;
    
    -- Consolidation flag
    v_has_consolidated        boolean;
    
    -- Financial information variables
    v_amount_paid             decimal(18,2);
    v_account_balance         decimal(18,2);
    v_amount_imposed          decimal(18,2);
    v_defendant_account_id    bigint := 999901;
    
    -- Business unit information variables
    v_business_unit_id        smallint;
    v_business_unit_name      varchar;
    v_business_unit_code      varchar;
    v_welsh_language          boolean;

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
        originator_type::varchar,
        originator_name,
        collection_order,
        party_id,
        forenames,
        surname,
        title,
        paid_written_off,
        account_balance,
        amount_imposed,
        has_consolidated_accounts,
        business_unit_id,
        business_unit_name,
        business_unit_code,
        welsh_language,
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
        v_originator_type,
        v_originator_name,
        v_collection_order,
        v_party_id,
        v_forenames,
        v_surname,
        v_title,
        v_amount_paid,
        v_account_balance,
        v_amount_imposed,
        v_has_consolidated,
        v_business_unit_id,
        v_business_unit_name,
        v_business_unit_code,
        v_welsh_language,
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
    ASSERT v_account_status = 'L', 'Account status should match';
    ASSERT v_account_type = 'Fine', 'Account type should match';
    ASSERT v_originator_type = 'TFO', 'Originator type should match';
    ASSERT v_originator_name = 'Test Originator Court', 'Originator name should match';
    ASSERT v_collection_order = TRUE, 'Collection order should be TRUE';
    
    -- 2. Party information
    ASSERT v_party_id = 999901, 'Party ID should match';
    ASSERT v_forenames = 'Test', 'Forenames should match';
    ASSERT v_surname = 'Defendant', 'Surname should match';
    ASSERT v_title = 'Mr', 'Title should match';
    
    -- 3. Financial information
    ASSERT v_amount_paid = 50.00, 'Amount paid should be 50.00';
    ASSERT v_account_balance = 100.00, 'Account balance should be 100.00';
    ASSERT v_amount_imposed = 150.00, 'Amount imposed should be 150.00';
    ASSERT v_has_consolidated = TRUE, 'has_consolidated_accounts should be TRUE when consolidation transactions exist';
    
    -- 4. Business unit information
    ASSERT v_business_unit_id = 9999, 'Business unit ID should be 9999';
    ASSERT v_business_unit_name = 'Test Business Unit', 'Business unit name should match';
    ASSERT v_business_unit_code = 'TBU', 'Business unit code should match';
    ASSERT v_welsh_language IS TRUE, 'PO-11608: welsh_language should be TRUE for business unit 9999';

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
    v_originator_type         varchar;
    v_originator_name         varchar;
    v_collection_order        boolean;
    
    -- Party information variables
    v_forenames               varchar;
    v_surname                 varchar;
    v_title                   varchar;
    
    -- Consolidation flag
    v_has_consolidated        boolean;
    
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

    -- Business unit information variables
    v_welsh_language          boolean;
BEGIN
    RAISE NOTICE '=== TEST 2: Comprehensive test for account without parent/guardian ===';

    -- Query the view for key information
    SELECT
        account_number,
        prosecutor_case_reference,
        originator_type::varchar,
        originator_name,
        collection_order,
        forenames,
        surname,
        title,
        paid_written_off,
        account_balance,
        amount_imposed,
        has_consolidated_accounts,
        welsh_language,
        has_parent_guardian,
        parent_guardian_debtor_type,
        ticket_number,
        defendant_account_party_id,
        parent_guardian_account_party_id
    INTO 
        v_account_number,
        v_prosecutor_case_ref,
        v_originator_type,
        v_originator_name,
        v_collection_order,
        v_forenames,
        v_surname,
        v_title,
        v_amount_paid,
        v_account_balance,
        v_amount_imposed,
        v_has_consolidated,
        v_welsh_language,
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
    ASSERT v_originator_type = 'FP', 'Originator type should match';
    ASSERT v_originator_name = 'Test Fixed Penalty Office', 'Originator name should match';
    ASSERT v_collection_order = FALSE, 'Collection order should be FALSE';
    
    -- 2. Party information
    ASSERT v_forenames = 'Another', 'Forenames should match';
    ASSERT v_surname = 'Person', 'Surname should match';
    ASSERT v_title = 'Ms', 'Title should match';
    
    -- 3. Financial information
    ASSERT v_amount_paid = 0.00, 'Amount paid should be 0.00';
    ASSERT v_account_balance = 200.00, 'Account balance should be 200.00';
    ASSERT v_amount_imposed = 200.00, 'Amount imposed should be 200.00';
    ASSERT v_has_consolidated = FALSE, 'has_consolidated_accounts should be FALSE when no consolidation transactions exist';

    -- PO-11608: Business unit Welsh-language value
    ASSERT v_welsh_language IS FALSE, 'PO-11608: welsh_language should be FALSE for business unit 9998';

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

-- Test 3: PO-11608 - NULL welsh_language is preserved
DO $$
DECLARE
    v_account_number          varchar;
    v_business_unit_id        smallint;
    v_business_unit_code      varchar;
    v_welsh_language          boolean;
    v_row_found               boolean := FALSE;
BEGIN
    RAISE NOTICE '=== TEST 3: PO-11608 NULL welsh_language is preserved ===';

    SELECT
        TRUE,
        account_number,
        business_unit_id,
        business_unit_code,
        welsh_language
    INTO
        v_row_found,
        v_account_number,
        v_business_unit_id,
        v_business_unit_code,
        v_welsh_language
    FROM v_defendant_accounts_header
    WHERE account_number = 'TEST345678';

    ASSERT v_row_found IS TRUE, 'Account TEST345678 should be returned by the view';
    ASSERT v_business_unit_id = 9997, 'Business unit ID should be 9997';
    ASSERT v_business_unit_code = 'NWB', 'Business unit code should match';
    ASSERT v_welsh_language IS NULL, 'PO-11608: welsh_language should be NULL for business unit 9997';

    RAISE NOTICE 'TEST 3 PASSED: NULL welsh_language value preserved';
END $$;

-- Test 4: PO-11608 - Existing columns retain names, types and order; welsh_language is appended as boolean
DO $$
DECLARE
    v_expected_columns  text[] := ARRAY[
        'defendant_account_id', 'version_number', 'account_number', 'prosecutor_case_reference',
        'account_status', 'account_type', 'paid_written_off', 'account_balance', 'amount_imposed',
        'arrears', 'defendant_account_party_id', 'debtor_type', 'party_id', 'title', 'forenames',
        'surname', 'birth_date', 'organisation', 'organisation_name', 'business_unit_id',
        'business_unit_name', 'business_unit_code', 'ticket_number', 'parent_guardian_account_party_id',
        'has_parent_guardian', 'parent_guardian_debtor_type', 'has_consolidated_accounts',
        'originator_type', 'originator_name', 'collection_order', 'welsh_language'
    ];
    v_actual_columns    text[];
    v_welsh_type        text;
BEGIN
    RAISE NOTICE '=== TEST 4: PO-11608 column names, order and welsh_language type ===';

    SELECT array_agg(column_name::text ORDER BY ordinal_position)
      INTO v_actual_columns
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND table_name = 'v_defendant_accounts_header';

    ASSERT v_actual_columns = v_expected_columns,
        FORMAT('Column list/order mismatch: expected=%s actual=%s', v_expected_columns, v_actual_columns);

    SELECT data_type
      INTO v_welsh_type
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND table_name = 'v_defendant_accounts_header'
       AND column_name = 'welsh_language';

    ASSERT v_welsh_type = 'boolean', FORMAT('welsh_language should be boolean, actual=%s', v_welsh_type);

    RAISE NOTICE 'TEST 4 PASSED: Existing columns unchanged and welsh_language appended as boolean';
END $$;

-- Test 5: PO-11608 - Row selection unchanged (exactly one header row per test account)
DO $$
DECLARE
    rec RECORD;
BEGIN
    RAISE NOTICE '=== TEST 5: PO-11608 one header row per defendant account ===';

    FOR rec IN
        SELECT a.defendant_account_id,
               (SELECT COUNT(*) FROM v_defendant_accounts_header v
                 WHERE v.defendant_account_id = a.defendant_account_id) AS row_count
          FROM (VALUES (999901::bigint), (999902::bigint), (999903::bigint)) AS a(defendant_account_id)
    LOOP
        ASSERT rec.row_count = 1,
            FORMAT('Expected 1 row for defendant_account_id %s, actual=%s', rec.defendant_account_id, rec.row_count);
    END LOOP;

    RAISE NOTICE 'TEST 5 PASSED: Each test account returns exactly one header row';
END $$;

-- Clean up test data
DO $$
BEGIN
    RAISE NOTICE '=== Cleaning up test data ===';

    -- Delete test records in the correct order to avoid foreign key constraint issues
    DELETE FROM payment_terms WHERE payment_terms_id IN (999901);

    DELETE FROM fixed_penalty_offences WHERE defendant_account_id IN (999901, 999902, 999903);

    DELETE FROM defendant_transactions WHERE defendant_transaction_id IN (999901);

    DELETE FROM defendant_account_parties WHERE defendant_account_party_id IN (999901, 999902, 999903, 999904);

    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999901, 999902, 999903);

    DELETE FROM parties WHERE party_id IN (999901, 999902, 999903, 999904);

    DELETE FROM business_units WHERE business_unit_id IN (9999, 9998, 9997);
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing
