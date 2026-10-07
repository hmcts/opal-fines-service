/**
* OPAL Program
*
* MODULE      : p_insert_suspense_transaction_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_suspense_transaction.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 01/10/2026    C Cho       1.0         PO-3474 Unit tests for p_insert_suspense_transaction
*
**/
\timing

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM court_fees_received
     WHERE court_fee_received_id = 999900000101;
    DELETE FROM suspense_transactions
     WHERE suspense_item_id = 999900000101;
    DELETE FROM payments_in
     WHERE payment_in_id IN (999900000101, 999900000102);
    DELETE FROM suspense_items
     WHERE suspense_item_id = 999900000101;
    DELETE FROM tills
     WHERE till_id = 999900000101;
    DELETE FROM court_fees
     WHERE court_fee_id = 999900000101;
    DELETE FROM suspense_accounts
     WHERE suspense_account_id = 999900000101;
    DELETE FROM business_units
     WHERE business_unit_id = 9988;

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
    VALUES (
        9988,
        'Suspense Transaction Unit Test',
        'U474',
        'Accounting Division'
    );

    INSERT INTO suspense_accounts (
        suspense_account_id,
        business_unit_id,
        account_number
    )
    VALUES (
        999900000101,
        9988,
        'P3474-UNIT-TEST'
    );

    INSERT INTO suspense_items (
        suspense_item_id,
        suspense_account_id,
        suspense_item_number,
        suspense_item_type,
        created_date,
        payment_method,
        court_fee_id
    )
    VALUES (
        999900000101,
        999900000101,
        30001,
        'UN'::t_suspense_item_type_enum,
        CURRENT_TIMESTAMP,
        'CT'::t_payment_method_enum,
        NULL
    );

    INSERT INTO tills (
        till_id,
        business_unit_id,
        till_number,
        owned_by
    )
    VALUES (
        999900000101,
        9988,
        9988,
        'P3474UT'
    );

    INSERT INTO payments_in (
        payment_in_id,
        till_id,
        payment_amount,
        payment_date,
        payment_method,
        destination_type,
        additional_information,
        receipt,
        allocated,
        auto_payment
    )
    VALUES
    (
        999900000101,
        999900000101,
        25.50,
        CURRENT_TIMESTAMP,
        'CT'::t_payment_method_enum,
        'S'::t_pi_destination_type_enum,
        (
            '{"additional_information":{"payment_reference":"MAN-REF-1234567890",'
                || '"transfer_source":"BACS-TRANSFER-SOURCE",'
                || '"suspense_reason_detail":"Unmatched payment requiring manual review",'
                || '"payer_details":{"individual":{"title":"Ms","forenames":"Alexandra Elizabeth",'
                || '"surname":"Montgomery"},"address_line_1":"100 Long Street",'
                || '"address_line_2":"Test District","address_line_3":"London","postcode":"SW1A 1AA"}}}'
        )::JSON,
        FALSE,
        FALSE,
        FALSE
    ),
    (
        999900000102,
        999900000101,
        40.00,
        CURRENT_TIMESTAMP,
        'CT'::t_payment_method_enum,
        'S'::t_pi_destination_type_enum,
        (
            '{"additional_information":'
                || '{"originator_reference":"AUTO-REF-1234567890-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-LONG"}}'
        )::JSON,
        FALSE,
        FALSE,
        TRUE
    );

    INSERT INTO court_fees (
        court_fee_id,
        business_unit_id,
        court_fee_code,
        description,
        amount,
        stats_code
    )
    VALUES (
        999900000101,
        9988,
        'U474',
        'Suspense transaction unit test fee',
        40.00,
        'U474'
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
        999900000101,
        9988,
        999900000101,
        FALSE,
        1,
        CURRENT_TIMESTAMP
    );

    COMMIT;

    RAISE NOTICE 'Test data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Insert a manual suspense transaction and build its text from payment and payer details
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information  payments_in.additional_information%TYPE;
    v_expected_text           CONSTANT TEXT :=
        'MAN-REF-1234567890, BACS-TRANSFER-SOURCE, Unmatched payment requiring manual review, '
        || 'Ms, Alexandra Elizabeth, Montgomery, 100 Long Street, Test District, London, SW1A 1AA';
    v_posted_before           TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_transaction             suspense_transactions%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: Insert a manual suspense transaction ===';

    SELECT additional_information
      INTO v_additional_information
      FROM payments_in
     WHERE payment_in_id = 999900000101;

    CALL p_insert_suspense_transaction(
        pi_suspense_item_id := 999900000101::BIGINT,
        pi_posted_by := 'P3474UT'::VARCHAR,
        pi_posted_by_name := 'PO-3474 Unit Test'::VARCHAR,
        pi_suspense_amount := 25.50::NUMERIC,
        pi_transaction_type := 'UN'::t_suspense_transaction_type_enum,
        pi_payment_in_id := 999900000101::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fees_received_id := NULL::BIGINT
    );

    SELECT *
      INTO v_transaction
      FROM suspense_transactions
     WHERE suspense_item_id = 999900000101
       AND associated_record_id = '999900000101';

    ASSERT v_transaction.suspense_transaction_id IS NOT NULL,
        'A suspense transaction should have been inserted';
    ASSERT v_transaction.suspense_item_id = 999900000101, 'suspense_item_id should match';
    ASSERT v_transaction.posted_date >= v_posted_before,
        'posted_date should be on or after the call start time';
    ASSERT v_transaction.posted_by = 'P3474UT', 'posted_by should match';
    ASSERT v_transaction.posted_by_name = 'PO-3474 Unit Test', 'posted_by_name should match';
    ASSERT v_transaction.transaction_type = 'UN'::t_suspense_transaction_type_enum,
        'transaction_type should match';
    ASSERT v_transaction.amount = 25.50, 'amount should match';
    ASSERT v_transaction.associated_record_type IS NULL, 'associated_record_type should be NULL';
    ASSERT v_transaction.associated_record_id = '999900000101',
        'associated_record_id should contain the payment in ID';
    ASSERT v_transaction.text = v_expected_text,
        'text should contain the full manual payment and payer details';
    ASSERT LENGTH(v_transaction.text) > 50,
        'manual suspense transaction text should not be truncated to 50 characters';
    ASSERT v_transaction.reversed IS NULL, 'reversed should be NULL';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Insert an auto-payment transaction and link its court_fees_received record
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information  payments_in.additional_information%TYPE;
    v_expected_text           CONSTANT TEXT :=
        'AUTO-REF-1234567890-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-LONG';
    v_transaction             suspense_transactions%ROWTYPE;
    v_linked_transaction_id   court_fees_received.suspense_transaction_id%TYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: Insert an auto-payment suspense transaction and link the court fee ===';

    SELECT additional_information
      INTO v_additional_information
      FROM payments_in
     WHERE payment_in_id = 999900000102;

    CALL p_insert_suspense_transaction(
        pi_suspense_item_id := 999900000101::BIGINT,
        pi_posted_by := 'P3474UT'::VARCHAR,
        pi_posted_by_name := 'PO-3474 Unit Test'::VARCHAR,
        pi_suspense_amount := 40.00::NUMERIC,
        pi_transaction_type := 'CF'::t_suspense_transaction_type_enum,
        pi_payment_in_id := 999900000102::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fees_received_id := 999900000101::BIGINT
    );

    SELECT *
      INTO v_transaction
      FROM suspense_transactions
     WHERE suspense_item_id = 999900000101
       AND associated_record_id = '999900000102';

    SELECT suspense_transaction_id
      INTO v_linked_transaction_id
      FROM court_fees_received
     WHERE court_fee_received_id = 999900000101;

    ASSERT v_transaction.suspense_transaction_id IS NOT NULL,
        'An auto-payment suspense transaction should have been inserted';
    ASSERT v_transaction.transaction_type = 'CF'::t_suspense_transaction_type_enum,
        'transaction_type should match';
    ASSERT v_transaction.amount = 40.00, 'amount should match';
    ASSERT v_transaction.text = v_expected_text, 'text should contain the full originator reference';
    ASSERT LENGTH(v_transaction.text) > 50,
        'auto-payment suspense transaction text should not be truncated to 50 characters';
    ASSERT v_linked_transaction_id = v_transaction.suspense_transaction_id,
        'court_fees_received should link to the inserted suspense transaction';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Reject a NULL suspense item ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
BEGIN
    RAISE NOTICE '=== TEST 3: Reject a NULL suspense item ID ===';

    BEGIN
        CALL p_insert_suspense_transaction(
            pi_suspense_item_id := NULL::BIGINT,
            pi_posted_by := 'P3474UT'::VARCHAR,
            pi_posted_by_name := 'PO-3474 Unit Test'::VARCHAR,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_transaction_type := 'UN'::t_suspense_transaction_type_enum,
            pi_payment_in_id := NULL::BIGINT,
            pi_additional_information := NULL,
            pi_court_fees_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3309' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_suspense_item_id must be provided',
                'P3309 should have the expected error message';
            ASSERT v_exception_detail = 'p_insert_suspense_transaction: pi_suspense_item_id is required',
                'P3309 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3309 error should have been raised for a NULL suspense item ID';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Reject a NULL suspense amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
BEGIN
    RAISE NOTICE '=== TEST 4: Reject a NULL suspense amount ===';

    BEGIN
        CALL p_insert_suspense_transaction(
            pi_suspense_item_id := 999900000101::BIGINT,
            pi_posted_by := 'P3474UT'::VARCHAR,
            pi_posted_by_name := 'PO-3474 Unit Test'::VARCHAR,
            pi_suspense_amount := NULL::NUMERIC,
            pi_transaction_type := 'UN'::t_suspense_transaction_type_enum,
            pi_payment_in_id := NULL::BIGINT,
            pi_additional_information := NULL,
            pi_court_fees_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3310' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_suspense_amount must be provided',
                'P3310 should have the expected error message';
            ASSERT v_exception_detail = 'p_insert_suspense_transaction: pi_suspense_amount is required',
                'P3310 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3310 error should have been raised for a NULL suspense amount';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Reject a NULL transaction type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught      BOOLEAN := FALSE;
    v_exception_detail  TEXT;
BEGIN
    RAISE NOTICE '=== TEST 5: Reject a NULL transaction type ===';

    BEGIN
        CALL p_insert_suspense_transaction(
            pi_suspense_item_id := 999900000101::BIGINT,
            pi_posted_by := 'P3474UT'::VARCHAR,
            pi_posted_by_name := 'PO-3474 Unit Test'::VARCHAR,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_transaction_type := NULL::t_suspense_transaction_type_enum,
            pi_payment_in_id := NULL::BIGINT,
            pi_additional_information := NULL,
            pi_court_fees_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3311' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_transaction_type must be provided',
                'P3311 should have the expected error message';
            ASSERT v_exception_detail = 'p_insert_suspense_transaction: pi_transaction_type is required',
                'P3311 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3311 error should have been raised for a NULL transaction type';

    RAISE NOTICE 'TEST 5 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Reject a court fee received ID that does not exist and roll back the transaction insert
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught             BOOLEAN := FALSE;
    v_exception_detail         TEXT;
    v_transaction_count_before INTEGER;
BEGIN
    RAISE NOTICE '=== TEST 6: Reject an unknown court fee received ID ===';

    SELECT COUNT(*)
      INTO v_transaction_count_before
      FROM suspense_transactions
     WHERE suspense_item_id = 999900000101;

    BEGIN
        CALL p_insert_suspense_transaction(
            pi_suspense_item_id := 999900000101::BIGINT,
            pi_posted_by := 'P3474UT'::VARCHAR,
            pi_posted_by_name := 'PO-3474 Unit Test'::VARCHAR,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_transaction_type := 'UN'::t_suspense_transaction_type_enum,
            pi_payment_in_id := NULL::BIGINT,
            pi_additional_information := NULL,
            pi_court_fees_received_id := 999900000199::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3312' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'court_fees_received record 999900000199 was not found',
                'P3312 should have the expected error message';
            ASSERT v_exception_detail =
                'p_insert_suspense_transaction: No court_fees_received record exists for '
                || 'court_fee_received_id 999900000199',
                'P3312 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3312 error should have been raised for an unknown court fee received ID';
    ASSERT (
        SELECT COUNT(*)
          FROM suspense_transactions
         WHERE suspense_item_id = 999900000101
    ) = v_transaction_count_before, 'The failed call should not insert a suspense transaction';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM court_fees_received
     WHERE court_fee_received_id = 999900000101;
    DELETE FROM suspense_transactions
     WHERE suspense_item_id = 999900000101;
    DELETE FROM payments_in
     WHERE payment_in_id IN (999900000101, 999900000102);
    DELETE FROM suspense_items
     WHERE suspense_item_id = 999900000101;
    DELETE FROM tills
     WHERE till_id = 999900000101;
    DELETE FROM court_fees
     WHERE court_fee_id = 999900000101;
    DELETE FROM suspense_accounts
     WHERE suspense_account_id = 999900000101;
    DELETE FROM business_units
     WHERE business_unit_id = 9988;

    COMMIT;

    RAISE NOTICE 'Test data cleanup completed';
END $$;
