/**
* OPAL Program
*
* MODULE      : p_insert_suspense_transaction_unit_tests_v2.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_insert_suspense_transaction.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 01/10/2026    C Cho       1.0         PO-3474 Unit tests for p_insert_suspense_transaction
* 20/11/2026    C Cho       2.0         PO-11493 - Test return transaction ID and persist structured payment metadata
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
        owned_by,
        source
    )
    VALUES (
        999900000101,
        9988,
        9988,
        'P3474UT',
        'NATWEST'
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
            '{"payment_received_from":"T","additional_information":{"payment_reference":"MAN-REF-1234567890",'
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
                || '{"originator_reference":"AUTO-REF-1234567890-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-LONG",'
                || '"source":"OTHER","transfer_source":"DWP"}}'
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
-- Test 1: Manual and automatic metadata, returned ID, association and clock timestamp
----------------------------------------------------------------------------------------------------------------------
DO $$
DECLARE
    v_payment_id bigint;
    v_returned_id bigint;
    v_input json;
    v_row suspense_transactions%ROWTYPE;
    v_before timestamp;
    v_expected jsonb;
BEGIN
    RAISE NOTICE '=== TEST 1: Manual and automatic metadata, returned ID, association and clock timestamp ===';

    FOREACH v_payment_id IN ARRAY ARRAY[999900000101::bigint, 999900000102::bigint] LOOP
        SELECT additional_information INTO v_input FROM payments_in WHERE payment_in_id = v_payment_id;
        v_before := clock_timestamp();
        CALL p_insert_suspense_transaction(
            999900000101::bigint, 'P3474UT'::varchar, 'PO-11493 Unit Test'::varchar,
            25.50::numeric, 'UN'::t_suspense_transaction_type_enum,
            v_payment_id, v_input, 999900000101::bigint,
            'test-account'::varchar, 'defendant_accounts'::t_associated_record_type_enum,
            v_returned_id
        );
        SELECT * INTO STRICT v_row FROM suspense_transactions WHERE suspense_transaction_id = v_returned_id;
        ASSERT v_row.suspense_item_id = 999900000101 AND v_row.amount = 25.50
           AND v_row.transaction_type = 'UN', 'Returned ID must identify the correct inserted row';
        ASSERT v_row.associated_record_id = 'test-account'
           AND v_row.associated_record_type = 'defendant_accounts', 'Association must come from explicit inputs';
        ASSERT v_row.posted_date >= v_before AND v_row.posted_date <= clock_timestamp(),
               'Posted date must reflect the wall-clock call time';
        ASSERT v_row.text IS NULL AND v_row.reversed IS NULL, 'New rows must leave legacy text/reversed unset';
        ASSERT v_row.posted_by = 'P3474UT' AND v_row.posted_by_name = 'PO-11493 Unit Test', 'Posting user must be retained';
        ASSERT (SELECT suspense_transaction_id FROM court_fees_received WHERE court_fee_received_id = 999900000101) = v_returned_id,
               'Court fee must link to the returned transaction';
        IF v_payment_id = 999900000102 THEN
            ASSERT v_row.additional_information::jsonb =
                '{"payment_in_id":999900000102,
                  "originator_reference":"AUTO-REF-1234567890-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-LONG",
                  "transfer_source":"NATWEST"}'::jsonb,
                'Automatic metadata must retain the payment ID, complete originator reference and till transfer source';
        ELSE
            v_expected := '{"payment_in_id":999900000101,"payment_received_from":"T",
                "payment_reference":"MAN-REF-1234567890","transfer_source":"BACS-TRANSFER-SOURCE",
                "payer_details":{"individual":{"title":"Ms","forenames":"Alexandra Elizabeth", "surname":"Montgomery"},
                    "organisation":{"organisation_name":null},"address_line_1":"100 Long Street",
                    "address_line_2":"Test District","address_line_3":"London","postcode":"SW1A 1AA"},
                "suspense_reason_detail":"Unmatched payment requiring manual review"}'::jsonb;
            ASSERT v_row.additional_information::jsonb = v_expected,
                'Manual metadata must retain the payment ID, received-from category, reference, transfer source, payer and suspense details';
        END IF;
    END LOOP;

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Court-fee and overpayment metadata and miscellaneous account association
----------------------------------------------------------------------------------------------------------------------
DO $$
DECLARE
    v_returned_id bigint;
    v_input json;
    v_row suspense_transactions%ROWTYPE;
    v_expected jsonb;
    v_transaction_type t_suspense_transaction_type_enum;
BEGIN
    RAISE NOTICE '=== TEST 2: Court-fee and overpayment metadata and miscellaneous account association ===';

    v_input := '{"payment_received_from":"A","payment_reference":"COURT-REF","transfer_source":"Cash","suspense_reason_detail":"Court fee",
        "payer_details":{"organisation":{"organisation_name":"Test Company"},"postcode":"AB1 2CD"}}';
    v_expected := '{"payment_in_id":999900000101,"payment_received_from":"A",
        "payment_reference":"COURT-REF","transfer_source":"Cash",
        "payer_details":{"individual":{"title":null,"forenames":null,"surname":null},
            "organisation":{"organisation_name":"Test Company"},"address_line_1":null,
            "address_line_2":null,"address_line_3":null,"postcode":"AB1 2CD"}}'::jsonb;
    FOREACH v_transaction_type IN ARRAY ARRAY['CF', 'OC']::t_suspense_transaction_type_enum[] LOOP
        IF v_transaction_type = 'OC' THEN
            v_input := json_build_object('additional_information', v_input);
        END IF;
        UPDATE payments_in
           SET destination_type = 'C', additional_information = v_input
         WHERE payment_in_id = 999900000101;
        CALL p_insert_suspense_transaction(999900000101::bigint, 'P3474UT'::varchar, 'Test'::varchar,
            3.00::numeric, v_transaction_type, 999900000101::bigint,
            v_input, NULL::bigint, 'misc-account'::varchar, 'miscellaneous_accounts'::t_associated_record_type_enum, v_returned_id);
        SELECT * INTO STRICT v_row FROM suspense_transactions WHERE suspense_transaction_id = v_returned_id;
        ASSERT v_row.additional_information::jsonb = v_expected,
               'Court fees and overpayments must retain payment ID, reference, transfer source, received-from and payer details without suspense details';
        ASSERT v_row.associated_record_id = 'misc-account' AND v_row.associated_record_type = 'miscellaneous_accounts',
               'Miscellaneous account association must be retained';
    END LOOP;

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Reject a NULL suspense item ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_new_transaction_id bigint;
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
            pi_court_fees_received_id := NULL::BIGINT,
            pi_associated_record_id := NULL::varchar,
            pi_associated_record_type := NULL::t_associated_record_type_enum,
            po_suspense_transaction_id := v_new_transaction_id
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
    v_new_transaction_id bigint;
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
            pi_court_fees_received_id := NULL::BIGINT,
            pi_associated_record_id := NULL::varchar,
            pi_associated_record_type := NULL::t_associated_record_type_enum,
            po_suspense_transaction_id := v_new_transaction_id
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
    v_new_transaction_id bigint;
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
            pi_court_fees_received_id := NULL::BIGINT,
            pi_associated_record_id := NULL::varchar,
            pi_associated_record_type := NULL::t_associated_record_type_enum,
            po_suspense_transaction_id := v_new_transaction_id
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
    v_new_transaction_id bigint;
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
            pi_court_fees_received_id := 999900000199::BIGINT,
            pi_associated_record_id := NULL::varchar,
            pi_associated_record_type := NULL::t_associated_record_type_enum,
            po_suspense_transaction_id := v_new_transaction_id
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
