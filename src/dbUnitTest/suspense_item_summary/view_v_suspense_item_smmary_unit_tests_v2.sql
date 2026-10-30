/**
 * OPAL Program
 * MODULE      : create_view_v_suspense_item_summary_unit_tests_PO-10344.sql
 * DESCRIPTION : Unit tests for v_suspense_item_summary.version_number.
 *               Creates dedicated fixture data; validates new mapping and existing view behaviour.
 * VERSION HISTORY:
 * 19/10/2026  1.0  PO-10344  Initial dedicated-fixture view tests
 *
 * PREREQUISITE: Migrations adding suspense_items.version_number and updating
 *               v_suspense_item_summary have both been applied.
 * Run with: psql -X -v ON_ERROR_STOP=1 -f <this file>
 * NOTE: psql \timing is a psql meta-command (not supported by every DBeaver execution mode).
 */
\timing

-- Use one transaction. All fixture inserts and deletes are rolled back, even when tests pass.
BEGIN;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    DELETE FROM public.suspense_transactions WHERE suspense_transaction_id IN (344001,344002,344003);
    DELETE FROM public.suspense_items WHERE suspense_item_id IN (344001,344002,344003,344004);
    DELETE FROM public.suspense_accounts WHERE suspense_account_id = 344001;
    DELETE FROM public.business_units WHERE business_unit_id = 3440;
    RAISE NOTICE 'Cleanup completed';
END $$;

DO $$
DECLARE
    v_item_type text;
    v_transaction_type text;
BEGIN
    RAISE NOTICE '=== Setting up test data for v_suspense_item_summary tests ===';

    -- Pick valid enum values from the actual database, without assuming their labels.
    SELECT e.enumlabel INTO v_item_type
      FROM pg_attribute a
      JOIN pg_type t ON t.oid = a.atttypid
      JOIN pg_enum e ON e.enumtypid = t.oid
     WHERE a.attrelid = 'public.suspense_items'::regclass
       AND a.attname = 'suspense_item_type'
       AND a.attnum > 0 AND NOT a.attisdropped
     ORDER BY e.enumsortorder LIMIT 1;

    SELECT e.enumlabel INTO v_transaction_type
      FROM pg_attribute a
      JOIN pg_type t ON t.oid = a.atttypid
      JOIN pg_enum e ON e.enumtypid = t.oid
     WHERE a.attrelid = 'public.suspense_transactions'::regclass
       AND a.attname = 'transaction_type'
       AND a.attnum > 0 AND NOT a.attisdropped
     ORDER BY e.enumsortorder LIMIT 1;

    ASSERT v_item_type IS NOT NULL, 'No suspense_item_type enum label found';
    ASSERT v_transaction_type IS NOT NULL, 'No transaction_type enum label found';

    INSERT INTO public.business_units
        (business_unit_id,business_unit_name,business_unit_code,business_unit_type)
    VALUES
        (3440,'PO-10344 Test Business Unit','P344','Area');

    INSERT INTO public.suspense_accounts
        (suspense_account_id,business_unit_id,account_number)
    VALUES
        (344001,3440,'PO10344-344001');

    -- Format %L yields quoted SQL literals of type unknown which PostgreSQL
    -- can implicitly resolve to the destination enum column during INSERT.
    EXECUTE format($stmt$
        INSERT INTO public.suspense_items
            (suspense_item_id,suspense_account_id,suspense_item_number,
             suspense_item_type,created_date,version_number)
        VALUES
            (344001,344001,344001,%1$L,'2026-10-19 09:00:00',1),
            (344002,344001,344002,%1$L,'2026-10-19 09:05:00',2),
            (344003,344001,344003,%1$L,'2026-10-19 09:10:00',NULL),
            (344004,344001,344004,%1$L,'2026-10-19 09:15:00',3)
    $stmt$,v_item_type);

    EXECUTE format($stmt$
        INSERT INTO public.suspense_transactions
            (suspense_transaction_id,suspense_item_id,posted_date,transaction_type,amount)
        VALUES
            (344001,344004,'2026-10-19 10:00:00',%1$L,10.00),
            (344002,344004,'2026-10-19 10:05:00',%1$L,15.00)
    $stmt$,v_transaction_type);

    RAISE NOTICE 'Test data setup completed';
END $$;

DO $$
DECLARE v_summary public.v_suspense_item_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: Suspense item version_number 1 is returned ===';
    SELECT * INTO STRICT v_summary FROM public.v_suspense_item_summary WHERE suspense_item_id=344001;
    ASSERT v_summary.version_number = 1, 'version_number should be 1';
    ASSERT v_summary.suspense_item_number=344001, 'suspense_item_number should match';
    ASSERT v_summary.business_unit_id=3440, 'business_unit_id should match';
    ASSERT v_summary.business_unit_name='PO-10344 Test Business Unit', 'business_unit_name should match';
    RAISE NOTICE 'TEST 1 PASSED';
END $$;

DO $$
DECLARE v_summary public.v_suspense_item_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: Suspense item version_number 2 is returned ===';
    SELECT * INTO STRICT v_summary FROM public.v_suspense_item_summary WHERE suspense_item_id=344002;
    ASSERT v_summary.version_number = 2, 'version_number should be 2';
    RAISE NOTICE 'TEST 2 PASSED';
END $$;

DO $$
DECLARE v_summary public.v_suspense_item_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 3: NULL version_number remains NULL ===';
    SELECT * INTO STRICT v_summary FROM public.v_suspense_item_summary WHERE suspense_item_id=344003;
    ASSERT v_summary.version_number IS NULL, 'version_number should be NULL';
    RAISE NOTICE 'TEST 3 PASSED';
END $$;

DO $$
DECLARE v_summary public.v_suspense_item_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 4: Multiple transactions are summed without changing version_number ===';
    SELECT * INTO STRICT v_summary FROM public.v_suspense_item_summary WHERE suspense_item_id=344004;
    ASSERT v_summary.version_number = 3, 'version_number should be 3';
    ASSERT v_summary.balance = 25.00, 'balance should equal 10.00 + 15.00';
    RAISE NOTICE 'TEST 4 PASSED';
END $$;

DO $$
DECLARE v_summary public.v_suspense_item_summary%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 5: Suspense item without transactions has zero balance ===';
    SELECT * INTO STRICT v_summary FROM public.v_suspense_item_summary WHERE suspense_item_id=344001;
    ASSERT v_summary.balance = 0.00, 'balance should default to zero';
    RAISE NOTICE 'TEST 5 PASSED';
END $$;

DO $$
DECLARE v_count integer;
BEGIN
    RAISE NOTICE '=== TEST 6: Exactly one view row is returned per fixture suspense item ===';
    SELECT count(*) INTO v_count
      FROM public.v_suspense_item_summary
     WHERE suspense_item_id IN (344001,344002,344003,344004);
    ASSERT v_count=4, 'view must return four rows for four suspense items';
    RAISE NOTICE 'TEST 6 PASSED';
END $$;

DO $$
DECLARE v_mismatch_count integer;
BEGIN
    RAISE NOTICE '=== TEST 7: View version_number matches source table including NULL ===';
    SELECT count(*) INTO v_mismatch_count
      FROM public.suspense_items si
      JOIN public.v_suspense_item_summary v ON v.suspense_item_id=si.suspense_item_id
     WHERE si.suspense_item_id IN (344001,344002,344003,344004)
       AND si.version_number IS DISTINCT FROM v.version_number;
    ASSERT v_mismatch_count=0, 'view version_number must match suspense_items';
    RAISE NOTICE 'TEST 7 PASSED';
END $$;

DO $$
DECLARE v_data_type text;
BEGIN
    RAISE NOTICE '=== TEST 8: View version_number has bigint data type ===';
    SELECT data_type INTO v_data_type
      FROM information_schema.columns
     WHERE table_schema='public'
       AND table_name='v_suspense_item_summary'
       AND column_name='version_number';
    ASSERT v_data_type='bigint', 'view version_number must have bigint data type';
    RAISE NOTICE 'TEST 8 PASSED';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data after tests ===';
    DELETE FROM public.suspense_transactions WHERE suspense_transaction_id IN (344001,344002,344003);
    DELETE FROM public.suspense_items WHERE suspense_item_id IN (344001,344002,344003,344004);
    DELETE FROM public.suspense_accounts WHERE suspense_account_id=344001;
    DELETE FROM public.business_units WHERE business_unit_id=3440;
    RAISE NOTICE 'Cleanup completed';
END $$;

-- Even if cleanup passes, preserve any pre-existing database state.
ROLLBACK;
