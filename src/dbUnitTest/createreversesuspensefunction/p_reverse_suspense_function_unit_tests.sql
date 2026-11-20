
/**
 * OPAL Program
 *
 * MODULE      : p_reverse_suspense_function_unit_tests.sql
 * DESCRIPTION : Unit tests for p_reverse_suspense_function.
 *               Validates standard reversals, dishonoured cheque
 *               reversals, report entries, optimistic concurrency,
 *               rollback, balance validation and mandatory parameters.
 *
 * VERSION HISTORY:
 * Date        Author      Version  Nature of Change
 * 18/11/2026  I Chandio   1.0      Initial unit tests for PO-10350
 **/

BEGIN;

DO $test$
DECLARE
    v_bu_id business_units.business_unit_id%TYPE;
    v_account_id suspense_accounts.suspense_account_id%TYPE;
    v_item_id suspense_items.suspense_item_id%TYPE;
    v_test_id suspense_items.suspense_item_id%TYPE;
    v_transaction_id suspense_transactions.suspense_transaction_id%TYPE;
    v_balance suspense_transactions.amount%TYPE;
    v_version suspense_items.version_number%TYPE;

    v_count INTEGER;
    v_report_count INTEGER;
    v_failed BOOLEAN;
    v_sqlstate TEXT;
    v_detail TEXT;
    v_test_number INTEGER;
    v_expected_code TEXT;

BEGIN

    --------------------------------------------------------------------
    -- SETUP: Create isolated test data.
    --------------------------------------------------------------------

    SELECT candidate::SMALLINT
      INTO v_bu_id
      FROM generate_series(9000, 9999) AS candidate
     WHERE NOT EXISTS (
         SELECT 1
           FROM business_units
          WHERE business_unit_id = candidate
     )
       AND NOT EXISTS (
         SELECT 1
           FROM business_units
          WHERE business_unit_code = candidate::VARCHAR
     )
     LIMIT 1;

    IF v_bu_id IS NULL THEN
        RAISE EXCEPTION
            'No unused business unit available for PO-10350 tests';
    END IF;

    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_code,
        business_unit_type
    )
    VALUES (
        v_bu_id,
        'PO-10350 Unit Test',
        v_bu_id::VARCHAR,
        'Area'::t_business_unit_type_enum
    );

    SELECT COALESCE(MAX(suspense_account_id), 0) + 100000
      INTO v_account_id
      FROM suspense_accounts;

    INSERT INTO suspense_accounts (
        suspense_account_id,
        business_unit_id,
        account_number
    )
    VALUES (
        v_account_id,
        v_bu_id,
        'PO10350-' || v_bu_id
    );

    SELECT COALESCE(MAX(suspense_item_id), 0) + 100000
      INTO v_item_id
      FROM suspense_items;

    --------------------------------------------------------------------
    -- Create five suspense items.
    --
    -- Item 0: +125.00 Standard reversal
    -- Item 1:  +75.00 Dishonoured cheque reversal
    -- Item 2:  -10.00 Negative balance
    -- Item 3:    0.00 Zero balance
    -- Item 4:  +50.00 Stale version test
    --------------------------------------------------------------------

    INSERT INTO suspense_items (
        suspense_item_id,
        suspense_account_id,
        suspense_item_number,
        suspense_item_type,
        created_date,
        version_number
    )
    SELECT
        v_item_id + n,
        v_account_id,
        v_item_id + n,
        'UN'::t_suspense_item_type_enum,
        clock_timestamp(),
        1
    FROM generate_series(0, 4) AS n;

    SELECT COALESCE(MAX(suspense_transaction_id), 0) + 100000
      INTO v_transaction_id
      FROM suspense_transactions;

    --------------------------------------------------------------------
    -- Seed transactions for items 0, 1, 2 and 4.
    -- Item 3 deliberately has no transactions.
    --------------------------------------------------------------------

    INSERT INTO suspense_transactions (
        suspense_transaction_id,
        suspense_item_id,
        posted_date,
        posted_by,
        posted_by_name,
        transaction_type,
        amount
    )
    SELECT
        v_transaction_id + n,
        v_item_id + n,
        clock_timestamp(),
        'PO10350_TEST',
        'PO-10350 Unit Test',
        'UN'::t_suspense_transaction_type_enum,
        CASE n
            WHEN 0 THEN 125.00
            WHEN 1 THEN 75.00
            WHEN 2 THEN -10.00
            WHEN 4 THEN 50.00
        END
    FROM generate_series(0, 4) AS n
    WHERE n <> 3;

    RAISE NOTICE
        'PO-10350: Test data created successfully';

    --------------------------------------------------------------------
    -- TEST 01: Standard reversal reduces balance to zero.
    --------------------------------------------------------------------

    CALL p_reverse_suspense_function(
        v_item_id,
        1,
        'PO10350_TEST',
        'PO-10350 Unit Test',
        FALSE
    );

    SELECT COALESCE(SUM(amount), 0)
      INTO v_balance
      FROM suspense_transactions
     WHERE suspense_item_id = v_item_id;

    IF v_balance <> 0 THEN
        RAISE EXCEPTION
            'TEST 01 FAILED: Expected balance 0, actual %',
            v_balance;
    END IF;

    RAISE NOTICE
        'TEST 01 PASSED: Suspense balance reversed';

    --------------------------------------------------------------------
    -- TEST 02: RV transaction created with correct amount.
    --------------------------------------------------------------------

    SELECT COUNT(*)
      INTO v_count
      FROM suspense_transactions
     WHERE suspense_item_id = v_item_id
       AND transaction_type = 'RV'
       AND amount = -125.00;

    IF v_count <> 1 THEN
        RAISE EXCEPTION
            'TEST 02 FAILED: Expected one RV transaction, found %',
            v_count;
    END IF;

    RAISE NOTICE
        'TEST 02 PASSED: RV transaction created';

    --------------------------------------------------------------------
    -- TEST 03: Reversed Payments report entry created.
    --------------------------------------------------------------------

    SELECT COUNT(*)
      INTO v_report_count
      FROM report_entries re
      JOIN suspense_transactions st
        ON re.associated_record_id =
           st.suspense_transaction_id::VARCHAR
     WHERE st.suspense_item_id = v_item_id
       AND st.transaction_type = 'RV'
       AND re.report_id = 'reversed_payments_report'
       AND re.business_unit_id = v_bu_id
       AND re.associated_record_type =
           'suspense_transactions'::t_associated_record_type_enum;

    IF v_report_count <> 1 THEN
        RAISE EXCEPTION
            'TEST 03 FAILED: Expected one report entry, found %',
            v_report_count;
    END IF;

    RAISE NOTICE
        'TEST 03 PASSED: Report entry created';

    --------------------------------------------------------------------
    -- TEST 04: Successful reversal increments version.
    --------------------------------------------------------------------

    SELECT version_number
      INTO v_version
      FROM suspense_items
     WHERE suspense_item_id = v_item_id;

    IF v_version IS DISTINCT FROM 2 THEN
        RAISE EXCEPTION
            'TEST 04 FAILED: Expected version 2, actual %',
            v_version;
    END IF;

    RAISE NOTICE
        'TEST 04 PASSED: Version incremented';

    --------------------------------------------------------------------
    -- TEST 05: Stale version rejected.
    --
    -- Use item 4 with a positive balance of 50.00 and version 1.
    -- Pass version 999 to force optimistic concurrency failure.
    --------------------------------------------------------------------

    v_test_id := v_item_id + 4;
    v_failed := FALSE;

    BEGIN
        CALL p_reverse_suspense_function(
            v_test_id,
            999,
            'PO10350_TEST',
            'PO-10350 Unit Test',
            FALSE
        );

    EXCEPTION WHEN OTHERS THEN

        IF SQLSTATE = 'P0001'
           AND SQLERRM =
               'Some information on this page may be out of date.'
        THEN
            v_failed := TRUE;
        ELSE
            RAISE;
        END IF;

    END;

    IF NOT v_failed THEN
        RAISE EXCEPTION
            'TEST 05 FAILED: Stale version was accepted';
    END IF;

    RAISE NOTICE
        'TEST 05 PASSED: Stale version rejected';

    --------------------------------------------------------------------
    -- TEST 06: Failed reversal does not persist any changes.
    --------------------------------------------------------------------

    SELECT COUNT(*)
      INTO v_count
      FROM suspense_transactions
     WHERE suspense_item_id = v_test_id
       AND transaction_type = 'RV';

    IF v_count <> 0 THEN
        RAISE EXCEPTION
            'TEST 06 FAILED: Failed reversal persisted RV transaction';
    END IF;

    SELECT COALESCE(SUM(amount), 0)
      INTO v_balance
      FROM suspense_transactions
     WHERE suspense_item_id = v_test_id;

    IF v_balance <> 50.00 THEN
        RAISE EXCEPTION
            'TEST 06 FAILED: Expected balance 50.00, actual %',
            v_balance;
    END IF;

    SELECT COUNT(*)
      INTO v_report_count
      FROM report_entries re
      JOIN suspense_transactions st
        ON re.associated_record_id =
           st.suspense_transaction_id::VARCHAR
     WHERE st.suspense_item_id = v_test_id
       AND re.report_id = 'reversed_payments_report'
       AND re.associated_record_type =
           'suspense_transactions'::t_associated_record_type_enum;

    IF v_report_count <> 0 THEN
        RAISE EXCEPTION
            'TEST 06 FAILED: Failed reversal persisted report entry';
    END IF;

    SELECT version_number
      INTO v_version
      FROM suspense_items
     WHERE suspense_item_id = v_test_id;

    IF v_version IS DISTINCT FROM 1 THEN
        RAISE EXCEPTION
            'TEST 06 FAILED: Version changed to %',
            v_version;
    END IF;

    RAISE NOTICE
        'TEST 06 PASSED: Failed reversal rolled back';

    --------------------------------------------------------------------
    -- TEST 07: Dishonoured cheque creates DQ transaction.
    --------------------------------------------------------------------

    v_test_id := v_item_id + 1;

    CALL p_reverse_suspense_function(
        v_test_id,
        1,
        'PO10350_TEST',
        'PO-10350 Unit Test',
        TRUE
    );

    SELECT COUNT(*)
      INTO v_count
      FROM suspense_transactions
     WHERE suspense_item_id = v_test_id
       AND transaction_type = 'DQ'
       AND amount = -75.00;

    IF v_count <> 1 THEN
        RAISE EXCEPTION
            'TEST 07 FAILED: Expected one DQ transaction, found %',
            v_count;
    END IF;

    RAISE NOTICE
        'TEST 07 PASSED: Dishonoured cheque reversal created';

    --------------------------------------------------------------------
    -- TEST 08: Negative balance rejected with P4006.
    --------------------------------------------------------------------

    v_test_id := v_item_id + 2;
    v_failed := FALSE;

    BEGIN
        CALL p_reverse_suspense_function(
            v_test_id,
            1,
            'PO10350_TEST',
            'PO-10350 Unit Test',
            FALSE
        );

    EXCEPTION WHEN OTHERS THEN

        GET STACKED DIAGNOSTICS
            v_sqlstate = RETURNED_SQLSTATE,
            v_detail = PG_EXCEPTION_DETAIL;

        IF v_sqlstate = 'P4006'
           AND v_detail =
               'p_reverse_suspense_function: suspense item balance must be greater than zero'
        THEN
            v_failed := TRUE;
        ELSE
            RAISE EXCEPTION
                'TEST 08 FAILED: Expected P4006, received %. Detail: %',
                v_sqlstate,
                v_detail;
        END IF;

    END;

    IF NOT v_failed THEN
        RAISE EXCEPTION
            'TEST 08 FAILED: Negative balance was accepted';
    END IF;

    RAISE NOTICE
        'TEST 08 PASSED: Negative balance rejected (P4006)';

    --------------------------------------------------------------------
    -- TEST 09: Zero balance rejected with P4006.
    --------------------------------------------------------------------

    v_test_id := v_item_id + 3;
    v_failed := FALSE;

    BEGIN
        CALL p_reverse_suspense_function(
            v_test_id,
            1,
            'PO10350_TEST',
            'PO-10350 Unit Test',
            FALSE
        );

    EXCEPTION WHEN OTHERS THEN

        GET STACKED DIAGNOSTICS
            v_sqlstate = RETURNED_SQLSTATE,
            v_detail = PG_EXCEPTION_DETAIL;

        IF v_sqlstate = 'P4006'
           AND v_detail =
               'p_reverse_suspense_function: suspense item balance must be greater than zero'
        THEN
            v_failed := TRUE;
        ELSE
            RAISE EXCEPTION
                'TEST 09 FAILED: Expected P4006, received %. Detail: %',
                v_sqlstate,
                v_detail;
        END IF;

    END;

    IF NOT v_failed THEN
        RAISE EXCEPTION
            'TEST 09 FAILED: Zero balance was accepted';
    END IF;

    RAISE NOTICE
        'TEST 09 PASSED: Zero balance rejected (P4006)';

    --------------------------------------------------------------------
    -- TEST 10: Zero balance rejection leaves data unchanged.
    --------------------------------------------------------------------

    SELECT COUNT(*)
      INTO v_count
      FROM suspense_transactions
     WHERE suspense_item_id = v_test_id;

    IF v_count <> 0 THEN
        RAISE EXCEPTION
            'TEST 10 FAILED: Unexpected suspense transactions: %',
            v_count;
    END IF;

    SELECT version_number
      INTO v_version
      FROM suspense_items
     WHERE suspense_item_id = v_test_id;

    IF v_version IS DISTINCT FROM 1 THEN
        RAISE EXCEPTION
            'TEST 10 FAILED: Expected version 1, actual %',
            v_version;
    END IF;

    SELECT COUNT(*)
      INTO v_report_count
      FROM report_entries re
      JOIN suspense_transactions st
        ON re.associated_record_id =
           st.suspense_transaction_id::VARCHAR
     WHERE st.suspense_item_id = v_test_id
       AND re.report_id = 'reversed_payments_report'
       AND re.associated_record_type =
           'suspense_transactions'::t_associated_record_type_enum;

    IF v_report_count <> 0 THEN
        RAISE EXCEPTION
            'TEST 10 FAILED: Unexpected report entries: %',
            v_report_count;
    END IF;

    RAISE NOTICE
        'TEST 10 PASSED: Zero-balance rejection left data unchanged';

    --------------------------------------------------------------------
    -- TESTS 11-15: Mandatory parameter validation.
    --
    -- P4001: Suspense item ID
    -- P4002: Suspense item version
    -- P4003: Posted by
    -- P4004: Posted by name
    -- P4005: Dishonoured cheque indicator
    --------------------------------------------------------------------

    FOR v_test_number IN 11..15 LOOP

        v_failed := FALSE;
        v_expected_code :=
            'P400' || (v_test_number - 10)::TEXT;

        BEGIN

            CASE v_test_number

                WHEN 11 THEN
                    -- TEST 11: Suspense item ID is NULL.
                    CALL p_reverse_suspense_function(
                        NULL,
                        1,
                        'PO10350_TEST',
                        'PO-10350 Unit Test',
                        FALSE
                    );

                WHEN 12 THEN
                    -- TEST 12: Suspense item version is NULL.
                    CALL p_reverse_suspense_function(
                        v_item_id + 4,
                        NULL,
                        'PO10350_TEST',
                        'PO-10350 Unit Test',
                        FALSE
                    );

                WHEN 13 THEN
                    -- TEST 13: Posted-by user ID is NULL.
                    CALL p_reverse_suspense_function(
                        v_item_id + 4,
                        1,
                        NULL,
                        'PO-10350 Unit Test',
                        FALSE
                    );

                WHEN 14 THEN
                    -- TEST 14: Posted-by name is NULL.
                    CALL p_reverse_suspense_function(
                        v_item_id + 4,
                        1,
                        'PO10350_TEST',
                        NULL,
                        FALSE
                    );

                WHEN 15 THEN
                    -- TEST 15: Dishonoured cheque flag is NULL.
                    CALL p_reverse_suspense_function(
                        v_item_id + 4,
                        1,
                        'PO10350_TEST',
                        'PO-10350 Unit Test',
                        NULL
                    );

            END CASE;

        EXCEPTION WHEN OTHERS THEN

            GET STACKED DIAGNOSTICS
                v_sqlstate = RETURNED_SQLSTATE,
                v_detail = PG_EXCEPTION_DETAIL;

            IF v_sqlstate = v_expected_code
               AND v_detail LIKE
                   'p_reverse_suspense_function:%'
            THEN
                v_failed := TRUE;
            ELSE
                RAISE EXCEPTION
                    'TEST % FAILED: Expected %, received %. Detail: %',
                    v_test_number,
                    v_expected_code,
                    v_sqlstate,
                    v_detail;
            END IF;

        END;

        IF NOT v_failed THEN
            RAISE EXCEPTION
                'TEST % FAILED: Expected validation error %',
                v_test_number,
                v_expected_code;
        END IF;

        RAISE NOTICE
            'TEST % PASSED: Mandatory parameter validation (%)',
            v_test_number,
            v_expected_code;

    END LOOP;

    --------------------------------------------------------------------
    -- Final result.
    --------------------------------------------------------------------

    RAISE NOTICE
        'PO-10350: ALL 15 UNIT TESTS PASSED';

END;
$test$;

ROLLBACK;
