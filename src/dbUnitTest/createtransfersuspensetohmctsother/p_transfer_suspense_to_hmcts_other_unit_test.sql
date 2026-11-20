/**
* OPAL Program
*
* MODULE      : p_transfer_suspense_to_hmcts_other_unit_tests.sql
*
* DESCRIPTION : Self-contained regression tests for PO-10349 using newly
*               created suspense fixtures and transaction-safe rollback.
*
* PARAMETERS  : None - fixtures are created within this test transaction
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    -------      --------    --------------------------------------------------------
* 18/11/2026    I Chandio    1.0         PO-10349 Add isolated transfer procedure unit tests
*
**/

BEGIN;

DO $$
DECLARE
    c_bu_id             CONSTANT business_units.business_unit_id%TYPE := 30000;
    c_base_account_id   CONSTANT suspense_accounts.suspense_account_id%TYPE := 9900000000594950;
    c_positive          CONSTANT suspense_items.suspense_item_id%TYPE := 9900000000594901;
    c_negative          CONSTANT suspense_items.suspense_item_id%TYPE := 9900000000594902;
    c_zero              CONSTANT suspense_items.suspense_item_id%TYPE := 9900000000594903;
    c_stale             CONSTANT suspense_items.suspense_item_id%TYPE := 9900000000594904;
    c_no_cf             CONSTANT suspense_items.suspense_item_id%TYPE := 9900000000594951;
    c_missing           CONSTANT suspense_items.suspense_item_id%TYPE := 9900000000594999;
    v_transaction_id    suspense_transactions.suspense_transaction_id%TYPE;
    v_initial_xfer      BIGINT;
    v_final_xfer        BIGINT;
    v_count             BIGINT;
    v_version           suspense_items.version_number%TYPE;
    v_balance           suspense_transactions.amount%TYPE;
    v_state             TEXT;
    v_message           TEXT;
    v_detail            TEXT;
    v_test_count        INTEGER := 0;
BEGIN
    -- Fail safely rather than overwriting another fixture.
    IF EXISTS (SELECT 1 FROM business_units WHERE business_unit_id = c_bu_id)
       OR EXISTS (SELECT 1 FROM suspense_accounts WHERE suspense_account_id = c_base_account_id)
       OR EXISTS (
           SELECT 1 FROM suspense_items
           WHERE suspense_item_id IN (c_positive, c_negative, c_zero, c_stale, c_no_cf, c_missing)
              OR suspense_item_number IN (90005901, 90005902, 90005903, 90005904, 90005951)
       ) THEN
        RAISE EXCEPTION 'PO-10349 fixture IDs already in use. Choose unused IDs.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM suspense_accounts
        WHERE suspense_account_id = 990001 AND business_unit_id = 5
    ) OR NOT EXISTS (
        SELECT 1 FROM creditor_accounts
        WHERE creditor_account_id = 5 AND business_unit_id = 5
          AND creditor_account_type = 'CF'
    ) THEN
        RAISE EXCEPTION 'PO-10349 baseline accounts (BU 5, suspense 990001, CF creditor 5) are missing.';
    END IF;

    -- Dedicated suspense items in BU 5 for normal and exception paths.
    INSERT INTO suspense_items (
        suspense_item_id, suspense_account_id, suspense_item_number,
        suspense_item_type, created_date, version_number
    ) VALUES
        (c_positive, 990001, 90005901, 'CF', clock_timestamp(), 1),
        (c_negative, 990001, 90005902, 'CF', clock_timestamp(), 1),
        (c_zero,     990001, 90005903, 'CF', clock_timestamp(), 1),
        (c_stale,    990001, 90005904, 'CF', clock_timestamp(), 2);

    CALL p_insert_suspense_transactions_common(
        c_positive, clock_timestamp()::TIMESTAMP, 'opal-test', 'OPAL Test User',
        'IN', 150.00, NULL, NULL, 'PO-10349 positive fixture', NULL, v_transaction_id
    );
    CALL p_insert_suspense_transactions_common(
        c_negative, clock_timestamp()::TIMESTAMP, 'opal-test', 'OPAL Test User',
        'IN', -50.00, NULL, NULL, 'PO-10349 negative fixture', NULL, v_transaction_id
    );
    CALL p_insert_suspense_transactions_common(
        c_stale, clock_timestamp()::TIMESTAMP, 'opal-test', 'OPAL Test User',
        'IN', 75.00, NULL, NULL, 'PO-10349 stale-version fixture', NULL, v_transaction_id
    );

    -- A separate BU with no CF creditor account, based on the known-good BU 5.
    INSERT INTO business_units (
        business_unit_id, business_unit_name, business_unit_code, business_unit_type,
        account_number_prefix, parent_business_unit_id, opal_domain,
        welsh_language, account_number_suffix
    )
    SELECT c_bu_id, 'PO-10349 Isolated Test', 'T349', business_unit_type,
           account_number_prefix, parent_business_unit_id, opal_domain,
           welsh_language, account_number_suffix
    FROM business_units WHERE business_unit_id = 5;

    INSERT INTO suspense_accounts (suspense_account_id, business_unit_id, account_number)
    VALUES (c_base_account_id, c_bu_id, 'T349');

    INSERT INTO suspense_items (
        suspense_item_id, suspense_account_id, suspense_item_number,
        suspense_item_type, created_date, version_number
    ) VALUES (c_no_cf, c_base_account_id, 90005951, 'CF', clock_timestamp(), 1);

    CALL p_insert_suspense_transactions_common(
        c_no_cf, clock_timestamp()::TIMESTAMP, 'opal-test', 'OPAL Test User',
        'IN', 100.00, NULL, NULL, 'PO-10349 missing-CF fixture', NULL, v_transaction_id
    );

    IF EXISTS (
        SELECT 1 FROM creditor_accounts WHERE business_unit_id = c_bu_id
          AND creditor_account_type = 'CF'
    ) THEN
        RAISE EXCEPTION 'Fixture error: temporary business unit has a CF creditor account.';
    END IF;

    -- TEST 01: positive transfer; version, balance, XP and XFER.
    CALL p_transfer_suspense_to_hmcts_other(c_positive, 1, 'opal-test', 'OPAL Test User');
    SELECT si.version_number, COALESCE(SUM(st.amount), 0)
      INTO v_version, v_balance
      FROM suspense_items si
      LEFT JOIN suspense_transactions st ON st.suspense_item_id = si.suspense_item_id
     WHERE si.suspense_item_id = c_positive
     GROUP BY si.version_number;
    IF v_version IS DISTINCT FROM 2 OR v_balance <> 0 THEN
        RAISE EXCEPTION 'TEST 01 FAILED: version %, balance %', v_version, v_balance;
    END IF;
    SELECT COUNT(*) INTO v_count FROM suspense_transactions
    WHERE suspense_item_id = c_positive AND transaction_type = 'XP' AND amount = -150.00;
    IF v_count <> 1 THEN RAISE EXCEPTION 'TEST 01 FAILED: missing XP'; END IF;
    SELECT COUNT(*) INTO v_count
    FROM creditor_transactions ct
    JOIN suspense_transactions st
      ON ct.associated_record_id = st.suspense_transaction_id::VARCHAR
    WHERE st.suspense_item_id = c_positive AND st.transaction_type = 'XP'
      AND ct.creditor_account_id = 5 AND ct.transaction_type = 'XFER'
      AND ct.transaction_amount = 150.00
      AND ct.associated_record_type = 'suspense_items';
    IF v_count <> 1 THEN RAISE EXCEPTION 'TEST 01 FAILED: missing XFER'; END IF;
    RAISE NOTICE 'TEST 01 PASS: Positive balance, XP, XFER, zero remainder and version increment';
    v_test_count := v_test_count + 1;

    -- TEST 02: stale version must abort and leave original balance/transactions.
    BEGIN
        CALL p_transfer_suspense_to_hmcts_other(c_stale, 1, 'opal-test', 'OPAL Test User');
        RAISE EXCEPTION 'TEST 02 FAILED: stale version accepted';
    EXCEPTION WHEN SQLSTATE 'P0001' THEN
        GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
        IF v_detail NOT LIKE 'p_transfer_suspense_to_hmcts_other:%' THEN
            RAISE EXCEPTION 'TEST 02 FAILED: DETAIL missing';
        END IF;
    END;
    SELECT version_number INTO v_version FROM suspense_items WHERE suspense_item_id = c_stale;
    SELECT COALESCE(SUM(amount), 0) INTO v_balance FROM suspense_transactions WHERE suspense_item_id = c_stale;
    SELECT COUNT(*) INTO v_count FROM suspense_transactions WHERE suspense_item_id = c_stale AND transaction_type = 'XP';
    IF v_version IS DISTINCT FROM 2 OR v_balance <> 75 OR v_count <> 0 THEN
        RAISE EXCEPTION 'TEST 02 FAILED: incomplete rollback';
    END IF;
    RAISE NOTICE 'TEST 02 PASS: Stale version P0001 and no XP persisted';
    v_test_count := v_test_count + 1;

    -- TEST 03: negative balance must be rejected (P4105).
    BEGIN
        CALL p_transfer_suspense_to_hmcts_other(c_negative, 1, 'opal-test', 'OPAL Test User');
        RAISE EXCEPTION 'TEST 03 FAILED: negative balance accepted';
    EXCEPTION WHEN SQLSTATE 'P4105' THEN
        GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
        IF v_detail NOT LIKE 'p_transfer_suspense_to_hmcts_other:%' THEN
            RAISE EXCEPTION 'TEST 03 FAILED: DETAIL missing';
        END IF;
    END;
    RAISE NOTICE 'TEST 03 PASS: Negative balance rejected (P4105)';
    v_test_count := v_test_count + 1;

    -- TEST 04: zero balance must now be rejected (changed from old PO-10349).
    BEGIN
        CALL p_transfer_suspense_to_hmcts_other(c_zero, 1, 'opal-test', 'OPAL Test User');
        RAISE EXCEPTION 'TEST 04 FAILED: zero balance accepted';
    EXCEPTION WHEN SQLSTATE 'P4105' THEN
        GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
        IF v_detail NOT LIKE 'p_transfer_suspense_to_hmcts_other:%' THEN
            RAISE EXCEPTION 'TEST 04 FAILED: DETAIL missing';
        END IF;
    END;
    RAISE NOTICE 'TEST 04 PASS: Zero balance rejected (P4105)';
    v_test_count := v_test_count + 1;

    -- TEST 05: null item ID validation.
    BEGIN
        CALL p_transfer_suspense_to_hmcts_other(NULL::BIGINT, 1, 'opal-test', 'OPAL Test User');
        RAISE EXCEPTION 'TEST 05 FAILED: NULL item ID accepted';
    EXCEPTION WHEN SQLSTATE 'P4101' THEN NULL;
    END;
    RAISE NOTICE 'TEST 05 PASS: NULL item ID rejected (P4101)';
    v_test_count := v_test_count + 1;

    -- TEST 06: null version validation.
    BEGIN
        CALL p_transfer_suspense_to_hmcts_other(c_stale, NULL::BIGINT, 'opal-test', 'OPAL Test User');
        RAISE EXCEPTION 'TEST 06 FAILED: NULL version accepted';
    EXCEPTION WHEN SQLSTATE 'P4102' THEN NULL;
    END;
    RAISE NOTICE 'TEST 06 PASS: NULL version rejected (P4102)';
    v_test_count := v_test_count + 1;

    -- TEST 07: missing posting user validation.
    BEGIN
        CALL p_transfer_suspense_to_hmcts_other(c_stale, 2, NULL::VARCHAR, 'OPAL Test User');
        RAISE EXCEPTION 'TEST 07 FAILED: NULL posted-by accepted';
    EXCEPTION WHEN SQLSTATE 'P4103' THEN NULL;
    END;
    RAISE NOTICE 'TEST 07 PASS: NULL posted-by rejected (P4103)';
    v_test_count := v_test_count + 1;

    -- TEST 08: missing posting name validation.
    BEGIN
        CALL p_transfer_suspense_to_hmcts_other(c_stale, 2, 'opal-test', ' '::VARCHAR);
        RAISE EXCEPTION 'TEST 08 FAILED: blank posted-by name accepted';
    EXCEPTION WHEN SQLSTATE 'P4104' THEN NULL;
    END;
    RAISE NOTICE 'TEST 08 PASS: Blank posted-by name rejected (P4104)';
    v_test_count := v_test_count + 1;

    -- TEST 09: missing CF creditor must raise P0002; verify rollback.
    SELECT COUNT(*) INTO v_initial_xfer FROM creditor_transactions WHERE transaction_type = 'XFER';
    BEGIN
        CALL p_transfer_suspense_to_hmcts_other(c_no_cf, 1, 'opal-test', 'OPAL Test User');
        RAISE EXCEPTION 'TEST 09 FAILED: missing CF accepted';
    EXCEPTION WHEN SQLSTATE 'P0002' THEN NULL;
    END;
    SELECT COUNT(*) INTO v_count FROM suspense_transactions
    WHERE suspense_item_id = c_no_cf AND transaction_type = 'XP';
    SELECT COUNT(*) INTO v_final_xfer FROM creditor_transactions WHERE transaction_type = 'XFER';
    SELECT version_number INTO v_version FROM suspense_items WHERE suspense_item_id = c_no_cf;
    SELECT COALESCE(SUM(amount), 0) INTO v_balance
    FROM suspense_transactions WHERE suspense_item_id = c_no_cf;
    IF v_count <> 0 OR v_initial_xfer <> v_final_xfer
       OR v_version IS DISTINCT FROM 1 OR v_balance <> 100 THEN
        RAISE EXCEPTION 'TEST 09 FAILED: missing-CF rollback verification';
    END IF;
    RAISE NOTICE 'TEST 09 PASS: Missing CF rejected (P0002); all attempted writes rolled back';
    v_test_count := v_test_count + 1;

    -- TEST 10: missing suspense item must raise explicit P4106 validation.
    BEGIN
        CALL p_transfer_suspense_to_hmcts_other(c_missing, 1, 'opal-test', 'OPAL Test User');
        RAISE EXCEPTION 'TEST 10 FAILED: nonexistent suspense item accepted';
    EXCEPTION WHEN SQLSTATE 'P4106' THEN NULL;
    END;
    RAISE NOTICE 'TEST 10 PASS: Missing suspense item rejected (P4106)';
    v_test_count := v_test_count + 1;

    -- TEST 11: ensure negative-balance failure has not generated an XP.
    SELECT COUNT(*) INTO v_count FROM suspense_transactions
    WHERE suspense_item_id = c_negative AND transaction_type = 'XP';
    SELECT COALESCE(SUM(amount), 0) INTO v_balance
    FROM suspense_transactions WHERE suspense_item_id = c_negative;
    IF v_count <> 0 OR v_balance <> -50 THEN
        RAISE EXCEPTION 'TEST 11 FAILED: negative-balance fixture altered';
    END IF;
    RAISE NOTICE 'TEST 11 PASS: Negative-balance fixture unchanged';
    v_test_count := v_test_count + 1;

    -- TEST 12: ensure zero-balance failure has not generated an XP.
    SELECT COUNT(*) INTO v_count FROM suspense_transactions
    WHERE suspense_item_id = c_zero AND transaction_type = 'XP';
    IF v_count <> 0 THEN RAISE EXCEPTION 'TEST 12 FAILED: zero-balance XP exists'; END IF;
    RAISE NOTICE 'TEST 12 PASS: Zero-balance fixture unchanged';
    v_test_count := v_test_count + 1;

    RAISE NOTICE 'PO-10349: % tests passed (all fixture changes will be rolled back)', v_test_count;
END;
$$;

ROLLBACK;
