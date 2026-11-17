/**
* OPAL Program
*
* MODULE      : p_insert_defendant_transactions_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_defendant_transactions.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 13/10/2026    C Cho       1.0         PO-3446 Unit tests for p_insert_defendant_transactions
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
     WHERE defendant_account_id IN (999900003446);
    DELETE FROM defendant_accounts
     WHERE defendant_account_id IN (999900003446);
    DELETE FROM business_units
     WHERE business_unit_id = 9946;

    RAISE NOTICE 'Cleanup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test setup
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Setting up test data ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
    VALUES (9946, 'PO-3446 Unit Tests', 'U446', 'Accounting Division');

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type, version_number
    )
    VALUES (999900003446, 9946, 'UT3446-TRANSACTIONS', -100.00, 20.00, -80.00, 'L', 'Fine', 1);

    RAISE NOTICE 'Test data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: all supplied fields
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id     defendant_transactions.defendant_transaction_id%TYPE;
    v_transaction        defendant_transactions%ROWTYPE;
    v_before             TIMESTAMP;
    v_after              TIMESTAMP;
    v_count              BIGINT;
    v_posted_by          defendant_transactions.posted_by%TYPE := 'UT3446'::VARCHAR;
    v_posted_by_name     defendant_transactions.posted_by_name%TYPE := 'Unit Test User'::VARCHAR;
    v_transaction_type   defendant_transactions.transaction_type%TYPE := 'PAYMNT'::t_defendant_transaction_type_enum;
    v_payment_amount     defendant_transactions.transaction_amount%TYPE := 25.50::NUMERIC;
    v_payment_method     defendant_transactions.payment_method%TYPE := 'CT'::t_payment_method_enum;
    v_payment_reference  defendant_transactions.payment_reference%TYPE := 'PAYMENT-REF-LONGER-THAN-TEN'::VARCHAR;
    v_payer_name         defendant_transactions.text%TYPE := 'Third Party Payer'::VARCHAR;
    v_associated_id      defendant_transactions.associated_record_id%TYPE := 'UT3446-PAYMENT-1'::VARCHAR;
    v_source             defendant_transactions.source%TYPE := 'BACS'::VARCHAR;
BEGIN
    RAISE NOTICE '=== TEST 1: all supplied fields ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    v_before := CLOCK_TIMESTAMP();
    v_transaction_id := NULL;
    CALL p_insert_defendant_transactions(
        pi_defendant_account_id := 999900003446::BIGINT,
        pi_posted_by := v_posted_by,
        pi_posted_by_name := v_posted_by_name,
        pi_transaction_type := v_transaction_type,
        pi_payment_amount := v_payment_amount,
        pi_payment_method := v_payment_method,
        pi_payment_reference := v_payment_reference,
        pi_third_party_payer_name := v_payer_name,
        pi_associated_record_id := v_associated_id,
        pi_source := v_source,
        po_defendant_transaction_id := v_transaction_id
    );
    v_after := CLOCK_TIMESTAMP();

    ASSERT v_transaction_id IS NOT NULL, 'OUT parameter must return the inserted transaction ID';
    SELECT * INTO STRICT v_transaction FROM defendant_transactions WHERE defendant_transaction_id = v_transaction_id;
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count + 1, 'Each call must insert exactly one row';
    ASSERT v_transaction.defendant_account_id = 999900003446, 'Transaction must reference the supplied account';
    ASSERT v_transaction.posted_by IS NOT DISTINCT FROM v_posted_by, 'posted_by must match, including NULL';
    ASSERT v_transaction.posted_by_name IS NOT DISTINCT FROM v_posted_by_name, 'posted_by_name must match, including NULL';
    ASSERT v_transaction.transaction_type = v_transaction_type, 'transaction_type must match';
    ASSERT v_transaction.transaction_amount = v_payment_amount, 'Payment amount must be stored exactly';
    ASSERT v_transaction.payment_method = v_payment_method, 'payment_method must match';
    ASSERT v_transaction.payment_reference IS NOT DISTINCT FROM v_payment_reference, 'payment_reference must match, including NULL';
    ASSERT v_transaction.text IS NOT DISTINCT FROM v_payer_name, 'Payer name must be stored as text, including NULL';
    ASSERT v_transaction.status IS NULL, 'New transaction status must be NULL';
    ASSERT v_transaction.associated_record_type::TEXT = 'payments_in', 'Associated record type must be payments_in';
    ASSERT v_transaction.associated_record_id = v_associated_id, 'Associated record ID must match';
    ASSERT v_transaction.source = v_source, 'Source must match';
    ASSERT v_transaction.posted_date BETWEEN v_before AND v_after, 'posted_date must be generated during the call';
    ASSERT v_transaction.status_date BETWEEN v_transaction.posted_date AND v_after, 'status_date must be generated after posted_date';
    ASSERT v_transaction.status_amount IS NULL AND v_transaction.write_off_code IS NULL
        AND v_transaction.imposed_amount IS NULL, 'Unspecified transaction fields must stay NULL';

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
-- Test 2: NULL optional fields
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id     defendant_transactions.defendant_transaction_id%TYPE;
    v_transaction        defendant_transactions%ROWTYPE;
    v_before             TIMESTAMP;
    v_after              TIMESTAMP;
    v_count              BIGINT;
    v_posted_by          defendant_transactions.posted_by%TYPE := NULL;
    v_posted_by_name     defendant_transactions.posted_by_name%TYPE := NULL;
    v_transaction_type   defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount     defendant_transactions.transaction_amount%TYPE := 0.01;
    v_payment_method     defendant_transactions.payment_method%TYPE := 'NC';
    v_payment_reference  defendant_transactions.payment_reference%TYPE := NULL;
    v_payer_name         defendant_transactions.text%TYPE := NULL;
    v_associated_id      defendant_transactions.associated_record_id%TYPE := 'UT3446-PAYMENT-2';
    v_source             defendant_transactions.source%TYPE := 'Cash';
BEGIN
    RAISE NOTICE '=== TEST 2: NULL optional fields ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    v_before := CLOCK_TIMESTAMP();
    v_transaction_id := NULL;
    CALL p_insert_defendant_transactions(
        pi_defendant_account_id := 999900003446::BIGINT,
        pi_posted_by := v_posted_by,
        pi_posted_by_name := v_posted_by_name,
        pi_transaction_type := v_transaction_type,
        pi_payment_amount := v_payment_amount,
        pi_payment_method := v_payment_method,
        pi_payment_reference := v_payment_reference,
        pi_third_party_payer_name := v_payer_name,
        pi_associated_record_id := v_associated_id,
        pi_source := v_source,
        po_defendant_transaction_id := v_transaction_id
    );
    v_after := CLOCK_TIMESTAMP();

    ASSERT v_transaction_id IS NOT NULL, 'OUT parameter must return the inserted transaction ID';
    SELECT * INTO STRICT v_transaction FROM defendant_transactions WHERE defendant_transaction_id = v_transaction_id;
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count + 1, 'Each call must insert exactly one row';
    ASSERT v_transaction.defendant_account_id = 999900003446, 'Transaction must reference the supplied account';
    ASSERT v_transaction.posted_by IS NOT DISTINCT FROM v_posted_by, 'posted_by must match, including NULL';
    ASSERT v_transaction.posted_by_name IS NOT DISTINCT FROM v_posted_by_name, 'posted_by_name must match, including NULL';
    ASSERT v_transaction.transaction_type = v_transaction_type, 'transaction_type must match';
    ASSERT v_transaction.transaction_amount = v_payment_amount, 'Payment amount must be stored exactly';
    ASSERT v_transaction.payment_method = v_payment_method, 'payment_method must match';
    ASSERT v_transaction.payment_reference IS NOT DISTINCT FROM v_payment_reference, 'payment_reference must match, including NULL';
    ASSERT v_transaction.text IS NOT DISTINCT FROM v_payer_name, 'Payer name must be stored as text, including NULL';
    ASSERT v_transaction.status IS NULL, 'New transaction status must be NULL';
    ASSERT v_transaction.associated_record_type::TEXT = 'payments_in', 'Associated record type must be payments_in';
    ASSERT v_transaction.associated_record_id = v_associated_id, 'Associated record ID must match';
    ASSERT v_transaction.source = v_source, 'Source must match';
    ASSERT v_transaction.posted_date BETWEEN v_before AND v_after, 'posted_date must be generated during the call';
    ASSERT v_transaction.status_date BETWEEN v_transaction.posted_date AND v_after, 'status_date must be generated after posted_date';
    ASSERT v_transaction.status_amount IS NULL AND v_transaction.write_off_code IS NULL
        AND v_transaction.imposed_amount IS NULL, 'Unspecified transaction fields must stay NULL';

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
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := NULL::BIGINT;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT'::t_defendant_transaction_type_enum;
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 10.00::NUMERIC;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT'::t_payment_method_enum;
    v_associated_id     defendant_transactions.associated_record_id%TYPE := 'UT3446-VALID'::VARCHAR;
    v_source            defendant_transactions.source%TYPE := 'BACS'::VARCHAR;
BEGIN
    RAISE NOTICE '=== TEST 3: null account ID ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3401' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3401 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_defendant_account_id is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 4: null transaction type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := NULL;
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 10.00;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := 'UT3446-VALID';
    v_source            defendant_transactions.source%TYPE := 'BACS';
BEGIN
    RAISE NOTICE '=== TEST 4: null transaction type ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3402' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3402 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_transaction_type is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 5: null payment
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := NULL;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := 'UT3446-VALID';
    v_source            defendant_transactions.source%TYPE := 'BACS';
BEGIN
    RAISE NOTICE '=== TEST 5: null payment ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3403' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3403 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_payment_amount is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 6: zero payment
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 0.00;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := 'UT3446-VALID';
    v_source            defendant_transactions.source%TYPE := 'BACS';
BEGIN
    RAISE NOTICE '=== TEST 6: zero payment ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3404' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3404 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_payment_amount must be a positive value', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 7: negative payment
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := -0.01;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := 'UT3446-VALID';
    v_source            defendant_transactions.source%TYPE := 'BACS';
BEGIN
    RAISE NOTICE '=== TEST 7: negative payment ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3404' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3404 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_payment_amount must be a positive value', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 8: null payment method
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 10.00;
    v_payment_method    defendant_transactions.payment_method%TYPE := NULL;
    v_associated_id     defendant_transactions.associated_record_id%TYPE := 'UT3446-VALID';
    v_source            defendant_transactions.source%TYPE := 'BACS';
BEGIN
    RAISE NOTICE '=== TEST 8: null payment method ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3405' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3405 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_payment_method is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 9: null associated record ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 10.00;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := NULL;
    v_source            defendant_transactions.source%TYPE := 'BACS';
BEGIN
    RAISE NOTICE '=== TEST 9: null associated record ID ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3406' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3406 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_associated_record_id is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 10: empty associated record ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 10.00;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := '';
    v_source            defendant_transactions.source%TYPE := 'BACS';
BEGIN
    RAISE NOTICE '=== TEST 10: empty associated record ID ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3406' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3406 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_associated_record_id is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 11: blank associated record ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 10.00;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := '   ';
    v_source            defendant_transactions.source%TYPE := 'BACS';
BEGIN
    RAISE NOTICE '=== TEST 11: blank associated record ID ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3406' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3406 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_associated_record_id is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 12: null source
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 10.00;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := 'UT3446-VALID';
    v_source            defendant_transactions.source%TYPE := NULL;
BEGIN
    RAISE NOTICE '=== TEST 12: null source ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3407' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3407 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_source is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 13: empty source
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 10.00;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := 'UT3446-VALID';
    v_source            defendant_transactions.source%TYPE := '';
BEGIN
    RAISE NOTICE '=== TEST 13: empty source ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3407' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3407 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_source is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Test 14: blank source
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction_id    BIGINT;
    v_error_caught      BOOLEAN := FALSE;
    v_detail            TEXT;
    v_count             BIGINT;
    v_account_id        defendant_transactions.defendant_account_id%TYPE := 999900003446;
    v_transaction_type  defendant_transactions.transaction_type%TYPE := 'PAYMNT';
    v_payment_amount    defendant_transactions.transaction_amount%TYPE := 10.00;
    v_payment_method    defendant_transactions.payment_method%TYPE := 'CT';
    v_associated_id     defendant_transactions.associated_record_id%TYPE := 'UT3446-VALID';
    v_source            defendant_transactions.source%TYPE := '   ';
BEGIN
    RAISE NOTICE '=== TEST 14: blank source ===';

    SELECT COUNT(*) INTO v_count FROM defendant_transactions;
    BEGIN
        CALL p_insert_defendant_transactions(
            pi_defendant_account_id := v_account_id,
            pi_posted_by := 'UT3446'::VARCHAR,
            pi_posted_by_name := 'Unit Test User'::VARCHAR,
            pi_transaction_type := v_transaction_type,
            pi_payment_amount := v_payment_amount,
            pi_payment_method := v_payment_method,
            pi_payment_reference := 'REF'::VARCHAR,
            pi_third_party_payer_name := 'Payer'::VARCHAR,
            pi_associated_record_id := v_associated_id,
            pi_source := v_source,
            po_defendant_transaction_id := v_transaction_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3407' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3407 was not raised';
    ASSERT v_detail = 'p_insert_defendant_transactions: pi_source is required', 'Validation DETAIL must be preserved';
    ASSERT (SELECT COUNT(*) FROM defendant_transactions) = v_count, 'Invalid inputs must not insert a row';

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
-- Cleanup test data
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM defendant_transactions
     WHERE defendant_account_id IN (999900003446);
    DELETE FROM defendant_accounts
     WHERE defendant_account_id IN (999900003446);
    DELETE FROM business_units
     WHERE business_unit_id = 9946;

    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing
