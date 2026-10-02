\timing

/**
* OPAL Program
*
* MODULE      : p_insert_court_fees_received_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_court_fees_received.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 16/09/2026    P Brumby    1.0         PO-3473 - Unit tests for p_insert_court_fees_received.
*
**/

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data before tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before p_insert_court_fees_received tests ===';

    DELETE FROM court_fees_received
    WHERE business_unit_id = 9998
       OR court_fee_id = 999801
       OR court_fee_received_id BETWEEN 999801 AND 999899;

    DELETE FROM court_fees
    WHERE court_fee_id = 999801;

    DELETE FROM business_units
    WHERE business_unit_id = 9998;

    RAISE NOTICE 'Cleanup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test setup
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Setting up test data for p_insert_court_fees_received tests ===';

    INSERT INTO business_units (
          business_unit_id
        , business_unit_name
        , business_unit_code
        , business_unit_type
        , welsh_language
    )
    VALUES (
          9998
        , 'Test Court Fees Received Unit'
        , 'TCFR'
        , 'Area'
        , FALSE
    );

    INSERT INTO court_fees (
          court_fee_id
        , business_unit_id
        , court_fee_code
        , description
        , amount
        , stats_code
    )
    VALUES (
          999801
        , 9998
        , 'TESTFEE'
        , 'Test Court Fee'
        , 25.00
        , 'TEST'
    );

    RAISE NOTICE 'Test data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Test successful non-overpayment court_fees_received insert
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_court_fee_received_id    court_fees_received.court_fee_received_id%TYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: Test successful non-overpayment court_fees_received insert ===';

    CALL p_insert_court_fees_received(
        pi_business_unit_id       := 9998::SMALLINT,
        pi_court_fee_id           := 999801::BIGINT,
        pi_overpayment            := FALSE,
        pi_number_of_items        := 3::SMALLINT,
        po_court_fees_received_id := v_court_fee_received_id
    );

    ASSERT v_court_fee_received_id IS NOT NULL,
        'TEST 1: court_fee_received_id should be returned for a non-overpayment record';

    PERFORM 1
    FROM court_fees_received
    WHERE court_fee_received_id = v_court_fee_received_id
      AND business_unit_id = 9998
      AND court_fee_id = 999801
      AND overpayment = FALSE
      AND suspense_transaction_id IS NULL
      AND transferred_date IS NULL
      AND number_of_items = 3
      AND received_date IS NULL;

    ASSERT FOUND,
        'TEST 1: court_fees_received row should contain the expected non-overpayment data';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Test successful overpayment court_fees_received insert
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_court_fee_received_id    court_fees_received.court_fee_received_id%TYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: Test successful overpayment court_fees_received insert ===';

    CALL p_insert_court_fees_received(
        pi_business_unit_id       := 9998::SMALLINT,
        pi_court_fee_id           := 999801::BIGINT,
        pi_overpayment            := TRUE,
        pi_number_of_items        := NULL::SMALLINT,
        po_court_fees_received_id := v_court_fee_received_id
    );

    ASSERT v_court_fee_received_id IS NULL,
        'TEST 2: court_fee_received_id should be NULL for an overpayment record';

    PERFORM 1
    FROM court_fees_received
    WHERE business_unit_id = 9998
      AND court_fee_id IS NULL
      AND overpayment = TRUE
      AND suspense_transaction_id IS NULL
      AND transferred_date IS NULL
      AND number_of_items = 0
      AND received_date IS NULL;

    ASSERT FOUND, 'TEST 2: court_fees_received row should contain the expected overpayment data';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Test NULL business_unit_id raises P3205
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_court_fee_received_id    court_fees_received.court_fee_received_id%TYPE;
    v_error_caught             BOOLEAN := FALSE;
    v_expected_sqlstate        VARCHAR := 'P3205';
    v_expected_message         VARCHAR := 'Business unit ID cannot be null';
BEGIN
    RAISE NOTICE '=== TEST 3: Test NULL business_unit_id raises P3205 ===';

    BEGIN
        CALL p_insert_court_fees_received(
            pi_business_unit_id       := NULL::SMALLINT,
            pi_court_fee_id           := 999801::BIGINT,
            pi_overpayment            := FALSE,
            pi_number_of_items        := 1::SMALLINT,
            po_court_fees_received_id := v_court_fee_received_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3205' THEN
            IF SQLERRM = v_expected_message THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF;
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, format('TEST 3: A %s error should have been raised due to NULL pi_business_unit_id', v_expected_sqlstate);

    PERFORM 1
    FROM court_fees_received
    WHERE court_fee_id = 999801
      AND number_of_items = 1;

    ASSERT NOT FOUND, 'TEST 3: No court_fees_received row should be inserted when pi_business_unit_id is NULL';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Test NULL overpayment raises P3206
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_court_fee_received_id    court_fees_received.court_fee_received_id%TYPE;
    v_error_caught             BOOLEAN := FALSE;
    v_expected_sqlstate        VARCHAR := 'P3206';
    v_expected_message         VARCHAR := 'Overpayment cannot be null';
BEGIN
    RAISE NOTICE '=== TEST 4: Test NULL overpayment raises P3206 ===';

    BEGIN
        CALL p_insert_court_fees_received(
            pi_business_unit_id       := 9998::SMALLINT,
            pi_court_fee_id           := 999801::BIGINT,
            pi_overpayment            := NULL,
            pi_number_of_items        := 1::SMALLINT,
            po_court_fees_received_id := v_court_fee_received_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3206' THEN
            IF SQLERRM = v_expected_message THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF;
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, format('TEST 4: A %s error should have been raised due to NULL pi_overpayment', v_expected_sqlstate);

    PERFORM 1
    FROM court_fees_received
    WHERE business_unit_id = 9998
      AND court_fee_id = 999801
      AND number_of_items = 1;

    ASSERT NOT FOUND, 'TEST 4: No court_fees_received row should be inserted when pi_overpayment is NULL';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Test NULL number_of_items for non-overpayment raises P3207
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_court_fee_received_id    court_fees_received.court_fee_received_id%TYPE;
    v_error_caught             BOOLEAN := FALSE;
    v_expected_sqlstate        VARCHAR := 'P3207';
    v_expected_message         VARCHAR := 'Number of items cannot be null';
BEGIN
    RAISE NOTICE '=== TEST 5: Test NULL number_of_items for non-overpayment raises P3207 ===';

    BEGIN
        CALL p_insert_court_fees_received(
            pi_business_unit_id       := 9998::SMALLINT,
            pi_court_fee_id           := 999801::BIGINT,
            pi_overpayment            := FALSE,
            pi_number_of_items        := NULL::SMALLINT,
            po_court_fees_received_id := v_court_fee_received_id
        );
    EXCEPTION
        WHEN SQLSTATE 'P3207' THEN
            IF SQLERRM = v_expected_message THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF;
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, format('TEST 5: A %s error should have been raised due to NULL pi_number_of_items for a non-overpayment record', v_expected_sqlstate);

    PERFORM 1
    FROM court_fees_received
    WHERE business_unit_id = 9998
      AND court_fee_id = 999801
      AND overpayment = FALSE
      AND number_of_items IS NULL;

    ASSERT NOT FOUND, 'TEST 5: No non-overpayment court_fees_received row should be inserted when pi_number_of_items is NULL';

    RAISE NOTICE 'TEST 5 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Test NULL number_of_items is allowed for overpayment
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_court_fee_received_id    court_fees_received.court_fee_received_id%TYPE;
BEGIN
    RAISE NOTICE '=== TEST 6: Test NULL number_of_items is allowed for overpayment ===';

    CALL p_insert_court_fees_received(
        pi_business_unit_id       := 9998::SMALLINT,
        pi_court_fee_id           := 999801::BIGINT,
        pi_overpayment            := TRUE,
        pi_number_of_items        := NULL::SMALLINT,
        po_court_fees_received_id := v_court_fee_received_id
    );

    ASSERT v_court_fee_received_id IS NULL,
        'TEST 6: court_fee_received_id should be NULL for an overpayment record';

    PERFORM 1
    FROM court_fees_received
    WHERE business_unit_id = 9998
      AND court_fee_id IS NULL
      AND overpayment = TRUE
      AND number_of_items = 0
      AND suspense_transaction_id IS NULL
      AND transferred_date IS NULL
      AND received_date IS NULL;

    ASSERT FOUND, 'TEST 6: overpayment court_fees_received row should be inserted with number_of_items set to 0';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data after tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data after p_insert_court_fees_received tests ===';

    DELETE FROM court_fees_received
    WHERE business_unit_id = 9998
       OR court_fee_id = 999801
       OR court_fee_received_id BETWEEN 999801 AND 999899;

    DELETE FROM court_fees
    WHERE court_fee_id = 999801;

    DELETE FROM business_units
    WHERE business_unit_id = 9998;

    RAISE NOTICE 'Cleanup completed';
END $$;

\timing
