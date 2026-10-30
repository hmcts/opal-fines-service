/**
* OPAL Program
*
* MODULE      : p_allocate_payments_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_allocate_payments.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 27/10/2026    C Cho       1.0         PO-3452 Unit tests for p_allocate_payments
*
**/
\timing

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data before tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM allocations WHERE imposition_id IN (
        SELECT imposition_id FROM impositions WHERE defendant_account_id IN (
               999900003452, 999900013452, 999900023452, 999900033452, 999900043452,
               999900053452, 999900063452, 999900073452, 999900083452, 999900093452
           )
    );
    DELETE FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);
    DELETE FROM defendant_transactions WHERE defendant_account_id IN (
               999900003452, 999900013452, 999900023452, 999900033452, 999900043452,
               999900053452, 999900063452, 999900073452, 999900083452, 999900093452
           );
    DELETE FROM impositions WHERE defendant_account_id IN (
               999900003452, 999900013452, 999900023452, 999900033452, 999900043452,
               999900053452, 999900063452, 999900073452, 999900083452, 999900093452
           );
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (
               999900003452, 999900013452, 999900023452, 999900033452, 999900043452,
               999900053452, 999900063452, 999900073452, 999900083452, 999900093452
           );
    DELETE FROM creditor_accounts WHERE creditor_account_id IN (999900003452, 999900003453);
    DELETE FROM results WHERE result_id IN (
        'U45201', 'U45202', 'U45203', 'U45204', 'U45205', 'U45206',
        'U45207', 'U45208', 'U45209', 'U45200', 'U45210', 'U452NA'
    );
    DELETE FROM business_units WHERE business_unit_id = 9952;

    RAISE NOTICE 'Cleanup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test setup
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Setting up test data ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
    VALUES (9952, 'PO-3452 Unit Tests', 'U452', 'Accounting Division');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    )
    VALUES
        (999900003452, 9952, 'UT3452-CREDITOR-1', 'CF', FALSE, FALSE, FALSE, FALSE),
        (999900003453, 9952, 'UT3452-CREDITOR-2', 'CF', FALSE, FALSE, FALSE, FALSE);

    -- Each payment scenario has its own account and existing defendant transaction.
    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type, version_number
    )
    VALUES
        (999900003452, 9952, 'UT3452-PARTIAL', -190.00, 20.00, -170.00, 'L', 'Fine', 1),
        (999900013452, 9952, 'UT3452-ROUNDING', -3.00, 0.00, -3.00, 'L', 'Fine', 1),
        (999900023452, 9952, 'UT3452-PENNY', -6.00, 0.00, -6.00, 'L', 'Fine', 1),
        (999900033452, 9952, 'UT3452-ORDER', -120.00, 0.00, -120.00, 'L', 'Fine', 1),
        (999900043452, 9952, 'UT3452-EXCESS', -90.00, 0.00, -90.00, 'L', 'Fine', 1),
        (999900053452, 9952, 'UT3452-INELIGIBLE', -30.00, 10.00, -20.00, 'L', 'Fine', 1),
        (999900063452, 9952, 'UT3452-EMPTY', 0.00, 0.00, 0.00, 'L', 'Fine', 1),
        (999900073452, 9952, 'UT3452-ROLLBACK', -100.00, 20.00, -80.00, 'L', 'Fine', 1),
        (999900083452, 9952, 'UT3452-VALIDATION', -100.00, 20.00, -80.00, 'L', 'Fine', 1),
        (999900093452, 9952, 'UT3452-OTHER', -100.00, 20.00, -80.00, 'L', 'Fine', 1);

    INSERT INTO defendant_transactions (
        defendant_transaction_id, defendant_account_id, posted_date, transaction_type, transaction_amount
    )
    VALUES
        (999900003452, 999900003452, TIMESTAMP '2020-01-01', 'PAYMNT', 30.00),
        (999900013452, 999900013452, TIMESTAMP '2020-01-01', 'PAYMNT', 1.00),
        (999900023452, 999900023452, TIMESTAMP '2020-01-01', 'PAYMNT', 0.01),
        (999900033452, 999900033452, TIMESTAMP '2020-01-01', 'PAYMNT', 65.00),
        (999900043452, 999900043452, TIMESTAMP '2020-01-01', 'PAYMNT', 100.00),
        (999900053452, 999900053452, TIMESTAMP '2020-01-01', 'PAYMNT', 20.00),
        (999900063452, 999900063452, TIMESTAMP '2020-01-01', 'PAYMNT', 20.00),
        (999900073452, 999900073452, TIMESTAMP '2020-01-01', 'PAYMNT', 25.00),
        (999900083452, 999900083452, TIMESTAMP '2020-01-01', 'PAYMNT', 25.00),
        (999900093452, 999900093452, TIMESTAMP '2020-01-01', 'PAYMNT', 100.00);

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    )
    VALUES
        ('U45201', 'PO-3452 Priority 01', 'Result', TRUE, TRUE, NULL,
         1, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45202', 'PO-3452 Priority 02', 'Result', TRUE, TRUE, NULL,
         2, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45203', 'PO-3452 Priority 03', 'Result', TRUE, TRUE, NULL,
         3, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45204', 'PO-3452 Priority 04', 'Result', TRUE, TRUE, NULL,
         4, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45205', 'PO-3452 Priority 05', 'Result', TRUE, TRUE, NULL,
         5, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45206', 'PO-3452 Priority 06', 'Result', TRUE, TRUE, NULL,
         6, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45207', 'PO-3452 Priority 07', 'Result', TRUE, TRUE, NULL,
         7, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45208', 'PO-3452 Priority 08', 'Result', TRUE, TRUE, NULL,
         8, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45209', 'PO-3452 Priority 09', 'Result', TRUE, TRUE, NULL,
         9, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45200', 'PO-3452 Priority 00', 'Result', TRUE, TRUE, NULL,
         0, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U45210', 'PO-3452 Priority 10', 'Result', TRUE, TRUE, NULL,
         10, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U452NA', 'PO-3452 Priority NA', 'Result', TRUE, TRUE, NULL,
         NULL, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE);

    -- These impositions verify validation leaves data unchanged and other accounts are untouched.
    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES
        (999900004253, 999900083452, TIMESTAMP '2020-01-01', 'U45201', -100.00, 20.00, 999900003452, FALSE),
        (999900004353, 999900093452, TIMESTAMP '2020-01-01', 'U45201', -100.00, 20.00, 999900003452, FALSE);

    RAISE NOTICE 'Test data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Allocate proportionally using outstanding balances and verify fields
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_expected   RECORD;
    v_allocation allocations%ROWTYPE;
    v_creditor   creditor_transactions%ROWTYPE;
    v_imposition impositions%ROWTYPE;
    v_before     TIMESTAMP;
    v_after      TIMESTAMP;
BEGIN
    RAISE NOTICE '=== TEST 1: Allocate proportionally using outstanding balances and verify fields ===';

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES
        (999900003453, 999900003452, TIMESTAMP '2020-01-01', 'U45201', -100, 20, 999900003452, FALSE),
        (999900003454, 999900003452, TIMESTAMP '2020-01-01', 'U45201', -40, 0, 999900003453, FALSE),
        (999900003455, 999900003452, TIMESTAMP '2020-01-01', 'U45202', -50, 0, 999900003452, FALSE);

    -- Outstanding balances are 80 and 40, so a payment of 30 must split into 20 and 10.
    v_before := CLOCK_TIMESTAMP();
    CALL p_allocate_payments(
        pi_defendant_account_id := 999900003452::BIGINT,
        pi_posted_by := 'UT3452'::VARCHAR,
        pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
        pi_payment_amount := 30::NUMERIC,
        pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
        pi_allocation_function := 'p_allocate_payments'::VARCHAR,
        pi_payment_reference := 'REF3452'::VARCHAR,
        pi_defendant_transaction_id := 999900003452::BIGINT
    );
    v_after := CLOCK_TIMESTAMP();

    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900003452) = 2,
        'Expected two allocations';
    ASSERT (SELECT SUM(allocated_amount) FROM allocations WHERE defendant_transaction_id = 999900003452) = 30.00,
        'The full payment should be allocated';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = 2,
        'Expected one creditor transaction for each allocation';

    FOR v_expected IN
        SELECT * FROM (VALUES
            (999900003453::BIGINT, 20.00, 40.00, -100.00, 999900003452::BIGINT),
            (999900003454::BIGINT, 10.00, 10.00, -40.00, 999900003453::BIGINT)
        ) AS expected(imposition_id, allocated_amount, paid_amount, imposed_amount, creditor_account_id)
    LOOP
        SELECT * INTO STRICT v_allocation FROM allocations WHERE imposition_id = v_expected.imposition_id;
        SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = v_expected.imposition_id;
        SELECT * INTO STRICT v_creditor FROM creditor_transactions
         WHERE associated_record_type = 'impositions'
           AND associated_record_id = v_expected.imposition_id::TEXT;

        ASSERT v_allocation.allocation_id IS NOT NULL, 'allocation_id should be generated';
        ASSERT v_allocation.imposition_id = v_expected.imposition_id, 'imposition_id should match';
        ASSERT v_allocation.allocated_amount = v_expected.allocated_amount, 'allocated_amount should match the share';
        ASSERT v_allocation.allocated_date BETWEEN v_before AND v_after, 'allocated_date should be set during the call';
        ASSERT v_allocation.transaction_type::TEXT = 'PAYMNT', 'Allocation transaction_type should be PAYMNT';
        ASSERT v_allocation.allocation_function = 'p_allocate_payments', 'allocation_function should match';
        ASSERT v_allocation.defendant_transaction_id = 999900003452, 'defendant_transaction_id should match';

        ASSERT v_imposition.paid_amount = v_expected.paid_amount, 'paid_amount should increase by the share';
        ASSERT v_imposition.imposed_amount = v_expected.imposed_amount, 'imposed_amount should remain unchanged';
        ASSERT v_imposition.completed = FALSE, 'Partially paid impositions should remain incomplete';

        ASSERT v_creditor.creditor_transaction_id IS NOT NULL, 'creditor_transaction_id should be generated';
        ASSERT v_creditor.creditor_account_id = v_expected.creditor_account_id,
            'The correct creditor should receive the share';
        ASSERT v_creditor.transaction_amount = v_expected.allocated_amount,
            'Creditor amount should match the allocation';
        ASSERT v_creditor.transaction_type::TEXT = 'PAYMNT', 'Creditor transaction_type should be PAYMNT';
        ASSERT v_creditor.posted_by = 'UT3452', 'posted_by should match';
        ASSERT v_creditor.posted_by_name = 'PO-3452 Unit Test', 'posted_by_name should match';
        ASSERT v_creditor.posted_date BETWEEN v_before AND v_after, 'posted_date should be set during the call';
        ASSERT v_creditor.imposition_result_id = 'U45201', 'imposition_result_id should match';
        ASSERT v_creditor.payment_reference = 'REF3452', 'payment_reference should match';
        ASSERT v_creditor.payment_processed = FALSE, 'payment_processed should be FALSE';
        ASSERT v_creditor.status IS NULL, 'status should be NULL';
        ASSERT v_creditor.status_date IS NULL, 'status_date should be NULL';
        ASSERT v_creditor.associated_record_type::TEXT = 'impositions',
            'The associated record type should be impositions';
        ASSERT v_creditor.associated_record_id = v_expected.imposition_id::TEXT,
            'The associated imposition should match';
    END LOOP;

    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003455) = 0.00,
        'Priority 2 should not receive a payment before priority 1 is settled';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900004353) = 20.00,
        'Another account should remain unchanged';
    ASSERT NOT EXISTS (SELECT 1 FROM allocations WHERE imposition_id = 999900004353),
        'Another account should not receive an allocation';

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
-- Test 2: Distribute a rounding penny by imposition ID for equal shares
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== TEST 2: Distribute a rounding penny by imposition ID for equal shares ===';

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES
        (999900003555, 999900013452, TIMESTAMP '2020-01-01', 'U45201', -1, 0, 999900003452, FALSE),
        (999900003554, 999900013452, TIMESTAMP '2020-01-01', 'U45201', -1, 0, 999900003452, FALSE),
        (999900003553, 999900013452, TIMESTAMP '2020-01-01', 'U45201', -1, 0, 999900003452, FALSE);

    CALL p_allocate_payments(
        pi_defendant_account_id := 999900013452::BIGINT,
        pi_posted_by := 'UT3452'::VARCHAR,
        pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
        pi_payment_amount := 1::NUMERIC,
        pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
        pi_allocation_function := 'p_allocate_payments'::VARCHAR,
        pi_payment_reference := 'REF3452'::VARCHAR,
        pi_defendant_transaction_id := 999900013452::BIGINT
    );

    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900013452) = 3,
        'Expected three allocations';
    ASSERT (SELECT allocated_amount FROM allocations WHERE imposition_id = 999900003553) = 0.34,
        'The lowest imposition ID should receive the rounding penny';
    ASSERT (SELECT allocated_amount FROM allocations WHERE imposition_id = 999900003554) = 0.33,
        'The second share should be 0.33';
    ASSERT (SELECT allocated_amount FROM allocations WHERE imposition_id = 999900003555) = 0.33,
        'The third share should be 0.33';
    ASSERT (SELECT SUM(allocated_amount) FROM allocations WHERE defendant_transaction_id = 999900013452) = 1.00,
        'Rounded shares should add up to the payment';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003553) = 0.34,
        'The first paid_amount should include the rounding penny';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003554) = 0.33,
        'The second paid_amount should match the allocation';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003555) = 0.33,
        'The third paid_amount should match the allocation';

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
-- Test 3: Give a single penny to the largest remainder and skip zero shares
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== TEST 3: Give a single penny to the largest remainder and skip zero shares ===';

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES
        (999900003653, 999900023452, TIMESTAMP '2020-01-01', 'U45201', -1, 0, 999900003452, FALSE),
        (999900003654, 999900023452, TIMESTAMP '2020-01-01', 'U45201', -2, 0, 999900003452, FALSE),
        (999900003655, 999900023452, TIMESTAMP '2020-01-01', 'U45201', -3, 0, 999900003452, FALSE);

    CALL p_allocate_payments(
        pi_defendant_account_id := 999900023452::BIGINT,
        pi_posted_by := 'UT3452'::VARCHAR,
        pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
        pi_payment_amount := 0.01::NUMERIC,
        pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
        pi_allocation_function := 'p_allocate_payments'::VARCHAR,
        pi_payment_reference := 'REF3452'::VARCHAR,
        pi_defendant_transaction_id := 999900023452::BIGINT
    );

    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900023452) = 1,
        'Zero shares should not create allocations';
    ASSERT (SELECT allocated_amount FROM allocations WHERE imposition_id = 999900003655) = 0.01,
        'The largest fractional remainder should receive the penny';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003653) = 0.00,
        'The first imposition should remain unpaid';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003654) = 0.00,
        'The second imposition should remain unpaid';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003655) = 0.01,
        'The third imposition should receive the penny';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions
             WHERE associated_record_type = 'impositions'
               AND associated_record_id IN ('999900003653', '999900003654', '999900003655')) = 1,
        'Zero shares should not create creditor transactions';

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
-- Test 4: Allocate by priority then posted date then imposition ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== TEST 4: Allocate by priority then posted date then imposition ID ===';

    -- Priority 2 must precede the older priority 3 row.
    -- Within priority 2, the oldest row must precede a lower ID with a later date.
    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES
        (999900003753, 999900033452, TIMESTAMP '2020-04-01', 'U45201', -10, 0, 999900003452, FALSE),
        (999900003756, 999900033452, TIMESTAMP '2020-03-01', 'U45202', -20, 0, 999900003452, FALSE),
        (999900003755, 999900033452, TIMESTAMP '2020-02-01', 'U45202', -20, 0, 999900003452, FALSE),
        (999900003754, 999900033452, TIMESTAMP '2020-03-01', 'U45202', -20, 0, 999900003452, FALSE),
        (999900003757, 999900033452, TIMESTAMP '2020-01-01', 'U45203', -50, 0, 999900003452, FALSE);

    CALL p_allocate_payments(
        pi_defendant_account_id := 999900033452::BIGINT,
        pi_posted_by := 'UT3452'::VARCHAR,
        pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
        pi_payment_amount := 65::NUMERIC,
        pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
        pi_allocation_function := 'p_allocate_payments'::VARCHAR,
        pi_payment_reference := 'REF3452'::VARCHAR,
        pi_defendant_transaction_id := 999900033452::BIGINT
    );

    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900033452) = 4,
        'Expected four allocations';
    ASSERT (SELECT allocated_amount FROM allocations WHERE imposition_id = 999900003753) = 10.00,
        'Priority 1 should be settled first';
    ASSERT (SELECT allocated_amount FROM allocations WHERE imposition_id = 999900003755) = 20.00,
        'The oldest priority 2 imposition should be settled next';
    ASSERT (SELECT allocated_amount FROM allocations WHERE imposition_id = 999900003754) = 20.00,
        'The lower ID should be settled first when posted dates match';
    ASSERT (SELECT allocated_amount FROM allocations WHERE imposition_id = 999900003756) = 15.00,
        'Only the remaining payment should reach the next imposition';
    ASSERT (SELECT SUM(allocated_amount) FROM allocations WHERE defendant_transaction_id = 999900033452) = 65.00,
        'No more than the payment should be allocated';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003753) = 10.00,
        'Priority 1 paid_amount should match';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003755) = 20.00,
        'The oldest priority 2 paid_amount should match';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003754) = 20.00,
        'The lower ID paid_amount should match';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003756) = 15.00,
        'The partially paid imposition should receive the remainder';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = 999900003753) = TRUE,
        'The fully paid priority 1 imposition should be complete';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = 999900003756) = FALSE,
        'The partially paid imposition should remain incomplete';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003757) = 0.00,
        'Priority 3 should remain unpaid when the payment is exhausted';

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
-- Test 5: Settle priorities 1 to 9 and leave the excess unallocated
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== TEST 5: Settle priorities 1 to 9 and leave the excess unallocated ===';

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES
        (999900003853, 999900043452, TIMESTAMP '2020-01-01', 'U45201', -10, 0, 999900003452, FALSE),
        (999900003854, 999900043452, TIMESTAMP '2020-01-01', 'U45202', -10, 0, 999900003452, FALSE),
        (999900003855, 999900043452, TIMESTAMP '2020-01-01', 'U45203', -10, 0, 999900003452, FALSE),
        (999900003856, 999900043452, TIMESTAMP '2020-01-01', 'U45204', -10, 0, 999900003452, FALSE),
        (999900003857, 999900043452, TIMESTAMP '2020-01-01', 'U45205', -10, 0, 999900003452, FALSE),
        (999900003858, 999900043452, TIMESTAMP '2020-01-01', 'U45206', -10, 0, 999900003452, FALSE),
        (999900003859, 999900043452, TIMESTAMP '2020-01-01', 'U45207', -10, 0, 999900003452, FALSE),
        (999900003860, 999900043452, TIMESTAMP '2020-01-01', 'U45208', -10, 0, 999900003452, FALSE),
        (999900003861, 999900043452, TIMESTAMP '2020-01-01', 'U45209', -10, 0, 999900003452, FALSE);

    CALL p_allocate_payments(
        pi_defendant_account_id := 999900043452::BIGINT,
        pi_posted_by := 'UT3452'::VARCHAR,
        pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
        pi_payment_amount := 100::NUMERIC,
        pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
        pi_allocation_function := 'p_allocate_payments'::VARCHAR,
        pi_payment_reference := 'REF3452'::VARCHAR,
        pi_defendant_transaction_id := 999900043452::BIGINT
    );

    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900043452) = 9,
        'Each eligible priority should receive an allocation';
    ASSERT (SELECT SUM(allocated_amount) FROM allocations WHERE defendant_transaction_id = 999900043452) = 90.00,
        'The excess 10.00 should remain unallocated';
    ASSERT NOT EXISTS (
        SELECT 1 FROM impositions WHERE defendant_account_id = 999900043452
          AND (paid_amount IS DISTINCT FROM 10.00 OR completed IS DISTINCT FROM TRUE)
    ), 'All nine impositions should be fully paid and complete';
    ASSERT NOT EXISTS (
        SELECT 1 FROM allocations WHERE defendant_transaction_id = 999900043452
          AND allocated_amount IS DISTINCT FROM 10.00
    ), 'Each allocation should be capped at the outstanding balance';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions
             WHERE associated_record_type = 'impositions'
               AND associated_record_id IN (
                   '999900003853', '999900003854', '999900003855', '999900003856', '999900003857',
                   '999900003858', '999900003859', '999900003860', '999900003861'
               )) = 9,
        'Each settled imposition should have a creditor transaction';

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
-- Test 6: Skip settled impositions and priorities outside 1 to 9
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_before BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 6: Skip settled impositions and priorities outside 1 to 9 ===';

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES
        (999900003953, 999900053452, TIMESTAMP '2020-01-01', 'U45201', -10, 10, 999900003452, TRUE),
        (999900003954, 999900053452, TIMESTAMP '2020-01-01', 'U45202', 0, 0, 999900003452, FALSE),
        (999900003955, 999900053452, TIMESTAMP '2020-01-01', 'U45203', 10, 0, 999900003452, FALSE),
        (999900003956, 999900053452, TIMESTAMP '2020-01-01', 'U45200', -10, 0, 999900003452, FALSE),
        (999900003957, 999900053452, TIMESTAMP '2020-01-01', 'U45210', -10, 0, 999900003452, FALSE),
        (999900003958, 999900053452, TIMESTAMP '2020-01-01', 'U452NA', -10, 0, 999900003452, FALSE);

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);
    CALL p_allocate_payments(
        pi_defendant_account_id := 999900053452::BIGINT,
        pi_posted_by := 'UT3452'::VARCHAR,
        pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
        pi_payment_amount := 20::NUMERIC,
        pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
        pi_allocation_function := 'p_allocate_payments'::VARCHAR,
        pi_payment_reference := 'REF3452'::VARCHAR,
        pi_defendant_transaction_id := 999900053452::BIGINT
    );

    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900053452) = 0,
        'Ineligible impositions should not receive allocations';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003953) = 10.00,
        'The settled imposition should remain unchanged';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = 999900003953) = TRUE,
        'The settled imposition should remain complete';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003954) = 0.00,
        'A zero balance should remain unchanged';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003955) = 0.00,
        'A positive imposed amount should remain unchanged';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003956) = 0.00,
        'Priority 0 should remain unpaid';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003957) = 0.00,
        'Priority 10 should remain unpaid';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900003958) = 0.00,
        'A NULL priority should remain unpaid';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Ineligible impositions should not create creditor transactions';

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
-- Test 7: Leave a payment unallocated when the account has no impositions
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_before BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 7: Leave a payment unallocated when the account has no impositions ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);
    CALL p_allocate_payments(
        pi_defendant_account_id := 999900063452::BIGINT,
        pi_posted_by := 'UT3452'::VARCHAR,
        pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
        pi_payment_amount := 20::NUMERIC,
        pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
        pi_allocation_function := 'p_allocate_payments'::VARCHAR,
        pi_payment_reference := 'REF3452'::VARCHAR,
        pi_defendant_transaction_id := 999900063452::BIGINT
    );

    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900063452) = 0,
        'An account without impositions should not receive allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'An account without impositions should not create creditor transactions';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900004353) = 20.00,
        'Another account should remain unchanged';

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
-- Test 8: Roll back allocations and balances when a dependency fails
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 8: Roll back allocations and balances when a dependency fails ===';

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES
        (999900004153, 999900073452, TIMESTAMP '2020-01-01', 'U45201', -100, 20, 999900003452, FALSE);

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900073452::BIGINT,
            pi_posted_by := NULL::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900073452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3809' THEN
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3809 from p_insert_creditor_transactions';
    ASSERT v_detail = 'p_insert_creditor_transactions: pi_posted_by is required',
        'The dependency error DETAIL should be preserved';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900073452) = 0,
        'The earlier allocation insert should be rolled back';
    ASSERT (SELECT paid_amount FROM impositions WHERE imposition_id = 999900004153) = 20.00,
        'The earlier imposition update should be rolled back';
    ASSERT (SELECT completed FROM impositions WHERE imposition_id = 999900004153) = FALSE,
        'The imposition should remain incomplete';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'The failed call should not create a creditor transaction';

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
-- Test 9: Reject a NULL defendant account ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 9: Reject a NULL defendant account ID ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := NULL::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3901' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3901 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_defendant_account_id is required',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

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
-- Test 10: Reject a NULL payment amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 10: Reject a NULL payment amount ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := NULL::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3902' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3902 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_payment_amount must be a positive value',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

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
-- Test 11: Reject a zero payment amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 11: Reject a zero payment amount ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 0.00::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3902' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3902 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_payment_amount must be a positive value',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 11 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 11 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 11 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 12: Reject a negative payment amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 12: Reject a negative payment amount ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := -1.00::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3902' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3902 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_payment_amount must be a positive value',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 12 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 12 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 12 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 13: Reject a NULL transaction type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 13: Reject a NULL transaction type ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := NULL::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3903' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3903 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_transaction_type must map to both allocation and creditor transaction type PAYMNT',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 13 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 13 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 13 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 14: Reject a transaction type other than PAYMNT
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 14: Reject a transaction type other than PAYMNT ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'FR-SUS'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3903' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3903 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_transaction_type must map to both allocation and creditor transaction type PAYMNT',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 14 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 14 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 14 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 15: Reject a NULL allocation function
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 15: Reject a NULL allocation function ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := NULL::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3904' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3904 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_allocation_function is required',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 15 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 15 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 15 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 16: Reject an empty allocation function
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 16: Reject an empty allocation function ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := ''::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3904' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3904 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_allocation_function is required',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 16 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 16 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 16 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 17: Reject a blank allocation function
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 17: Reject a blank allocation function ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := '   '::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3904' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3904 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_allocation_function is required',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 17 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 17 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 17 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 18: Reject a NULL defendant transaction ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 18: Reject a NULL defendant transaction ID ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3905' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3905 was not raised';
    ASSERT v_detail = 'p_allocate_payments: pi_defendant_transaction_id is required',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 18 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 18 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 18 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 19: Reject an unknown defendant account
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 19: Reject an unknown defendant account ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := -3452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900083452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3906' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3906 was not raised';
    ASSERT v_detail = 'p_allocate_payments: no defendant account exists for the passed pi_defendant_account_id',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 19 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 19 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 19 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 20: Reject an unknown defendant transaction
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 20: Reject an unknown defendant transaction ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := -3452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3907' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3907 was not raised';
    ASSERT v_detail = 'p_allocate_payments: the defendant transaction must exist and belong to the passed defendant account',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 20 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 20 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 20 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 21: Reject a defendant transaction belonging to another account
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_imposition   impositions%ROWTYPE;
    v_before       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 21: Reject a defendant transaction belonging to another account ===';

    SELECT COUNT(*) INTO v_before FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);

    BEGIN
        CALL p_allocate_payments(
            pi_defendant_account_id := 999900083452::BIGINT,
            pi_posted_by := 'UT3452'::VARCHAR,
            pi_posted_by_name := 'PO-3452 Unit Test'::VARCHAR,
            pi_payment_amount := 25::NUMERIC,
            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
            pi_allocation_function := 'p_allocate_payments'::VARCHAR,
            pi_payment_reference := 'REF3452'::VARCHAR,
            pi_defendant_transaction_id := 999900093452::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3907' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3907 was not raised';
    ASSERT v_detail = 'p_allocate_payments: the defendant transaction must exist and belong to the passed defendant account',
        'Validation DETAIL should be preserved';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 999900004253;
    ASSERT v_imposition.paid_amount = 20.00, 'Rejected payment should not change paid_amount';
    ASSERT v_imposition.imposed_amount = -100.00, 'Rejected payment should not change imposed_amount';
    ASSERT v_imposition.completed = FALSE, 'Rejected payment should not change completed';
    ASSERT (SELECT COUNT(*) FROM allocations WHERE defendant_transaction_id = 999900083452) = 0,
        'Rejected payment should not create allocations';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453)) = v_before,
        'Rejected payment should not create creditor transactions';

    RAISE NOTICE 'TEST 21 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 21 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 21 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Cleanup test data
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM allocations WHERE imposition_id IN (
        SELECT imposition_id FROM impositions WHERE defendant_account_id IN (
               999900003452, 999900013452, 999900023452, 999900033452, 999900043452,
               999900053452, 999900063452, 999900073452, 999900083452, 999900093452
           )
    );
    DELETE FROM creditor_transactions WHERE creditor_account_id IN (999900003452, 999900003453);
    DELETE FROM defendant_transactions WHERE defendant_account_id IN (
               999900003452, 999900013452, 999900023452, 999900033452, 999900043452,
               999900053452, 999900063452, 999900073452, 999900083452, 999900093452
           );
    DELETE FROM impositions WHERE defendant_account_id IN (
               999900003452, 999900013452, 999900023452, 999900033452, 999900043452,
               999900053452, 999900063452, 999900073452, 999900083452, 999900093452
           );
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (
               999900003452, 999900013452, 999900023452, 999900033452, 999900043452,
               999900053452, 999900063452, 999900073452, 999900083452, 999900093452
           );
    DELETE FROM creditor_accounts WHERE creditor_account_id IN (999900003452, 999900003453);
    DELETE FROM results WHERE result_id IN (
        'U45201', 'U45202', 'U45203', 'U45204', 'U45205', 'U45206',
        'U45207', 'U45208', 'U45209', 'U45200', 'U45210', 'U452NA'
    );
    DELETE FROM business_units WHERE business_unit_id = 9952;

    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing
