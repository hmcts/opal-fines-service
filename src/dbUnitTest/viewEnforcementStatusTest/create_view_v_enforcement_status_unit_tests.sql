/**
* CGI OPAL Program
*
* MODULE      : v_enforcement_status_tests.sql
*
* DESCRIPTION : Unit tests for v_enforcement_status view
*               Tests verify that the view correctly retrieves enforcement status
*               information with defendant account details and party information.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 03/09/2025    C Cho       1.0         PO-1703 Unit tests for v_enforcement_status view
**/

-- Start timing measurements for test execution
\timing

-- Test setup: Create test data
DO $$
DECLARE
    -- Test data identifiers
    v_business_unit_id        smallint := 9998;
    v_business_unit_name      varchar(200) := 'Test Enforcement Unit';
    v_business_unit_type      varchar(20) := 'Enforcement Division';
    v_local_justice_area_id   smallint := 998;
    v_lja_name                varchar(100) := 'Test Enforcement LJA';
    v_lja_code                varchar(3) := 'ELA';
    v_defendant_account_id    bigint := 999801;
    v_party_id_defendant      bigint := 999801;
    v_court_id                bigint := 999801;
    v_court_name              varchar(60) := 'Test Enforcement Court';
    v_court_code              varchar(10) := 'TESTECRT';
    v_result_id_enforcement   varchar(6) := 'DW';
    v_result_id_override      varchar(6) := 'SE';
    v_enforcer_id             bigint := 999801;
    v_enforcement_id          bigint := 999801;
    v_defendant_account_party_id bigint := 999801;
BEGIN
    RAISE NOTICE '=== Setting up test data for v_enforcement_status tests ===';
    
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
        'TE01'
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
        'Enforcement LJA Address',
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
        5678,
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
        5678,
        'Test Enforcement Officer'
    )
    ON CONFLICT (enforcer_id) DO UPDATE
    SET business_unit_id = EXCLUDED.business_unit_id,
        enforcer_code = EXCLUDED.enforcer_code,
        name = EXCLUDED.name;
    
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
        imposition_accruing,
        enf_next_permitted_actions
    ) VALUES
    (
        v_result_id_enforcement,
        'Distress Warrant',
        'Result',
        TRUE,
        FALSE,
        TRUE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        TRUE,
        FALSE,
        TRUE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        TRUE,
        FALSE,
        'WARRANT,PAYMENT'
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
        FALSE,
        'SUSPEND,REVIEW'
    )
    ON CONFLICT (result_id) DO UPDATE
    SET result_title = EXCLUDED.result_title,
        result_type = EXCLUDED.result_type,
        active = EXCLUDED.active,
        enforcement = EXCLUDED.enforcement,
        enforcement_override = EXCLUDED.enforcement_override,
        enf_next_permitted_actions = EXCLUDED.enf_next_permitted_actions;
    
    -- Create test parties
    INSERT INTO parties (
        party_id,
        title,
        forenames,
        surname,
        birth_date,
        age,
        organisation,
        organisation_name,
        address_line_1,
        address_line_2,
        address_line_3,
        postcode,
        national_insurance_number,
        account_type
    ) VALUES
    (
        v_party_id_defendant,
        'Ms',
        'Jane',
        'Enforced',
        '1985-06-15',
        38,
        FALSE,
        NULL,
        '456 Enforcement Street',
        'Enforcement District',
        'Enforcement City',
        'EN1 2CE',
        'JE123456D',
        'Fine'
    )
    ON CONFLICT (party_id) DO UPDATE 
    SET title = EXCLUDED.title,
        forenames = EXCLUDED.forenames,
        surname = EXCLUDED.surname,
        birth_date = EXCLUDED.birth_date,
        age = EXCLUDED.age,
        organisation = EXCLUDED.organisation;
    
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
        jail_days,
        enf_override_result_id,
        enf_override_enforcer_id,
        enf_override_tfo_lja_id,
        last_movement_date,
        enforcing_court_id,
        imposing_court_id,
        originator_name
    ) VALUES
    (
        v_defendant_account_id,
        v_business_unit_id,
        'ENF123456',
        'A',
        'Fine',
        25.00,
        75.00,
        100.00,
        1,
        TRUE,
        TRUE,
        v_result_id_enforcement,
        TRUE,
        15,
        v_result_id_override,
        v_enforcer_id,
        v_local_justice_area_id,
        CURRENT_DATE - INTERVAL '3 days',
        v_court_id,
        v_court_id,
        'Test Enforcement Court'
    )
    ON CONFLICT (defendant_account_id) DO UPDATE 
    SET business_unit_id = EXCLUDED.business_unit_id,
        account_number = EXCLUDED.account_number,
        account_status = EXCLUDED.account_status,
        last_enforcement = EXCLUDED.last_enforcement,
        enf_override_result_id = EXCLUDED.enf_override_result_id,
        enf_override_enforcer_id = EXCLUDED.enf_override_enforcer_id,
        enf_override_tfo_lja_id = EXCLUDED.enf_override_tfo_lja_id;
    
    -- Create defendant associations
    INSERT INTO defendant_account_parties (
        defendant_account_party_id,
        defendant_account_id,
        party_id,
        association_type,
        debtor
    ) VALUES
    (
        v_defendant_account_party_id,
        v_defendant_account_id,
        v_party_id_defendant,
        'Defendant',
        TRUE
    )
    ON CONFLICT (defendant_account_party_id) DO UPDATE 
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        party_id = EXCLUDED.party_id,
        association_type = EXCLUDED.association_type;
    
    -- Create enforcement records
    INSERT INTO enforcements (
        enforcement_id,
        defendant_account_id,
        reason,
        enforcer_id,
        posted_date,
        hearing_date,
        hearing_court_id,
        result_responses,
        warrant_reference,
        jail_days
    ) VALUES (
        v_enforcement_id,
        v_defendant_account_id,
        'Outstanding balance enforcement',
        v_enforcer_id,
        CURRENT_DATE - INTERVAL '7 days',
        CURRENT_DATE + INTERVAL '14 days',
        v_court_id,
        '{"status": "pending", "actions": ["warrant_issued"]}'::json,
        'WR123456789',
        7
    ) ON CONFLICT (enforcement_id) DO UPDATE
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        reason = EXCLUDED.reason,
        enforcer_id = EXCLUDED.enforcer_id,
        posted_date = EXCLUDED.posted_date,
        hearing_date = EXCLUDED.hearing_date,
        hearing_court_id = EXCLUDED.hearing_court_id,
        result_responses = EXCLUDED.result_responses,
        warrant_reference = EXCLUDED.warrant_reference,
        jail_days = EXCLUDED.jail_days;
    
    -- Create debtor detail record
    INSERT INTO debtor_detail (
        party_id,
        employer_name
    ) VALUES (
        v_party_id_defendant,
        'Test Enforcement Employer Ltd'
    ) ON CONFLICT (party_id) DO UPDATE
    SET employer_name = EXCLUDED.employer_name;
    RAISE NOTICE 'Test data setup completed: defendant_account_id = %', v_defendant_account_id;
END $$;

-- Test 1: Comprehensive test for enforcement status view
DO $$
DECLARE
    -- Defendant account fields
    v_defendant_account_id      bigint;
    v_account_status            varchar(1);
    v_collection_order          boolean;
    v_days_in_default           integer;
    v_enforcing_court_id        bigint;
    v_last_enforcement          varchar(6);
    v_enf_override_result_id    varchar(6);
    v_enf_override_enforcer_id  bigint;
    v_enf_override_tfo_lja_id   smallint;
    
    -- Enforcement fields
    v_reason                    text;
    v_enforcer_id               bigint;
    v_posted_date               timestamp;
    v_hearing_date              timestamp;
    v_hearing_court_id          bigint;
    v_result_responses          json;
    v_warrant_reference         varchar(20);
    v_jail_days                 integer;
    
    -- Defendant Account Parties fields
    v_association_type          varchar(30);
    
    -- Party fields
    v_birth_date                timestamp;
    v_age                       integer;
    v_organisation              boolean;
    
    -- Result fields
    v_result_id                 varchar(6);
    v_result_title              varchar(60);
    v_enf_next_permitted_actions varchar(200);
    
    -- Additional fields
    v_employer_flag             boolean;
BEGIN
    RAISE NOTICE '=== TEST 1: Comprehensive test for enforcement status view ===';
    
    -- Query the view for enforcement status information
    SELECT 
        defendant_account_id,
        account_status,
        collection_order,
        days_in_default,
        enforcing_court_id,
        last_enforcement,
        enf_override_result_id,
        enf_override_enforcer_id,
        enf_override_tfo_lja_id,
        reason,
        enforcer_id,
        posted_date,
        hearing_date,
        hearing_court_id,
        result_responses,
        warrant_reference,
        jail_days,
        association_type,
        birth_date,
        age,
        organisation,
        result_id,
        result_title,
        enf_next_permitted_actions,
        employer_flag
    INTO 
        v_defendant_account_id,
        v_account_status,
        v_collection_order,
        v_days_in_default,
        v_enforcing_court_id,
        v_last_enforcement,
        v_enf_override_result_id,
        v_enf_override_enforcer_id,
        v_enf_override_tfo_lja_id,
        v_reason,
        v_enforcer_id,
        v_posted_date,
        v_hearing_date,
        v_hearing_court_id,
        v_result_responses,
        v_warrant_reference,
        v_jail_days,
        v_association_type,
        v_birth_date,
        v_age,
        v_organisation,
        v_result_id,
        v_result_title,
        v_enf_next_permitted_actions,
        v_employer_flag
    FROM v_enforcement_status
    WHERE defendant_account_id = 999801;
    
    -- Verify defendant account information
    ASSERT v_defendant_account_id = 999801, 'Defendant account ID should be 999801';
    ASSERT v_account_status = 'A', 'Account status should be A (Active)';
    ASSERT v_collection_order = TRUE, 'Collection order should be TRUE';
    ASSERT v_days_in_default = 15, 'Days in default should be 15';
    ASSERT v_enforcing_court_id = 999801, 'Enforcing court ID should match';
    ASSERT v_last_enforcement = 'DW', 'Last enforcement should be DW (Distress Warrant)';
    ASSERT v_enf_override_result_id = 'SE', 'Enforcement override result should be SE';
    ASSERT v_enf_override_enforcer_id = 999801, 'Enforcement override enforcer ID should match';
    ASSERT v_enf_override_tfo_lja_id = 998, 'Enforcement override TFO LJA ID should match';
    
    -- Verify enforcement information
    ASSERT v_reason = 'Outstanding balance enforcement', 'Enforcement reason should match';
    ASSERT v_enforcer_id = 999801, 'Enforcer ID should match';
    ASSERT v_posted_date::date = (CURRENT_DATE - INTERVAL '7 days')::date, 'Posted date should match';
    ASSERT v_hearing_date::date = (CURRENT_DATE + INTERVAL '14 days')::date, 'Hearing date should match';
    ASSERT v_hearing_court_id = 999801, 'Hearing court ID should match';
    ASSERT v_result_responses::text = '{"status": "pending", "actions": ["warrant_issued"]}'::text, 'Result responses JSON should match';
    ASSERT v_warrant_reference = 'WR123456789', 'Warrant reference should match';
    ASSERT v_jail_days = 7, 'Enforcement jail days should be 7';
    
    -- Verify defendant account party information
    ASSERT v_association_type = 'Defendant', 'Association type should be Defendant';
    
    -- Verify party information
    ASSERT v_birth_date::date = '1985-06-15'::date, 'Birth date should match';
    ASSERT v_age = 38, 'Age should be 38';
    ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
    
    -- Verify result information
    ASSERT v_result_id = 'SE', 'Result ID should match enforcement override result';
    ASSERT v_result_title = 'Suspend Enforcement', 'Result title should match';
    ASSERT v_enf_next_permitted_actions = 'SUSPEND,REVIEW', 'Enforcement next permitted actions should match';
    
    -- Verify additional fields
    ASSERT v_employer_flag = TRUE, 'Employer flag should be TRUE when employer exists';
    
    RAISE NOTICE 'TEST 1 PASSED: All enforcement status information retrieved and validated successfully';
END $$;

-- Test 2: Test for organisation party type
DO $$
DECLARE
    v_org_party_id              bigint := 999802;
    v_org_defendant_account_id  bigint := 999802;
    v_org_account_party_id      bigint := 999802;
    v_organisation              boolean;
    v_birth_date                timestamp;
    v_age                       integer;
    v_employer_flag             boolean;
BEGIN
    RAISE NOTICE '=== TEST 2: Test for organisation party type ===';
    
    -- Create organisation party
    INSERT INTO parties (
        party_id,
        organisation,
        organisation_name,
        birth_date,
        age,
        account_type
    ) VALUES
    (
        v_org_party_id,
        TRUE,
        'Test Enforcement Organisation Ltd',
        NULL,
        NULL,
        'Fine'
    )
    ON CONFLICT (party_id) DO UPDATE 
    SET organisation = EXCLUDED.organisation,
        organisation_name = EXCLUDED.organisation_name;
    
    -- Create defendant account for organisation
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
        last_enforcement,
        collection_order,
        imposing_court_id,
        originator_name
    ) VALUES
    (
        v_org_defendant_account_id,
        9998,
        'ORG123456',
        'A',
        'Fine',
        0.00,
        200.00,
        200.00,
        1,
        'DW',
        TRUE,
        999801,
        'Test Enforcement Court'
    )
    ON CONFLICT (defendant_account_id) DO NOTHING;
    
    -- Link organisation to defendant account
    INSERT INTO defendant_account_parties (
        defendant_account_party_id,
        defendant_account_id,
        party_id,
        association_type,
        debtor
    ) VALUES
    (
        v_org_account_party_id,
        v_org_defendant_account_id,
        v_org_party_id,
        'Defendant',
        TRUE
    )
    ON CONFLICT (defendant_account_party_id) DO NOTHING;
    
    -- Query the view for organisation information
    SELECT 
        organisation,
        birth_date,
        age,
        employer_flag
    INTO 
        v_organisation,
        v_birth_date,
        v_age,
        v_employer_flag
    FROM v_enforcement_status
    WHERE defendant_account_id = v_org_defendant_account_id;
    
    -- Verify organisation type determination
    ASSERT v_organisation = TRUE, 'Organisation flag should be TRUE';
    ASSERT v_birth_date IS NULL, 'Birth date should be NULL for organisation';
    ASSERT v_age IS NULL, 'Age should be NULL for organisation';
    ASSERT v_employer_flag = FALSE, 'Employer flag should be FALSE when no employer exists';
    
    -- Clean up organisation test data
    DELETE FROM defendant_account_parties WHERE defendant_account_party_id = v_org_account_party_id;
    DELETE FROM defendant_accounts WHERE defendant_account_id = v_org_defendant_account_id;
    DELETE FROM parties WHERE party_id = v_org_party_id;
    
    RAISE NOTICE 'TEST 2 PASSED: Organisation party type correctly identified';
END $$;

-- Test 3: Test employer flag functionality
DO $$
DECLARE
    v_emp_party_id              bigint := 999803;
    v_emp_defendant_account_id  bigint := 999803;
    v_emp_account_party_id      bigint := 999803;
    v_employer_flag             boolean;
BEGIN
    RAISE NOTICE '=== TEST 3: Test employer flag functionality ===';
    
    -- Create party without employer
    INSERT INTO parties (
        party_id,
        title,
        forenames,
        surname,
        birth_date,
        age,
        organisation,
        account_type
    ) VALUES
    (
        v_emp_party_id,
        'Mr',
        'John',
        'NoEmployer',
        '1990-01-01',
        34,
        FALSE,
        'Fine'
    );
    
    -- Create defendant account
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
        last_enforcement,
        collection_order,
        imposing_court_id,
        originator_name
    ) VALUES
    (
        v_emp_defendant_account_id,
        9998,
        'EMP123456',
        'A',
        'Fine',
        0.00,
        100.00,
        100.00,
        1,
        'DW',
        TRUE,
        999801,
        'Test Enforcement Court'
    );
    
    -- Link party to defendant account
    INSERT INTO defendant_account_parties (
        defendant_account_party_id,
        defendant_account_id,
        party_id,
        association_type,
        debtor
    ) VALUES
    (
        v_emp_account_party_id,
        v_emp_defendant_account_id,
        v_emp_party_id,
        'Defendant',
        TRUE
    );
    
    -- Query the view for employer flag
    SELECT employer_flag
    INTO v_employer_flag
    FROM v_enforcement_status
    WHERE defendant_account_id = v_emp_defendant_account_id;
    
    -- Verify employer flag is FALSE when no employer exists
    ASSERT v_employer_flag = FALSE, 'Employer flag should be FALSE when no employer exists';
    
    -- Clean up test data
    DELETE FROM defendant_account_parties WHERE defendant_account_party_id = v_emp_account_party_id;
    DELETE FROM defendant_accounts WHERE defendant_account_id = v_emp_defendant_account_id;
    DELETE FROM parties WHERE party_id = v_emp_party_id;
    
    RAISE NOTICE 'TEST 3 PASSED: Employer flag functionality correctly implemented';
END $$;

-- Clean up test data
DO $$
BEGIN
    RAISE NOTICE '=== Cleaning up test data ===';
    
    -- Clean up in dependency order to avoid foreign key violations
    DELETE FROM debtor_detail WHERE party_id = 999801;
    DELETE FROM enforcements WHERE enforcement_id = 999801;
    DELETE FROM defendant_account_parties WHERE defendant_account_party_id = 999801;
    DELETE FROM defendant_accounts WHERE defendant_account_id = 999801;
    DELETE FROM parties WHERE party_id = 999801;
    
    -- Clean up result_documents that may reference our test results
    DELETE FROM result_documents WHERE result_id IN ('DW', 'SE');
    
    -- Now safe to delete results
    DELETE FROM results WHERE result_id IN ('DW', 'SE');
    
    DELETE FROM enforcers WHERE enforcer_id = 999801;
    DELETE FROM courts WHERE court_id = 999801;
    DELETE FROM local_justice_areas WHERE local_justice_area_id = 998;
    DELETE FROM business_units WHERE business_unit_id = 9998;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing