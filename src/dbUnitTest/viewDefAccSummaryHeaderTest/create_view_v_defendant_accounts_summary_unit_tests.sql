/**
* CGI OPAL Program
*
* MODULE      : v_defendant_accounts_summary_tests.sql
*
* DESCRIPTION : Unit tests for v_defendant_accounts_summary view
*               Tests verify that the view correctly retrieves defendant account
*               summary information for the At a Glance section.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 31/07/2025    C Cho       1.0         PO-1641 Unit tests for v_defendant_accounts_summary view
**/

-- Start timing measurements for test execution
\timing

-- Test setup: Create test data
DO $$
DECLARE
    -- Account 1: With aliases, payment terms, and complete profile
    v_business_unit_id        smallint := 9999;
    v_business_unit_name      varchar(200) := 'Test Business Unit';
    v_business_unit_type      varchar(20) := 'Accounting Division';
    v_local_justice_area_id   smallint := 999;
    v_lja_name                varchar(100) := 'Test Local Justice Area';
    v_lja_code                varchar(3) := 'TLA';
    v_defendant_account_id    bigint := 999901;
    v_party_id_defendant      bigint := 999901;
    v_court_id                bigint := 999901;
    v_court_name              varchar(60) := 'Test Court';
    v_court_code              varchar(10) := 'TESTCRT';
    v_result_id_enforcement   varchar(6) := 'DW';
    v_result_id_override      varchar(6) := 'SE';
    v_payment_terms_id        bigint := 999901;
    v_alias_id                bigint := 999901;
    v_document_id             varchar(12) := '999901';
    v_enforcer_id             bigint := 999901;
    v_enforcement_id          bigint := 999901;
BEGIN
    RAISE NOTICE '=== Setting up test data for v_defendant_accounts_summary tests ===';
    
    -- Create business units
    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_type,
        business_unit_code
    ) VALUES
    (
        v_business_unit_id,
        v_business_unit_name,
        v_business_unit_type,
        'TB01'
    )
    ON CONFLICT (business_unit_id) DO UPDATE
    SET business_unit_name = EXCLUDED.business_unit_name,
        business_unit_type = EXCLUDED.business_unit_type;
    
    -- Create local justice areas
    INSERT INTO local_justice_areas (
        local_justice_area_id,
        name,
        address_line_1,
        lja_code,
        lja_type
    ) VALUES
    (
        v_local_justice_area_id,
        v_lja_name,
        'LJA Address Line 1',
        v_lja_code,
        'Standard'
    )
    ON CONFLICT (local_justice_area_id) DO UPDATE
    SET name = EXCLUDED.name,
        address_line_1 = EXCLUDED.address_line_1;
    
    -- Create courts
    INSERT INTO courts (
        court_id,
        business_unit_id,
        name,
        court_code,
        local_justice_area_id
    ) VALUES
    (
        v_court_id,
        v_business_unit_id,
        v_court_name,
        1234,
        v_local_justice_area_id
    )
    ON CONFLICT (court_id) DO UPDATE
    SET business_unit_id = EXCLUDED.business_unit_id,
        name = EXCLUDED.name,
        court_code = EXCLUDED.court_code;
    
    -- Create enforcers
    INSERT INTO enforcers (
        enforcer_id,
        business_unit_id,
        enforcer_code,
        name
    ) VALUES
    (
        v_enforcer_id,
        v_business_unit_id,
        1234,
        'Test Enforcer'
    )
    ON CONFLICT (enforcer_id) DO UPDATE
    SET business_unit_id = EXCLUDED.business_unit_id,
        enforcer_code = EXCLUDED.enforcer_code,
        name = EXCLUDED.name;
    
    -- Create documents
    INSERT INTO documents (
        document_id,
        recipient,
        document_language,
        priority,
        header_type,
        signature_source
    ) VALUES
    (
        v_document_id,
        'DEF',                 -- Must be from allowed list per check constraint
        'EN',                  -- Must be 'EN' or 'CY' per check constraint
        1,                     -- Must be 0, 1, or 2 per check constraint
        'A',                   -- Must be 'A', 'AP', or 'EO' per check constraint if provided
        'Area'                 -- Must be 'Area' or 'LJA' per check constraint if provided
    )
    ON CONFLICT (document_id) DO UPDATE
    SET recipient = EXCLUDED.recipient,
        document_language = EXCLUDED.document_language,
        priority = EXCLUDED.priority;
    
    -- Create test enforcement results
    INSERT INTO results (
        result_id,
        result_title,
        result_type,
        active,
        imposition,
        enforcement,
        enforcement_override,
        further_enforcement_warn,
        further_enforcement_disallow,
        enforcement_hold,
        requires_enforcer,
        generates_hearing,
        generates_warrant,
        collection_order,
        extend_ttp_disallow,
        extend_ttp_preserve_last_enf,
        prevent_payment_card,
        lists_monies,
        imposition_accruing
    ) VALUES
    (
        v_result_id_enforcement,
        'Distress Warrant',
        'Result',              -- Must be 'Result' or 'Action' per check constraint
        TRUE,
        FALSE,
        TRUE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        TRUE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        TRUE,
        FALSE
    ),
    (
        v_result_id_override,
        'Suspend Enforcement',
        'Result',
        TRUE,
        FALSE,
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        TRUE,
        FALSE,
        FALSE,
        FALSE
    )
    ON CONFLICT (result_id) DO UPDATE
    SET result_title = EXCLUDED.result_title,
        result_type = EXCLUDED.result_type,
        active = EXCLUDED.active,
        imposition = EXCLUDED.imposition,
        enforcement = EXCLUDED.enforcement,
        enforcement_override = EXCLUDED.enforcement_override,
        further_enforcement_warn = EXCLUDED.further_enforcement_warn,
        further_enforcement_disallow = EXCLUDED.further_enforcement_disallow,
        enforcement_hold = EXCLUDED.enforcement_hold,
        requires_enforcer = EXCLUDED.requires_enforcer,
        generates_hearing = EXCLUDED.generates_hearing,
        generates_warrant = EXCLUDED.generates_warrant,
        collection_order = EXCLUDED.collection_order,
        extend_ttp_disallow = EXCLUDED.extend_ttp_disallow,
        extend_ttp_preserve_last_enf = EXCLUDED.extend_ttp_preserve_last_enf,
        prevent_payment_card = EXCLUDED.prevent_payment_card,
        lists_monies = EXCLUDED.lists_monies,
        imposition_accruing = EXCLUDED.imposition_accruing;
    
    -- Link documents to results with result_documents
    INSERT INTO result_documents (
        result_document_id,
        result_id,
        document_id
    ) VALUES
    (
        999901,
        v_result_id_enforcement,
        v_document_id
    )
    ON CONFLICT (result_document_id) DO NOTHING;
    
    -- Create test parties
    INSERT INTO parties (
        party_id,
        title,
        forenames,
        surname,
        birth_date,
        organisation,
        organisation_name,
        address_line_1,
        address_line_2,
        address_line_3,
        address_line_4,
        address_line_5,
        postcode,
        national_insurance_number,
        account_type
    ) VALUES
    (
        v_party_id_defendant,
        'Mr',
        'Test',
        'Defendant',
        '1990-01-01',
        FALSE,
        NULL,
        '123 Test Street',
        'Test Area',
        'Test Town',
        'Test County',
        NULL,
        'TE1 1ST',
        'AB123456C',
        'Fine'
    )
    ON CONFLICT (party_id) DO UPDATE 
    SET title = EXCLUDED.title,
        forenames = EXCLUDED.forenames,
        surname = EXCLUDED.surname,
        birth_date = EXCLUDED.birth_date;
    
    -- Create test defendant accounts
    INSERT INTO defendant_accounts (
        defendant_account_id,
        business_unit_id,
        account_number,
        account_status,
        account_type,
        amount_paid,
        account_balance,
        amount_imposed,
        version_number,
        allow_writeoffs,
        allow_cheques,
        last_enforcement,
        collection_order,
        account_comments,
        account_note_1,
        account_note_2,
        account_note_3,
        jail_days,
        enf_override_result_id,
        enf_override_enforcer_id,
        last_movement_date,
        enforcing_court_id,
        imposing_court_id,
        originator_name
    ) VALUES
    (
        v_defendant_account_id,
        v_business_unit_id,
        'TEST123456',
        'A',
        'Fine',
        50.00,
        100.00,
        150.00,
        1,
        TRUE,
        TRUE,
        v_result_id_enforcement,
        TRUE,
        'Test comments for account',
        'Note 1 details',
        'Note 2 details',
        'Note 3 details',
        30,
        v_result_id_override,
        v_enforcer_id,
        CURRENT_DATE - INTERVAL '5 days',
        v_court_id,
        v_court_id,
        'Test Court'
    )
    ON CONFLICT (defendant_account_id) DO UPDATE 
    SET business_unit_id = EXCLUDED.business_unit_id,
        account_number = EXCLUDED.account_number,
        account_status = EXCLUDED.account_status,
        account_type = EXCLUDED.account_type,
        amount_paid = EXCLUDED.amount_paid,
        account_balance = EXCLUDED.account_balance,
        amount_imposed = EXCLUDED.amount_imposed;
    
    -- Create defendant associations
    INSERT INTO defendant_account_parties (
        defendant_account_party_id,
        defendant_account_id,
        party_id,
        association_type,
        debtor
    ) VALUES
    (
        999901,
        v_defendant_account_id,
        v_party_id_defendant,
        'Defendant',
        TRUE
    )
    ON CONFLICT (defendant_account_party_id) DO UPDATE 
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        party_id = EXCLUDED.party_id,
        association_type = EXCLUDED.association_type,
        debtor = EXCLUDED.debtor;
    
    -- Create debtor details
    INSERT INTO debtor_detail (
        party_id,
        document_language,
        hearing_language
    ) VALUES
    (
        v_party_id_defendant,
        'EN',
        'EN'
    )
    ON CONFLICT (party_id) DO UPDATE 
    SET document_language = EXCLUDED.document_language,
        hearing_language = EXCLUDED.hearing_language;
    
    -- Create alias for first defendant
    INSERT INTO aliases (
        alias_id,
        party_id,
        sequence_number,
        forenames,
        surname
    ) VALUES
    (
        v_alias_id,
        v_party_id_defendant,
        1,
        'Tommy',
        'Smith'
    )
    ON CONFLICT (alias_id) DO UPDATE 
    SET party_id = EXCLUDED.party_id,
        sequence_number = EXCLUDED.sequence_number;
    
    -- Create enforcement record
    INSERT INTO enforcements (
        enforcement_id,
        defendant_account_id,
        posted_date,
        result_id,
        enforcer_id,
        hearing_court_id
    ) VALUES (
        v_enforcement_id,
        v_defendant_account_id,
        CURRENT_DATE - INTERVAL '10 days',
        v_result_id_enforcement,
        v_enforcer_id,
        v_court_id
    ) ON CONFLICT (enforcement_id) DO UPDATE
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        posted_date = EXCLUDED.posted_date;
    
    -- Create payment terms
    INSERT INTO payment_terms (
        payment_terms_id,
        defendant_account_id,
        posted_date,
        terms_type_code,
        effective_date,
        instalment_period,
        instalment_amount,
        instalment_lump_sum,
        account_balance,
        active
    ) VALUES (
        v_payment_terms_id,
        v_defendant_account_id,
        CURRENT_DATE - INTERVAL '1 month',
        'I',
        CURRENT_DATE - INTERVAL '1 month',
        'M',
        50.00,
        20.00,
        100.00,
        TRUE
    ) ON CONFLICT (payment_terms_id) DO UPDATE 
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        posted_date = EXCLUDED.posted_date,
        terms_type_code = EXCLUDED.terms_type_code;
    
    RAISE NOTICE 'Test data setup completed: defendant_account_id = %', v_defendant_account_id;
END $$;

-- Test 1: Comprehensive test for account with all details
DO $$
DECLARE
    -- Account information variables
    v_defendant_account_id      bigint;
    v_account_number            varchar(20);
    v_version_number            bigint;
    v_last_enforcement          varchar(6);
    v_last_enf_title            varchar(60);
    v_collection_order          boolean;
    v_account_comments          text;
    v_account_note_1            text;
    v_account_note_2            text;
    v_account_note_3            text;
    v_jail_days                 integer;
    v_enf_override_result_id    varchar(10);
    v_enf_override_title        varchar(60);
    v_last_movement_date        timestamp;
    
    -- Party and debtor type information
    v_debtor_type               varchar(30);
    v_document_language         varchar(2);
    v_hearing_language          varchar(2);
    v_party_id                  bigint;
    v_title                     varchar(20);
    v_forenames                 varchar(50);
    v_surname                   varchar(50);
    v_birth_date                timestamp;
    v_organisation              boolean;
    v_organisation_name         varchar(80);
    
    -- Address information
    v_address_line_1            varchar(35);
    v_address_line_2            varchar(35);
    v_address_line_3            varchar(35);
    v_address_line_4            varchar(35);
    v_address_line_5            varchar(35);
    v_postcode                  varchar(10);
    v_national_insurance_number varchar(10);
    
    -- Alias information
    v_alias_id                  bigint;
    v_sequence_number           integer;
    v_alias_org_name            varchar(50);
    v_alias_forenames           varchar(50);
    v_alias_surname             varchar(50);
    
    -- Payment terms information
    v_terms_type_code           varchar(1);
    v_instalment_period         varchar(1);
    v_instalment_amount         decimal(18,2);
    v_instalment_lump_sum       decimal(18,2);
    v_effective_date            timestamp;
BEGIN
    RAISE NOTICE '=== TEST 1: Comprehensive test for account with all details ===';
    
    -- Query the view for all information in one go
    SELECT 
        defendant_account_id,
        account_number,
        version_number,
        last_enforcement,
        last_enf_title,
        collection_order,
        account_comments,
        account_note_1,
        account_note_2,
        account_note_3,
        jail_days,
        enf_override_result_id,
        enf_override_title,
        last_movement_date,
        debtor_type,
        document_language,
        hearing_language,
        party_id,
        title,
        forenames,
        surname,
        birth_date,
        organisation,
        organisation_name,
        address_line_1,
        address_line_2,
        address_line_3,
        address_line_4,
        address_line_5,
        postcode,
        national_insurance_number,
        alias_id,
        sequence_number,
        alias_org_name,
        alias_forenames,
        alias_surname,
        terms_type_code,
        instalment_period,
        instalment_amount,
        instalment_lump_sum,
        effective_date
    INTO 
        v_defendant_account_id,
        v_account_number,
        v_version_number,
        v_last_enforcement,
        v_last_enf_title,
        v_collection_order,
        v_account_comments,
        v_account_note_1,
        v_account_note_2,
        v_account_note_3,
        v_jail_days,
        v_enf_override_result_id,
        v_enf_override_title,
        v_last_movement_date,
        v_debtor_type,
        v_document_language,
        v_hearing_language,
        v_party_id,
        v_title,
        v_forenames,
        v_surname,
        v_birth_date,
        v_organisation,
        v_organisation_name,
        v_address_line_1,
        v_address_line_2,
        v_address_line_3,
        v_address_line_4,
        v_address_line_5,
        v_postcode,
        v_national_insurance_number,
        v_alias_id,
        v_sequence_number,
        v_alias_org_name,
        v_alias_forenames,
        v_alias_surname,
        v_terms_type_code,
        v_instalment_period,
        v_instalment_amount,
        v_instalment_lump_sum,
        v_effective_date
    FROM v_defendant_accounts_summary
    WHERE account_number = 'TEST123456';
    
    -- Verify results in groups of related data
    
    -- 1. Basic account information
    ASSERT v_defendant_account_id = 999901, 'Defendant account ID should be 999901';
    ASSERT v_account_number = 'TEST123456', 'Account number should match';
    ASSERT v_version_number = 1, 'Version number should be 1';
    
    -- 2. Enforcement information
    ASSERT v_last_enforcement = 'DW', 'Last enforcement ID should match';
    ASSERT v_last_enf_title = 'Distress Warrant', 'Last enforcement title should match';
    ASSERT v_collection_order = TRUE, 'Collection order should be TRUE';
    ASSERT v_jail_days = 30, 'Jail days should be 30';
    ASSERT v_enf_override_result_id = 'SE', 'Enforcement override result ID should match';
    ASSERT v_enf_override_title = 'Suspend Enforcement', 'Enforcement override title should match';
    ASSERT v_last_movement_date::date = (CURRENT_DATE - INTERVAL '5 days')::date, 'Last movement date should match';
    
    -- 3. Notes and comments
    ASSERT v_account_comments = 'Test comments for account', 'Account comments should match';
    ASSERT v_account_note_1 = 'Note 1 details', 'Account note 1 should match';
    ASSERT v_account_note_2 = 'Note 2 details', 'Account note 2 should match';
    ASSERT v_account_note_3 = 'Note 3 details', 'Account note 3 should match';
    
    -- 4. Party and debtor information
    ASSERT v_debtor_type = 'Defendant', 'Debtor type should match';
    ASSERT v_document_language = 'EN', 'Document language should be EN';
    ASSERT v_hearing_language = 'EN', 'Hearing language should be EN';
    ASSERT v_party_id = 999901, 'Party ID should match';
    ASSERT v_title = 'Mr', 'Title should match';
    ASSERT v_forenames = 'Test', 'Forenames should match';
    ASSERT v_surname = 'Defendant', 'Surname should match';
    ASSERT v_birth_date::date = '1990-01-01'::date, 'Birth date should match';
    ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
    ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
    
    -- 5. Address information
    ASSERT v_address_line_1 = '123 Test Street', 'Address line 1 should match';
    ASSERT v_address_line_2 = 'Test Area', 'Address line 2 should match';
    ASSERT v_address_line_3 = 'Test Town', 'Address line 3 should match';
    ASSERT v_address_line_4 = 'Test County', 'Address line 4 should match';
    ASSERT v_address_line_5 IS NULL, 'Address line 5 should be NULL';
    ASSERT v_postcode = 'TE1 1ST', 'Postcode should match';
    ASSERT v_national_insurance_number = 'AB123456C', 'National Insurance number should match';
    
    -- 6. Alias information
    ASSERT v_alias_id = 999901, 'Alias ID should match';
    ASSERT v_sequence_number = 1, 'Sequence number should be 1';
    ASSERT v_alias_org_name IS NULL, 'Alias organisation name should be NULL';
    ASSERT v_alias_forenames = 'Tommy', 'Alias forenames should match';
    ASSERT v_alias_surname = 'Smith', 'Alias surname should match';
    
    -- 7. Payment terms information
    ASSERT v_terms_type_code = 'I', 'Terms type code should be I (Instalment)';
    ASSERT v_instalment_period = 'M', 'Instalment period should be M (Monthly)';
    ASSERT v_instalment_amount = 50.00, 'Instalment amount should be 50.00';
    ASSERT v_instalment_lump_sum = 20.00, 'Instalment lump sum should be 20.00';
    ASSERT v_effective_date::date = (CURRENT_DATE - INTERVAL '1 month')::date, 'Effective date should match';
    
    RAISE NOTICE 'TEST 1 PASSED: All information retrieved and validated successfully for account with all details';
END $$;

-- Clean up test data
DO $$
BEGIN
    RAISE NOTICE '=== Cleaning up test data ===';
    
    DELETE FROM payment_terms WHERE payment_terms_id = 999901;
    DELETE FROM enforcements WHERE enforcement_id = 999901;
    DELETE FROM aliases WHERE alias_id = 999901;
    DELETE FROM debtor_detail WHERE party_id = 999901;
    DELETE FROM defendant_account_parties WHERE defendant_account_party_id = 999901;
    DELETE FROM defendant_accounts WHERE defendant_account_id = 999901;
    DELETE FROM parties WHERE party_id = 999901;
    DELETE FROM result_documents WHERE result_id IN ('DW', 'SE');  
    DELETE FROM results WHERE result_id IN ('DW', 'SE');
    DELETE FROM documents WHERE document_id = '999901';
    DELETE FROM enforcers WHERE enforcer_id = 999901;
    DELETE FROM courts WHERE court_id = 999901;
    DELETE FROM local_justice_areas WHERE local_justice_area_id = 999;
    DELETE FROM business_units WHERE business_unit_id = 9999;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing