/**
* OPAL Program
*
* MODULE      : p_allocate_suspense_payment_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_allocate_suspense_payment
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 08/10/2026    C Cho       1.0         PO-3467 Unit tests for p_allocate_suspense_payment
**/
\timing

DO $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM notes
     WHERE associated_record_type = 'suspense_items'
       AND associated_record_id IN (
           SELECT suspense_item_id::TEXT FROM suspense_items WHERE suspense_account_id = 999900000467
       );
    DELETE FROM court_fees_received WHERE court_fee_received_id IN (999900000467, 999900000468, 999900000474);
    DELETE FROM suspense_transactions
     WHERE suspense_item_id IN (
         SELECT suspense_item_id FROM suspense_items WHERE suspense_account_id = 999900000467
     );
    DELETE FROM payments_in WHERE till_id = 999900000467;
    DELETE FROM suspense_items WHERE suspense_account_id = 999900000467;
    DELETE FROM suspense_item_number_index WHERE business_unit_id = 9987;
    DELETE FROM tills WHERE till_id = 999900000467;
    DELETE FROM court_fees WHERE court_fee_id = 999900000467;
    DELETE FROM suspense_accounts WHERE suspense_account_id = 999900000467;
    DELETE FROM business_units WHERE business_unit_id IN (9986, 9987);
    COMMIT;
    RAISE NOTICE 'Cleanup completed';
END $$;

DO $$
BEGIN
    RAISE NOTICE '=== Setting up test data ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
    VALUES
        (9987, 'PO-3467 allocation test', 'U467', 'Accounting Division'),
        (9986, 'PO-3467 no suspense account', 'U466', 'Accounting Division');
    INSERT INTO suspense_accounts (suspense_account_id, business_unit_id, account_number)
    VALUES (999900000467, 9987, 'PO-3467-TEST');
    INSERT INTO tills (till_id, business_unit_id, till_number, owned_by)
    VALUES (999900000467, 9987, 9987, 'PO3467UT');
    INSERT INTO court_fees (court_fee_id, business_unit_id, court_fee_code, description, amount, stats_code)
    VALUES (999900000467, 9987, 'U467', 'PO-3467 test fee', 40.00, 'U467');
    INSERT INTO court_fees_received
        (court_fee_received_id, business_unit_id, court_fee_id, overpayment, number_of_items, received_date)
    VALUES
        (999900000467, 9987, 999900000467, FALSE, 1, CURRENT_TIMESTAMP),
        (999900000468, 9987, NULL, TRUE, 0, CURRENT_TIMESTAMP),
        (999900000474, 9987, 999900000467, FALSE, 1, CURRENT_TIMESTAMP);

    INSERT INTO payments_in (payment_in_id, till_id, payment_amount, payment_date,
        payment_method, destination_type, additional_information, receipt, allocated, auto_payment)
    VALUES
        (999900001001, 999900000467, 60.00, CURRENT_TIMESTAMP,
            'CT'::t_payment_method_enum, 'S'::t_pi_destination_type_enum,
            '{"payment_received_from":"T","additional_information":{"suspense_reason_code":"UN","payment_reference":"PAY123456","transfer_source":"NATWEST","suspense_reason_detail":"Unmatched payment","suspense_note":"Created during till allocation","payer_details":{"individual":{"title":"Mr","forenames":"Sam","surname":"Smith"},"address_line_1":"1 High Street","address_line_2":"London","address_line_3":"Greater London","postcode":"SW1A 1AA"}}}', FALSE, FALSE, FALSE),
        (999900001004, 999900000467, 60.00, CURRENT_TIMESTAMP,
            'CT'::t_payment_method_enum, 'S'::t_pi_destination_type_enum,
            '{"additional_information":{"originator_reference":"AUTO-REF-1234567890-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-LONG","suspense_reason_code":"OF","suspense_note":"Do not insert"}}', FALSE, FALSE, TRUE),
        (999900001006, 999900000467, 60.00, CURRENT_TIMESTAMP,
            'CT'::t_payment_method_enum, 'F'::t_pi_destination_type_enum,
            '{}', FALSE, FALSE, FALSE),
        (999900001007, 999900000467, 60.00, CURRENT_TIMESTAMP,
            'CT'::t_payment_method_enum, 'F'::t_pi_destination_type_enum,
            '{}', FALSE, FALSE, FALSE),
        (999900001008, 999900000467, 60.00, CURRENT_TIMESTAMP,
            'CT'::t_payment_method_enum, 'C'::t_pi_destination_type_enum,
            '{"payment_received_from":"T","additional_information":{"suspense_reason_code":"UN","payment_reference":"PAY123456","transfer_source":"NATWEST","suspense_reason_detail":"Unmatched payment","suspense_note":"Created during till allocation","payer_details":{"individual":{"title":"Mr","forenames":"Sam","surname":"Smith"},"address_line_1":"1 High Street","address_line_2":"London","address_line_3":"Greater London","postcode":"SW1A 1AA"}}}', FALSE, FALSE, FALSE),
        (999900001009, 999900000467, 40.00, CURRENT_TIMESTAMP,
            'CT'::t_payment_method_enum, 'C'::t_pi_destination_type_enum,
            '{}', FALSE, FALSE, FALSE),
        (999900001014, 999900000467, 60.00, CURRENT_TIMESTAMP,
            'CT'::t_payment_method_enum, 'S'::t_pi_destination_type_enum,
            '{"payer_details":{"organisation":{"organisation_name":"Example Ltd"}}}', FALSE, FALSE, FALSE);

    COMMIT;
    RAISE NOTICE 'Test data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Allocate a manual suspense payment with full payer details and a note
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information payments_in.additional_information%TYPE;
    v_suspense_transaction   suspense_transactions%ROWTYPE;
    v_suspense_item          suspense_items%ROWTYPE;
    v_note                   notes%ROWTYPE;
    v_created_before         TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_item_count_before      BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_number_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987);
BEGIN
    RAISE NOTICE '=== TEST 1: Allocate a manual suspense payment with full payer details and a note ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    CALL p_allocate_suspense_payment(
        pi_posted_by := 'PO3467UT'::VARCHAR,
        pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
        pi_business_unit_id := 9987::SMALLINT,
        pi_suspense_amount := 60.00::NUMERIC,
        pi_destination_type := 'S'::t_pi_destination_type_enum,
        pi_suspense_item_type := 'OF'::t_suspense_item_type_enum,
        pi_payment_method := 'CT'::t_payment_method_enum,
        pi_payment_in_id := 999900001001::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fee_received_id := NULL::BIGINT
    );

    SELECT * INTO STRICT v_suspense_transaction
      FROM suspense_transactions
     WHERE associated_record_id = '999900001001' AND transaction_type::TEXT = 'UN' AND posted_by = 'PO3467UT';
    SELECT * INTO STRICT v_suspense_item
      FROM suspense_items WHERE suspense_item_id = v_suspense_transaction.suspense_item_id;

    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before + 1, 'Expected one new suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987)
        = v_number_count_before + 1, 'Expected one new item number reservation';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before + 1, 'Expected one new suspense transaction';
    ASSERT v_suspense_item.suspense_account_id = 999900000467, 'The suspense account should match';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'UN', 'The suspense item type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CT', 'The payment method should match';
    ASSERT v_suspense_item.court_fee_id IS NOT DISTINCT FROM NULL::BIGINT,
        'The suspense item court fee should match';
    ASSERT v_suspense_transaction.transaction_type::TEXT = 'UN', 'The transaction type should match';
    ASSERT v_suspense_transaction.amount = 60.00, 'The transaction should contain only this allocation amount';
    ASSERT v_suspense_transaction.text IS NOT DISTINCT FROM 'PAY123456, NATWEST, Unmatched payment, Mr, Sam, Smith, 1 High Street, London, Greater London, SW1A 1AA', 'The transaction text should match';
    ASSERT v_suspense_transaction.posted_by = 'PO3467UT', 'The posting user should match';
    ASSERT v_suspense_transaction.posted_by_name = 'PO-3467 Unit Test', 'The posting user name should match';
    ASSERT v_suspense_transaction.posted_date >= v_created_before, 'The transaction should have the current posting date';
    ASSERT v_suspense_transaction.associated_record_type IS NULL, 'The associated record type should be NULL';
    ASSERT v_suspense_transaction.associated_record_id = '999900001001', 'The payment in relationship should match';
    ASSERT v_suspense_transaction.reversed IS NULL, 'The reversed flag should be NULL';
    ASSERT v_suspense_item.created_date >= v_created_before, 'The item should have the current creation date';
    
    SELECT * INTO STRICT v_note FROM notes
     WHERE associated_record_type = 'suspense_items'
       AND associated_record_id = v_suspense_item.suspense_item_id::TEXT;
    ASSERT v_note.note_text = 'Created during till allocation', 'The suspense note text should match';
    ASSERT v_note.note_type::TEXT = 'AA', 'The suspense note should be an account activity note';
    ASSERT v_note.posted_by = 'PO3467UT' AND v_note.posted_by_name = 'PO-3467 Unit Test',
        'The note posting details should match';
    ASSERT v_note.posted_date = v_suspense_item.created_date, 'The note should be created with the shared item';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Use the full auto-payment originator reference and omit manual notes
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information payments_in.additional_information%TYPE;
    v_suspense_transaction   suspense_transactions%ROWTYPE;
    v_suspense_item          suspense_items%ROWTYPE;
    v_note                   notes%ROWTYPE;
    v_created_before         TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_item_count_before      BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_number_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987);
BEGIN
    RAISE NOTICE '=== TEST 2: Use the full auto-payment originator reference and omit manual notes ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001004;

    CALL p_allocate_suspense_payment(
        pi_posted_by := 'PO3467UT'::VARCHAR,
        pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
        pi_business_unit_id := 9987::SMALLINT,
        pi_suspense_amount := 60.00::NUMERIC,
        pi_destination_type := 'S'::t_pi_destination_type_enum,
        pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
        pi_payment_method := 'CT'::t_payment_method_enum,
        pi_payment_in_id := 999900001004::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fee_received_id := NULL::BIGINT
    );

    SELECT * INTO STRICT v_suspense_transaction
      FROM suspense_transactions
     WHERE associated_record_id = '999900001004' AND transaction_type::TEXT = 'UN' AND posted_by = 'PO3467UT';
    SELECT * INTO STRICT v_suspense_item
      FROM suspense_items WHERE suspense_item_id = v_suspense_transaction.suspense_item_id;

    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before + 1, 'Expected one new suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987)
        = v_number_count_before + 1, 'Expected one new item number reservation';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before + 1, 'Expected one new suspense transaction';
    ASSERT v_suspense_item.suspense_account_id = 999900000467, 'The suspense account should match';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'UN', 'The suspense item type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CT', 'The payment method should match';
    ASSERT v_suspense_item.court_fee_id IS NOT DISTINCT FROM NULL::BIGINT,
        'The suspense item court fee should match';
    ASSERT v_suspense_transaction.transaction_type::TEXT = 'UN', 'The transaction type should match';
    ASSERT v_suspense_transaction.amount = 60.00, 'The transaction should contain only this allocation amount';
    ASSERT v_suspense_transaction.text IS NOT DISTINCT FROM 'AUTO-REF-1234567890-ABCDEFGHIJKLMNOPQRSTUVWXYZ-1234567890-LONG', 'The transaction text should match';
    ASSERT v_suspense_transaction.posted_by = 'PO3467UT', 'The posting user should match';
    ASSERT v_suspense_transaction.posted_by_name = 'PO-3467 Unit Test', 'The posting user name should match';
    ASSERT v_suspense_transaction.posted_date >= v_created_before, 'The transaction should have the current posting date';
    ASSERT v_suspense_transaction.associated_record_type IS NULL, 'The associated record type should be NULL';
    ASSERT v_suspense_transaction.associated_record_id = '999900001004', 'The payment in relationship should match';
    ASSERT v_suspense_transaction.reversed IS NULL, 'The reversed flag should be NULL';
    ASSERT v_suspense_item.created_date >= v_created_before, 'The item should have the current creation date';
    
    ASSERT NOT EXISTS (SELECT 1 FROM notes WHERE associated_record_type = 'suspense_items'
        AND associated_record_id = v_suspense_item.suspense_item_id::TEXT), 'No suspense note should be inserted';

    RAISE NOTICE 'TEST 2 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Allocate only the fines overpayment amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information payments_in.additional_information%TYPE;
    v_suspense_transaction   suspense_transactions%ROWTYPE;
    v_suspense_item          suspense_items%ROWTYPE;
    v_note                   notes%ROWTYPE;
    v_created_before         TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_item_count_before      BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_number_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987);
BEGIN
    RAISE NOTICE '=== TEST 3: Allocate only the fines overpayment amount ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001006;

    CALL p_allocate_suspense_payment(
        pi_posted_by := 'PO3467UT'::VARCHAR,
        pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
        pi_business_unit_id := 9987::SMALLINT,
        pi_suspense_amount := 20.00::NUMERIC,
        pi_destination_type := 'F'::t_pi_destination_type_enum,
        pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
        pi_payment_method := 'CT'::t_payment_method_enum,
        pi_payment_in_id := 999900001006::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fee_received_id := NULL::BIGINT
    );

    SELECT * INTO STRICT v_suspense_transaction
      FROM suspense_transactions
     WHERE associated_record_id = '999900001006' AND transaction_type::TEXT = 'OF' AND posted_by = 'PO3467UT';
    SELECT * INTO STRICT v_suspense_item
      FROM suspense_items WHERE suspense_item_id = v_suspense_transaction.suspense_item_id;

    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before + 1, 'Expected one new suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987)
        = v_number_count_before + 1, 'Expected one new item number reservation';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before + 1, 'Expected one new suspense transaction';
    ASSERT v_suspense_item.suspense_account_id = 999900000467, 'The suspense account should match';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'OF', 'The suspense item type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CT', 'The payment method should match';
    ASSERT v_suspense_item.court_fee_id IS NOT DISTINCT FROM NULL::BIGINT,
        'The suspense item court fee should match';
    ASSERT v_suspense_transaction.transaction_type::TEXT = 'OF', 'The transaction type should match';
    ASSERT v_suspense_transaction.amount = 20.00, 'The transaction should contain only this allocation amount';
    ASSERT v_suspense_transaction.text IS NOT DISTINCT FROM NULL, 'The transaction text should match';
    ASSERT v_suspense_transaction.posted_by = 'PO3467UT', 'The posting user should match';
    ASSERT v_suspense_transaction.posted_by_name = 'PO-3467 Unit Test', 'The posting user name should match';
    ASSERT v_suspense_transaction.posted_date >= v_created_before, 'The transaction should have the current posting date';
    ASSERT v_suspense_transaction.associated_record_type IS NULL, 'The associated record type should be NULL';
    ASSERT v_suspense_transaction.associated_record_id = '999900001006', 'The payment in relationship should match';
    ASSERT v_suspense_transaction.reversed IS NULL, 'The reversed flag should be NULL';
    ASSERT v_suspense_item.created_date >= v_created_before, 'The item should have the current creation date';
    ASSERT NOT EXISTS (SELECT 1 FROM notes WHERE associated_record_type = 'suspense_items'
        AND associated_record_id = v_suspense_item.suspense_item_id::TEXT), 'No suspense note should be inserted';

    RAISE NOTICE 'TEST 3 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Allocate a fines payment in advance
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information payments_in.additional_information%TYPE;
    v_suspense_transaction   suspense_transactions%ROWTYPE;
    v_suspense_item          suspense_items%ROWTYPE;
    v_note                   notes%ROWTYPE;
    v_created_before         TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_item_count_before      BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_number_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987);
BEGIN
    RAISE NOTICE '=== TEST 4: Allocate a fines payment in advance ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001007;

    CALL p_allocate_suspense_payment(
        pi_posted_by := 'PO3467UT'::VARCHAR,
        pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
        pi_business_unit_id := 9987::SMALLINT,
        pi_suspense_amount := 60.00::NUMERIC,
        pi_destination_type := 'F'::t_pi_destination_type_enum,
        pi_suspense_item_type := 'FA'::t_suspense_item_type_enum,
        pi_payment_method := 'CT'::t_payment_method_enum,
        pi_payment_in_id := 999900001007::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fee_received_id := NULL::BIGINT
    );

    SELECT * INTO STRICT v_suspense_transaction
      FROM suspense_transactions
     WHERE associated_record_id = '999900001007' AND transaction_type::TEXT = 'FA' AND posted_by = 'PO3467UT';
    SELECT * INTO STRICT v_suspense_item
      FROM suspense_items WHERE suspense_item_id = v_suspense_transaction.suspense_item_id;

    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before + 1, 'Expected one new suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987)
        = v_number_count_before + 1, 'Expected one new item number reservation';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before + 1, 'Expected one new suspense transaction';
    ASSERT v_suspense_item.suspense_account_id = 999900000467, 'The suspense account should match';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'FA', 'The suspense item type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CT', 'The payment method should match';
    ASSERT v_suspense_item.court_fee_id IS NOT DISTINCT FROM NULL::BIGINT,
        'The suspense item court fee should match';
    ASSERT v_suspense_transaction.transaction_type::TEXT = 'FA', 'The transaction type should match';
    ASSERT v_suspense_transaction.amount = 60.00, 'The transaction should contain only this allocation amount';
    ASSERT v_suspense_transaction.text IS NOT DISTINCT FROM NULL, 'The transaction text should match';
    ASSERT v_suspense_transaction.posted_by = 'PO3467UT', 'The posting user should match';
    ASSERT v_suspense_transaction.posted_by_name = 'PO-3467 Unit Test', 'The posting user name should match';
    ASSERT v_suspense_transaction.posted_date >= v_created_before, 'The transaction should have the current posting date';
    ASSERT v_suspense_transaction.associated_record_type IS NULL, 'The associated record type should be NULL';
    ASSERT v_suspense_transaction.associated_record_id = '999900001007', 'The payment in relationship should match';
    ASSERT v_suspense_transaction.reversed IS NULL, 'The reversed flag should be NULL';
    ASSERT v_suspense_item.created_date >= v_created_before, 'The item should have the current creation date';
    ASSERT NOT EXISTS (SELECT 1 FROM notes WHERE associated_record_type = 'suspense_items'
        AND associated_record_id = v_suspense_item.suspense_item_id::TEXT), 'No suspense note should be inserted';

    RAISE NOTICE 'TEST 4 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Allocate the court fee using a non-overpayment Court Fee Received record
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information payments_in.additional_information%TYPE;
    v_suspense_transaction   suspense_transactions%ROWTYPE;
    v_suspense_item          suspense_items%ROWTYPE;
    v_note                   notes%ROWTYPE;
    v_created_before         TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_item_count_before      BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_number_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987);
BEGIN
    RAISE NOTICE '=== TEST 5: Allocate the court fee using a non-overpayment Court Fee Received record ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001008;

    CALL p_allocate_suspense_payment(
        pi_posted_by := 'PO3467UT'::VARCHAR,
        pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
        pi_business_unit_id := 9987::SMALLINT,
        pi_suspense_amount := 40.00::NUMERIC,
        pi_destination_type := 'C'::t_pi_destination_type_enum,
        pi_suspense_item_type := 'OC'::t_suspense_item_type_enum,
        pi_payment_method := 'CT'::t_payment_method_enum,
        pi_payment_in_id := 999900001008::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fee_received_id := 999900000467::BIGINT
    );

    SELECT * INTO STRICT v_suspense_transaction
      FROM suspense_transactions
     WHERE associated_record_id = '999900001008' AND transaction_type::TEXT = 'CF' AND posted_by = 'PO3467UT';
    SELECT * INTO STRICT v_suspense_item
      FROM suspense_items WHERE suspense_item_id = v_suspense_transaction.suspense_item_id;

    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before + 1, 'Expected one new suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987)
        = v_number_count_before + 1, 'Expected one new item number reservation';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before + 1, 'Expected one new suspense transaction';
    ASSERT v_suspense_item.suspense_account_id = 999900000467, 'The suspense account should match';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'CF', 'The suspense item type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CT', 'The payment method should match';
    ASSERT v_suspense_item.court_fee_id IS NOT DISTINCT FROM 999900000467,
        'The suspense item court fee should match';
    ASSERT v_suspense_transaction.transaction_type::TEXT = 'CF', 'The transaction type should match';
    ASSERT v_suspense_transaction.amount = 40.00, 'The transaction should contain only this allocation amount';
    ASSERT v_suspense_transaction.text IS NOT DISTINCT FROM 'PAY123456, NATWEST, Unmatched payment, Mr, Sam, Smith, 1 High Street, London, Greater London, SW1A 1AA', 'The transaction text should match';
    ASSERT v_suspense_transaction.posted_by = 'PO3467UT', 'The posting user should match';
    ASSERT v_suspense_transaction.posted_by_name = 'PO-3467 Unit Test', 'The posting user name should match';
    ASSERT v_suspense_transaction.posted_date >= v_created_before, 'The transaction should have the current posting date';
    ASSERT v_suspense_transaction.associated_record_type IS NULL, 'The associated record type should be NULL';
    ASSERT v_suspense_transaction.associated_record_id = '999900001008', 'The payment in relationship should match';
    ASSERT v_suspense_transaction.reversed IS NULL, 'The reversed flag should be NULL';
    ASSERT v_suspense_item.created_date >= v_created_before, 'The item should have the current creation date';
    
    SELECT * INTO STRICT v_note FROM notes
     WHERE associated_record_type = 'suspense_items'
       AND associated_record_id = v_suspense_item.suspense_item_id::TEXT;
    ASSERT v_note.note_text = 'Created during till allocation', 'The suspense note text should match';
    ASSERT v_note.note_type::TEXT = 'AA', 'The suspense note should be an account activity note';
    ASSERT v_note.posted_by = 'PO3467UT' AND v_note.posted_by_name = 'PO-3467 Unit Test',
        'The note posting details should match';
    ASSERT v_note.posted_date = v_suspense_item.created_date, 'The note should be created with the shared item';
    ASSERT (SELECT suspense_transaction_id FROM court_fees_received
        WHERE court_fee_received_id = 999900000467) = v_suspense_transaction.suspense_transaction_id,
        'The Court Fee Received record should link to its own transaction';

    RAISE NOTICE 'TEST 5 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Allocate the court fee excess to the same suspense item
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information payments_in.additional_information%TYPE;
    v_suspense_transaction   suspense_transactions%ROWTYPE;
    v_suspense_item          suspense_items%ROWTYPE;
    v_note                   notes%ROWTYPE;
    v_created_before         TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_item_count_before      BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_number_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987);
BEGIN
    RAISE NOTICE '=== TEST 6: Allocate the court fee excess to the same suspense item ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001008;

    CALL p_allocate_suspense_payment(
        pi_posted_by := 'PO3467UT'::VARCHAR,
        pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
        pi_business_unit_id := 9987::SMALLINT,
        pi_suspense_amount := 20.00::NUMERIC,
        pi_destination_type := 'C'::t_pi_destination_type_enum,
        pi_suspense_item_type := 'CF'::t_suspense_item_type_enum,
        pi_payment_method := 'CT'::t_payment_method_enum,
        pi_payment_in_id := 999900001008::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fee_received_id := 999900000468::BIGINT
    );

    SELECT * INTO STRICT v_suspense_transaction
      FROM suspense_transactions
     WHERE associated_record_id = '999900001008' AND transaction_type::TEXT = 'OC' AND posted_by = 'PO3467UT';
    SELECT * INTO STRICT v_suspense_item
      FROM suspense_items WHERE suspense_item_id = v_suspense_transaction.suspense_item_id;

    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before + 0, 'Expected the existing shared suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987)
        = v_number_count_before + 0, 'Expected no extra item number reservation';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before + 1, 'Expected one new suspense transaction';
    ASSERT v_suspense_item.suspense_account_id = 999900000467, 'The suspense account should match';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'CF', 'The suspense item type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CT', 'The payment method should match';
    ASSERT v_suspense_item.court_fee_id IS NOT DISTINCT FROM 999900000467,
        'The suspense item court fee should match';
    ASSERT v_suspense_transaction.transaction_type::TEXT = 'OC', 'The transaction type should match';
    ASSERT v_suspense_transaction.amount = 20.00, 'The transaction should contain only this allocation amount';
    ASSERT v_suspense_transaction.text IS NOT DISTINCT FROM 'PAY123456, NATWEST, Unmatched payment, Mr, Sam, Smith, 1 High Street, London, Greater London, SW1A 1AA', 'The transaction text should match';
    ASSERT v_suspense_transaction.posted_by = 'PO3467UT', 'The posting user should match';
    ASSERT v_suspense_transaction.posted_by_name = 'PO-3467 Unit Test', 'The posting user name should match';
    ASSERT v_suspense_transaction.posted_date >= v_created_before, 'The transaction should have the current posting date';
    ASSERT v_suspense_transaction.associated_record_type IS NULL, 'The associated record type should be NULL';
    ASSERT v_suspense_transaction.associated_record_id = '999900001008', 'The payment in relationship should match';
    ASSERT v_suspense_transaction.reversed IS NULL, 'The reversed flag should be NULL';
    
    SELECT * INTO STRICT v_note FROM notes
     WHERE associated_record_type = 'suspense_items'
       AND associated_record_id = v_suspense_item.suspense_item_id::TEXT;
    ASSERT v_note.note_text = 'Created during till allocation', 'The suspense note text should match';
    ASSERT v_note.note_type::TEXT = 'AA', 'The suspense note should be an account activity note';
    ASSERT v_note.posted_by = 'PO3467UT' AND v_note.posted_by_name = 'PO-3467 Unit Test',
        'The note posting details should match';
    ASSERT v_note.posted_date = v_suspense_item.created_date, 'The note should be created with the shared item';
    ASSERT (SELECT suspense_transaction_id FROM court_fees_received
        WHERE court_fee_received_id = 999900000468) = v_suspense_transaction.suspense_transaction_id,
        'The Court Fee Received record should link to its own transaction';
    ASSERT (SELECT COUNT(*) = 2 AND COUNT(DISTINCT suspense_item_id) = 1 AND SUM(amount) = 60.00
        AND COUNT(*) FILTER (WHERE transaction_type::TEXT = 'CF') = 1
        AND COUNT(*) FILTER (WHERE transaction_type::TEXT = 'OC') = 1
        FROM suspense_transactions WHERE associated_record_id = '999900001008'),
        'The fee and excess should have two transactions, one shared item and the correct combined amount';

    RAISE NOTICE 'TEST 6 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Allocate a court fee without an overpayment
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information payments_in.additional_information%TYPE;
    v_suspense_transaction   suspense_transactions%ROWTYPE;
    v_suspense_item          suspense_items%ROWTYPE;
    v_note                   notes%ROWTYPE;
    v_created_before         TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_item_count_before      BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_number_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987);
BEGIN
    RAISE NOTICE '=== TEST 7: Allocate a court fee without an overpayment ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001009;

    CALL p_allocate_suspense_payment(
        pi_posted_by := 'PO3467UT'::VARCHAR,
        pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
        pi_business_unit_id := 9987::SMALLINT,
        pi_suspense_amount := 40.00::NUMERIC,
        pi_destination_type := 'C'::t_pi_destination_type_enum,
        pi_suspense_item_type := 'OC'::t_suspense_item_type_enum,
        pi_payment_method := 'CT'::t_payment_method_enum,
        pi_payment_in_id := 999900001009::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fee_received_id := 999900000474::BIGINT
    );

    SELECT * INTO STRICT v_suspense_transaction
      FROM suspense_transactions
     WHERE associated_record_id = '999900001009' AND transaction_type::TEXT = 'CF' AND posted_by = 'PO3467UT';
    SELECT * INTO STRICT v_suspense_item
      FROM suspense_items WHERE suspense_item_id = v_suspense_transaction.suspense_item_id;

    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before + 1, 'Expected one new suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987)
        = v_number_count_before + 1, 'Expected one new item number reservation';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before + 1, 'Expected one new suspense transaction';
    ASSERT v_suspense_item.suspense_account_id = 999900000467, 'The suspense account should match';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'CF', 'The suspense item type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CT', 'The payment method should match';
    ASSERT v_suspense_item.court_fee_id IS NOT DISTINCT FROM 999900000467,
        'The suspense item court fee should match';
    ASSERT v_suspense_transaction.transaction_type::TEXT = 'CF', 'The transaction type should match';
    ASSERT v_suspense_transaction.amount = 40.00, 'The transaction should contain only this allocation amount';
    ASSERT v_suspense_transaction.text IS NOT DISTINCT FROM NULL, 'The transaction text should match';
    ASSERT v_suspense_transaction.posted_by = 'PO3467UT', 'The posting user should match';
    ASSERT v_suspense_transaction.posted_by_name = 'PO-3467 Unit Test', 'The posting user name should match';
    ASSERT v_suspense_transaction.posted_date >= v_created_before, 'The transaction should have the current posting date';
    ASSERT v_suspense_transaction.associated_record_type IS NULL, 'The associated record type should be NULL';
    ASSERT v_suspense_transaction.associated_record_id = '999900001009', 'The payment in relationship should match';
    ASSERT v_suspense_transaction.reversed IS NULL, 'The reversed flag should be NULL';
    ASSERT v_suspense_item.created_date >= v_created_before, 'The item should have the current creation date';
    ASSERT NOT EXISTS (SELECT 1 FROM notes WHERE associated_record_type = 'suspense_items'
        AND associated_record_id = v_suspense_item.suspense_item_id::TEXT), 'No suspense note should be inserted';
    ASSERT (SELECT suspense_transaction_id FROM court_fees_received
        WHERE court_fee_received_id = 999900000474) = v_suspense_transaction.suspense_transaction_id,
        'The Court Fee Received record should link to its own transaction';

    RAISE NOTICE 'TEST 7 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 8: Include the organisation name in the transaction text
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_additional_information payments_in.additional_information%TYPE;
    v_suspense_transaction   suspense_transactions%ROWTYPE;
    v_suspense_item          suspense_items%ROWTYPE;
    v_note                   notes%ROWTYPE;
    v_created_before         TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP;
    v_item_count_before      BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_number_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987);
BEGIN
    RAISE NOTICE '=== TEST 8: Include the organisation name in the transaction text ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001014;

    CALL p_allocate_suspense_payment(
        pi_posted_by := 'PO3467UT'::VARCHAR,
        pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
        pi_business_unit_id := 9987::SMALLINT,
        pi_suspense_amount := 60.00::NUMERIC,
        pi_destination_type := 'S'::t_pi_destination_type_enum,
        pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
        pi_payment_method := 'CT'::t_payment_method_enum,
        pi_payment_in_id := 999900001014::BIGINT,
        pi_additional_information := v_additional_information,
        pi_court_fee_received_id := NULL::BIGINT
    );

    SELECT * INTO STRICT v_suspense_transaction
      FROM suspense_transactions
     WHERE associated_record_id = '999900001014' AND transaction_type::TEXT = 'UN' AND posted_by = 'PO3467UT';
    SELECT * INTO STRICT v_suspense_item
      FROM suspense_items WHERE suspense_item_id = v_suspense_transaction.suspense_item_id;

    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before + 1, 'Expected one new suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id = 9987)
        = v_number_count_before + 1, 'Expected one new item number reservation';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before + 1, 'Expected one new suspense transaction';
    ASSERT v_suspense_item.suspense_account_id = 999900000467, 'The suspense account should match';
    ASSERT v_suspense_item.suspense_item_type::TEXT = 'UN', 'The suspense item type should match';
    ASSERT v_suspense_item.payment_method::TEXT = 'CT', 'The payment method should match';
    ASSERT v_suspense_item.court_fee_id IS NOT DISTINCT FROM NULL::BIGINT,
        'The suspense item court fee should match';
    ASSERT v_suspense_transaction.transaction_type::TEXT = 'UN', 'The transaction type should match';
    ASSERT v_suspense_transaction.amount = 60.00, 'The transaction should contain only this allocation amount';
    ASSERT v_suspense_transaction.text IS NOT DISTINCT FROM 'Example Ltd', 'The transaction text should match';
    ASSERT v_suspense_transaction.posted_by = 'PO3467UT', 'The posting user should match';
    ASSERT v_suspense_transaction.posted_by_name = 'PO-3467 Unit Test', 'The posting user name should match';
    ASSERT v_suspense_transaction.posted_date >= v_created_before, 'The transaction should have the current posting date';
    ASSERT v_suspense_transaction.associated_record_type IS NULL, 'The associated record type should be NULL';
    ASSERT v_suspense_transaction.associated_record_id = '999900001014', 'The payment in relationship should match';
    ASSERT v_suspense_transaction.reversed IS NULL, 'The reversed flag should be NULL';
    ASSERT v_suspense_item.created_date >= v_created_before, 'The item should have the current creation date';
    ASSERT NOT EXISTS (SELECT 1 FROM notes WHERE associated_record_type = 'suspense_items'
        AND associated_record_id = v_suspense_item.suspense_item_id::TEXT), 'No suspense note should be inserted';

    RAISE NOTICE 'TEST 8 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 9: Reject a NULL business unit ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 9: Reject a NULL business unit ID ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := NULL::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := 'S'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3461' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_business_unit_id must be provided', 'P3461 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: pi_business_unit_id is required', 'P3461 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3461 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 9 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 10: Reject a NULL allocation amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 10: Reject a NULL allocation amount ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := NULL::NUMERIC,
            pi_destination_type := 'S'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3462' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_suspense_amount must be greater than zero', 'P3462 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: Passed pi_suspense_amount: NULL', 'P3462 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3462 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 10 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 11: Reject a zero allocation amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 11: Reject a zero allocation amount ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := 0.00::NUMERIC,
            pi_destination_type := 'S'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3462' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_suspense_amount must be greater than zero', 'P3462 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: Passed pi_suspense_amount: 0.00', 'P3462 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3462 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 11 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 12: Reject a negative allocation amount
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 12: Reject a negative allocation amount ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := -10.00::NUMERIC,
            pi_destination_type := 'S'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3462' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_suspense_amount must be greater than zero', 'P3462 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: Passed pi_suspense_amount: -10.00', 'P3462 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3462 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 12 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 13: Reject a NULL destination type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 13: Reject a NULL destination type ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := NULL::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3463' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_destination_type must be C, S or F', 'P3463 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: Passed pi_destination_type: NULL; expected C, S or F', 'P3463 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3463 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 13 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 14: Reject a NULL payment in ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 14: Reject a NULL payment in ID ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := 'S'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := NULL::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3464' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'payments_in record <NULL> was not found', 'P3464 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: No payments_in record exists for pi_payment_in_id: NULL', 'P3464 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3464 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 14 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 15: Reject an unknown payment in ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 15: Reject an unknown payment in ID ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := 'S'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := -1::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3464' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'payments_in record -1 was not found', 'P3464 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: No payments_in record exists for pi_payment_in_id: -1', 'P3464 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3464 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 15 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 16: Reject a Court Fee Received ID for a suspense destination
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 16: Reject a Court Fee Received ID for a suspense destination ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := 'S'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := 999900000467::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3465' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_court_fee_received_id is only applicable to Court Fee allocations', 'P3465 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: Passed pi_court_fee_received_id: 999900000467, pi_destination_type: S; court fee received IDs require destination C', 'P3465 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3465 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 16 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 17: Reject a missing Court Fee Received ID for a court fee
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 17: Reject a missing Court Fee Received ID for a court fee ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := 'C'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3468' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_court_fee_received_id must be provided for Court Fee allocations', 'P3468 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: pi_destination_type is C; pi_court_fee_received_id is required to determine the overpayment flag', 'P3468 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3468 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 17 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 18: Reject an OC item type without a Court Fee Received ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 18: Reject an OC item type without a Court Fee Received ID ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := 'C'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'OC'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3468' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'pi_court_fee_received_id must be provided for Court Fee allocations', 'P3468 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: pi_destination_type is C; pi_court_fee_received_id is required to determine the overpayment flag', 'P3468 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3468 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 18 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 19: Reject an unknown Court Fee Received ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 19: Reject an unknown Court Fee Received ID ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9987::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := 'C'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := -1::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3466' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'court_fees_received record -1 was not found', 'P3466 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: No court_fees_received record exists for pi_court_fee_received_id: -1', 'P3466 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3466 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 19 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 20: Reject a Court Fee Received record from another business unit
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 20: Reject a Court Fee Received record from another business unit ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9986::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := 'C'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := 999900000467::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3467' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'court_fees_received record 999900000467 belongs to a different business unit', 'P3467 should have the expected error message';
            ASSERT v_exception_detail = 'p_allocate_suspense_payment: Court fee received ID: 999900000467 belongs to business unit: 9987; passed pi_business_unit_id: 9986', 'P3467 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3467 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 20 PASSED';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 21: Reject a business unit without a suspense account
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught         BOOLEAN := FALSE;
    v_exception_detail     TEXT;
    v_additional_information payments_in.additional_information%TYPE;
    v_item_count_before    BIGINT := (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467);
    v_transaction_count_before BIGINT := (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT');
    v_note_count_before    BIGINT := (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT');
    v_number_count_before  BIGINT := (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987));
BEGIN
    RAISE NOTICE '=== TEST 21: Reject a business unit without a suspense account ===';

    SELECT additional_information INTO STRICT v_additional_information
      FROM payments_in WHERE payment_in_id = 999900001001;

    BEGIN
        CALL p_allocate_suspense_payment(
            pi_posted_by := 'PO3467UT'::VARCHAR,
            pi_posted_by_name := 'PO-3467 Unit Test'::VARCHAR,
            pi_business_unit_id := 9986::SMALLINT,
            pi_suspense_amount := 10.00::NUMERIC,
            pi_destination_type := 'S'::t_pi_destination_type_enum,
            pi_suspense_item_type := 'UN'::t_suspense_item_type_enum,
            pi_payment_method := 'CT'::t_payment_method_enum,
            pi_payment_in_id := 999900001001::BIGINT,
            pi_additional_information := v_additional_information,
            pi_court_fee_received_id := NULL::BIGINT
        );
    EXCEPTION
        WHEN SQLSTATE 'P3303' THEN
            GET STACKED DIAGNOSTICS v_exception_detail = PG_EXCEPTION_DETAIL;
            ASSERT SQLERRM = 'No suspense account found for business_unit_id 9986', 'P3303 should have the expected error message';
            ASSERT v_exception_detail = 'p_insert_suspense_item: No suspense account exists for business_unit_id 9986', 'P3303 should have the expected error detail';
            v_error_caught := TRUE;
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    ASSERT v_error_caught = TRUE, 'A P3303 error should have been raised';
    ASSERT (SELECT COUNT(*) FROM suspense_items WHERE suspense_account_id = 999900000467)
        = v_item_count_before, 'The failed call should not insert a suspense item';
    ASSERT (SELECT COUNT(*) FROM suspense_transactions WHERE posted_by = 'PO3467UT')
        = v_transaction_count_before, 'The failed call should not insert a suspense transaction';
    ASSERT (SELECT COUNT(*) FROM notes WHERE posted_by = 'PO3467UT')
        = v_note_count_before, 'The failed call should not insert a note';
    ASSERT (SELECT COUNT(*) FROM suspense_item_number_index WHERE business_unit_id IN (9986, 9987))
        = v_number_count_before, 'The failed call should not reserve a suspense item number';

    RAISE NOTICE 'TEST 21 PASSED';
END $$;


DO $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    DELETE FROM notes
     WHERE associated_record_type = 'suspense_items'
       AND associated_record_id IN (
           SELECT suspense_item_id::TEXT FROM suspense_items WHERE suspense_account_id = 999900000467
       );
    DELETE FROM court_fees_received WHERE court_fee_received_id IN (999900000467, 999900000468, 999900000474);
    DELETE FROM suspense_transactions
     WHERE suspense_item_id IN (
         SELECT suspense_item_id FROM suspense_items WHERE suspense_account_id = 999900000467
     );
    DELETE FROM payments_in WHERE till_id = 999900000467;
    DELETE FROM suspense_items WHERE suspense_account_id = 999900000467;
    DELETE FROM suspense_item_number_index WHERE business_unit_id = 9987;
    DELETE FROM tills WHERE till_id = 999900000467;
    DELETE FROM court_fees WHERE court_fee_id = 999900000467;
    DELETE FROM suspense_accounts WHERE suspense_account_id = 999900000467;
    DELETE FROM business_units WHERE business_unit_id IN (9986, 9987);
    COMMIT;
    RAISE NOTICE 'Test data cleanup completed';
    RAISE NOTICE 'All 21 p_allocate_suspense_payment unit tests passed';
END $$;
