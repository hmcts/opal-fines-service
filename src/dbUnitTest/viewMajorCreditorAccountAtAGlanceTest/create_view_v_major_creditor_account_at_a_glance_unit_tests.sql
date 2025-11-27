/**
* CGI OPAL Program
*
* MODULE      : test_view_v_major_creditor_account_at_a_glance.sql
*
* DESCRIPTION : Unit test for v_major_creditor_account_at_a_glance view
*               Tests verify that the view correctly retrieves major creditor account at a glance information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 19/11/2025    C Cho       1.0         PO-2123 Unit tests for major creditor account at a glance view
*
**/

-- Start timing measurements for test execution
\timing

-- Test setup: Create test data
DO $$
DECLARE
    v_business_unit_id_mj     smallint := 9998;
    v_business_unit_id_cf     smallint := 9999;
    v_creditor_account_id_mj  bigint := 999801;
    v_creditor_account_id_cf  bigint := 999802;
    v_major_creditor_id       bigint := 999801;
BEGIN
    RAISE NOTICE '=== Setting up test data for v_major_creditor_account_at_a_glance tests ===';
    
    -- Create test business units
    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_type
    ) VALUES 
    (
        v_business_unit_id_mj,
        'Test Major Creditor Unit',
        'Area'
    ),
    (
        v_business_unit_id_cf,
        'Test Central Fund Unit',
        'Area'
    )
    ON CONFLICT (business_unit_id) DO UPDATE 
    SET business_unit_name = EXCLUDED.business_unit_name,
        business_unit_type = EXCLUDED.business_unit_type;
    
    -- Create test major creditor
    INSERT INTO major_creditors (
        major_creditor_id,
        business_unit_id,
        name,
        address_line_1,
        address_line_2,
        address_line_3,
        postcode
    ) VALUES (
        v_major_creditor_id,
        v_business_unit_id_mj,
        'Test Police Authority',
        '100 Police Headquarters',
        'Central District',
        'Law Enforcement Area',
        'P1 1AA'
    )
    ON CONFLICT (major_creditor_id) DO UPDATE 
    SET business_unit_id = EXCLUDED.business_unit_id,
        name = EXCLUDED.name,
        address_line_1 = EXCLUDED.address_line_1,
        address_line_2 = EXCLUDED.address_line_2,
        address_line_3 = EXCLUDED.address_line_3,
        postcode = EXCLUDED.postcode;
    
    -- Create test configuration item for Central Fund
    INSERT INTO configuration_items (
        configuration_item_id,
        item_name,
        business_unit_id,
        item_values
    ) VALUES (
        999801,
        'CENTRAL_FUND_ACCOUNT',
        v_business_unit_id_cf,
        '{"name": "HM Courts & Tribunals Service Test", "address_line_1": "Test HMCS Address 1", "address_line_2": "Test HMCS Address 2", "address_line_3": "Test HMCS Address 3", "pay_by_bacs": "N"}'::jsonb
    )
    ON CONFLICT (configuration_item_id) DO UPDATE 
    SET item_name = EXCLUDED.item_name,
        business_unit_id = EXCLUDED.business_unit_id,
        item_values = EXCLUDED.item_values;
    
    -- Create test creditor accounts
    INSERT INTO creditor_accounts (
        creditor_account_id,
        business_unit_id,
        account_number,
        creditor_account_type,
        prosecution_service,
        from_suspense,
        hold_payout,
        pay_by_bacs,
        major_creditor_id
    ) VALUES 
    (
        v_creditor_account_id_mj,
        v_business_unit_id_mj,
        'CA001',
        'MJ',
        FALSE,
        FALSE,
        FALSE,
        TRUE,
        v_major_creditor_id
    ),
    (
        v_creditor_account_id_cf,
        v_business_unit_id_cf,
        'CA002',
        'CF',
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        NULL
    )
    ON CONFLICT (creditor_account_id) DO UPDATE 
    SET business_unit_id = EXCLUDED.business_unit_id,
        account_number = EXCLUDED.account_number,
        creditor_account_type = EXCLUDED.creditor_account_type,
        prosecution_service = EXCLUDED.prosecution_service,
        from_suspense = EXCLUDED.from_suspense,
        hold_payout = EXCLUDED.hold_payout,
        pay_by_bacs = EXCLUDED.pay_by_bacs,
        major_creditor_id = EXCLUDED.major_creditor_id;
    
    RAISE NOTICE 'Test data setup completed: MJ creditor_account_id = %, CF creditor_account_id = %', 
        v_creditor_account_id_mj, v_creditor_account_id_cf;
END $$;

-- Test 1: Test Major Creditor (MJ) account at a glance
DO $$
DECLARE
    v_creditor_account_id   bigint;
    v_bacs_details          varchar(12);
    v_name                  varchar(80);
    v_address_line_1        varchar(35);
    v_address_line_2        varchar(35);
    v_address_line_3        varchar(35);
    v_postcode              varchar(10);
BEGIN
    RAISE NOTICE '=== TEST 1: Test Major Creditor (MJ) account at a glance ===';
    
    -- Query the view for Major Creditor account
    SELECT 
        creditor_account_id,
        bacs_details,
        name,
        address_line_1,
        address_line_2,
        address_line_3,
        postcode
    INTO 
        v_creditor_account_id,
        v_bacs_details,
        v_name,
        v_address_line_1,
        v_address_line_2,
        v_address_line_3,
        v_postcode
    FROM v_major_creditor_account_at_a_glance
    WHERE creditor_account_id = 999801;
    
    -- Verify Major Creditor fields
    ASSERT v_creditor_account_id = 999801, 'Creditor account ID should be 999801';
    ASSERT v_bacs_details = 'PROVIDED', 'BACS details should be "PROVIDED" when pay_by_bacs is TRUE';
    ASSERT v_name = 'Test Police Authority', 'Major creditor name should be "Test Police Authority"';
    ASSERT v_address_line_1 = '100 Police Headquarters', 'Address line 1 should match';
    ASSERT v_address_line_2 = 'Central District', 'Address line 2 should match';
    ASSERT v_address_line_3 = 'Law Enforcement Area', 'Address line 3 should match';
    ASSERT v_postcode = 'P1 1AA', 'Postcode should match';
    
    RAISE NOTICE 'TEST 1 PASSED: Major Creditor account at a glance retrieved and validated successfully';
END $$;

-- Test 2: Test Central Fund (CF) account at a glance
DO $$
DECLARE
    v_creditor_account_id   bigint;
    v_bacs_details          varchar(12);
    v_name                  text;
    v_address_line_1        text;
    v_address_line_2        text;
    v_address_line_3        text;
    v_postcode              text;
BEGIN
    RAISE NOTICE '=== TEST 2: Test Central Fund (CF) account at a glance ===';
    
    -- Query the view for Central Fund account
    SELECT 
        creditor_account_id,
        bacs_details,
        name,
        address_line_1,
        address_line_2,
        address_line_3,
        postcode
    INTO 
        v_creditor_account_id,
        v_bacs_details,
        v_name,
        v_address_line_1,
        v_address_line_2,
        v_address_line_3,
        v_postcode
    FROM v_major_creditor_account_at_a_glance
    WHERE creditor_account_id = 999802;
    
    -- Verify Central Fund fields
    ASSERT v_creditor_account_id = 999802, 'Creditor account ID should be 999802';
    ASSERT v_bacs_details = 'NOT PROVIDED', 'BACS details should be "NOT PROVIDED" when pay_by_bacs is FALSE';
    ASSERT v_name = 'HM Courts & Tribunals Service Test', 'Central Fund name should match JSON value';
    ASSERT v_address_line_1 = 'Test HMCS Address 1', 'Address line 1 should match JSON value';
    ASSERT v_address_line_2 = 'Test HMCS Address 2', 'Address line 2 should match JSON value';
    ASSERT v_address_line_3 = 'Test HMCS Address 3', 'Address line 3 should match JSON value';
    ASSERT v_postcode IS NULL, 'Postcode should be NULL for Central Fund';
    
    RAISE NOTICE 'TEST 2 PASSED: Central Fund account at a glance retrieved and validated successfully';
END $$;

-- Test 3: Test Major Creditor with BACS not provided
DO $$
DECLARE
    v_major_creditor_id_2     bigint := 999802;
    v_creditor_account_id_2   bigint := 999803;
    v_bacs_details            varchar(12);
BEGIN
    RAISE NOTICE '=== TEST 3: Test Major Creditor with BACS not provided ===';
    
    -- Create another major creditor
    INSERT INTO major_creditors (
        major_creditor_id,
        business_unit_id,
        name,
        address_line_1,
        postcode
    ) VALUES (
        v_major_creditor_id_2,
        9998,
        'Test Council Authority',
        '200 Council Offices',
        'C2 2BB'
    );
    
    -- Create creditor account with BACS disabled
    INSERT INTO creditor_accounts (
        creditor_account_id,
        business_unit_id,
        account_number,
        creditor_account_type,
        prosecution_service,
        from_suspense,
        hold_payout,
        pay_by_bacs,
        major_creditor_id
    ) VALUES (
        v_creditor_account_id_2,
        9998,
        'CA003',
        'MJ',
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        v_major_creditor_id_2
    );
    
    -- Test BACS details for disabled BACS
    SELECT bacs_details INTO v_bacs_details
    FROM v_major_creditor_account_at_a_glance
    WHERE creditor_account_id = v_creditor_account_id_2;
    
    ASSERT v_bacs_details = 'NOT PROVIDED', 'BACS details should be "NOT PROVIDED" when pay_by_bacs is FALSE';
    
    RAISE NOTICE 'TEST 3 PASSED: Major Creditor with BACS disabled handled correctly';
END $$;

-- Test 4: Test Central Fund with different JSON structure
DO $$
DECLARE
    v_business_unit_id_cf2    smallint := 9997;
    v_creditor_account_id_cf2 bigint := 999804;
    v_name                    text;
    v_address_line_1          text;
BEGIN
    RAISE NOTICE '=== TEST 4: Test Central Fund with different JSON structure ===';
    
    -- Create another business unit
    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_type
    ) VALUES (
        v_business_unit_id_cf2,
        'Test Central Fund Unit 2',
        'Area'
    );
    
    -- Create configuration item with minimal JSON
    INSERT INTO configuration_items (
        configuration_item_id,
        item_name,
        business_unit_id,
        item_values
    ) VALUES (
        999802,
        'CENTRAL_FUND_ACCOUNT',
        v_business_unit_id_cf2,
        '{"name": "Minimal Central Fund", "address_line_1": "Minimal Address"}'::jsonb
    );
    
    -- Create Central Fund creditor account
    INSERT INTO creditor_accounts (
        creditor_account_id,
        business_unit_id,
        account_number,
        creditor_account_type,
        prosecution_service,
        from_suspense,
        hold_payout,
        pay_by_bacs
    ) VALUES (
        v_creditor_account_id_cf2,
        v_business_unit_id_cf2,
        'CA004',
        'CF',
        FALSE,
        FALSE,
        FALSE,
        TRUE
    );
    
    -- Test minimal JSON structure
    SELECT name, address_line_1
    INTO v_name, v_address_line_1
    FROM v_major_creditor_account_at_a_glance
    WHERE creditor_account_id = v_creditor_account_id_cf2;
    
    ASSERT v_name = 'Minimal Central Fund', 'Name should match JSON value';
    ASSERT v_address_line_1 = 'Minimal Address', 'Address line 1 should match JSON value';
    
    RAISE NOTICE 'TEST 4 PASSED: Central Fund with minimal JSON structure handled correctly';
END $$;

-- Test 5: Test UNION operation - verify both MJ and CF records are returned
DO $$
DECLARE
    v_record_count  integer;
    v_mj_count      integer;
    v_cf_count      integer;
BEGIN
    RAISE NOTICE '=== TEST 5: Test UNION operation - verify both MJ and CF records are returned ===';
    
    -- Count total records
    SELECT COUNT(*) INTO v_record_count
    FROM v_major_creditor_account_at_a_glance
    WHERE creditor_account_id IN (999801, 999802, 999803, 999804);
    
    -- Count MJ records
    SELECT COUNT(*) INTO v_mj_count
    FROM v_major_creditor_account_at_a_glance
    WHERE creditor_account_id IN (999801, 999803);
    
    -- Count CF records
    SELECT COUNT(*) INTO v_cf_count
    FROM v_major_creditor_account_at_a_glance
    WHERE creditor_account_id IN (999802, 999804);
    
    ASSERT v_record_count = 4, 'Total record count should be 4, got: ' || v_record_count;
    ASSERT v_mj_count = 2, 'MJ record count should be 2, got: ' || v_mj_count;
    ASSERT v_cf_count = 2, 'CF record count should be 2, got: ' || v_cf_count;
    
    RAISE NOTICE 'TEST 5 PASSED: UNION operation working correctly - MJ: %, CF: %, Total: %', 
        v_mj_count, v_cf_count, v_record_count;
END $$;

-- Clean up test data
DO $$
BEGIN
    RAISE NOTICE '=== Cleaning up test data ===';
    
    -- Delete test records in the correct order to avoid foreign key constraint issues
    DELETE FROM creditor_accounts WHERE creditor_account_id IN (999801, 999802, 999803, 999804);
    DELETE FROM configuration_items WHERE configuration_item_id IN (999801, 999802);
    DELETE FROM major_creditors WHERE major_creditor_id IN (999801, 999802);
    DELETE FROM business_units WHERE business_unit_id IN (9998, 9999, 9997);
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing

RAISE NOTICE '=== All tests completed for v_major_creditor_account_at_a_glance view ===';