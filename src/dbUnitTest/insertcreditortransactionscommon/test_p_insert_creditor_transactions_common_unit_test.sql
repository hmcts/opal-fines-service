-- PO-10347 DB unit test: run with psql -v ON_ERROR_STOP=1 -f <this file>
-- Uses one existing creditor transaction as a valid fixture, rolls back inserted test rows.
-- IMPORTANT: Run only in a non-production test database.
BEGIN;

DO $test$
DECLARE
    v_fixture public.creditor_transactions%ROWTYPE;
    v_new_id public.creditor_transactions.creditor_transaction_id%TYPE;
    v_new_id_2 public.creditor_transactions.creditor_transaction_id%TYPE;
    v_actual public.creditor_transactions%ROWTYPE;
    v_count integer;
BEGIN
    -- TEST 1: Procedure exists with the required input signature and OUT parameter.
    IF NOT EXISTS (
        SELECT 1 FROM pg_proc p
        JOIN pg_namespace n ON n.oid = p.pronamespace
        WHERE n.nspname = 'public'
          AND p.proname = 'p_insert_creditor_transactions_common'
          AND p.prokind = 'p'
          AND array_length(p.proargtypes::oid[], 1) = 13
    ) THEN
        RAISE EXCEPTION 'TEST 1 FAILED: Common procedure with 13 inputs does not exist';
    END IF;
    RAISE NOTICE 'TEST 1 PASSED: Procedure exists';

    -- Reuse a valid fixture rather than guessing enum/result/foreign-key values.
    SELECT * INTO v_fixture
    FROM public.creditor_transactions
    ORDER BY creditor_transaction_id
    LIMIT 1;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'TEST SETUP FAILED: No creditor_transactions row available. Seed valid test data first.';
    END IF;

    -- TEST 2: Each input maps to its column; generated ID is returned.
    CALL public.p_insert_creditor_transactions_common(
        v_fixture.creditor_account_id,
        v_fixture.posted_date,
        v_fixture.posted_by,
        v_fixture.posted_by_name,
        v_fixture.transaction_type,
        v_fixture.transaction_amount,
        v_fixture.imposition_result_id,
        v_fixture.payment_processed,
        v_fixture.payment_reference,
        v_fixture.status,
        v_fixture.status_date,
        v_fixture.associated_record_type,
        v_fixture.associated_record_id,
        v_new_id
    );

    IF v_new_id IS NULL THEN
        RAISE EXCEPTION 'TEST 2 FAILED: Returned ID is NULL';
    END IF;
    SELECT * INTO STRICT v_actual
    FROM public.creditor_transactions
    WHERE creditor_transaction_id = v_new_id;

    IF ROW(v_actual.creditor_account_id, v_actual.posted_date, v_actual.posted_by,
           v_actual.posted_by_name, v_actual.transaction_type, v_actual.transaction_amount,
           v_actual.imposition_result_id, v_actual.payment_processed, v_actual.payment_reference,
           v_actual.status, v_actual.status_date, v_actual.associated_record_type,
           v_actual.associated_record_id)
       IS DISTINCT FROM
       ROW(v_fixture.creditor_account_id, v_fixture.posted_date, v_fixture.posted_by,
           v_fixture.posted_by_name, v_fixture.transaction_type, v_fixture.transaction_amount,
           v_fixture.imposition_result_id, v_fixture.payment_processed, v_fixture.payment_reference,
           v_fixture.status, v_fixture.status_date, v_fixture.associated_record_type,
           v_fixture.associated_record_id) THEN
        RAISE EXCEPTION 'TEST 2 FAILED: Inserted column values differ from supplied parameters';
    END IF;
    RAISE NOTICE 'TEST 2 PASSED: All input values inserted and generated ID returned: %', v_new_id;

    -- TEST 3: A second invocation returns a different ID.
    CALL public.p_insert_creditor_transactions_common(
        v_fixture.creditor_account_id, v_fixture.posted_date,
        v_fixture.posted_by, v_fixture.posted_by_name,
        v_fixture.transaction_type, v_fixture.transaction_amount,
        v_fixture.imposition_result_id, v_fixture.payment_processed,
        v_fixture.payment_reference, v_fixture.status, v_fixture.status_date,
        v_fixture.associated_record_type, v_fixture.associated_record_id,
        v_new_id_2
    );
    IF v_new_id_2 IS NULL OR v_new_id_2 = v_new_id THEN
        RAISE EXCEPTION 'TEST 3 FAILED: Generated IDs not distinct';
    END IF;
    SELECT COUNT(*) INTO v_count
    FROM public.creditor_transactions
    WHERE creditor_transaction_id IN (v_new_id, v_new_id_2);
    IF v_count <> 2 THEN
        RAISE EXCEPTION 'TEST 3 FAILED: Expected 2 rows; got %', v_count;
    END IF;
    RAISE NOTICE 'TEST 3 PASSED: Two unique IDs created';

    -- TEST 4: NULL required creditor_account_id must propagate NOT NULL violation.
    BEGIN
        CALL public.p_insert_creditor_transactions_common(
            NULL::bigint, v_fixture.posted_date,
            v_fixture.posted_by, v_fixture.posted_by_name,
            v_fixture.transaction_type, v_fixture.transaction_amount,
            v_fixture.imposition_result_id, v_fixture.payment_processed,
            v_fixture.payment_reference, v_fixture.status, v_fixture.status_date,
            v_fixture.associated_record_type, v_fixture.associated_record_id,
            v_new_id_2
        );
        RAISE EXCEPTION 'TEST 4 FAILED: Expected NOT NULL constraint error was not raised';
    EXCEPTION
        WHEN not_null_violation THEN
            RAISE NOTICE 'TEST 4 PASSED: NOT NULL error propagated (SQLSTATE 23502)';
    END;
END;
$test$;
ROLLBACK;
-- Note: PostgreSQL sequence increments are not rolled back.
