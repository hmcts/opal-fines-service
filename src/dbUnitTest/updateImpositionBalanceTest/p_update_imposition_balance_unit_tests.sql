/**
* CGI Opal Program
*
* MODULE      : p_update_imposition_balance_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_update_imposition_balance.
*               These tests cover validation, update logic, ETL NULL completed handling,
*               and error conditions for Admin Write Off imposition balance updates.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------
* 12/06/2026    P Brumby    1.0         Unit tests for p_update_imposition_balance.
*
**/

\timing

-- Cleanup data before tests
-- Note: business_unit_id is SMALLINT, so 9901-9910 is used for BU test data.
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM impositions WHERE imposition_id IN (999921, 999922, 999923, 999924, 999925, 999926, 999927, 999929, 999930);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999901, 999902, 999903, 999904, 999909, 999910);
    DELETE FROM creditor_accounts WHERE creditor_account_id IN (999911, 999912, 999913, 999914, 999919, 999920);
    DELETE FROM results WHERE result_id LIKE 'UT99%';
    DELETE FROM business_units WHERE business_unit_id BETWEEN 9901 AND 9910;

    RAISE NOTICE 'Data cleanup before tests completed';
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 1: Valid write off updates paid_amount and preserves completed = FALSE when resultant balance is non-zero
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_business_unit_id       smallint          := 9901;
    v_defendant_account_id   bigint            := 999901;
    v_creditor_account_id    bigint            := 999911;
    v_result_id              character varying := 'UT9901';
    v_imposition_id          bigint            := 999921;
    v_write_off_amount       numeric(18,2)     := 30.00;
BEGIN
    RAISE NOTICE '=== TEST 1: Valid write off updates paid_amount and preserves completed = FALSE when resultant balance is non-zero ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_type)
    VALUES (v_business_unit_id, 'UT Business Unit 9901', 'Area');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    ) VALUES (
        v_creditor_account_id, v_business_unit_id, 'UTCA999911', 'CF',
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'UTDA999901', -100.00,
        20.00, -80.00, 'L', 'Fine'
    );

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    ) VALUES (
        v_result_id, 'Unit Test Result 1', 'Result', TRUE, TRUE, 'Fines',
        1, FALSE, 'CF',
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    ) VALUES (
        v_imposition_id, v_defendant_account_id, CURRENT_TIMESTAMP, v_result_id,
        -100.00, 20.00, v_creditor_account_id, FALSE
    );

    CALL p_update_imposition_balance(v_imposition_id, v_write_off_amount);

    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = v_imposition_id) = 50.00,
           'paid_amount should be incremented from 20.00 to 50.00';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = v_imposition_id) = FALSE,
           'completed should remain FALSE when resultant balance is non-zero';

    RAISE NOTICE 'TEST 1 PASSED';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 1 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 2: Valid write off updates paid_amount and sets completed = TRUE when resultant balance is zero
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_business_unit_id       smallint          := 9902;
    v_defendant_account_id   bigint            := 999902;
    v_creditor_account_id    bigint            := 999912;
    v_result_id              character varying := 'UT9902';
    v_imposition_id          bigint            := 999922;
    v_write_off_amount       numeric(18,2)     := 80.00;
BEGIN
    RAISE NOTICE '=== TEST 2: Valid write off updates paid_amount and sets completed = TRUE when resultant balance is zero ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_type)
    VALUES (v_business_unit_id, 'UT Business Unit 9902', 'Area');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    ) VALUES (
        v_creditor_account_id, v_business_unit_id, 'UTCA999912', 'CF',
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'UTDA999902', -100.00,
        20.00, -80.00, 'L', 'Fine'
    );

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    ) VALUES (
        v_result_id, 'Unit Test Result 2', 'Result', TRUE, TRUE, 'Fines',
        1, FALSE, 'CF',
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    ) VALUES (
        v_imposition_id, v_defendant_account_id, CURRENT_TIMESTAMP, v_result_id,
        -100.00, 20.00, v_creditor_account_id, FALSE
    );

    CALL p_update_imposition_balance(v_imposition_id, v_write_off_amount);

    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = v_imposition_id) = 100.00,
           'paid_amount should be incremented from 20.00 to 100.00';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = v_imposition_id) = TRUE,
           'completed should be TRUE when resultant balance is zero';

    RAISE NOTICE 'TEST 2 PASSED';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 2 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 3: ETL behaviour preserves completed = NULL when resultant balance is non-zero
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_business_unit_id       smallint          := 9903;
    v_defendant_account_id   bigint            := 999903;
    v_creditor_account_id    bigint            := 999913;
    v_result_id              character varying := 'UT9903';
    v_imposition_id          bigint            := 999923;
    v_write_off_amount       numeric(18,2)     := 30.00;
BEGIN
    RAISE NOTICE '=== TEST 3: ETL behaviour preserves completed = NULL when resultant balance is non-zero ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_type)
    VALUES (v_business_unit_id, 'UT Business Unit 9903', 'Area');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    ) VALUES (
        v_creditor_account_id, v_business_unit_id, 'UTCA999913', 'CF',
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'UTDA999903', -100.00,
        20.00, -80.00, 'L', 'Fine'
    );

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    ) VALUES (
        v_result_id, 'Unit Test Result 3', 'Result', TRUE, TRUE, 'Fines',
        1, FALSE, 'CF',
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    ) VALUES (
        v_imposition_id, v_defendant_account_id, CURRENT_TIMESTAMP, v_result_id,
        -100.00, 20.00, v_creditor_account_id, NULL
    );

    CALL p_update_imposition_balance(v_imposition_id, v_write_off_amount);

    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = v_imposition_id) = 50.00,
           'paid_amount should be incremented from 20.00 to 50.00';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = v_imposition_id) IS NULL,
           'completed should remain NULL when resultant balance is non-zero';

    RAISE NOTICE 'TEST 3 PASSED';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 3 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 4: ETL behaviour sets completed = TRUE when resultant balance is zero
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_business_unit_id       smallint          := 9904;
    v_defendant_account_id   bigint            := 999904;
    v_creditor_account_id    bigint            := 999914;
    v_result_id              character varying := 'UT9904';
    v_imposition_id          bigint            := 999924;
    v_write_off_amount       numeric(18,2)     := 80.00;
BEGIN
    RAISE NOTICE '=== TEST 4: ETL behaviour sets completed = TRUE when resultant balance is zero ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_type)
    VALUES (v_business_unit_id, 'UT Business Unit 9904', 'Area');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    ) VALUES (
        v_creditor_account_id, v_business_unit_id, 'UTCA999914', 'CF',
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'UTDA999904', -100.00,
        20.00, -80.00, 'L', 'Fine'
    );

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    ) VALUES (
        v_result_id, 'Unit Test Result 4', 'Result', TRUE, TRUE, 'Fines',
        1, FALSE, 'CF',
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    ) VALUES (
        v_imposition_id, v_defendant_account_id, CURRENT_TIMESTAMP, v_result_id,
        -100.00, 20.00, v_creditor_account_id, NULL
    );

    CALL p_update_imposition_balance(v_imposition_id, v_write_off_amount);

    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = v_imposition_id) = 100.00,
           'paid_amount should be incremented from 20.00 to 100.00';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = v_imposition_id) = TRUE,
           'completed should be TRUE when resultant balance is zero';

    RAISE NOTICE 'TEST 4 PASSED';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 4 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 5: Null imposition_id raises P3101
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught boolean := FALSE;
BEGIN
    RAISE NOTICE '=== TEST 5: Null imposition_id raises P3101 ===';

    BEGIN
        CALL p_update_imposition_balance(NULL, 10.00);
    EXCEPTION
        WHEN SQLSTATE 'P3101' THEN
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3101 was not raised';

    RAISE NOTICE 'TEST 5 PASSED';
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 6: Null write_off_amount raises P3102
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught boolean := FALSE;
BEGIN
    RAISE NOTICE '=== TEST 6: Null write_off_amount raises P3102 ===';

    BEGIN
        CALL p_update_imposition_balance(999925, NULL);
    EXCEPTION
        WHEN SQLSTATE 'P3102' THEN
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3102 was not raised';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 7: Non-positive write_off_amount raises P3103
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught boolean := FALSE;
BEGIN
    RAISE NOTICE '=== TEST 7: Non-positive write_off_amount raises P3103 ===';

    BEGIN
        CALL p_update_imposition_balance(999926, 0.00);
    EXCEPTION
        WHEN SQLSTATE 'P3103' THEN
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3103 was not raised';

    RAISE NOTICE 'TEST 7 PASSED';
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 8: Unknown imposition_id raises P3104
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught boolean := FALSE;
BEGIN
    RAISE NOTICE '=== TEST 8: Unknown imposition_id raises P3104 ===';

    BEGIN
        CALL p_update_imposition_balance(999927, 10.00);
    EXCEPTION
        WHEN SQLSTATE 'P3104' THEN
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3104 was not raised';

    RAISE NOTICE 'TEST 8 PASSED';
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 9: Completed imposition raises P3105
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_business_unit_id       smallint          := 9909;
    v_defendant_account_id   bigint            := 999909;
    v_creditor_account_id    bigint            := 999919;
    v_result_id              character varying := 'UT9909';
    v_imposition_id          bigint            := 999929;
    v_error_caught           boolean           := FALSE;
BEGIN
    RAISE NOTICE '=== TEST 9: Completed imposition raises P3105 ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_type)
    VALUES (v_business_unit_id, 'UT Business Unit 9909', 'Area');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    ) VALUES (
        v_creditor_account_id, v_business_unit_id, 'UTCA999919', 'CF',
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'UTDA999909', -100.00,
        100.00, 0.00, 'L', 'Fine'
    );

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    ) VALUES (
        v_result_id, 'Unit Test Result 9', 'Result', TRUE, TRUE, 'Fines',
        1, FALSE, 'CF',
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    ) VALUES (
        v_imposition_id, v_defendant_account_id, CURRENT_TIMESTAMP, v_result_id,
        -100.00, 100.00, v_creditor_account_id, TRUE
    );

    BEGIN
        CALL p_update_imposition_balance(v_imposition_id, 10.00);
    EXCEPTION
        WHEN SQLSTATE 'P3105' THEN
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3105 was not raised';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = v_imposition_id) = 100.00,
           'paid_amount should remain unchanged after P3105 validation failure';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = v_imposition_id) = TRUE,
           'completed should remain unchanged after P3105 validation failure';

    RAISE NOTICE 'TEST 9 PASSED';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 9 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

-----------------------------------------------------------------------------------------------------------------------------------------------------
-- Test 10: write_off_amount exceeding outstanding balance raises P3106 and does not update the row
-----------------------------------------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_business_unit_id       smallint          := 9910;
    v_defendant_account_id   bigint            := 999910;
    v_creditor_account_id    bigint            := 999920;
    v_result_id              character varying := 'UT9910';
    v_imposition_id          bigint            := 999930;
    v_error_caught           boolean           := FALSE;
BEGIN
    RAISE NOTICE '=== TEST 10: write_off_amount exceeding outstanding balance raises P3106 and does not update the row ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_type)
    VALUES (v_business_unit_id, 'UT Business Unit 9910', 'Area');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    ) VALUES (
        v_creditor_account_id, v_business_unit_id, 'UTCA999920', 'CF',
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'UTDA999910', -100.00,
        20.00, -80.00, 'L', 'Fine'
    );

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    ) VALUES (
        v_result_id, 'Unit Test Result 10', 'Result', TRUE, TRUE, 'Fines',
        1, FALSE, 'CF',
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    ) VALUES (
        v_imposition_id, v_defendant_account_id, CURRENT_TIMESTAMP, v_result_id,
        -100.00, 20.00, v_creditor_account_id, FALSE
    );

    BEGIN
        CALL p_update_imposition_balance(v_imposition_id, 81.00);
    EXCEPTION
        WHEN SQLSTATE 'P3106' THEN
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3106 was not raised';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = v_imposition_id) = 20.00,
           'paid_amount should remain unchanged after P3106 validation failure';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = v_imposition_id) = FALSE,
           'completed should remain unchanged after P3106 validation failure';

    RAISE NOTICE 'TEST 10 PASSED';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 10 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

-- Cleanup data after tests
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data after tests ===';

    DELETE FROM impositions WHERE imposition_id IN (999921, 999922, 999923, 999924, 999925, 999926, 999927, 999929, 999930);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999901, 999902, 999903, 999904, 999909, 999910);
    DELETE FROM creditor_accounts WHERE creditor_account_id IN (999911, 999912, 999913, 999914, 999919, 999920);
    DELETE FROM results WHERE result_id LIKE 'UT99%';
    DELETE FROM business_units WHERE business_unit_id BETWEEN 9901 AND 9910;

    RAISE NOTICE 'Data cleanup after tests completed';
END $$;

\timing
