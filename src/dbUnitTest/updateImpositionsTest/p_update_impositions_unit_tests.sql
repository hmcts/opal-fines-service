/**
* OPAL Program
*
* MODULE      : p_update_impositions_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_update_impositions.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 15/10/2026    C Cho       1.0         PO-3448 Unit tests for p_update_impositions
*
**/
\timing

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data before tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM impositions WHERE imposition_id IN (999900003448, 999900013448);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999900003448, 999900013448);
    DELETE FROM creditor_accounts WHERE creditor_account_id = 999900003448;
    DELETE FROM results WHERE result_id = 'UT3448';
    DELETE FROM business_units WHERE business_unit_id = 9948;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test setup
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Test setup ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
    VALUES (9948, 'PO-3448 Unit Tests', 'U448', 'Accounting Division');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    )
    VALUES (999900003448, 9948, 'UT3448-CREDITOR', 'CF', FALSE, FALSE, FALSE, FALSE);

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type, version_number
    )
    VALUES (999900003448, 9948, 'UT3448-DEFENDANT', 100.00, 20.00, 80.00, 'L', 'Fine', 1);

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    )
    VALUES (
        'UT3448', 'PO-3448 Unit Test Result', 'Result', TRUE, TRUE, NULL,
        1, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES (999900003448, 999900003448, CURRENT_TIMESTAMP, 'UT3448',
            100.00, 20.00, 999900003448, FALSE);

    -- A second account and imposition must not be affected by the update.
    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type, version_number
    )
    VALUES (999900013448, 9948, 'UT3448-OTHER', 60.00, 10.00, 50.00, 'L', 'Fine', 1);

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES (999900013448, 999900013448, CURRENT_TIMESTAMP, 'UT3448',
            60.00, 10.00, 999900003448, FALSE);
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Update the imposition and defendant account
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_imposition impositions%ROWTYPE;
    v_account    defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== Test 1: Update the imposition and defendant account ===';

    CALL p_update_impositions(
        pi_imposition_id := 999900003448::BIGINT,
        pi_imposition_allocation_amount := 25.50::NUMERIC
    );

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900003448;
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 999900003448;

    ASSERT v_imposition.paid_amount = 45.50, 'Imposition paid_amount should be 45.50';
    ASSERT v_imposition.imposed_amount = 100.00, 'Imposition imposed_amount should remain unchanged';
    ASSERT v_imposition.completed = FALSE, 'completed should remain unchanged';
    ASSERT v_account.amount_paid = 45.50, 'Account amount_paid should be 45.50';
    ASSERT v_account.account_balance = 54.50, 'Account balance should be 54.50';
    ASSERT v_account.amount_imposed = 100.00, 'Account amount_imposed should remain unchanged';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900013448) = 10.00,
        'The other imposition should remain unchanged';
    ASSERT (SELECT amount_paid FROM defendant_accounts WHERE defendant_account_id = 999900013448) = 10.00,
        'The other account amount_paid should remain unchanged';
    ASSERT (SELECT account_balance FROM defendant_accounts WHERE defendant_account_id = 999900013448) = 50.00,
        'The other account balance should remain unchanged';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Add a second payment of one penny
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_imposition impositions%ROWTYPE;
    v_account    defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== Test 2: Add a second payment of one penny ===';

    CALL p_update_impositions(
        pi_imposition_id := 999900003448::BIGINT,
        pi_imposition_allocation_amount := 0.01::NUMERIC
    );

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900003448;
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 999900003448;

    ASSERT v_imposition.paid_amount = 45.51, 'Imposition paid_amount should be 45.51';
    ASSERT v_imposition.imposed_amount = 100.00, 'Imposition imposed_amount should remain unchanged';
    ASSERT v_imposition.completed = FALSE, 'completed should remain unchanged';
    ASSERT v_account.amount_paid = 45.51, 'Account amount_paid should be 45.51';
    ASSERT v_account.account_balance = 54.49, 'Account balance should be 54.49';
    ASSERT v_account.amount_imposed = 100.00, 'Account amount_imposed should remain unchanged';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900013448) = 10.00,
        'The other imposition should remain unchanged';
    ASSERT (SELECT amount_paid FROM defendant_accounts WHERE defendant_account_id = 999900013448) = 10.00,
        'The other account amount_paid should remain unchanged';
    ASSERT (SELECT account_balance FROM defendant_accounts WHERE defendant_account_id = 999900013448) = 50.00,
        'The other account balance should remain unchanged';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Reject a NULL imposition ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 3: Reject a NULL imposition ID ===';

    BEGIN
        CALL p_update_impositions(
            pi_imposition_id := NULL::BIGINT,
            pi_imposition_allocation_amount := 10.00::NUMERIC
        );
    EXCEPTION
        WHEN SQLSTATE 'P3601' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3601 was not raised';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003448) = 45.51,
        'Invalid calls should not change imposition paid_amount';
    ASSERT (SELECT amount_paid FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 45.51,
        'Invalid calls should not change account amount_paid';
    ASSERT (SELECT account_balance FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 54.49,
        'Invalid calls should not change account_balance';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Reject a NULL amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 4: Reject a NULL amount ===';

    BEGIN
        CALL p_update_impositions(
            pi_imposition_id := 999900003448::BIGINT,
            pi_imposition_allocation_amount := NULL::NUMERIC
        );
    EXCEPTION
        WHEN SQLSTATE 'P3602' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3602 was not raised';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003448) = 45.51,
        'Invalid calls should not change imposition paid_amount';
    ASSERT (SELECT amount_paid FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 45.51,
        'Invalid calls should not change account amount_paid';
    ASSERT (SELECT account_balance FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 54.49,
        'Invalid calls should not change account_balance';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Reject a zero amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 5: Reject a zero amount ===';

    BEGIN
        CALL p_update_impositions(
            pi_imposition_id := 999900003448::BIGINT,
            pi_imposition_allocation_amount := 0.00::NUMERIC
        );
    EXCEPTION
        WHEN SQLSTATE 'P3602' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3602 was not raised';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003448) = 45.51,
        'Invalid calls should not change imposition paid_amount';
    ASSERT (SELECT amount_paid FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 45.51,
        'Invalid calls should not change account amount_paid';
    ASSERT (SELECT account_balance FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 54.49,
        'Invalid calls should not change account_balance';

    RAISE NOTICE 'TEST 5 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Reject a negative amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 6: Reject a negative amount ===';

    BEGIN
        CALL p_update_impositions(
            pi_imposition_id := 999900003448::BIGINT,
            pi_imposition_allocation_amount := -1.00::NUMERIC
        );
    EXCEPTION
        WHEN SQLSTATE 'P3602' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3602 was not raised';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003448) = 45.51,
        'Invalid calls should not change imposition paid_amount';
    ASSERT (SELECT amount_paid FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 45.51,
        'Invalid calls should not change account amount_paid';
    ASSERT (SELECT account_balance FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 54.49,
        'Invalid calls should not change account_balance';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Reject an unknown imposition
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 7: Reject an unknown imposition ===';

    BEGIN
        CALL p_update_impositions(
            pi_imposition_id := -3448::BIGINT,
            pi_imposition_allocation_amount := 10.00::NUMERIC
        );
    EXCEPTION
        WHEN SQLSTATE 'P3603' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3603 was not raised';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003448) = 45.51,
        'Invalid calls should not change imposition paid_amount';
    ASSERT (SELECT amount_paid FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 45.51,
        'Invalid calls should not change account amount_paid';
    ASSERT (SELECT account_balance FROM defendant_accounts WHERE defendant_account_id = 999900003448) = 54.49,
        'Invalid calls should not change account_balance';

    RAISE NOTICE 'TEST 7 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data after tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data after tests ===';

    DELETE FROM impositions WHERE imposition_id IN (999900003448, 999900013448);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999900003448, 999900013448);
    DELETE FROM creditor_accounts WHERE creditor_account_id = 999900003448;
    DELETE FROM results WHERE result_id = 'UT3448';
    DELETE FROM business_units WHERE business_unit_id = 9948;
END $$;

\timing
