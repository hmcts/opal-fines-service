/**
* OPAL Program
*
* MODULE      : p_update_defendant_accounts_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_update_defendant_accounts.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 14/10/2026    C Cho       1.0         PO-3447 Unit tests for p_update_defendant_accounts
*
**/
\timing

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data before tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM defendant_transactions
     WHERE defendant_account_id IN (
           999900003447, 999900003448, 999900003451, 999900003452, 999900003453
       );
    DELETE FROM defendant_accounts
     WHERE defendant_account_id IN (
           999900003447, 999900003448, 999900003451, 999900003452, 999900003453
       );
    DELETE FROM business_units
     WHERE business_unit_id = 9947;

    RAISE NOTICE 'Cleanup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test setup
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Setting up test data ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
    VALUES (9947, 'PO-3447 Unit Tests', 'U447', 'Accounting Division');

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type,
        last_movement_date, completed_date, version_number
    )
    VALUES
        (999900003447, 9947, 'UT3447-PARTIAL', -100.00, 0.00, -100.00, 'L', 'Fine', '2020-01-01', NULL, 4),
        (999900003448, 9947, 'UT3447-FULL', -100.00, 0.00, -100.00, 'L', 'Fine', '2020-01-01', NULL, 7),
        (999900003451, 9947, 'UT3447-VALIDATION', -100.00, 20.00, -80.00, 'L', 'Fine', '2020-01-01', NULL, 1),
        (999900003452, 9947, 'UT3447-POSITIVE', 10.00, 0.00, 10.00, 'L', 'Fine', '2020-01-01', NULL, 1),
        (999900003453, 9947, 'UT3447-ZERO', -100.00, 100.00, 0.00, 'L', 'Fine', '2020-01-01', '2020-02-01', 1);

    RAISE NOTICE 'Test data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: A partial payment updates the account
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account         defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: A partial payment updates the account ===';

    CALL p_update_defendant_accounts(999900003447::BIGINT, 25.50::NUMERIC);

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 999900003447;
    ASSERT v_account.amount_paid = 25.50, 'Partial payment must increase amount_paid';
    ASSERT v_account.account_balance = -74.50, 'Partial payment must reduce the outstanding balance';
    ASSERT v_account.completed_date IS NULL, 'A partial payment must not complete the account';
    ASSERT v_account.last_movement_date = CURRENT_TIMESTAMP::TIMESTAMP, 'last_movement_date must use the transaction timestamp';
    ASSERT v_account.version_number = 5, 'version_number must increase once';
    ASSERT v_account.amount_imposed = -100.00, 'amount_imposed must remain unchanged';

    RAISE NOTICE 'TEST 1 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 1 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 1 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Paying an account in full sets completed_date and last_movement_date
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account  defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: Paying an account in full sets completed_date and last_movement_date ===';

    CALL p_update_defendant_accounts(999900003448::BIGINT, 100.00::NUMERIC);
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 999900003448;
    ASSERT v_account.amount_paid = 100.00 AND v_account.account_balance = 0.00, 'Full payment must settle the account';
    ASSERT v_account.completed_date = CURRENT_TIMESTAMP::TIMESTAMP, 'Full payment must set completed_date';
    ASSERT v_account.last_movement_date = v_account.completed_date, 'Movement and completion timestamps must match';
    ASSERT v_account.version_number = 8, 'Full payment must increment version_number';

    RAISE NOTICE 'TEST 2 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 2 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 2 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: null account ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught    BOOLEAN := FALSE;
    v_detail          TEXT;
    v_account_id      defendant_accounts.defendant_account_id%TYPE := NULL::BIGINT;
    v_payment_amount  defendant_accounts.amount_paid%TYPE := 10.00::NUMERIC;
BEGIN
    RAISE NOTICE '=== TEST 3: null account ID ===';

    BEGIN
        CALL p_update_defendant_accounts(v_account_id, v_payment_amount);
    EXCEPTION
        WHEN SQLSTATE 'P3501' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3501 was not raised';
    ASSERT v_detail = 'p_update_defendant_accounts: pi_defendant_account_id is required', 'Validation DETAIL must be preserved';

    RAISE NOTICE 'TEST 3 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 3 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 3 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: null payment
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account         defendant_accounts%ROWTYPE;
    v_error_caught    BOOLEAN := FALSE;
    v_detail          TEXT;
    v_account_id      defendant_accounts.defendant_account_id%TYPE := 999900003451;
    v_payment_amount  defendant_accounts.amount_paid%TYPE := NULL;
BEGIN
    RAISE NOTICE '=== TEST 4: null payment ===';

    BEGIN
        CALL p_update_defendant_accounts(v_account_id, v_payment_amount);
    EXCEPTION
        WHEN SQLSTATE 'P3502' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3502 was not raised';
    ASSERT v_detail = 'p_update_defendant_accounts: pi_payment_amount is required', 'Validation DETAIL must be preserved';

    SELECT *
      INTO STRICT v_account
      FROM defendant_accounts
     WHERE defendant_account_id = v_account_id;

    ASSERT v_account.amount_paid = 20.00, 'Rejected payment must not change amount_paid';
    ASSERT v_account.account_balance = -80.00, 'Rejected payment must not change account_balance';
    ASSERT v_account.version_number = 1, 'Rejected payment must not change version_number';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected payment must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected payment must not change completed_date';

    RAISE NOTICE 'TEST 4 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 4 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 4 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: zero payment
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account         defendant_accounts%ROWTYPE;
    v_error_caught    BOOLEAN := FALSE;
    v_detail          TEXT;
    v_account_id      defendant_accounts.defendant_account_id%TYPE := 999900003451;
    v_payment_amount  defendant_accounts.amount_paid%TYPE := 0.00;
BEGIN
    RAISE NOTICE '=== TEST 5: zero payment ===';

    BEGIN
        CALL p_update_defendant_accounts(v_account_id, v_payment_amount);
    EXCEPTION
        WHEN SQLSTATE 'P3503' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3503 was not raised';
    ASSERT v_detail = 'p_update_defendant_accounts: pi_payment_amount must be a positive value', 'Validation DETAIL must be preserved';

    SELECT *
      INTO STRICT v_account
      FROM defendant_accounts
     WHERE defendant_account_id = v_account_id;

    ASSERT v_account.amount_paid = 20.00, 'Rejected payment must not change amount_paid';
    ASSERT v_account.account_balance = -80.00, 'Rejected payment must not change account_balance';
    ASSERT v_account.version_number = 1, 'Rejected payment must not change version_number';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected payment must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected payment must not change completed_date';

    RAISE NOTICE 'TEST 5 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 5 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 5 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: negative payment
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account         defendant_accounts%ROWTYPE;
    v_error_caught    BOOLEAN := FALSE;
    v_detail          TEXT;
    v_account_id      defendant_accounts.defendant_account_id%TYPE := 999900003451;
    v_payment_amount  defendant_accounts.amount_paid%TYPE := -0.01;
BEGIN
    RAISE NOTICE '=== TEST 6: negative payment ===';

    BEGIN
        CALL p_update_defendant_accounts(v_account_id, v_payment_amount);
    EXCEPTION
        WHEN SQLSTATE 'P3503' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3503 was not raised';
    ASSERT v_detail = 'p_update_defendant_accounts: pi_payment_amount must be a positive value', 'Validation DETAIL must be preserved';

    SELECT *
      INTO STRICT v_account
      FROM defendant_accounts
     WHERE defendant_account_id = v_account_id;

    ASSERT v_account.amount_paid = 20.00, 'Rejected payment must not change amount_paid';
    ASSERT v_account.account_balance = -80.00, 'Rejected payment must not change account_balance';
    ASSERT v_account.version_number = 1, 'Rejected payment must not change version_number';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected payment must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected payment must not change completed_date';

    RAISE NOTICE 'TEST 6 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 6 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 6 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: missing account
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught    BOOLEAN := FALSE;
    v_detail          TEXT;
    v_account_id      defendant_accounts.defendant_account_id%TYPE := 999900003459;
    v_payment_amount  defendant_accounts.amount_paid%TYPE := 10.00;
BEGIN
    RAISE NOTICE '=== TEST 7: missing account ===';

    BEGIN
        CALL p_update_defendant_accounts(v_account_id, v_payment_amount);
    EXCEPTION
        WHEN SQLSTATE 'P3504' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3504 was not raised';
    ASSERT v_detail = 'p_update_defendant_accounts: no defendant account exists for the passed pi_defendant_account_id', 'Validation DETAIL must be preserved';

    RAISE NOTICE 'TEST 7 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 7 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 7 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 8: positive balance
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account         defendant_accounts%ROWTYPE;
    v_error_caught    BOOLEAN := FALSE;
    v_detail          TEXT;
    v_account_id      defendant_accounts.defendant_account_id%TYPE := 999900003452;
    v_payment_amount  defendant_accounts.amount_paid%TYPE := 1.00;
BEGIN
    RAISE NOTICE '=== TEST 8: positive balance ===';

    BEGIN
        CALL p_update_defendant_accounts(v_account_id, v_payment_amount);
    EXCEPTION
        WHEN SQLSTATE 'P3505' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3505 was not raised';
    ASSERT v_detail = 'p_update_defendant_accounts: account_balance must be zero or negative before allocation', 'Validation DETAIL must be preserved';

    SELECT *
      INTO STRICT v_account
      FROM defendant_accounts
     WHERE defendant_account_id = v_account_id;

    ASSERT v_account.amount_paid = 0.00, 'Rejected payment must not change amount_paid';
    ASSERT v_account.account_balance = 10.00, 'Rejected payment must not change account_balance';
    ASSERT v_account.version_number = 1, 'Rejected payment must not change version_number';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected payment must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected payment must not change completed_date';

    RAISE NOTICE 'TEST 8 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 8 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 8 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 9: overpayment by one penny
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account         defendant_accounts%ROWTYPE;
    v_error_caught    BOOLEAN := FALSE;
    v_detail          TEXT;
    v_account_id      defendant_accounts.defendant_account_id%TYPE := 999900003451;
    v_payment_amount  defendant_accounts.amount_paid%TYPE := 80.01;
BEGIN
    RAISE NOTICE '=== TEST 9: overpayment by one penny ===';

    BEGIN
        CALL p_update_defendant_accounts(v_account_id, v_payment_amount);
    EXCEPTION
        WHEN SQLSTATE 'P3506' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3506 was not raised';
    ASSERT v_detail = 'p_update_defendant_accounts: overpayment must be routed to suspense by p_allocate_till', 'Validation DETAIL must be preserved';

    SELECT *
      INTO STRICT v_account
      FROM defendant_accounts
     WHERE defendant_account_id = v_account_id;

    ASSERT v_account.amount_paid = 20.00, 'Rejected payment must not change amount_paid';
    ASSERT v_account.account_balance = -80.00, 'Rejected payment must not change account_balance';
    ASSERT v_account.version_number = 1, 'Rejected payment must not change version_number';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected payment must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected payment must not change completed_date';

    RAISE NOTICE 'TEST 9 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 9 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 9 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 10: payment to a settled account
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account         defendant_accounts%ROWTYPE;
    v_error_caught    BOOLEAN := FALSE;
    v_detail          TEXT;
    v_account_id      defendant_accounts.defendant_account_id%TYPE := 999900003453;
    v_payment_amount  defendant_accounts.amount_paid%TYPE := 0.01;
BEGIN
    RAISE NOTICE '=== TEST 10: payment to a settled account ===';

    BEGIN
        CALL p_update_defendant_accounts(v_account_id, v_payment_amount);
    EXCEPTION
        WHEN SQLSTATE 'P3506' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3506 was not raised';
    ASSERT v_detail = 'p_update_defendant_accounts: overpayment must be routed to suspense by p_allocate_till', 'Validation DETAIL must be preserved';

    SELECT *
      INTO STRICT v_account
      FROM defendant_accounts
     WHERE defendant_account_id = v_account_id;

    ASSERT v_account.amount_paid = 100.00, 'Rejected payment must not change amount_paid';
    ASSERT v_account.account_balance = 0.00, 'Rejected payment must not change account_balance';
    ASSERT v_account.version_number = 1, 'Rejected payment must not change version_number';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected payment must not change last_movement_date';
    ASSERT v_account.completed_date = TIMESTAMP '2020-02-01', 'Rejected payment must not change completed_date';

    RAISE NOTICE 'TEST 10 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 10 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 10 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Cleanup test data
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM defendant_transactions
     WHERE defendant_account_id IN (
           999900003447, 999900003448, 999900003451, 999900003452, 999900003453
       );
    DELETE FROM defendant_accounts
     WHERE defendant_account_id IN (
           999900003447, 999900003448, 999900003451, 999900003452, 999900003453
       );
    DELETE FROM business_units
     WHERE business_unit_id = 9947;

    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing
