/**
* OPAL Program
*
* MODULE      : p_insert_creditor_transactions_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_creditor_transactions.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 19/10/2026    C Cho       1.0         PO-3450 Unit tests for p_insert_creditor_transactions
*
**/
\timing

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data before tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM creditor_transactions WHERE creditor_account_id = 999900003450;
    DELETE FROM impositions WHERE imposition_id IN (999900003450, 999900013450);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999900003450, 999900013450);
    DELETE FROM creditor_accounts WHERE creditor_account_id = 999900003450;
    DELETE FROM results WHERE result_id = 'UT3450';
    DELETE FROM business_units WHERE business_unit_id = 9950;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test setup
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Test setup ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
    VALUES (9950, 'PO-3450 Unit Tests', 'U450', 'Accounting Division');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    )
    VALUES (999900003450, 9950, 'UT3450-CREDITOR', 'CF', FALSE, FALSE, FALSE, FALSE);

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type, version_number
    )
    VALUES (999900003450, 9950, 'UT3450-DEFENDANT', 100.00, 20.00, 80.00, 'L', 'Fine', 1);

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    )
    VALUES (
        'UT3450', 'PO-3450 Unit Test Result', 'Result', TRUE, TRUE, NULL,
        1, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
        FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE
    );

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES (999900003450, 999900003450, CURRENT_TIMESTAMP, 'UT3450',
            100.00, 20.00, 999900003450, FALSE);
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Insert and verify all fields
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction creditor_transactions%ROWTYPE;
    v_before      TIMESTAMP;
    v_after       TIMESTAMP;
BEGIN
    RAISE NOTICE '=== Test 1: Insert and verify all fields ===';

    v_before := CLOCK_TIMESTAMP();
    CALL p_insert_creditor_transactions(
        pi_creditor_account_id := 999900003450::BIGINT,
        pi_posted_by := 'UT3450'::VARCHAR,
        pi_posted_by_name := 'Unit Test User'::VARCHAR,
        pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
        pi_imposition_allocation_amount := 25.50::NUMERIC,
        pi_result_id := 'UT3450'::VARCHAR,
        pi_payment_reference := 'REF3450'::VARCHAR,
        pi_imposition_id := 999900003450::BIGINT
    );
    v_after := CLOCK_TIMESTAMP();

    SELECT * INTO STRICT v_transaction
      FROM creditor_transactions
     WHERE creditor_account_id = 999900003450
       AND transaction_amount = 25.50;

    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 1,
        'Each call should insert exactly one row';
    ASSERT v_transaction.creditor_transaction_id IS NOT NULL, 'A transaction ID should be generated';
    ASSERT v_transaction.creditor_account_id = 999900003450, 'creditor_account_id should match';
    ASSERT v_transaction.posted_date BETWEEN v_before AND v_after, 'posted_date should be generated during the call';
    ASSERT v_transaction.posted_by = 'UT3450'::VARCHAR, 'posted_by should match';
    ASSERT v_transaction.posted_by_name = 'Unit Test User'::VARCHAR, 'posted_by_name should match';
    ASSERT v_transaction.transaction_type = 'PAYMNT'::t_creditor_transaction_type_enum, 'transaction_type should match';
    ASSERT v_transaction.transaction_amount = 25.50, 'transaction_amount should match';
    ASSERT v_transaction.imposition_result_id = 'UT3450', 'imposition_result_id should match';
    ASSERT v_transaction.payment_processed = FALSE, 'payment_processed should be FALSE';
    ASSERT v_transaction.payment_reference = 'REF3450'::VARCHAR, 'payment_reference should match';
    ASSERT v_transaction.status IS NULL, 'status should be NULL';
    ASSERT v_transaction.status_date IS NULL, 'status_date should be NULL';
    ASSERT v_transaction.associated_record_type = 'impositions'::t_associated_record_type_enum, 'associated_record_type should be impositions';
    ASSERT v_transaction.associated_record_id = '999900003450', 'associated_record_id should match the imposition ID';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Insert a one penny payment with required posting fields
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction creditor_transactions%ROWTYPE;
    v_before      TIMESTAMP;
    v_after       TIMESTAMP;
BEGIN
    RAISE NOTICE '=== Test 2: Insert a one penny payment with required posting fields ===';

    v_before := CLOCK_TIMESTAMP();
    CALL p_insert_creditor_transactions(
        pi_creditor_account_id := 999900003450::BIGINT,
        pi_posted_by := 'UT3450'::VARCHAR,
        pi_posted_by_name := 'Unit Test User'::VARCHAR,
        pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
        pi_imposition_allocation_amount := 0.01::NUMERIC,
        pi_result_id := 'UT3450'::VARCHAR,
        pi_payment_reference := 'REF3450'::VARCHAR,
        pi_imposition_id := 999900003450::BIGINT
    );
    v_after := CLOCK_TIMESTAMP();

    SELECT * INTO STRICT v_transaction
      FROM creditor_transactions
     WHERE creditor_account_id = 999900003450
       AND transaction_amount = 0.01;

    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Each call should insert exactly one row';
    ASSERT v_transaction.creditor_transaction_id IS NOT NULL, 'A transaction ID should be generated';
    ASSERT v_transaction.creditor_account_id = 999900003450, 'creditor_account_id should match';
    ASSERT v_transaction.posted_date BETWEEN v_before AND v_after, 'posted_date should be generated during the call';
    ASSERT v_transaction.posted_by = 'UT3450'::VARCHAR, 'posted_by should match';
    ASSERT v_transaction.posted_by_name = 'Unit Test User'::VARCHAR, 'posted_by_name should match';
    ASSERT v_transaction.transaction_type = 'PAYMNT'::t_creditor_transaction_type_enum, 'transaction_type should match';
    ASSERT v_transaction.transaction_amount = 0.01, 'transaction_amount should match';
    ASSERT v_transaction.imposition_result_id = 'UT3450', 'imposition_result_id should match';
    ASSERT v_transaction.payment_processed = FALSE, 'payment_processed should be FALSE';
    ASSERT v_transaction.payment_reference = 'REF3450'::VARCHAR, 'payment_reference should match';
    ASSERT v_transaction.status IS NULL, 'status should be NULL';
    ASSERT v_transaction.status_date IS NULL, 'status_date should be NULL';
    ASSERT v_transaction.associated_record_type = 'impositions'::t_associated_record_type_enum, 'associated_record_type should be impositions';
    ASSERT v_transaction.associated_record_id = '999900003450', 'associated_record_id should match the imposition ID';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Reject a NULL creditor account ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 3: Reject a NULL creditor account ID ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := NULL::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3801' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3801 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
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
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := NULL::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3802' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3802 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
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
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 0.00::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3802' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3802 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
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
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := -1.00::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3802' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3802 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Reject a NULL result ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 7: Reject a NULL result ID ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := NULL::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3803' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3803 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 7 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 8: Reject an empty result ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 8: Reject an empty result ID ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := ''::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3803' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3803 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 8 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 9: Reject a blank result ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 9: Reject a blank result ID ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := '   '::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3803' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3803 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 9 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 10: Reject a NULL transaction type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 10: Reject a NULL transaction type ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := NULL::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3804' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3804 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 10 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 11: Reject a NULL imposition ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 11: Reject a NULL imposition ID ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3805' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3805 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 11 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 12: Reject an unknown creditor account
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 12: Reject an unknown creditor account ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := -3450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3806' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3806 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 12 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 13: Reject an unknown imposition
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 13: Reject an unknown imposition ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := -3450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3807' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3807 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 13 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 14: Reject an unknown result ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 14: Reject an unknown result ID ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := 'UT9999'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3808' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3808 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 14 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 15: Reject a NULL posted by
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 15: Reject a NULL posted by ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := NULL::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3809' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3809 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 15 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 16: Reject a NULL posted by name
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 16: Reject a NULL posted by name ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := NULL::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := 'REF3450'::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3810' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3810 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 16 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 17: Reject a NULL payment reference
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE '=== Test 17: Reject a NULL payment reference ===';

    BEGIN
        CALL p_insert_creditor_transactions(
            pi_creditor_account_id := 999900003450::BIGINT,
            pi_posted_by := 'UT3450'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := 'PAYMNT'::t_creditor_transaction_type_enum,
            pi_imposition_allocation_amount := 25.50::NUMERIC,
            pi_result_id := 'UT3450'::VARCHAR,
            pi_payment_reference := NULL::VARCHAR,
            pi_imposition_id := 999900003450::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3811' THEN
            v_error_caught := TRUE;
    END;

    ASSERT v_error_caught, 'Expected SQLSTATE P3811 was not raised';
    ASSERT (SELECT COUNT(*) FROM creditor_transactions WHERE creditor_account_id = 999900003450) = 2,
        'Invalid calls should not insert a row';

    RAISE NOTICE 'TEST 17 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data after tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data after tests ===';

    DELETE FROM creditor_transactions WHERE creditor_account_id = 999900003450;
    DELETE FROM impositions WHERE imposition_id IN (999900003450, 999900013450);
    DELETE FROM defendant_accounts WHERE defendant_account_id IN (999900003450, 999900013450);
    DELETE FROM creditor_accounts WHERE creditor_account_id = 999900003450;
    DELETE FROM results WHERE result_id = 'UT3450';
    DELETE FROM business_units WHERE business_unit_id = 9950;
END $$;

\timing
