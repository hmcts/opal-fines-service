/**
* CGI OPAL Program
*
* MODULE      : test_view_v_audit_creditor_accounts.sql
*
* DESCRIPTION : Unit test for v_audit_creditor_accounts view
*               Tests verify that the view correctly retrieves creditor account information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 15/08/2025    C Cho       1.0         PO-1664 Unit test for audit creditor accounts view
*
**/

-- Start timing measurements for test execution
\timing

-- Test setup: Create test data
DO $$
DECLARE
    v_business_unit_id        smallint := 9999;
    v_party_id_individual     bigint := 999901;
    v_creditor_account_id_1   bigint := 999901;
BEGIN
    RAISE NOTICE '=== Setting up test data for v_audit_creditor_accounts tests ===';
    
    -- Create test business unit
    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_code,
        business_unit_type,
        welsh_language
    ) VALUES (
        v_business_unit_id,
        'Test Audit Business Unit',
        'TABU',
        'Accounting',
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
        v_party_id_individual,
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
    )
    ON CONFLICT (party_id) DO UPDATE 
    SET organisation = EXCLUDED.organisation,
        title = EXCLUDED.title,
        forenames = EXCLUDED.forenames,
        surname = EXCLUDED.surname,
        organisation_name = EXCLUDED.organisation_name,
        address_line_1 = EXCLUDED.address_line_1,
        address_line_2 = EXCLUDED.address_line_2,
        address_line_3 = EXCLUDED.address_line_3,
        postcode = EXCLUDED.postcode,
        account_type = EXCLUDED.account_type;
    
    -- Create test creditor accounts
    INSERT INTO creditor_accounts (
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
        v_creditor_account_id_1,
        v_business_unit_id,
        'CA001',
        'MN',
        FALSE,
        v_party_id_individual,
        FALSE,
        TRUE,
        TRUE,
        '123456',
        '12345678',
        'John Smith Account',
        'REF001',
        '1'
    )
    ON CONFLICT (creditor_account_id) DO UPDATE 
    SET business_unit_id = EXCLUDED.business_unit_id,
        account_number = EXCLUDED.account_number,
        creditor_account_type = EXCLUDED.creditor_account_type,
        prosecution_service = EXCLUDED.prosecution_service,
        minor_creditor_party_id = EXCLUDED.minor_creditor_party_id,
        from_suspense = EXCLUDED.from_suspense,
        hold_payout = EXCLUDED.hold_payout,
        pay_by_bacs = EXCLUDED.pay_by_bacs,
        bank_sort_code = EXCLUDED.bank_sort_code,
        bank_account_number = EXCLUDED.bank_account_number,
        bank_account_name = EXCLUDED.bank_account_name,
        bank_account_reference = EXCLUDED.bank_account_reference,
        bank_account_type = EXCLUDED.bank_account_type;
    
    RAISE NOTICE 'Test data setup completed: creditor_account_id = %', v_creditor_account_id_1;
END $$;

-- Test 1: Comprehensive test for individual party with full details
DO $$
DECLARE
    -- Party information variables
    v_name                    varchar(255);
    v_address_line_1          varchar(255);
    v_address_line_2          varchar(255);
    v_address_line_3          varchar(255);
    v_postcode                varchar(10);
    
    -- Creditor account variables
    v_creditor_account_id     bigint;
    v_hold_payout             boolean;
    v_pay_by_bacs             boolean;
    
    -- Banking information variables
    v_bank_sort_code          varchar(6);
    v_bank_account_number     varchar(10);
    v_bank_account_name       varchar(18);
    v_bank_account_reference  varchar(18);
    v_bank_account_type       varchar(1);
BEGIN
    RAISE NOTICE '=== TEST 1: Comprehensive test for individual party with full details ===';
    
    -- Query the view for all information
    SELECT 
        name,
        address_line_1,
        address_line_2,
        address_line_3,
        postcode,
        creditor_account_id,
        hold_payout,
        pay_by_bacs,
        bank_sort_code,
        bank_account_number,
        bank_account_name,
        bank_account_reference,
        bank_account_type
    INTO 
        v_name,
        v_address_line_1,
        v_address_line_2,
        v_address_line_3,
        v_postcode,
        v_creditor_account_id,
        v_hold_payout,
        v_pay_by_bacs,
        v_bank_sort_code,
        v_bank_account_number,
        v_bank_account_name,
        v_bank_account_reference,
        v_bank_account_type
    FROM v_audit_creditor_accounts
    WHERE creditor_account_id = 999901;
    
    -- Verify all results in groups of related data
    
    -- 1. Party information
    ASSERT v_name = 'Mr John Smith', 'Name should be "Mr John Smith", got: ' || COALESCE(v_name, 'NULL');
    ASSERT v_address_line_1 = '123 Main Street', 'Address line 1 should match';
    ASSERT v_address_line_2 = 'Apartment 4B', 'Address line 2 should match';
    ASSERT v_address_line_3 = 'Downtown', 'Address line 3 should match';
    ASSERT v_postcode = 'SW1A 1AA', 'Postcode should match';
    
    -- 2. Creditor account information
    ASSERT v_creditor_account_id = 999901, 'Creditor account ID should be 999901';
    ASSERT v_hold_payout = TRUE, 'Hold payout should be TRUE';
    ASSERT v_pay_by_bacs = TRUE, 'Pay by BACS should be TRUE';
    
    -- 3. Banking information
    ASSERT v_bank_sort_code = '123456', 'Bank sort code should be 123456';
    ASSERT v_bank_account_number = '12345678', 'Bank account number should be 12345678';
    ASSERT v_bank_account_name = 'John Smith Account', 'Bank account name should match';
    ASSERT v_bank_account_reference = 'REF001', 'Bank account reference should be REF001';
    ASSERT v_bank_account_type = '1', 'Bank account type should be 1';
    
    RAISE NOTICE 'TEST 1 PASSED: All information retrieved and validated successfully for individual party';
END $$;

-- Clean up test data
DO $$
BEGIN
    RAISE NOTICE '=== Cleaning up test data ===';
    
    -- Delete test records in the correct order to avoid foreign key constraint issues
    DELETE FROM creditor_accounts WHERE creditor_account_id = 999901;
    
    DELETE FROM parties WHERE party_id = 999901;
    
    DELETE FROM business_units WHERE business_unit_id = 9999;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing

RAISE NOTICE '=== All tests completed for v_audit_creditor_accounts view ===';