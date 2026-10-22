/**
* OPAL Program
*
* MODULE      : p_insert_suspense_item_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_suspense_item.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 30/09/2026    C Cho       1.0         PO-3477 Unit tests for p_insert_suspense_item
*
**/
\timing

DO $$
DECLARE
    v_suspense_item_number_data_type TEXT;
BEGIN
    SELECT data_type
      INTO v_suspense_item_number_data_type
      FROM information_schema.columns
     WHERE table_schema = CURRENT_SCHEMA()
       AND table_name = 'suspense_items'
       AND column_name = 'suspense_item_number';

    ASSERT v_suspense_item_number_data_type = 'bigint',
        'Prerequisite missing: apply V1_326__alter_suspense_items_suspense_item_number_bigint.sql';
    ASSERT TO_REGCLASS(CURRENT_SCHEMA() || '.suspense_item_number_index') IS NOT NULL,
        'Prerequisite missing: apply V1_328__create_suspense_item_number_index.sql';

    RAISE NOTICE 'Database prerequisites verified';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DROP TRIGGER IF EXISTS p_insert_suspense_item_unit_test_force_unique_violation
        ON suspense_item_number_index;
    DROP FUNCTION IF EXISTS p_insert_suspense_item_unit_test_force_unique_violation();

    DELETE FROM suspense_items
     WHERE suspense_account_id IN (999900000001, 999900000002, 999900000003);
    DELETE FROM suspense_item_number_index
     WHERE business_unit_id IN (9991, 9992, 9993);
    DELETE FROM court_fees_received
     WHERE court_fee_received_id = 999900000001;
    DELETE FROM court_fees
     WHERE court_fee_id = 999900000001;
    DELETE FROM suspense_accounts
     WHERE suspense_account_id IN (999900000001, 999900000002, 999900000003);
    DELETE FROM business_units
     WHERE business_unit_id IN (9991, 9992, 9993);

    COMMIT;

    RAISE NOTICE 'Cleanup completed';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Setting up test data ===';

    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_code,
        business_unit_type
    )
    VALUES
        (9991, 'Suspense Item Unit Test 1', 'T991', 'Accounting Division'),
        (9992, 'Suspense Item Unit Test 2', 'T992', 'Accounting Division'),
        (9993, 'Suspense Item Unit Test 3', 'T993', 'Accounting Division');

    INSERT INTO suspense_accounts (
        suspense_account_id,
        business_unit_id,
        account_number
    )
    VALUES
        (999900000001, 9991, 'SUSPENSE-UNIT-TEST-1'),
        (999900000002, 9991, 'SUSPENSE-UNIT-TEST-2'),
        (999900000003, 9992, 'SUSPENSE-UNIT-TEST-3');

    INSERT INTO court_fees (
        court_fee_id,
        business_unit_id,
        court_fee_code,
        description,
        amount,
        stats_code
    )
    VALUES (
        999900000001,
        9991,
        'UT-FEE',
        'Suspense item unit test fee',
        25.00,
        'UT-STATS'
    );

    INSERT INTO court_fees_received (
        court_fee_received_id,
        business_unit_id,
        court_fee_id,
        overpayment,
        number_of_items,
        received_date
    )
    VALUES (
        999900000001,
        9991,
        999900000001,
        FALSE,
        1,
        CURRENT_TIMESTAMP
    );

    COMMIT;

    RAISE NOTICE 'Test data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Insert a suspense item without a court fee and allocate the first item number
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense_item_id  suspense_items.suspense_item_id%TYPE;
    v_created_before    TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_suspense_item     suspense_items%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: Insert a suspense item without a court fee ===';

    CALL p_insert_suspense_item(
        pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
        pi_payment_method := 'CT'::t_payment_method_enum,
        pi_business_unit_id := 9991::SMALLINT,
        pi_court_fee_received_id := NULL::BIGINT,
        po_suspense_item_id := v_suspense_item_id
    );

    SELECT *
      INTO v_suspense_item
      FROM suspense_items
     WHERE suspense_item_id = v_suspense_item_id;

    ASSERT v_suspense_item_id IS NOT NULL, 'A suspense_item_id should be returned';
    ASSERT v_suspense_item.suspense_item_id = v_suspense_item_id,
        'The inserted suspense_item_id should match the returned ID';
    ASSERT v_suspense_item.suspense_account_id = 999900000001,
        'The lowest suspense account ID for the business unit should be used';
    ASSERT v_suspense_item.suspense_item_number = 100000,
        'The first suspense item number should be 100000';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'UN',
        'suspense_item_type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CT',
        'payment_method should match';
    ASSERT v_suspense_item.court_fee_id IS NULL,
        'court_fee_id should be NULL when no court_fee_received_id is supplied';
    ASSERT v_suspense_item.created_date >= v_created_before,
        'created_date should be on or after the call start time';
    ASSERT EXISTS (
        SELECT 1
          FROM suspense_item_number_index
         WHERE business_unit_id = 9991
           AND suspense_item_number = 100000
    ), 'The allocated suspense item number should be recorded in suspense_item_number_index';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Allocate the next item number and copy the court fee ID from court_fees_received
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense_item_id  suspense_items.suspense_item_id%TYPE;
    v_suspense_item     suspense_items%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: Allocate the next item number and populate the court fee ===';

    CALL p_insert_suspense_item(
        pi_suspense_item_type := 'CF'::t_suspense_item_type_enum,
        pi_payment_method := 'CQ'::t_payment_method_enum,
        pi_business_unit_id := 9991::SMALLINT,
        pi_court_fee_received_id := 999900000001::BIGINT,
        po_suspense_item_id := v_suspense_item_id
    );

    SELECT *
      INTO v_suspense_item
      FROM suspense_items
     WHERE suspense_item_id = v_suspense_item_id;

    ASSERT v_suspense_item.suspense_item_number = 100001,
        'The next suspense item number for the same business unit should be allocated';
    ASSERT v_suspense_item.court_fee_id = 999900000001,
        'court_fee_id should be copied from court_fees_received';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'CF',
        'suspense_item_type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CQ',
        'payment_method should match';
    ASSERT (
        SELECT COUNT(*)
          FROM suspense_item_number_index
         WHERE business_unit_id = 9991
    ) = 2, 'Two suspense item numbers should have been allocated for the business unit';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Allocate item numbers independently for each business unit
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense_item_id  suspense_items.suspense_item_id%TYPE;
    v_suspense_item     suspense_items%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 3: Allocate item numbers independently for each business unit ===';

    CALL p_insert_suspense_item(
        pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
        pi_payment_method := 'PO'::t_payment_method_enum,
        pi_business_unit_id := 9992::SMALLINT,
        pi_court_fee_received_id := NULL::BIGINT,
        po_suspense_item_id := v_suspense_item_id
    );

    SELECT *
      INTO v_suspense_item
      FROM suspense_items
     WHERE suspense_item_id = v_suspense_item_id;

    ASSERT v_suspense_item.suspense_account_id = 999900000003,
        'The suspense account for the requested business unit should be used';
    ASSERT v_suspense_item.suspense_item_number = 100000,
        'A different business unit should start its suspense item numbers at 100000';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Reject a NULL suspense item type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
    v_suspense_item_id  suspense_items.suspense_item_id%TYPE;
BEGIN
    RAISE NOTICE '=== TEST 4: Reject a NULL suspense item type ===';

    BEGIN
        CALL p_insert_suspense_item(
            pi_suspense_item_type := NULL::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_business_unit_id := 9991::SMALLINT,
            pi_court_fee_received_id := NULL::BIGINT,
            po_suspense_item_id := v_suspense_item_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3301' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_suspense_item_type must be provided',
                'P3301 should have the expected error message';
            ASSERT v_exception_detail = 'p_insert_suspense_item: pi_suspense_item_type is required',
                'P3301 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3301 error should have been raised for a NULL suspense item type';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Reject a NULL business unit ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
    v_suspense_item_id  suspense_items.suspense_item_id%TYPE;
BEGIN
    RAISE NOTICE '=== TEST 5: Reject a NULL business unit ID ===';

    BEGIN
        CALL p_insert_suspense_item(
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_business_unit_id := NULL::SMALLINT,
            pi_court_fee_received_id := NULL::BIGINT,
            po_suspense_item_id := v_suspense_item_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3302' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_business_unit_id must be provided',
                'P3302 should have the expected error message';
            ASSERT v_exception_detail = 'p_insert_suspense_item: pi_business_unit_id is required',
                'P3302 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3302 error should have been raised for a NULL business unit ID';

    RAISE NOTICE 'TEST 5 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Reject a business unit with no suspense account
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
    v_suspense_item_id  suspense_items.suspense_item_id%TYPE;
BEGIN
    RAISE NOTICE '=== TEST 6: Reject a business unit with no suspense account ===';

    BEGIN
        CALL p_insert_suspense_item(
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_business_unit_id := 9993::SMALLINT,
            pi_court_fee_received_id := NULL::BIGINT,
            po_suspense_item_id := v_suspense_item_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3303' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'No suspense account found for business_unit_id 9993',
                'P3303 should have the expected error message';
            ASSERT v_exception_detail =
                'p_insert_suspense_item: No suspense account exists for business_unit_id 9993',
                'P3303 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3303 error should have been raised when no suspense account exists';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Reject a court fee received ID that does not exist
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
    v_suspense_item_id  suspense_items.suspense_item_id%TYPE;
    v_item_count_before INTEGER;
    v_index_count_before INTEGER;
BEGIN
    RAISE NOTICE '=== TEST 7: Reject a court fee received ID that does not exist ===';

    SELECT COUNT(*)
      INTO v_item_count_before
      FROM suspense_items
     WHERE suspense_account_id IN (999900000001, 999900000002);

    SELECT COUNT(*)
      INTO v_index_count_before
      FROM suspense_item_number_index
     WHERE business_unit_id = 9991;

    BEGIN
        CALL p_insert_suspense_item(
            pi_suspense_item_type := 'CF'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_business_unit_id := 9991::SMALLINT,
            pi_court_fee_received_id := 999900000099::BIGINT,
            po_suspense_item_id := v_suspense_item_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3304' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'court_fees_received record 999900000099 was not found',
                'P3304 should have the expected error message';
            ASSERT v_exception_detail =
                'p_insert_suspense_item: No court_fees_received record exists for court_fee_received_id 999900000099',
                'P3304 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3304 error should have been raised for an unknown court fee received ID';
    ASSERT (
        SELECT COUNT(*)
          FROM suspense_items
         WHERE suspense_account_id IN (999900000001, 999900000002)
    ) = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (
        SELECT COUNT(*)
          FROM suspense_item_number_index
         WHERE business_unit_id = 9991
    ) = v_index_count_before, 'The failed call should not allocate a suspense item number';

    RAISE NOTICE 'TEST 7 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 8: Stop retrying after five unique violations
----------------------------------------------------------------------------------------------------------------------
BEGIN;

CREATE FUNCTION p_insert_suspense_item_unit_test_force_unique_violation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.business_unit_id = 9992 THEN
        RAISE EXCEPTION 'forced unique violation' USING ERRCODE = '23505';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER p_insert_suspense_item_unit_test_force_unique_violation
    BEFORE INSERT ON suspense_item_number_index
    FOR EACH ROW
    EXECUTE FUNCTION p_insert_suspense_item_unit_test_force_unique_violation();

DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught       BOOLEAN := FALSE;
    v_exception_detail   TEXT;
    v_suspense_item_id   suspense_items.suspense_item_id%TYPE;
    v_item_count_before  INTEGER;
    v_index_count_before INTEGER;
BEGIN
    RAISE NOTICE '=== TEST 8: Stop retrying after five unique violations ===';

    SELECT COUNT(*)
      INTO v_item_count_before
      FROM suspense_items
     WHERE suspense_account_id = 999900000003;

    SELECT COUNT(*)
      INTO v_index_count_before
      FROM suspense_item_number_index
     WHERE business_unit_id = 9992;

    BEGIN
        CALL p_insert_suspense_item(
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_business_unit_id := 9992::SMALLINT,
            pi_court_fee_received_id := NULL::BIGINT,
            po_suspense_item_id := v_suspense_item_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3305' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM =
                'Unique violation inserting suspense_item_number = 100001, business_unit_id = 9992. '
                || 'Error: 23505 - forced unique violation',
                'P3305 should report the failed suspense item number allocation';
            ASSERT v_exception_detail =
                'p_insert_suspense_item: Failed to allocate a unique suspense item number after 5 attempts',
                'P3305 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3305 error should have been raised after five unique violations';
    ASSERT (
        SELECT COUNT(*)
          FROM suspense_items
         WHERE suspense_account_id = 999900000003
    ) = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (
        SELECT COUNT(*)
          FROM suspense_item_number_index
         WHERE business_unit_id = 9992
    ) = v_index_count_before, 'The failed call should not allocate a suspense item number';

    RAISE NOTICE 'TEST 8 PASSED';
END $$;

ROLLBACK;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM suspense_items
     WHERE suspense_account_id IN (999900000001, 999900000002, 999900000003);
    DELETE FROM suspense_item_number_index
     WHERE business_unit_id IN (9991, 9992, 9993);
    DELETE FROM court_fees_received
     WHERE court_fee_received_id = 999900000001;
    DELETE FROM court_fees
     WHERE court_fee_id = 999900000001;
    DELETE FROM suspense_accounts
     WHERE suspense_account_id IN (999900000001, 999900000002, 999900000003);
    DELETE FROM business_units
     WHERE business_unit_id IN (9991, 9992, 9993);

    COMMIT;

    RAISE NOTICE 'Test data cleanup completed';
END $$;
