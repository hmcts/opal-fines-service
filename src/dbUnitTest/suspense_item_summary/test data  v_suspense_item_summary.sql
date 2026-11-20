/**
 * OPAL Program
 *
 * MODULE      : create_view_v_suspense_item_summary_unit_tests.sql
 *
 * DESCRIPTION : Unit tests for v_suspense_item_summary.
 *
 * TEST COVERAGE:
 *
 *   TEST 1 - Found suspense item returns expected summary.
 *
 *   TEST 2 - Unknown suspense reference returns no rows.
 *
 *   TEST 3 - Suspense item with no transactions has zero balance.
 *
 *   TEST 4 - Transactions which sum to zero have zero balance.
 *
 *   TEST 5 - Multiple positive/negative transactions calculate
 *            the correct balance.
 *
 *   TEST 6 - Same suspense_item_number can be correctly retrieved
 *            for a different business unit.
 *
 *   TEST 7 - Legacy suspense item without a record in
 *            suspense_item_number_index is still returned.
 *
 * TEST DATA:
 *
 *   BU 9900 / 100001 -> 100 + 50 - 25 = 125.00
 *   BU 9900 / 100002 -> no transactions  = 0.00
 *   BU 9900 / 100003 -> 100 - 100        = 0.00
 *   BU 9900 / 100004 -> 200 - 25 - 25    = 150.00
 *   BU 9901 / 100001 -> 999              = 999.00
 *
 *   Legacy:
 *   BU 9900 / 30001  -> 50               = 50.00
 *
 *   The legacy suspense item deliberately has NO
 *   suspense_item_number_index record.
 */


/* ============================================================
   CLEANUP BEFORE TESTS

   Allows the script to be rerun safely following a previous
   successful or partially completed execution.
   ============================================================ */

DO $$
BEGIN

    RAISE NOTICE '=== Cleanup data before v_suspense_item_summary tests ===';


    ------------------------------------------------------------
    -- Child records must be removed before their parent records.
    ------------------------------------------------------------

    DELETE FROM public.suspense_transactions
    WHERE suspense_item_id IN (
        990001,
        990002,
        990003,
        990004,
        990005,
        990006
    );


    DELETE FROM public.suspense_item_number_index
    WHERE business_unit_id IN (9900, 9901)
      AND suspense_item_number IN (
          100001,
          100002,
          100003,
          100004
      );


    DELETE FROM public.suspense_items
    WHERE suspense_item_id IN (
        990001,
        990002,
        990003,
        990004,
        990005,
        990006
    );


    DELETE FROM public.suspense_accounts
    WHERE suspense_account_id IN (
        990001,
        990002
    );


    DELETE FROM public.business_units
    WHERE business_unit_id IN (
        9900,
        9901
    );


    RAISE NOTICE 'Cleanup completed';

END $$;


/* ============================================================
   SETUP TEST DATA
   ============================================================ */

DO $$
BEGIN

    RAISE NOTICE '=== Setting up test data for v_suspense_item_summary ===';


    /* ========================================================
       BUSINESS UNITS
       ======================================================== */

    INSERT INTO public.business_units (
        business_unit_id,
        business_unit_name,
        business_unit_code,
        business_unit_type
    )
    VALUES
    (
        9900,
        'Suspense Summary Test BU A',
        'S990',
        'Accounting Division'
    ),
    (
        9901,
        'Suspense Summary Test BU B',
        'S991',
        'Area'
    );


    /* ========================================================
       SUSPENSE ACCOUNTS

       Each test business unit has its own suspense account.
       ======================================================== */

    INSERT INTO public.suspense_accounts (
        suspense_account_id,
        business_unit_id,
        account_number
    )
    VALUES
    (
        990001,
        9900,
        'TEST-SUSPENSE-A'
    ),
    (
        990002,
        9901,
        'TEST-SUSPENSE-B'
    );


    /* ========================================================
       SUSPENSE ITEMS

       100001-100004 are Opal references because they are
       greater than or equal to 100000.

       30001 represents a legacy GOB suspense reference.
       ======================================================== */

    INSERT INTO public.suspense_items (
        suspense_item_id,
        suspense_account_id,
        suspense_item_number,
        suspense_item_type,
        created_date
    )
    VALUES

    ------------------------------------------------------------
    -- TEST 1
    -- Found item / balance = 125
    ------------------------------------------------------------
    (
        990001,
        990001,
        100001,
        'UN',
        '2026-10-01 10:00:00'
    ),

    ------------------------------------------------------------
    -- TEST 3
    -- No transactions / balance = 0
    ------------------------------------------------------------
    (
        990002,
        990001,
        100002,
        'UN',
        '2026-10-01 10:01:00'
    ),

    ------------------------------------------------------------
    -- TEST 4
    -- Transactions sum to zero
    ------------------------------------------------------------
    (
        990003,
        990001,
        100003,
        'UN',
        '2026-10-01 10:02:00'
    ),

    ------------------------------------------------------------
    -- TEST 5
    -- Multiple transactions / balance = 150
    ------------------------------------------------------------
    (
        990004,
        990001,
        100004,
        'UN',
        '2026-10-01 10:03:00'
    ),

    ------------------------------------------------------------
    -- TEST 6
    -- Same reference as 990001 but different business unit
    ------------------------------------------------------------
    (
        990005,
        990002,
        100001,
        'UN',
        '2026-10-01 11:00:00'
    ),

    ------------------------------------------------------------
    -- TEST 7
    -- Legacy suspense item.
    --
    -- Deliberately NO suspense_item_number_index record.
    ------------------------------------------------------------
    (
        990006,
        990001,
        30001,
        'UN',
        '2026-10-01 12:00:00'
    );


    /* ========================================================
       SUSPENSE ITEM NUMBER INDEX

       Opal-generated suspense references >= 100000 have an
       associated business_unit_id / suspense_item_number entry.

       Notice:
           30001 is NOT inserted.

       This deliberately tests the LEFT JOIN behaviour for
       legacy GOB suspense items.
       ======================================================== */

    INSERT INTO public.suspense_item_number_index (
        suspense_item_number,
        business_unit_id
    )
    VALUES
    (
        100001,
        9900
    ),
    (
        100002,
        9900
    ),
    (
        100003,
        9900
    ),
    (
        100004,
        9900
    ),
    (
        100001,
        9901
    );


    /* ========================================================
       SUSPENSE TRANSACTIONS
       ======================================================== */


    ------------------------------------------------------------
    -- Item 990001
    --
    -- 100.00
    --  50.00
    -- -25.00
    -- -------
    -- 125.00
    ------------------------------------------------------------

    INSERT INTO public.suspense_transactions (
        suspense_transaction_id,
        suspense_item_id,
        posted_date,
        transaction_type,
        amount
    )
    VALUES
    (
        990001,
        990001,
        '2026-10-01 10:10:00',
        'UN',
        100.00
    ),
    (
        990002,
        990001,
        '2026-10-01 10:11:00',
        'UN',
        50.00
    ),
    (
        990003,
        990001,
        '2026-10-01 10:12:00',
        'UN',
        -25.00
    );


    ------------------------------------------------------------
    -- Item 990002
    --
    -- Intentionally NO transactions.
    --
    -- Expected balance = 0.00
    ------------------------------------------------------------


    ------------------------------------------------------------
    -- Item 990003
    --
    -- 100 - 100 = 0
    ------------------------------------------------------------

    INSERT INTO public.suspense_transactions (
        suspense_transaction_id,
        suspense_item_id,
        posted_date,
        transaction_type,
        amount
    )
    VALUES
    (
        990004,
        990003,
        '2026-10-01 10:20:00',
        'UN',
        100.00
    ),
    (
        990005,
        990003,
        '2026-10-01 10:21:00',
        'UN',
        -100.00
    );


    ------------------------------------------------------------
    -- Item 990004
    --
    -- 200 - 25 - 25 = 150
    ------------------------------------------------------------

    INSERT INTO public.suspense_transactions (
        suspense_transaction_id,
        suspense_item_id,
        posted_date,
        transaction_type,
        amount
    )
    VALUES
    (
        990006,
        990004,
        '2026-10-01 10:30:00',
        'UN',
        200.00
    ),
    (
        990007,
        990004,
        '2026-10-01 10:31:00',
        'UN',
        -25.00
    ),
    (
        990008,
        990004,
        '2026-10-01 10:32:00',
        'UN',
        -25.00
    );


    ------------------------------------------------------------
    -- Item 990005
    --
    -- Same suspense reference 100001 but for BU 9901.
    --
    -- Expected balance = 999
    ------------------------------------------------------------

    INSERT INTO public.suspense_transactions (
        suspense_transaction_id,
        suspense_item_id,
        posted_date,
        transaction_type,
        amount
    )
    VALUES
    (
        990009,
        990005,
        '2026-10-01 11:10:00',
        'UN',
        999.00
    );


    ------------------------------------------------------------
    -- Item 990006
    --
    -- Legacy item without suspense_item_number_index record.
    ------------------------------------------------------------

    INSERT INTO public.suspense_transactions (
        suspense_transaction_id,
        suspense_item_id,
        posted_date,
        transaction_type,
        amount
    )
    VALUES
    (
        990010,
        990006,
        '2026-10-01 12:10:00',
        'UN',
        50.00
    );


    RAISE NOTICE 'Test data setup completed';

END $$;


/* ============================================================
   TEST 1
   FOUND
   ============================================================ */

DO $$
DECLARE

    v_summary public.v_suspense_item_summary%ROWTYPE;

BEGIN

    RAISE NOTICE '=== TEST 1: Found suspense item returns expected summary ===';


    SELECT *
    INTO STRICT v_summary
    FROM public.v_suspense_item_summary
    WHERE business_unit_id = 9900
      AND suspense_item_number = 100001;


    ASSERT v_summary.suspense_item_id = 990001,
        'suspense_item_id should be 990001';

    ASSERT v_summary.suspense_item_number = 100001,
        'suspense_item_number should be 100001';

    ASSERT v_summary.business_unit_id = 9900,
        'business_unit_id should be 9900';

    ASSERT v_summary.business_unit_name = 'Suspense Summary Test BU A',
        'business_unit_name should match';

    ASSERT v_summary.created_date = '2026-10-01 10:00:00',
        'created_date should match';

    ASSERT v_summary.suspense_item_type = 'UN',
        'suspense_item_type should be UN';

    ASSERT v_summary.balance = 125.00,
        'balance should equal 100 + 50 - 25 = 125';


    RAISE NOTICE 'TEST 1 PASSED';

END $$;


/* ============================================================
   TEST 2
   NOT FOUND
   ============================================================ */

DO $$
DECLARE

    v_row_count INTEGER;

BEGIN

    RAISE NOTICE '=== TEST 2: Unknown suspense reference returns no rows ===';


    SELECT COUNT(*)
    INTO v_row_count
    FROM public.v_suspense_item_summary
    WHERE business_unit_id = 9900
      AND suspense_item_number = 199999;


    ASSERT v_row_count = 0,
        'Unknown suspense reference should return zero rows';


    RAISE NOTICE 'TEST 2 PASSED';

END $$;


/* ============================================================
   TEST 3
   NO TRANSACTIONS = ZERO BALANCE
   ============================================================ */

DO $$
DECLARE

    v_summary public.v_suspense_item_summary%ROWTYPE;

BEGIN

    RAISE NOTICE '=== TEST 3: No transactions returns zero balance ===';


    SELECT *
    INTO STRICT v_summary
    FROM public.v_suspense_item_summary
    WHERE business_unit_id = 9900
      AND suspense_item_number = 100002;


    ASSERT v_summary.suspense_item_id = 990002,
        'Correct suspense item should be returned';

    ASSERT v_summary.balance = 0.00,
        'Balance should be zero when no transactions exist';


    RAISE NOTICE 'TEST 3 PASSED';

END $$;


/* ============================================================
   TEST 4
   TRANSACTIONS SUM TO ZERO
   ============================================================ */

DO $$
DECLARE

    v_summary public.v_suspense_item_summary%ROWTYPE;

BEGIN

    RAISE NOTICE '=== TEST 4: Transactions summing to zero return zero balance ===';


    SELECT *
    INTO STRICT v_summary
    FROM public.v_suspense_item_summary
    WHERE business_unit_id = 9900
      AND suspense_item_number = 100003;


    ASSERT v_summary.suspense_item_id = 990003,
        'Correct suspense item should be returned';

    ASSERT v_summary.balance = 0.00,
        'Balance should equal 100 - 100 = 0';


    RAISE NOTICE 'TEST 4 PASSED';

END $$;


/* ============================================================
   TEST 5
   MULTIPLE TRANSACTIONS
   ============================================================ */

DO $$
DECLARE

    v_summary public.v_suspense_item_summary%ROWTYPE;

BEGIN

    RAISE NOTICE '=== TEST 5: Multiple transactions calculate correct balance ===';


    SELECT *
    INTO STRICT v_summary
    FROM public.v_suspense_item_summary
    WHERE business_unit_id = 9900
      AND suspense_item_number = 100004;


    ASSERT v_summary.suspense_item_id = 990004,
        'Correct suspense item should be returned';

    ASSERT v_summary.balance = 150.00,
        'Balance should equal 200 - 25 - 25 = 150';


    RAISE NOTICE 'TEST 5 PASSED';

END $$;


/* ============================================================
   TEST 6
   BUSINESS UNIT + SUSPENSE REFERENCE
   ============================================================ */

DO $$
DECLARE

    v_summary public.v_suspense_item_summary%ROWTYPE;

BEGIN

    RAISE NOTICE '=== TEST 6: Same suspense reference is isolated by business unit ===';


    SELECT *
    INTO STRICT v_summary
    FROM public.v_suspense_item_summary
    WHERE business_unit_id = 9901
      AND suspense_item_number = 100001;


    ASSERT v_summary.suspense_item_id = 990005,
        'Suspense item for BU 9901 should be returned';

    ASSERT v_summary.business_unit_id = 9901,
        'business_unit_id should be 9901';

    ASSERT v_summary.business_unit_name = 'Suspense Summary Test BU B',
        'business unit name should match BU 9901';

    ASSERT v_summary.balance = 999.00,
        'Balance should belong to the BU 9901 suspense item';


    RAISE NOTICE 'TEST 6 PASSED';

END $$;


/* ============================================================
   TEST 7
   LEGACY GOB SUSPENSE ITEM
   ============================================================ */

DO $$
DECLARE

    v_summary public.v_suspense_item_summary%ROWTYPE;
    v_index_count INTEGER;

BEGIN

    RAISE NOTICE '=== TEST 7: Legacy item without number index record is returned ===';


    ------------------------------------------------------------
    -- First prove that no suspense_item_number_index record
    -- exists for this legacy reference.
    ------------------------------------------------------------

    SELECT COUNT(*)
    INTO v_index_count
    FROM public.suspense_item_number_index
    WHERE business_unit_id = 9900
      AND suspense_item_number = 30001;


    ASSERT v_index_count = 0,
        'Legacy suspense item should not have an index record';


    ------------------------------------------------------------
    -- The item should nevertheless still be returned by the
    -- view because suspense_item_number_index is a LEFT JOIN.
    ------------------------------------------------------------

    SELECT *
    INTO STRICT v_summary
    FROM public.v_suspense_item_summary
    WHERE business_unit_id = 9900
      AND suspense_item_number = 30001;


    ASSERT v_summary.suspense_item_id = 990006,
        'Legacy suspense item should be returned';

    ASSERT v_summary.suspense_item_number = 30001,
        'Legacy suspense reference should match';

    ASSERT v_summary.balance = 50.00,
        'Legacy suspense item balance should be 50';


    RAISE NOTICE 'TEST 7 PASSED';

END $$;


/* ============================================================
   CLEANUP AFTER TESTS
   ============================================================ */

DO $$
BEGIN

    RAISE NOTICE '=== Cleanup data after v_suspense_item_summary tests ===';


    DELETE FROM public.suspense_transactions
    WHERE suspense_item_id IN (
        990001,
        990002,
        990003,
        990004,
        990005,
        990006
    );


    DELETE FROM public.suspense_item_number_index
    WHERE business_unit_id IN (9900, 9901)
      AND suspense_item_number IN (
          100001,
          100002,
          100003,
          100004
      );


    DELETE FROM public.suspense_items
    WHERE suspense_item_id IN (
        990001,
        990002,
        990003,
        990004,
        990005,
        990006
    );


    DELETE FROM public.suspense_accounts
    WHERE suspense_account_id IN (
        990001,
        990002
    );


    DELETE FROM public.business_units
    WHERE business_unit_id IN (
        9900,
        9901
    );


    RAISE NOTICE 'Cleanup completed';

END $$;