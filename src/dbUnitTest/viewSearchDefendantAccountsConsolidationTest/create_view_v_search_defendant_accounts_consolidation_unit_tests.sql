/**
* CGI OPAL Program
*
* MODULE      : create_view_v_search_defendant_accounts_consolidation_unit_tests.sql
*
* DESCRIPTION : Unit tests for v_search_defendant_accounts_consolidation view
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 16/12/2025    C Cho       1.0         PO-2304 - Unit tests for v_search_defendant_accounts_consolidation view
*
**/

-- Start timing measurements for test execution
\timing

DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    -- Clear down existing test data (in dependency order)
    DELETE FROM defendant_transactions WHERE defendant_transaction_id IN (9401001, 9401002);
    DELETE FROM defendant_account_parties WHERE defendant_account_id IN (7701, 7702, 7703, 7704);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (7701, 7702, 7703, 7704);
    DELETE FROM parties WHERE party_id IN (7701, 7702, 7703, 7704);

    RAISE NOTICE '=== Data cleanup before tests completed ===';
END $$;

-- Test setup: Create test data
DO $$
DECLARE
    v_account_error          bigint := 7701;
    v_account_warning        bigint := 7702;
    v_account_clean          bigint := 7703;
    v_account_cleared       bigint := 7704;
BEGIN
    RAISE NOTICE '=== Setting up test data ===';
    
    -- Parties
    INSERT INTO parties (
        party_id,
        organisation,
        organisation_name,
        surname,
        forenames,
        title,
        address_line_1,
        postcode,
        birth_date
    ) VALUES
    (7701, FALSE, NULL, 'Error', 'Ellen', 'Ms', '1 Error Street', 'ER1 1ER', '1980-01-01'),
    (7702, FALSE, NULL, 'Warning', 'Will', 'Mr', '2 Warning Way', 'WR1 1WR', '1985-02-02'),
    (7703, FALSE, NULL, 'Clean', 'Chris', 'Mr', '3 Clean Close', 'CL1 1CL', '1990-03-03'),
    (7704, FALSE, NULL, 'Override', 'Olive', 'Dr', '4 Override Oval', 'OV1 1OV', '1992-04-04')
    ON CONFLICT (party_id) DO UPDATE
    SET organisation = EXCLUDED.organisation,
        organisation_name = EXCLUDED.organisation_name,
        surname = EXCLUDED.surname,
        forenames = EXCLUDED.forenames,
        title = EXCLUDED.title,
        address_line_1 = EXCLUDED.address_line_1,
        postcode = EXCLUDED.postcode,
        birth_date = EXCLUDED.birth_date;

    -- Defendant accounts
    INSERT INTO defendant_accounts (
        defendant_account_id,
        business_unit_id,
        account_number,
        amount_imposed,
        amount_paid,
        account_balance,
        account_status,
        account_type,
        prosecutor_case_reference,
        last_enforcement,
        jail_days,
        enforcement_case_status,
        collection_order,
        version_number,
        cheque_clearance_period
    ) VALUES
    (v_account_error, 77, 'CON-ERR-01', 500.00, 100.00, 400.00, 'TA', 'Fine', 'PCR-ERR', 'DW', 5, 'OPEN', TRUE, 3, NULL),
    (v_account_warning, 77, 'CON-WARN-01', 300.00, 50.00, 250.00, 'L', 'Fine', 'PCR-WARN', 'AEO', NULL, NULL, FALSE, 2, NULL),
    (v_account_clean, 77, 'CON-CLEAN-01', 200.00, 120.00, 80.00, 'L', 'Fine', 'PCR-CLEAN', NULL, NULL, NULL, FALSE, 1, NULL),
    (v_account_cleared, 77, 'CON-OVR-01', 150.00, 20.00, 130.00, 'L', 'Fine', 'PCR-OVR', NULL, NULL, NULL, FALSE, 1, 1)
    ON CONFLICT (defendant_account_id) DO UPDATE
    SET business_unit_id = EXCLUDED.business_unit_id,
        account_number = EXCLUDED.account_number,
        amount_imposed = EXCLUDED.amount_imposed,
        amount_paid = EXCLUDED.amount_paid,
        account_balance = EXCLUDED.account_balance,
        account_status = EXCLUDED.account_status,
        account_type = EXCLUDED.account_type,
        prosecutor_case_reference = EXCLUDED.prosecutor_case_reference,
        last_enforcement = EXCLUDED.last_enforcement,
        jail_days = EXCLUDED.jail_days,
        enforcement_case_status = EXCLUDED.enforcement_case_status,
        collection_order = EXCLUDED.collection_order,
        version_number = EXCLUDED.version_number,
        cheque_clearance_period = EXCLUDED.cheque_clearance_period;

    -- Defendant account parties
    INSERT INTO defendant_account_parties (
        defendant_account_party_id,
        defendant_account_id,
        party_id,
        association_type,
        debtor
    ) VALUES
    (7701, v_account_error, 7701, 'Defendant', TRUE),
    (7702, v_account_warning, 7702, 'Defendant', TRUE),
    (7703, v_account_clean, 7703, 'Defendant', TRUE),
    (7704, v_account_cleared, 7704, 'Defendant', TRUE)
    ON CONFLICT (defendant_account_party_id) DO UPDATE
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        party_id = EXCLUDED.party_id,
        association_type = EXCLUDED.association_type,
        debtor = EXCLUDED.debtor;

    -- Uncleared cheque
    INSERT INTO defendant_transactions (
        defendant_transaction_id,
        defendant_account_id,
        posted_date,
        transaction_type,
        transaction_amount,
        payment_method,
        associated_record_type,
        associated_record_id
    ) VALUES (
        9401001,
        v_account_warning,
        CURRENT_DATE - INTERVAL '9 days',
        'PAYMNT',
        50.00,
        'CQ',
        'defendant_accounts',
        v_account_warning::varchar
    )
    ON CONFLICT (defendant_transaction_id) DO UPDATE
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        posted_date = EXCLUDED.posted_date,
        transaction_type = EXCLUDED.transaction_type,
        transaction_amount = EXCLUDED.transaction_amount,
        payment_method = EXCLUDED.payment_method;

    -- Cleared cheque
    INSERT INTO defendant_transactions (
        defendant_transaction_id,
        defendant_account_id,
        posted_date,
        transaction_type,
        transaction_amount,
        payment_method,
        associated_record_type,
        associated_record_id
    ) VALUES (
        9401002,
        v_account_cleared,
        CURRENT_DATE - INTERVAL '11 days',
        'PAYMNT',
        45.00,
        'CQ',
        'defendant_accounts',
        v_account_cleared::varchar
    )
    ON CONFLICT (defendant_transaction_id) DO UPDATE
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        posted_date = EXCLUDED.posted_date,
        transaction_type = EXCLUDED.transaction_type,
        transaction_amount = EXCLUDED.transaction_amount,
        payment_method = EXCLUDED.payment_method;

    RAISE NOTICE 'Test data setup completed for accounts %, %, %', v_account_error, v_account_warning, v_account_clean;
END $$;

-- Test 1: All error messages are generated correctly
DO $$
DECLARE
    v_errors             text[];
    v_warnings           text[];
    v_has_collection     boolean;
    v_version_number     bigint;
BEGIN
    RAISE NOTICE '--- TEST 1: All error messages are generated correctly ---';

    SELECT errors, warnings, has_collection_order, version_number
    INTO v_errors, v_warnings, v_has_collection, v_version_number
    FROM v_search_defendant_accounts_consolidation
    WHERE defendant_account_id = 7701;

    ASSERT v_errors = ARRAY[
        'CON.ER.1|Account status is `TA`',
        'CON.ER.3|Last enforcement action on the account is `Warrant of Control(DW)`',
        'CON.ER.4|Account has days in default',
        'CON.ER.5|Account linked to outstanding active case'
    ], 'Expected all error codes for account with transfer out, enforcement, jail days, and active case';

    ASSERT v_warnings IS NULL, 'Warnings should be NULL when no warning conditions are present';
    ASSERT v_has_collection = TRUE, 'has_collection_order should reflect defendant_accounts.collection_order';
    ASSERT v_version_number = 3, 'version_number should be sourced from defendant_accounts';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

-- Test 2: Both warning messages are generated correctly
DO $$
DECLARE
    v_errors         text[];
    v_warnings       text[];
    v_collection     boolean;
BEGIN
    RAISE NOTICE '--- TEST 2: Both warning messages are generated correctly ---';

    SELECT errors, warnings, has_collection_order
    INTO v_errors, v_warnings, v_collection
    FROM v_search_defendant_accounts_consolidation
    WHERE defendant_account_id = 7702;

    ASSERT v_errors IS NULL, 'Errors should be NULL when only warning conditions apply';
    ASSERT v_warnings = ARRAY[
        'CON.WN.1|Last enforcement action on the account is `Attachment of Earnings Order - No Collection Order(AEO)`',
        'CON.WN.2|Account has uncleared cheque payments'
    ], 'Expected warnings for warning enforcement result and uncleared cheque';
    ASSERT v_collection = FALSE, 'has_collection_order should be FALSE for non-collection order account';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

-- Test 3: No error or warning conditions returns NULL arrays
DO $$
DECLARE
    v_errors         text[];
    v_warnings       text[];
BEGIN
    RAISE NOTICE '--- TEST 3: Clean account returns no errors or warnings ---';

    SELECT errors, warnings
    INTO v_errors, v_warnings
    FROM v_search_defendant_accounts_consolidation
    WHERE defendant_account_id = 7703;

    ASSERT v_errors IS NULL, 'Errors should be NULL when no error conditions exist';
    ASSERT v_warnings IS NULL, 'Warnings should be NULL when no warning conditions exist';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

-- Test 4: Account with cleared cheque should not generate warning
DO $$
DECLARE
    v_warnings       text[];
BEGIN
    RAISE NOTICE '--- TEST 4: Account with cleared cheque should not generate warning ---';

    SELECT warnings
    INTO v_warnings
    FROM v_search_defendant_accounts_consolidation
    WHERE defendant_account_id = 7704;

    ASSERT v_warnings IS NULL, 'Warnings should be NULL because the cheque has cleared';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

-- Cleanup after tests
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleaning up test data ===';

    DELETE FROM defendant_transactions WHERE defendant_transaction_id IN (9401001, 9401002);
    DELETE FROM defendant_account_parties WHERE defendant_account_id IN (7701, 7702, 7703, 7704);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (7701, 7702, 7703, 7704);
    DELETE FROM parties WHERE party_id IN (7701, 7702, 7703, 7704);

    RAISE NOTICE 'Test data cleanup completed';
END $$;
