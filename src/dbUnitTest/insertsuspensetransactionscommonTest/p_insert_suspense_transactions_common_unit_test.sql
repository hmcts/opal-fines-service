
/**
 * OPAL Program
 *
 * MODULE      : test_p_insert_suspense_transactions_common.sql
 *
 * DESCRIPTION : Unit tests for inserting suspense transactions.
 *
 * VERSION HISTORY:
 *
 * Date         Author       Version   Nature of Change
 * -----------  -----------  --------  --------------------------
 * 21/10/2026   I Chandio     0.1       PO-10356 p_insert_suspense_transactions_common unit test
 */

\set ON_ERROR_STOP on
\timing on

BEGIN;

-- TEST 1: Verify procedure exists.

DO $$
BEGIN
    RAISE NOTICE 'TEST 1: Procedure existence';

    IF NOT EXISTS (
        SELECT 1
        FROM pg_proc p
        JOIN pg_namespace n
          ON n.oid = p.pronamespace
        WHERE n.nspname = 'public'
          AND p.proname = 'p_insert_suspense_transactions_common'
          AND p.prokind = 'p'
    ) THEN
        RAISE EXCEPTION
            'TEST 1 FAILED: Procedure does not exist';
    END IF;

    RAISE NOTICE 'TEST 1 PASSED';
END;
$$;


-- TEST 2: Insert suspense transaction.
-- TEST 3: Validate inserted data.
-- TEST 4: Verify exception propagation.

DO $$
DECLARE
    v_item_id BIGINT;

    v_transaction_type
        public.suspense_transactions.transaction_type%TYPE;

    v_new_id BIGINT;

    v_actual public.suspense_transactions%ROWTYPE;

    v_posted_date TIMESTAMP := CURRENT_TIMESTAMP;
    v_posted_by VARCHAR := 'PO-10346-TEST';
    v_posted_by_name VARCHAR := 'Unit Test';
    v_amount NUMERIC := 25.50;
    v_text VARCHAR := 'PO-10346 unit test transaction';

    v_error_sqlstate TEXT;
BEGIN

    -- Select an existing suspense item.
    SELECT suspense_item_id
    INTO v_item_id
    FROM public.suspense_items
    ORDER BY suspense_item_id
    LIMIT 1;

    IF v_item_id IS NULL THEN
        RAISE EXCEPTION
            'TEST FAILED: No suspense item fixture exists';
    END IF;

    -- Retrieve a valid transaction type from the enum.
    SELECT e.enumlabel::public.t_suspense_transaction_type_enum
    INTO v_transaction_type
    FROM pg_enum e
    WHERE e.enumtypid =
        'public.t_suspense_transaction_type_enum'::regtype
    ORDER BY e.enumsortorder
    LIMIT 1;

    IF v_transaction_type IS NULL THEN
        RAISE EXCEPTION
            'TEST FAILED: No transaction type enum value exists';
    END IF;

    -- TEST 2
    RAISE NOTICE 'TEST 2: Insert suspense transaction';

    CALL public.p_insert_suspense_transactions_common(
        v_item_id,
        v_posted_date,
        v_posted_by,
        v_posted_by_name,
        v_transaction_type,
        v_amount,
        NULL,
        NULL,
        v_text,
        NULL,
        v_new_id
    );

    IF v_new_id IS NULL THEN
        RAISE EXCEPTION
            'TEST 2 FAILED: Returned ID is NULL';
    END IF;

    SELECT *
    INTO STRICT v_actual
    FROM public.suspense_transactions
    WHERE suspense_transaction_id = v_new_id;

    RAISE NOTICE
        'TEST 2 PASSED: Created transaction ID %',
        v_new_id;


    -- TEST 3
    RAISE NOTICE 'TEST 3: Validate inserted values';

    IF v_actual.suspense_item_id IS DISTINCT FROM v_item_id
       OR v_actual.posted_date IS DISTINCT FROM v_posted_date
       OR v_actual.posted_by IS DISTINCT FROM v_posted_by
       OR v_actual.posted_by_name IS DISTINCT FROM v_posted_by_name
       OR v_actual.transaction_type IS DISTINCT FROM v_transaction_type
       OR v_actual.amount IS DISTINCT FROM v_amount
       OR v_actual.associated_record_type IS NOT NULL
       OR v_actual.associated_record_id IS NOT NULL
       OR v_actual.text IS DISTINCT FROM v_text
       OR v_actual.reversed IS NOT NULL
    THEN
        RAISE EXCEPTION
            'TEST 3 FAILED: Inserted data mismatch';
    END IF;

    RAISE NOTICE 'TEST 3 PASSED';


    -- TEST 4
    -- Pass NULL for the NOT NULL suspense_item_id column.

    RAISE NOTICE 'TEST 4: Exception propagation';

    v_error_sqlstate := NULL;

    BEGIN
        CALL public.p_insert_suspense_transactions_common(
            NULL,
            v_posted_date,
            v_posted_by,
            v_posted_by_name,
            v_transaction_type,
            v_amount,
            NULL,
            NULL,
            'PO-10346 invalid transaction',
            NULL,
            v_new_id
        );

    EXCEPTION
        WHEN OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_sqlstate = RETURNED_SQLSTATE;
    END;

    IF v_error_sqlstate IS DISTINCT FROM '23502' THEN
        RAISE EXCEPTION
            'TEST 4 FAILED: Expected 23502, received %',
            v_error_sqlstate;
    END IF;

    RAISE NOTICE
        'TEST 4 PASSED: Original database exception propagated';

    RAISE NOTICE
        'ALL PO-10346 UNIT TESTS PASSED';

END;
$$;


-- Roll back all test data.
ROLLBACK;

\echo 'PO-10346 tests completed and rolled back'
