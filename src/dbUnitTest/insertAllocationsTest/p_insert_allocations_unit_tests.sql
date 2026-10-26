/**
* OPAL Program
*
* MODULE      : p_insert_allocations_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_allocations.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 16/10/2026    C Cho       1.0         PO-3449 Unit tests for p_insert_allocations
*
**/
\timing

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data before tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM allocations WHERE imposition_id = 999900003449;
    DELETE FROM defendant_transactions WHERE defendant_transaction_id = 999900003449;
    DELETE FROM impositions WHERE imposition_id IN (999900003449, 999900013449);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999900003449, 999900013449);
    DELETE FROM creditor_accounts WHERE creditor_account_id = 999900003449;
    DELETE FROM results WHERE result_id = 'UT3449';
    DELETE FROM business_units WHERE business_unit_id = 9949;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test setup
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Test setup ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
    VALUES (9949, 'PO-3449 Unit Tests', 'U449', 'Accounting Division');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    )
    VALUES (999900003449, 9949, 'UT3449-CREDITOR', 'CF', FALSE, FALSE, FALSE, FALSE);

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type, version_number
    )
    VALUES (999900003449, 9949, 'UT3449-DEFENDANT', 100.00, 20.00, 80.00, 'L', 'Fine', 1);

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    )
    VALUES (
        'UT3449', 'PO-3449 Unit Test Result', 'Result', TRUE, TRUE, NULL,
        1, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES (999900003449, 999900003449, CURRENT_TIMESTAMP, 'UT3449',
            100.00, 20.00, 999900003449, FALSE);

    INSERT INTO defendant_transactions (
        defendant_transaction_id, defendant_account_id, posted_date, transaction_type, transaction_amount
    )
    VALUES (999900003449, 999900003449, CURRENT_TIMESTAMP, 'PAYMNT', 25.50);
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Insert and verify all fields
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_allocation allocations%ROWTYPE;
    v_before     TIMESTAMP;
    v_after      TIMESTAMP;
BEGIN
    RAISE NOTICE '=== Test 1: Insert and verify all fields ===';

    v_before := CLOCK_TIMESTAMP();
    CALL p_insert_allocations(
        pi_imposition_id := 999900003449::BIGINT,
        pi_imposition_allocation_amount := 25.50::NUMERIC,
        pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
        pi_allocation_function := 'p_allocate_payments'::VARCHAR,
        pi_defendant_transaction_id := 999900003449::BIGINT
    );
    v_after := CLOCK_TIMESTAMP();

    SELECT * INTO STRICT v_allocation
      FROM allocations
     WHERE imposition_id = 999900003449
       AND allocated_amount = 25.50;

    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 1,
        'Each call should insert exactly one row';
    ASSERT v_allocation.allocation_id IS NOT NULL, 'An allocation ID should be generated';
    ASSERT v_allocation.imposition_id = 999900003449, 'imposition_id should match';
    ASSERT v_allocation.allocated_amount = 25.50, 'allocated_amount should match';
    ASSERT v_allocation.transaction_type = 'PAYMNT'::t_allocation_transaction_type_enum, 'transaction_type should match';
    ASSERT v_allocation.allocation_function = 'p_allocate_payments'::VARCHAR, 'allocation_function should match';
    ASSERT v_allocation.defendant_transaction_id = 999900003449, 'defendant_transaction_id should match';
    ASSERT v_allocation.allocated_date BETWEEN v_before AND v_after, 'allocated_date should be generated during the call';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Insert a second allocation with a different type and function
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_allocation allocations%ROWTYPE;
    v_before     TIMESTAMP;
    v_after      TIMESTAMP;
BEGIN
    RAISE NOTICE '=== Test 2: Insert a second allocation with a different type and function ===';

    v_before := CLOCK_TIMESTAMP();
    CALL p_insert_allocations(
        pi_imposition_id := 999900003449::BIGINT,
        pi_imposition_allocation_amount := 0.01::NUMERIC,
        pi_transaction_type := 'FR-SUS'::t_allocation_transaction_type_enum,
        pi_allocation_function := 'p_allocate_suspense_payment'::VARCHAR,
        pi_defendant_transaction_id := 999900003449::BIGINT
    );
    v_after := CLOCK_TIMESTAMP();

    SELECT * INTO STRICT v_allocation
      FROM allocations
     WHERE imposition_id = 999900003449
       AND allocated_amount = 0.01;

    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Each call should insert exactly one row';
    ASSERT v_allocation.allocation_id IS NOT NULL, 'An allocation ID should be generated';
    ASSERT v_allocation.imposition_id = 999900003449, 'imposition_id should match';
    ASSERT v_allocation.allocated_amount = 0.01, 'allocated_amount should match';
    ASSERT v_allocation.transaction_type = 'FR-SUS'::t_allocation_transaction_type_enum, 'transaction_type should match';
    ASSERT v_allocation.allocation_function = 'p_allocate_suspense_payment'::VARCHAR, 'allocation_function should match';
    ASSERT v_allocation.defendant_transaction_id = 999900003449, 'defendant_transaction_id should match';
    ASSERT v_allocation.allocated_date BETWEEN v_before AND v_after, 'allocated_date should be generated during the call';

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
        CALL p_insert_allocations(
            pi_imposition_id := NULL::BIGINT,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_defendant_transaction_id := 999900003449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3701' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3701 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

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
        CALL p_insert_allocations(
            pi_imposition_id := 999900003449::BIGINT,
            pi_imposition_allocation_amount := NULL::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_defendant_transaction_id := 999900003449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3702' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3702 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

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
        CALL p_insert_allocations(
            pi_imposition_id := 999900003449::BIGINT,
            pi_imposition_allocation_amount := 0.00::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_defendant_transaction_id := 999900003449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3702' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3702 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

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
        CALL p_insert_allocations(
            pi_imposition_id := 999900003449::BIGINT,
            pi_imposition_allocation_amount := -1.00::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_defendant_transaction_id := 999900003449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3702' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3702 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Reject a NULL transaction type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 7: Reject a NULL transaction type ===';

    BEGIN
        CALL p_insert_allocations(
            pi_imposition_id := 999900003449::BIGINT,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_transaction_type := NULL::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_defendant_transaction_id := 999900003449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3703' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3703 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 7 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 8: Reject a NULL allocation function
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 8: Reject a NULL allocation function ===';

    BEGIN
        CALL p_insert_allocations(
            pi_imposition_id := 999900003449::BIGINT,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := NULL::VARCHAR,
            pi_defendant_transaction_id := 999900003449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3704' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3704 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 8 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 9: Reject an empty allocation function
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 9: Reject an empty allocation function ===';

    BEGIN
        CALL p_insert_allocations(
            pi_imposition_id := 999900003449::BIGINT,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := ''::VARCHAR,
            pi_defendant_transaction_id := 999900003449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3704' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3704 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 9 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 10: Reject a blank allocation function
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 10: Reject a blank allocation function ===';

    BEGIN
        CALL p_insert_allocations(
            pi_imposition_id := 999900003449::BIGINT,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := '   '::VARCHAR,
            pi_defendant_transaction_id := 999900003449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3704' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3704 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 10 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 11: Reject a NULL defendant transaction ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 11: Reject a NULL defendant transaction ID ===';

    BEGIN
        CALL p_insert_allocations(
            pi_imposition_id := 999900003449::BIGINT,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_defendant_transaction_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3705' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3705 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 11 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 12: Reject an unknown imposition
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 12: Reject an unknown imposition ===';

    BEGIN
        CALL p_insert_allocations(
            pi_imposition_id := -3449::BIGINT,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_defendant_transaction_id := 999900003449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3706' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3706 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 12 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 13: Reject an unknown defendant transaction
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 13: Reject an unknown defendant transaction ===';

    BEGIN
        CALL p_insert_allocations(
            pi_imposition_id := 999900003449::BIGINT,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_defendant_transaction_id := -3449::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3707' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3707 was not raised';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE imposition_id = 999900003449) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 13 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data after tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data after tests ===';

    DELETE FROM allocations WHERE imposition_id = 999900003449;
    DELETE FROM defendant_transactions WHERE defendant_transaction_id = 999900003449;
    DELETE FROM impositions WHERE imposition_id IN (999900003449, 999900013449);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999900003449, 999900013449);
    DELETE FROM creditor_accounts WHERE creditor_account_id = 999900003449;
    DELETE FROM results WHERE result_id = 'UT3449';
    DELETE FROM business_units WHERE business_unit_id = 9949;
END $$;

\timing
