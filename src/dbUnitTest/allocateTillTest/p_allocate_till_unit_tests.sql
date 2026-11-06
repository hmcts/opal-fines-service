/**
* OPAL Program
*
* MODULE      : p_allocate_till_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_allocate_till.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 03/11/2026    C Cho       1.0         PO-3417 - p_allocate_till unit tests
*
**/
\timing

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data before tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    DELETE FROM notes
     WHERE associated_record_type = 'suspense_items'
       AND associated_record_id IN (
           SELECT suspense_item_id::TEXT FROM suspense_items WHERE suspense_account_id = 9999003417000
       );
    DELETE FROM court_fees_received WHERE business_unit_id IN (9417, 9418);
    DELETE FROM suspense_transactions
     WHERE suspense_item_id IN (
           SELECT suspense_item_id FROM suspense_items WHERE suspense_account_id = 9999003417000
       );
    DELETE FROM allocations
     WHERE imposition_id IN (
           SELECT imposition_id FROM impositions
            WHERE defendant_account_id BETWEEN 9999003417001 AND 9999003417083
       );
    DELETE FROM creditor_transactions WHERE creditor_account_id = 9999003417000;
    DELETE FROM defendant_transactions WHERE defendant_account_id BETWEEN 9999003417001 AND 9999003417083;
    DELETE FROM payments_in
     WHERE till_id IN (
           SELECT till_id FROM tills WHERE business_unit_id = 9417
       );
    DELETE FROM impositions WHERE defendant_account_id BETWEEN 9999003417001 AND 9999003417083;
    DELETE FROM defendant_accounts WHERE defendant_account_id BETWEEN 9999003417001 AND 9999003417083;
    DELETE FROM suspense_items WHERE suspense_account_id = 9999003417000;
    DELETE FROM suspense_item_number_index WHERE business_unit_id = 9417;
    DELETE FROM tills WHERE business_unit_id = 9417;
    DELETE FROM interface_files WHERE interface_file_id IN (9999003417000, 9999003417001);
    DELETE FROM interface_jobs WHERE interface_job_id IN (9999003417000, 9999003417001);
    DELETE FROM court_fees WHERE court_fee_id IN (9999003417000, 9999003417001);
    DELETE FROM suspense_accounts WHERE suspense_account_id = 9999003417000;
    DELETE FROM creditor_accounts WHERE creditor_account_id = 9999003417000;
    DELETE FROM configuration_items WHERE configuration_item_id = 9999003417000;
    DELETE FROM results WHERE result_id IN ('U41701', 'U41700', 'U417IN');
    DELETE FROM business_units WHERE business_unit_id IN (9417, 9418);

    RAISE NOTICE 'Cleanup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test setup
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Setting up test data ===';

    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
    VALUES
        (9417, 'PO-3417 Unit Tests', 'U417', 'Accounting Division'),
        (9418, 'PO-3417 Other Unit', 'U418', 'Accounting Division');

    INSERT INTO suspense_accounts (suspense_account_id, business_unit_id, account_number)
    VALUES (9999003417000, 9417, 'UT3417-SUSPENSE');

    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, creditor_account_type,
        prosecution_service, repayment, hold_payout, pay_by_bacs
    )
    VALUES (9999003417000, 9417, 'UT3417-CREDITOR', 'CF', FALSE, FALSE, FALSE, FALSE);

    INSERT INTO results (
        result_id, result_title, result_type, active, imposition, imposition_category,
        imposition_allocation_priority, imposition_accruing, imposition_creditor,
        enforcement, enforcement_override, further_enforcement_warn,
        further_enforcement_disallow, enforcement_hold, requires_enforcer,
        generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
        extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement
    )
    VALUES
        ('U41701', 'PO-3417 test result', 'Result', TRUE, TRUE, NULL,
         1, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U41700', 'PO-3417 test result', 'Result', TRUE, TRUE, NULL,
         0, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
        ('U417IN', 'PO-3417 test result', 'Result', TRUE, TRUE, NULL,
         9, FALSE, 'CF', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
         FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE);

    INSERT INTO configuration_items (configuration_item_id, business_unit_id, item_name, item_values)
    VALUES (9999003417000, 9417, 'Inhibit payment results', '["U417IN"]');

    INSERT INTO court_fees (court_fee_id, business_unit_id, court_fee_code, description, amount, stats_code)
    VALUES
        (9999003417000, 9417, 'U417', 'PO-3417 court fee', 40.00, 'U417'),
        (9999003417001, 9418, 'U418', 'PO-3417 other unit fee', 40.00, 'U418');

    INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name)
    VALUES
        (9999003417000, 9417, 'PO-3417-INHIBIT'),
        (9999003417001, 9417, 'PO-3417-OVERRIDE');

    INSERT INTO interface_files (interface_file_id, interface_job_id, file_name, override_inhibits)
    VALUES
        (9999003417000, 9999003417000, 'PO-3417-inhibit.dat', FALSE),
        (9999003417001, 9999003417001, 'PO-3417-override.dat', TRUE);

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, amount_imposed,
        amount_paid, account_balance, account_status, account_type,
        last_movement_date, completed_date, version_number, last_enforcement
    )
    VALUES
        (9999003417001, 9417, 'UT3417-PARTIAL', -100.00, 20.00, -80.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417002, 9417, 'UT3417-OVERPAYMENT', -100.00, 20.00, -80.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417003, 9417, 'UT3417-TWO-PAYMENTS', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417010, 9417, 'UT3417-WRITTEN-OFF', -120.00, 20.00, -100.00, 'WO', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417011, 9417, 'UT3417-TRANSFER-OUT', -120.00, 20.00, -100.00, 'TO', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417012, 9417, 'UT3417-TRANSFER-ACK', -120.00, 20.00, -100.00, 'TA', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417013, 9417, 'UT3417-NI-SCOTLAND', -120.00, 20.00, -100.00, 'TS', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417014, 9417, 'UT3417-ZERO', -20.00, 20.00, 0.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417016, 9417, 'UT3417-MAN-INHIBIT', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, 'U417IN'),
        (9999003417017, 9417, 'UT3417-AUTO-INHIBIT', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, 'U417IN'),
        (9999003417018, 9417, 'UT3417-OVERRIDE', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, 'U417IN'),
        (9999003417019, 9417, 'UT3417-OVERRIDE-WO', -120.00, 20.00, -100.00, 'WO', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, 'U417IN'),
        (9999003417020, 9417, 'UT3417-CONSOLIDATED', -120.00, 20.00, -100.00, 'CS', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417021, 9417, 'UT3417-MID-MASTER', -120.00, 20.00, -100.00, 'CS', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417022, 9417, 'UT3417-TOP-MASTER', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417041, 9417, 'UT3417-IMPOSITIONS', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417042, 9417, 'UT3417-INELIGIBLE', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417051, 9417, 'UT3417-ROLLBACK', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417063, 9418, 'UT3417-OTHER-UNIT', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417064, 9417, 'UT3417-POSITIVE', -10.00, 20.00, 10.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417065, 9417, 'UT3417-WRONG-MODE', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417066, 9417, 'UT3417-ZERO-PAYMENT', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417067, 9417, 'UT3417-NEGATIVE', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417080, 9417, 'UT3417-LEGACY', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL),
        (9999003417081, 9417, 'UT3417-ROOT-JSON', -120.00, 20.00, -100.00, 'L', 'Fine',
         TIMESTAMP '2020-01-01', NULL, 1, NULL);

    INSERT INTO impositions (
        imposition_id, defendant_account_id, posted_date, result_id,
        imposed_amount, paid_amount, creditor_account_id, completed
    )
    VALUES
        (9999003417001, 9999003417001, TIMESTAMP '2020-01-01', 'U41701', -80.00, 20.00, 9999003417000, FALSE),
        (9999003417002, 9999003417002, TIMESTAMP '2020-01-01', 'U41701', -100.00, 20.00, 9999003417000, FALSE),
        (9999003417003, 9999003417003, TIMESTAMP '2020-01-01', 'U41701', -120.00, 20.00, 9999003417000, FALSE),
        (9999003417018, 9999003417018, TIMESTAMP '2020-01-01', 'U41701', -120.00, 20.00, 9999003417000, FALSE),
        (9999003417022, 9999003417022, TIMESTAMP '2020-01-01', 'U41701', -120.00, 20.00, 9999003417000, FALSE),
        (9999003417041, 9999003417041, TIMESTAMP '2020-01-01', 'U41701', -50.00, 20.00, 9999003417000, FALSE),
        (9999003417042, 9999003417042, TIMESTAMP '2020-01-01', 'U41700', -120.00, 20.00, 9999003417000, FALSE),
        (9999003417051, 9999003417051, TIMESTAMP '2020-01-01', 'U41701', -120.00, 20.00, 9999003417000, FALSE),
        (9999003417080, 9999003417080, TIMESTAMP '2020-01-01', 'U41701', -120.00, 20.00, 9999003417000, FALSE),
        (9999003417081, 9999003417081, TIMESTAMP '2020-01-01', 'U41701', -120.00, 20.00, 9999003417000, FALSE),
        (9999003517001, 9999003417001, TIMESTAMP '2020-01-02', 'U41701', -20.00, 0.00, 9999003417000, FALSE);

    INSERT INTO defendant_transactions (
        defendant_transaction_id, defendant_account_id, posted_date,
        transaction_type, transaction_amount, associated_record_id, text
    )
    VALUES
        (9999003417020, 9999003417020, CURRENT_TIMESTAMP, 'WRTOFF', 0.00, '9999003417021', 'CONSOLIDATED test'),
        (9999003417021, 9999003417021, CURRENT_TIMESTAMP, 'WRTOFF', 0.00, '9999003417022', 'CONSOLIDATED test');

    INSERT INTO tills (
        till_id, business_unit_id, till_number, owned_by, status, auto_payment, source, interface_file_id
    )
    VALUES
        (9999003417001, 9417, 1, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417002, 9417, 2, 'PO3417UT', 'Created', TRUE, 'NATWEST', NULL),
        (9999003417003, 9417, 3, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417010, 9417, 10, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417011, 9417, 11, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417012, 9417, 12, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417013, 9417, 13, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417014, 9417, 14, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417015, 9417, 15, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417016, 9417, 16, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417017, 9417, 17, 'PO3417UT', 'Created', TRUE, 'NATWEST', 9999003417000),
        (9999003417018, 9417, 18, 'PO3417UT', 'Created', TRUE, 'NATWEST', 9999003417001),
        (9999003417019, 9417, 19, 'PO3417UT', 'Created', TRUE, 'NATWEST', 9999003417001),
        (9999003417020, 9417, 20, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417030, 9417, 30, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417031, 9417, 31, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417032, 9417, 32, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417033, 9417, 33, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417034, 9417, 34, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417040, 9417, 40, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417041, 9417, 41, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417042, 9417, 42, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417050, 9417, 50, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417060, 9417, 60, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417061, 9417, 61, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417062, 9417, 62, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417063, 9417, 63, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417064, 9417, 64, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417065, 9417, 65, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417066, 9417, 66, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417067, 9417, 67, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417072, 9417, 72, 'PO3417UT', 'Allocated', FALSE, NULL, NULL),
        (9999003417080, 9417, 80, 'PO3417UT', 'Created', TRUE, 'NATWEST', NULL),
        (9999003417081, 9417, 81, 'PO3417UT', 'Created', FALSE, NULL, NULL),
        (9999003417083, 9417, 83, 'PO3417UT', 'Created', FALSE, NULL, NULL);

    INSERT INTO payments_in (
        payment_in_id, till_id, payment_amount, payment_date, payment_method, destination_type,
        associated_record_type, associated_record_id, third_party_payer_name,
        additional_information, receipt, allocated, auto_payment
    )
    VALUES
        (9999003417001, 9999003417001, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417001', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417002, 9999003417002, 100.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417002', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, TRUE),
        (9999003417003, 9999003417003, 60.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417003', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417010, 9999003417010, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417010', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417011, 9999003417011, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417011', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417012, 9999003417012, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417012', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417013, 9999003417013, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417013', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417014, 9999003417014, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417014', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417015, 9999003417015, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417015', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417016, 9999003417016, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417016', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417017, 9999003417017, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417017', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, TRUE),
        (9999003417018, 9999003417018, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417018', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, TRUE),
        (9999003417019, 9999003417019, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417019', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, TRUE),
        (9999003417020, 9999003417020, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417020', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417030, 9999003417030, 40.00, TIMESTAMP '2020-01-01', 'CT', 'C',
         NULL, NULL, 'Third Party',
         '{"additional_information":{"court_fee_id":9999003417000,"number_of_items":1}}', FALSE, FALSE, FALSE),
        (9999003417031, 9999003417031, 65.00, TIMESTAMP '2020-01-01', 'CT', 'C',
         NULL, NULL, 'Third Party',
         '{"additional_information":{"court_fee_id":9999003417000,"number_of_items":1}}', FALSE, FALSE, FALSE),
        (9999003417032, 9999003417032, 90.00, TIMESTAMP '2020-01-01', 'CT', 'C',
         NULL, NULL, 'Third Party',
         '{"additional_information":{"court_fee_id":9999003417000,"number_of_items":2}}', FALSE, FALSE, FALSE),
        (9999003417033, 9999003417033, 20.00, TIMESTAMP '2020-01-01', 'CT', 'C',
         NULL, NULL, 'Third Party',
         '{"additional_information":{"court_fee_id":9999003417000,"number_of_items":1}}', FALSE, FALSE, FALSE),
        (9999003417034, 9999003417034, 40.00, TIMESTAMP '2020-01-01', 'CT', 'C',
         NULL, NULL, 'Third Party',
         '{"court_fee_code":"U417"}', FALSE, FALSE, FALSE),
        (9999003417040, 9999003417040, 25.00, TIMESTAMP '2020-01-01', 'CT', 'S',
         NULL, NULL, 'Third Party',
         '{"additional_information":{"suspense_reason_code":"FA","payment_reference":"REF-S","suspense_note":"Test suspense note"}}', FALSE, FALSE, FALSE),
        (9999003417041, 9999003417041, 75.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417041', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417042, 9999003417042, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417042', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417050, 9999003417050, 65.00, TIMESTAMP '2020-01-01', 'CT', 'C',
         NULL, NULL, 'Third Party',
         '{"court_fee_id":9999003417000,"suspense_note":"Must roll back"}', FALSE, FALSE, FALSE),
        (9999003417051, 9999003417050, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417051', 'Third Party',
         '{"transfer_source":"NATWEST"}', FALSE, FALSE, FALSE),
        (9999003417060, 9999003417060, 25.00, TIMESTAMP '2020-01-01', 'CT', 'C',
         NULL, NULL, 'Third Party',
         '{}', FALSE, FALSE, FALSE),
        (9999003417061, 9999003417061, 25.00, TIMESTAMP '2020-01-01', 'CT', 'C',
         NULL, NULL, 'Third Party',
         '{"court_fee_id":9999003417001}', FALSE, FALSE, FALSE),
        (9999003417062, 9999003417062, 25.00, TIMESTAMP '2020-01-01', 'CT', 'C',
         NULL, NULL, 'Third Party',
         '{"court_fee_id":9999003417000,"number_of_items":0}', FALSE, FALSE, FALSE),
        (9999003417063, 9999003417063, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417063', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417064, 9999003417064, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417064', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417065, 9999003417065, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417065', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, TRUE),
        (9999003417066, 9999003417066, 0.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417066', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417067, 9999003417067, -1.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417067', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417072, 9999003417072, 25.00, TIMESTAMP '2020-01-01', 'CT', 'S',
         NULL, NULL, 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE),
        (9999003417080, 9999003417080, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417080', 'Third Party',
         '"LEGACY-REF"', FALSE, FALSE, TRUE),
        (9999003417081, 9999003417081, 25.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417081', 'Third Party',
         '{"payment_reference":"ROOT-REF","source":"OTHER"}', FALSE, FALSE, FALSE),
        (9999003417083, 9999003417083, 25.00, TIMESTAMP '2020-01-01', 'CT', 'S',
         NULL, NULL, NULL, '{}', FALSE, FALSE, FALSE),
        (9999003517003, 9999003417003, 60.00, TIMESTAMP '2020-01-01', 'CT', 'F',
         'defendant_accounts', '9999003417003', 'Third Party',
         '{"additional_information":{"payment_reference":"REF-3417","transfer_source":"NATWEST"}}', FALSE, FALSE, FALSE);

    RAISE NOTICE 'Test data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: A manual partial payment updates the individual fields
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account     defendant_accounts%ROWTYPE;
    v_transaction defendant_transactions%ROWTYPE;
    v_imposition  impositions%ROWTYPE;
    v_allocation  allocations%ROWTYPE;
    v_creditor    creditor_transactions%ROWTYPE;
    v_payment     payments_in%ROWTYPE;
    v_till        tills%ROWTYPE;
    v_before      TIMESTAMP;
    v_after       TIMESTAMP;
BEGIN
    RAISE NOTICE '=== TEST 1: A manual partial payment updates the individual fields ===';

    v_before := CLOCK_TIMESTAMP();
    CALL p_allocate_till(9999003417001::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    v_after := CLOCK_TIMESTAMP();

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417001;
    ASSERT v_account.amount_imposed = -100.00, 'amount_imposed must remain unchanged';
    ASSERT v_account.amount_paid = 45.00, 'amount_paid must include the previous 20.00 and the new 25.00';
    ASSERT v_account.account_balance = -55.00, 'Partial payment must reduce the balance to -55.00';
    ASSERT v_account.account_status = 'L', 'Account status must remain live';
    ASSERT v_account.last_movement_date = CURRENT_TIMESTAMP::TIMESTAMP, 'last_movement_date must use the transaction timestamp';
    ASSERT v_account.completed_date IS NULL, 'Partial payment must not complete the account';
    ASSERT v_account.version_number = 2, 'version_number must increase once';

    SELECT * INTO STRICT v_transaction FROM defendant_transactions WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417001';
    ASSERT v_transaction.defendant_transaction_id IS NOT NULL, 'Defendant transaction ID must be generated';
    ASSERT v_transaction.defendant_account_id = 9999003417001, 'Transaction must reference the defendant account';
    ASSERT v_transaction.posted_by = 'PO3417UT', 'posted_by must match the supplied user';
    ASSERT v_transaction.posted_by_name = 'Unit Test User', 'posted_by_name must match the supplied name';
    ASSERT v_transaction.transaction_type = 'PAYMNT', 'Transaction type must be Payment';
    ASSERT v_transaction.transaction_amount = 25.00, 'Transaction amount must equal the allocated payment';
    ASSERT v_transaction.payment_method = 'CT', 'Payment method must come from payments_in';
    ASSERT v_transaction.payment_reference = 'REF-3417', 'Payment reference must come from the payment JSON';
    ASSERT v_transaction.text = 'Third Party', 'Payer name must be stored as transaction text';
    ASSERT v_transaction.source = 'NATWEST', 'Manual source must come from transfer_source';
    ASSERT v_transaction.status = 'C', 'Defendant transaction must be completed';
    ASSERT v_transaction.associated_record_type = 'payments_in', 'Associated record type must be payments_in';
    ASSERT v_transaction.associated_record_id = '9999003417001', 'Transaction must link to the payment';
    ASSERT v_transaction.posted_date BETWEEN v_before AND v_after, 'posted_date must be generated during allocation';
    ASSERT v_transaction.status_date BETWEEN v_transaction.posted_date AND v_after, 'status_date must be generated during allocation';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 9999003417001;
    ASSERT v_imposition.imposed_amount = -80.00, 'First imposed amount must remain unchanged';
    ASSERT v_imposition.paid_amount = 38.75, 'First imposition must receive 18.75 for its 60.00 outstanding balance';
    ASSERT v_imposition.completed = FALSE, 'First imposition must remain incomplete';
    SELECT * INTO STRICT v_allocation FROM allocations WHERE imposition_id = 9999003417001;
    ASSERT v_allocation.allocated_amount = 18.75, 'First allocation must be 18.75';
    ASSERT v_allocation.transaction_type = 'PAYMNT', 'Allocation transaction type must be Payment';
    ASSERT v_allocation.allocation_function = 'Manual Cash Input', 'Allocation function must identify manual input';
    ASSERT v_allocation.defendant_transaction_id = v_transaction.defendant_transaction_id, 'Allocation must link to the new transaction';
    SELECT * INTO STRICT v_creditor FROM creditor_transactions WHERE associated_record_type = 'impositions' AND associated_record_id = '9999003417001';
    ASSERT v_creditor.creditor_account_id = 9999003417000, 'Creditor must come from the imposition';
    ASSERT v_creditor.transaction_amount = 18.75, 'First creditor transaction must match its allocation';
    ASSERT v_creditor.transaction_type = 'PAYMNT', 'Creditor transaction type must be Payment';
    ASSERT v_creditor.payment_reference = 'REF-3417', 'Creditor reference must match the payment reference';
    ASSERT v_creditor.payment_processed = FALSE, 'Creditor payment must not be marked processed';

    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 9999003517001;
    ASSERT v_imposition.paid_amount = 6.25, 'Second imposition must receive 6.25 for its 20.00 outstanding balance';
    SELECT * INTO STRICT v_allocation FROM allocations WHERE imposition_id = 9999003517001;
    ASSERT v_allocation.allocated_amount = 6.25, 'Second allocation must be 6.25';
    ASSERT v_allocation.defendant_transaction_id = v_transaction.defendant_transaction_id, 'Both allocations must link to the same defendant transaction';
    SELECT * INTO STRICT v_creditor FROM creditor_transactions WHERE associated_record_type = 'impositions' AND associated_record_id = '9999003517001';
    ASSERT v_creditor.transaction_amount = 6.25, 'Second creditor transaction must match its allocation';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417001;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417001;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.payment_amount = 25.00, 'Original payment amount must remain unchanged';
    ASSERT v_till.business_unit_id = 9417, 'Till must remain in the supplied business unit';

    RAISE NOTICE 'TEST 1 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 1 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 1 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: An automatic overpayment settles the account and sends the excess to suspense
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account     defendant_accounts%ROWTYPE;
    v_transaction defendant_transactions%ROWTYPE;
    v_imposition  impositions%ROWTYPE;
    v_allocation  allocations%ROWTYPE;
    v_suspense    suspense_transactions%ROWTYPE;
    v_item        suspense_items%ROWTYPE;
    v_payment     payments_in%ROWTYPE;
    v_till        tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 2: An automatic overpayment settles the account and sends the excess to suspense ===';

    CALL p_allocate_till(9999003417002::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417002;
    ASSERT v_account.amount_paid = 100.00, 'Full payment must increase cumulative amount_paid to 100.00';
    ASSERT v_account.account_balance = 0.00, 'Account must be settled';
    ASSERT v_account.completed_date = CURRENT_TIMESTAMP::TIMESTAMP, 'Full payment must set completed_date';
    ASSERT v_account.last_movement_date = v_account.completed_date, 'Movement and completion dates must match';
    ASSERT v_account.version_number = 2, 'Account version must increase once';
    SELECT * INTO STRICT v_transaction FROM defendant_transactions WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417002';
    ASSERT v_transaction.transaction_amount = 80.00, 'Only the outstanding 80.00 must reach the account';
    ASSERT v_transaction.source = 'NATWEST', 'Automatic source must come from the till';
    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 9999003417002;
    ASSERT v_imposition.paid_amount = 100.00, 'Imposition must be fully paid';
    ASSERT v_imposition.completed = TRUE, 'Imposition must be completed';
    SELECT * INTO STRICT v_allocation FROM allocations WHERE imposition_id = 9999003417002;
    ASSERT v_allocation.allocated_amount = 80.00, 'Imposition allocation must exclude the excess';
    ASSERT v_allocation.allocation_function = 'Automatic Cash Input', 'Allocation function must identify automatic input';
    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417002';
    ASSERT v_suspense.amount = 20.00, 'Only the excess 20.00 must reach suspense';
    ASSERT v_suspense.transaction_type = 'OF', 'Excess must be a fines overpayment';
    ASSERT v_suspense.posted_by = 'PO3417UT', 'Suspense transaction must record the posting user';
    ASSERT v_suspense.posted_by_name = 'Unit Test User', 'Suspense transaction must record the posting name';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'OF', 'Suspense item must be a fines overpayment';
    ASSERT v_item.suspense_account_id = 9999003417000, 'Suspense item must belong to the till business unit';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417002;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417002;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 2 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 2 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 2 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Two payments to the same account use the latest account balance
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_first    defendant_transactions%ROWTYPE;
    v_second   defendant_transactions%ROWTYPE;
    v_account  defendant_accounts%ROWTYPE;
    v_suspense suspense_transactions%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 3: Two payments to the same account use the latest account balance ===';

    CALL p_allocate_till(9999003417003::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_first FROM defendant_transactions WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417003';
    SELECT * INTO STRICT v_second FROM defendant_transactions WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003517003';
    ASSERT v_first.transaction_amount = 60.00, 'First payment must allocate 60.00';
    ASSERT v_second.transaction_amount = 40.00, 'Second payment must allocate only the remaining 40.00';
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417003;
    ASSERT v_account.amount_paid = 120.00, 'Cumulative amount_paid must include both allocations and the previous 20.00';
    ASSERT v_account.account_balance = 0.00, 'Both payments must settle the account without overpaying it';
    ASSERT v_account.version_number = 3, 'Account version must increase for each payment';
    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003517003';
    ASSERT v_suspense.amount = 20.00, 'Second payment excess must reach suspense';
    ASSERT v_suspense.transaction_type = 'OF', 'Second payment excess must be a fines overpayment';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417003;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417003;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003517003;
    ASSERT v_payment.allocated = TRUE, 'Second payment must also be allocated';

    RAISE NOTICE 'TEST 3 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 3 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 3 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: A written-off account sends the payment to suspense
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
    v_account  defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 4: A written-off account sends the payment to suspense ===';

    CALL p_allocate_till(9999003417010::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417010';
    ASSERT v_suspense.amount = 25.00, 'The whole 25.00 payment must reach suspense';
    ASSERT v_suspense.transaction_type = 'UN', 'Rejected payment must be unidentified suspense';
    ASSERT v_suspense.associated_record_id = '9999003417010', 'Suspense transaction must link to the original payment';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'UN', 'Rejected payment must create an unidentified suspense item';
    ASSERT v_item.payment_method = 'CT', 'Suspense item must retain the payment method';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417010';
    ASSERT v_count = 0, 'Rejected payment must not create a defendant transaction';

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417010;
    ASSERT v_account.amount_paid = 20.00, 'Rejected account must not receive a payment';
    ASSERT v_account.account_balance = -100.00, 'Rejected account balance must remain unchanged';
    ASSERT v_account.account_status = 'WO', 'Rejected account status must remain unchanged';
    ASSERT v_account.version_number = 1, 'Rejected account version must remain unchanged';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417010;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417010;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.destination_type = 'F', 'Original payment destination must remain F';

    RAISE NOTICE 'TEST 4 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 4 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 4 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: A transfer-out pending account sends the payment to suspense
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
    v_account  defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 5: A transfer-out pending account sends the payment to suspense ===';

    CALL p_allocate_till(9999003417011::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417011';
    ASSERT v_suspense.amount = 25.00, 'The whole 25.00 payment must reach suspense';
    ASSERT v_suspense.transaction_type = 'UN', 'Rejected payment must be unidentified suspense';
    ASSERT v_suspense.associated_record_id = '9999003417011', 'Suspense transaction must link to the original payment';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'UN', 'Rejected payment must create an unidentified suspense item';
    ASSERT v_item.payment_method = 'CT', 'Suspense item must retain the payment method';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417011';
    ASSERT v_count = 0, 'Rejected payment must not create a defendant transaction';

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417011;
    ASSERT v_account.amount_paid = 20.00, 'Rejected account must not receive a payment';
    ASSERT v_account.account_balance = -100.00, 'Rejected account balance must remain unchanged';
    ASSERT v_account.account_status = 'TO', 'Rejected account status must remain unchanged';
    ASSERT v_account.version_number = 1, 'Rejected account version must remain unchanged';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417011;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417011;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.destination_type = 'F', 'Original payment destination must remain F';

    RAISE NOTICE 'TEST 5 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 5 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 5 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: A transfer-out acknowledged account sends the payment to suspense
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
    v_account  defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 6: A transfer-out acknowledged account sends the payment to suspense ===';

    CALL p_allocate_till(9999003417012::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417012';
    ASSERT v_suspense.amount = 25.00, 'The whole 25.00 payment must reach suspense';
    ASSERT v_suspense.transaction_type = 'UN', 'Rejected payment must be unidentified suspense';
    ASSERT v_suspense.associated_record_id = '9999003417012', 'Suspense transaction must link to the original payment';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'UN', 'Rejected payment must create an unidentified suspense item';
    ASSERT v_item.payment_method = 'CT', 'Suspense item must retain the payment method';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417012';
    ASSERT v_count = 0, 'Rejected payment must not create a defendant transaction';

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417012;
    ASSERT v_account.amount_paid = 20.00, 'Rejected account must not receive a payment';
    ASSERT v_account.account_balance = -100.00, 'Rejected account balance must remain unchanged';
    ASSERT v_account.account_status = 'TA', 'Rejected account status must remain unchanged';
    ASSERT v_account.version_number = 1, 'Rejected account version must remain unchanged';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417012;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417012;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.destination_type = 'F', 'Original payment destination must remain F';

    RAISE NOTICE 'TEST 6 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 6 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 6 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: A transfer to NI or Scotland sends the payment to suspense
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
    v_account  defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 7: A transfer to NI or Scotland sends the payment to suspense ===';

    CALL p_allocate_till(9999003417013::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417013';
    ASSERT v_suspense.amount = 25.00, 'The whole 25.00 payment must reach suspense';
    ASSERT v_suspense.transaction_type = 'UN', 'Rejected payment must be unidentified suspense';
    ASSERT v_suspense.associated_record_id = '9999003417013', 'Suspense transaction must link to the original payment';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'UN', 'Rejected payment must create an unidentified suspense item';
    ASSERT v_item.payment_method = 'CT', 'Suspense item must retain the payment method';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417013';
    ASSERT v_count = 0, 'Rejected payment must not create a defendant transaction';

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417013;
    ASSERT v_account.amount_paid = 20.00, 'Rejected account must not receive a payment';
    ASSERT v_account.account_balance = -100.00, 'Rejected account balance must remain unchanged';
    ASSERT v_account.account_status = 'TS', 'Rejected account status must remain unchanged';
    ASSERT v_account.version_number = 1, 'Rejected account version must remain unchanged';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417013;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417013;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.destination_type = 'F', 'Original payment destination must remain F';

    RAISE NOTICE 'TEST 7 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 7 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 7 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 8: A zero-balance account sends the payment to suspense
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
    v_account  defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 8: A zero-balance account sends the payment to suspense ===';

    CALL p_allocate_till(9999003417014::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417014';
    ASSERT v_suspense.amount = 25.00, 'The whole 25.00 payment must reach suspense';
    ASSERT v_suspense.transaction_type = 'UN', 'Rejected payment must be unidentified suspense';
    ASSERT v_suspense.associated_record_id = '9999003417014', 'Suspense transaction must link to the original payment';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'UN', 'Rejected payment must create an unidentified suspense item';
    ASSERT v_item.payment_method = 'CT', 'Suspense item must retain the payment method';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417014';
    ASSERT v_count = 0, 'Rejected payment must not create a defendant transaction';

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417014;
    ASSERT v_account.amount_paid = 20.00, 'Rejected account must not receive a payment';
    ASSERT v_account.account_balance = 0.00, 'Rejected account balance must remain unchanged';
    ASSERT v_account.account_status = 'L', 'Rejected account status must remain unchanged';
    ASSERT v_account.version_number = 1, 'Rejected account version must remain unchanged';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417014;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417014;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.destination_type = 'F', 'Original payment destination must remain F';

    RAISE NOTICE 'TEST 8 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 8 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 8 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 9: An unknown defendant account sends the payment to suspense
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 9: An unknown defendant account sends the payment to suspense ===';

    CALL p_allocate_till(9999003417015::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417015';
    ASSERT v_suspense.amount = 25.00, 'The whole 25.00 payment must reach suspense';
    ASSERT v_suspense.transaction_type = 'UN', 'Rejected payment must be unidentified suspense';
    ASSERT v_suspense.associated_record_id = '9999003417015', 'Suspense transaction must link to the original payment';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'UN', 'Rejected payment must create an unidentified suspense item';
    ASSERT v_item.payment_method = 'CT', 'Suspense item must retain the payment method';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417015';
    ASSERT v_count = 0, 'Rejected payment must not create a defendant transaction';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417015;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417015;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.destination_type = 'F', 'Original payment destination must remain F';

    RAISE NOTICE 'TEST 9 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 9 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 9 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 10: A manual payment respects inhibited enforcement results
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
    v_account  defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 10: A manual payment respects inhibited enforcement results ===';

    CALL p_allocate_till(9999003417016::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417016';
    ASSERT v_suspense.amount = 25.00, 'The whole 25.00 payment must reach suspense';
    ASSERT v_suspense.transaction_type = 'UN', 'Rejected payment must be unidentified suspense';
    ASSERT v_suspense.associated_record_id = '9999003417016', 'Suspense transaction must link to the original payment';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'UN', 'Rejected payment must create an unidentified suspense item';
    ASSERT v_item.payment_method = 'CT', 'Suspense item must retain the payment method';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417016';
    ASSERT v_count = 0, 'Rejected payment must not create a defendant transaction';

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417016;
    ASSERT v_account.amount_paid = 20.00, 'Rejected account must not receive a payment';
    ASSERT v_account.account_balance = -100.00, 'Rejected account balance must remain unchanged';
    ASSERT v_account.account_status = 'L', 'Rejected account status must remain unchanged';
    ASSERT v_account.version_number = 1, 'Rejected account version must remain unchanged';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417016;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417016;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.destination_type = 'F', 'Original payment destination must remain F';

    RAISE NOTICE 'TEST 10 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 10 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 10 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 11: An automatic payment respects inhibited enforcement results
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
    v_account  defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 11: An automatic payment respects inhibited enforcement results ===';

    CALL p_allocate_till(9999003417017::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417017';
    ASSERT v_suspense.amount = 25.00, 'The whole 25.00 payment must reach suspense';
    ASSERT v_suspense.transaction_type = 'UN', 'Rejected payment must be unidentified suspense';
    ASSERT v_suspense.associated_record_id = '9999003417017', 'Suspense transaction must link to the original payment';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'UN', 'Rejected payment must create an unidentified suspense item';
    ASSERT v_item.payment_method = 'CT', 'Suspense item must retain the payment method';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417017';
    ASSERT v_count = 0, 'Rejected payment must not create a defendant transaction';

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417017;
    ASSERT v_account.amount_paid = 20.00, 'Rejected account must not receive a payment';
    ASSERT v_account.account_balance = -100.00, 'Rejected account balance must remain unchanged';
    ASSERT v_account.account_status = 'L', 'Rejected account status must remain unchanged';
    ASSERT v_account.version_number = 1, 'Rejected account version must remain unchanged';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417017;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417017;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.destination_type = 'F', 'Original payment destination must remain F';

    RAISE NOTICE 'TEST 11 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 11 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 11 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 12: An inhibit override does not override written-off status
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
    v_account  defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 12: An inhibit override does not override written-off status ===';

    CALL p_allocate_till(9999003417019::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417019';
    ASSERT v_suspense.amount = 25.00, 'The whole 25.00 payment must reach suspense';
    ASSERT v_suspense.transaction_type = 'UN', 'Rejected payment must be unidentified suspense';
    ASSERT v_suspense.associated_record_id = '9999003417019', 'Suspense transaction must link to the original payment';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'UN', 'Rejected payment must create an unidentified suspense item';
    ASSERT v_item.payment_method = 'CT', 'Suspense item must retain the payment method';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417019';
    ASSERT v_count = 0, 'Rejected payment must not create a defendant transaction';

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417019;
    ASSERT v_account.amount_paid = 20.00, 'Rejected account must not receive a payment';
    ASSERT v_account.account_balance = -100.00, 'Rejected account balance must remain unchanged';
    ASSERT v_account.account_status = 'WO', 'Rejected account status must remain unchanged';
    ASSERT v_account.version_number = 1, 'Rejected account version must remain unchanged';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417019;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417019;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';
    ASSERT v_payment.destination_type = 'F', 'Original payment destination must remain F';

    RAISE NOTICE 'TEST 12 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 12 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 12 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 13: An automatic payment can override an enforcement inhibit
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account     defendant_accounts%ROWTYPE;
    v_transaction defendant_transactions%ROWTYPE;
    v_payment     payments_in%ROWTYPE;
    v_till        tills%ROWTYPE;
    v_count       BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 13: An automatic payment can override an enforcement inhibit ===';

    CALL p_allocate_till(9999003417018::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417018;
    ASSERT v_account.amount_paid = 45.00, 'Override must allow the 25.00 payment';
    ASSERT v_account.account_balance = -75.00, 'Override must reduce the balance by 25.00';
    ASSERT v_account.last_enforcement = 'U417IN', 'Override must not change the enforcement result';
    SELECT * INTO STRICT v_transaction FROM defendant_transactions WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417018';
    ASSERT v_transaction.transaction_amount = 25.00, 'Override must create the defendant payment';
    SELECT COUNT(*) INTO v_count FROM suspense_transactions WHERE associated_record_id = '9999003417018';
    ASSERT v_count = 0, 'Accepted payment must not reach suspense';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417018;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417018;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 13 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 13 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 13 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 14: A consolidated account allocates to its top-level master
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction defendant_transactions%ROWTYPE;
    v_master      defendant_accounts%ROWTYPE;
    v_child       defendant_accounts%ROWTYPE;
    v_payment     payments_in%ROWTYPE;
    v_till        tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 14: A consolidated account allocates to its top-level master ===';

    CALL p_allocate_till(9999003417020::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_transaction FROM defendant_transactions WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417020';
    ASSERT v_transaction.defendant_account_id = 9999003417022, 'Payment must reach the top-level master';
    ASSERT v_transaction.transaction_amount = 25.00, 'Master must receive the payment amount';
    SELECT * INTO STRICT v_master FROM defendant_accounts WHERE defendant_account_id = 9999003417022;
    ASSERT v_master.amount_paid = 45.00, 'Master amount_paid must increase by 25.00';
    ASSERT v_master.account_balance = -75.00, 'Master balance must reduce by 25.00';
    SELECT * INTO STRICT v_child FROM defendant_accounts WHERE defendant_account_id = 9999003417020;
    ASSERT v_child.amount_paid = 20.00, 'Consolidated child amount_paid must remain unchanged';
    ASSERT v_child.account_balance = -100.00, 'Consolidated child balance must remain unchanged';
    SELECT * INTO STRICT v_child FROM defendant_accounts WHERE defendant_account_id = 9999003417021;
    ASSERT v_child.amount_paid = 20.00, 'Intermediate master amount_paid must remain unchanged';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417020;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417020;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 14 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 14 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 14 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 15: An exact court fee creates one received record
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_received court_fees_received%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 15: An exact court fee creates one received record ===';

    CALL p_allocate_till(9999003417030::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417030' AND transaction_type = 'CF';
    ASSERT v_suspense.amount = 40.00, 'Court fee component must equal the amount due or the smaller receipt';
    ASSERT v_suspense.transaction_type = 'CF', 'Court fee transaction type must be CF';
    SELECT * INTO STRICT v_received FROM court_fees_received WHERE suspense_transaction_id = v_suspense.suspense_transaction_id;
    ASSERT v_received.business_unit_id = 9417, 'Received record must belong to the till business unit';
    ASSERT v_received.court_fee_id = 9999003417000, 'Received record must link to the selected fee';
    ASSERT v_received.overpayment = FALSE, 'Fee component must not be marked overpayment';
    ASSERT v_received.number_of_items = 1, 'Received record must store the expected item count';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'CF', 'Court fee must create a CF suspense item';
    ASSERT v_item.court_fee_id = 9999003417000, 'Court fee suspense item must link to the selected fee';
    SELECT COUNT(*) INTO v_count FROM suspense_transactions WHERE associated_record_id = '9999003417030';
    ASSERT v_count = 1, 'Receipt must create the expected number of suspense transactions';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417030;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417030;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 15 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 15 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 15 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 16: A court fee payment covers multiple items and its excess
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_received court_fees_received%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
    v_excess   suspense_transactions%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 16: A court fee payment covers multiple items and its excess ===';

    CALL p_allocate_till(9999003417032::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417032' AND transaction_type = 'CF';
    ASSERT v_suspense.amount = 80.00, 'Court fee component must equal the amount due or the smaller receipt';
    ASSERT v_suspense.transaction_type = 'CF', 'Court fee transaction type must be CF';
    SELECT * INTO STRICT v_received FROM court_fees_received WHERE suspense_transaction_id = v_suspense.suspense_transaction_id;
    ASSERT v_received.business_unit_id = 9417, 'Received record must belong to the till business unit';
    ASSERT v_received.court_fee_id = 9999003417000, 'Received record must link to the selected fee';
    ASSERT v_received.overpayment = FALSE, 'Fee component must not be marked overpayment';
    ASSERT v_received.number_of_items = 2, 'Received record must store the expected item count';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'CF', 'Court fee must create a CF suspense item';
    ASSERT v_item.court_fee_id = 9999003417000, 'Court fee suspense item must link to the selected fee';
    SELECT COUNT(*) INTO v_count FROM suspense_transactions WHERE associated_record_id = '9999003417032';
    ASSERT v_count = 2, 'Receipt must create the expected number of suspense transactions';

    SELECT * INTO STRICT v_excess FROM suspense_transactions WHERE associated_record_id = '9999003417032' AND transaction_type = 'OC';
    ASSERT v_excess.amount = 10.00, '90.00 receipt less two 40.00 fees must leave 10.00 excess';
    ASSERT v_excess.suspense_item_id = v_suspense.suspense_item_id, 'Fee and excess must share one suspense item';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417032;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417032;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 16 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 16 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 16 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 17: A partial court fee creates one non-overpayment record
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_received court_fees_received%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 17: A partial court fee creates one non-overpayment record ===';

    CALL p_allocate_till(9999003417033::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417033' AND transaction_type = 'CF';
    ASSERT v_suspense.amount = 20.00, 'Court fee component must equal the amount due or the smaller receipt';
    ASSERT v_suspense.transaction_type = 'CF', 'Court fee transaction type must be CF';
    SELECT * INTO STRICT v_received FROM court_fees_received WHERE suspense_transaction_id = v_suspense.suspense_transaction_id;
    ASSERT v_received.business_unit_id = 9417, 'Received record must belong to the till business unit';
    ASSERT v_received.court_fee_id = 9999003417000, 'Received record must link to the selected fee';
    ASSERT v_received.overpayment = FALSE, 'Fee component must not be marked overpayment';
    ASSERT v_received.number_of_items = 1, 'Received record must store the expected item count';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'CF', 'Court fee must create a CF suspense item';
    ASSERT v_item.court_fee_id = 9999003417000, 'Court fee suspense item must link to the selected fee';
    SELECT COUNT(*) INTO v_count FROM suspense_transactions WHERE associated_record_id = '9999003417033';
    ASSERT v_count = 1, 'Receipt must create the expected number of suspense transactions';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417033;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417033;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 17 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 17 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 17 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 18: A court fee code resolves the fee and defaults to one item
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_received court_fees_received%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 18: A court fee code resolves the fee and defaults to one item ===';

    CALL p_allocate_till(9999003417034::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417034' AND transaction_type = 'CF';
    ASSERT v_suspense.amount = 40.00, 'Court fee component must equal the amount due or the smaller receipt';
    ASSERT v_suspense.transaction_type = 'CF', 'Court fee transaction type must be CF';
    SELECT * INTO STRICT v_received FROM court_fees_received WHERE suspense_transaction_id = v_suspense.suspense_transaction_id;
    ASSERT v_received.business_unit_id = 9417, 'Received record must belong to the till business unit';
    ASSERT v_received.court_fee_id = 9999003417000, 'Received record must link to the selected fee';
    ASSERT v_received.overpayment = FALSE, 'Fee component must not be marked overpayment';
    ASSERT v_received.number_of_items = 1, 'Received record must store the expected item count';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'CF', 'Court fee must create a CF suspense item';
    ASSERT v_item.court_fee_id = 9999003417000, 'Court fee suspense item must link to the selected fee';
    SELECT COUNT(*) INTO v_count FROM suspense_transactions WHERE associated_record_id = '9999003417034';
    ASSERT v_count = 1, 'Receipt must create the expected number of suspense transactions';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417034;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417034;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 18 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 18 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 18 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 19: A court fee overpayment creates linked fee and excess records
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_fee          suspense_transactions%ROWTYPE;
    v_excess       suspense_transactions%ROWTYPE;
    v_fee_received court_fees_received%ROWTYPE;
    v_overpayment  court_fees_received%ROWTYPE;
    v_item         suspense_items%ROWTYPE;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 19: A court fee overpayment creates linked fee and excess records ===';

    CALL p_allocate_till(9999003417031::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_fee FROM suspense_transactions WHERE associated_record_id = '9999003417031' AND transaction_type = 'CF';
    SELECT * INTO STRICT v_excess FROM suspense_transactions WHERE associated_record_id = '9999003417031' AND transaction_type = 'OC';
    ASSERT v_fee.amount = 40.00, 'Fee component must be exactly 40.00';
    ASSERT v_excess.amount = 25.00, 'Excess component must be exactly 25.00';
    ASSERT v_excess.suspense_item_id = v_fee.suspense_item_id, 'Fee and excess must share one suspense item';
    SELECT * INTO STRICT v_fee_received FROM court_fees_received WHERE suspense_transaction_id = v_fee.suspense_transaction_id;
    ASSERT v_fee_received.court_fee_id = 9999003417000, 'Fee received must link to the selected fee';
    ASSERT v_fee_received.overpayment = FALSE, 'Fee received must not be marked overpayment';
    ASSERT v_fee_received.number_of_items = 1, 'Fee received must cover one item';
    SELECT * INTO STRICT v_overpayment FROM court_fees_received WHERE suspense_transaction_id = v_excess.suspense_transaction_id;
    ASSERT v_overpayment.court_fee_received_id <> v_fee_received.court_fee_received_id, 'Excess must have its own received record';
    ASSERT v_overpayment.business_unit_id = 9417, 'Excess received must belong to the till business unit';
    ASSERT v_overpayment.court_fee_id IS NULL, 'Excess received must not link to a fee';
    ASSERT v_overpayment.overpayment = TRUE, 'Excess received must be marked overpayment';
    ASSERT v_overpayment.number_of_items = 0, 'Excess received must have zero items';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_fee.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'CF', 'Shared item must retain its CF classification';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417031;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417031;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 19 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 19 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 19 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 20: A manual suspense payment retains its reason and creates a note
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_suspense suspense_transactions%ROWTYPE;
    v_item     suspense_items%ROWTYPE;
    v_note     notes%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 20: A manual suspense payment retains its reason and creates a note ===';

    CALL p_allocate_till(9999003417040::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417040';
    ASSERT v_suspense.amount = 25.00, 'Entire suspense payment must be recorded';
    ASSERT v_suspense.transaction_type = 'FA', 'Transaction type must reflect fine paid in advance';
    ASSERT v_suspense.text = 'REF-S', 'Transaction text must retain the payment reference';
    SELECT * INTO STRICT v_item FROM suspense_items WHERE suspense_item_id = v_suspense.suspense_item_id;
    ASSERT v_item.suspense_item_type = 'FA', 'Item type must come from the manual suspense reason';
    ASSERT v_item.payment_method = 'CT', 'Item must retain the payment method';
    SELECT * INTO STRICT v_note FROM notes WHERE associated_record_type = 'suspense_items' AND associated_record_id = v_item.suspense_item_id::TEXT;
    ASSERT v_note.note_type = 'AA', 'Suspense note must use note type AA';
    ASSERT v_note.note_text = 'Test suspense note', 'Note text must come from the payment JSON';
    ASSERT v_note.posted_by = 'PO3417UT', 'Note must record the posting user';
    ASSERT v_note.posted_by_name = 'Unit Test User', 'Note must record the posting name';
    ASSERT v_note.posted_date = CURRENT_TIMESTAMP::TIMESTAMP, 'Note must use the transaction timestamp';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417040;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417040;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 20 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 20 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 20 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 21: Money beyond the eligible imposition balance goes to suspense
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction defendant_transactions%ROWTYPE;
    v_account     defendant_accounts%ROWTYPE;
    v_imposition  impositions%ROWTYPE;
    v_suspense    suspense_transactions%ROWTYPE;
    v_payment     payments_in%ROWTYPE;
    v_till        tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 21: Money beyond the eligible imposition balance goes to suspense ===';

    CALL p_allocate_till(9999003417041::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_transaction FROM defendant_transactions WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417041';
    ASSERT v_transaction.transaction_amount = 30.00, 'Only the 30.00 imposition balance must reach the defendant';
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417041;
    ASSERT v_account.amount_paid = 50.00, 'Account amount_paid must increase only by the imposition allocation';
    ASSERT v_account.account_balance = -70.00, 'Account balance must reduce only by the imposition allocation';
    SELECT * INTO STRICT v_imposition FROM impositions WHERE imposition_id = 9999003417041;
    ASSERT v_imposition.paid_amount = 50.00, 'Eligible imposition must be fully paid';
    ASSERT v_imposition.completed = TRUE, 'Eligible imposition must be completed';
    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417041';
    ASSERT v_suspense.amount = 45.00, 'The remaining 45.00 must reach suspense';
    ASSERT v_suspense.transaction_type = 'OF', 'The remaining payment must be a fines overpayment';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417041;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417041;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 21 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 21 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 21 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 22: An account without eligible impositions sends the whole payment to suspense
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_account  defendant_accounts%ROWTYPE;
    v_suspense suspense_transactions%ROWTYPE;
    v_payment  payments_in%ROWTYPE;
    v_till     tills%ROWTYPE;
    v_count    BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 22: An account without eligible impositions sends the whole payment to suspense ===';

    CALL p_allocate_till(9999003417042::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417042;
    ASSERT v_account.amount_paid = 20.00, 'Account amount_paid must remain unchanged';
    ASSERT v_account.account_balance = -100.00, 'Account balance must remain unchanged';
    ASSERT v_account.version_number = 1, 'Account version must remain unchanged';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417042';
    ASSERT v_count = 0, 'No defendant transaction must be inserted without an eligible imposition';
    SELECT * INTO STRICT v_suspense FROM suspense_transactions WHERE associated_record_id = '9999003417042';
    ASSERT v_suspense.amount = 25.00, 'The whole payment must reach suspense';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417042;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417042;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 22 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 22 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 22 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 23: A later payment failure rolls back every earlier change in the till
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_account      defendant_accounts%ROWTYPE;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
    v_fee_before   BIGINT;
    v_note_before  BIGINT;
    v_item_before  BIGINT;
    v_count        BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 23: A later payment failure rolls back every earlier change in the till ===';

    SELECT COUNT(*) INTO v_fee_before FROM court_fees_received WHERE business_unit_id = 9417;
    SELECT COUNT(*) INTO v_note_before FROM notes;
    SELECT COUNT(*) INTO v_item_before FROM suspense_items WHERE suspense_account_id = 9999003417000;

    -- Payment 50 creates a fee, excess and note; payment 51 then fails its reference validation.
    BEGIN
        CALL p_allocate_till(9999003417050::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P3811' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P3811 was not raised';
    ASSERT v_detail = 'p_insert_creditor_transactions: pi_payment_reference is required', 'Validation DETAIL must be preserved';



    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417050;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 65.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417050;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417051;
    ASSERT v_payment.allocated = FALSE, 'Later failed payment must also remain unallocated';
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417051;
    ASSERT v_account.amount_paid = 20.00, 'Rejected allocation must not change amount_paid';
    ASSERT v_account.account_balance = -100.00, 'Rejected allocation must not change the account balance';
    ASSERT v_account.version_number = 1, 'Rejected allocation must not change the account version';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected allocation must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected allocation must not complete the account';

    SELECT COUNT(*) INTO v_count FROM suspense_transactions WHERE associated_record_id IN ('9999003417050', '9999003417051');
    ASSERT v_count = 0, 'Earlier fee and excess suspense transactions must be rolled back';
    SELECT COUNT(*) INTO v_count FROM defendant_transactions
     WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417051';
    ASSERT v_count = 0, 'Failed defendant transaction must be rolled back';
    SELECT COUNT(*) INTO v_count FROM court_fees_received WHERE business_unit_id = 9417;
    ASSERT v_count = v_fee_before, 'Fee and excess received records must be rolled back';
    SELECT COUNT(*) INTO v_count FROM notes;
    ASSERT v_count = v_note_before, 'Earlier suspense note must be rolled back';
    SELECT COUNT(*) INTO v_count FROM suspense_items WHERE suspense_account_id = 9999003417000;
    ASSERT v_count = v_item_before, 'Earlier suspense item must be rolled back';

    RAISE NOTICE 'TEST 23 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 23 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 23 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 24: A missing court fee is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 24: A missing court fee is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417060::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P0002' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P0002 was not raised';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417060;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417060;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 24 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 24 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 24 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 25: A court fee from another business unit is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 25: A court fee from another business unit is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417061::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P0002' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P0002 was not raised';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417061;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417061;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 25 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 25 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 25 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 26: A zero court fee item count is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 26: A zero court fee item count is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417062::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4110' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4110 was not raised';
    ASSERT v_detail = 'p_allocate_till: number_of_items must be greater than zero', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417062;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417062;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 26 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 26 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 26 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 27: A defendant account from another business unit is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
    v_account      defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 27: A defendant account from another business unit is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417063::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4112' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4112 was not raised';
    ASSERT v_detail = 'p_allocate_till: the master account must belong to the till business unit', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417063;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417063;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417063;
    ASSERT v_account.amount_paid = 20.00, 'Rejected allocation must not change amount_paid';
    ASSERT v_account.account_balance = -100.00, 'Rejected allocation must not change the account balance';
    ASSERT v_account.version_number = 1, 'Rejected allocation must not change the account version';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected allocation must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected allocation must not complete the account';

    RAISE NOTICE 'TEST 27 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 27 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 27 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 28: A positive defendant account balance is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
    v_account      defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 28: A positive defendant account balance is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417064::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4113' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4113 was not raised';
    ASSERT v_detail = 'p_allocate_till: outstanding account balances must be negative', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417064;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417064;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417064;
    ASSERT v_account.amount_paid = 20.00, 'Rejected allocation must not change amount_paid';
    ASSERT v_account.account_balance = 10.00, 'Rejected allocation must not change the account balance';
    ASSERT v_account.version_number = 1, 'Rejected allocation must not change the account version';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected allocation must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected allocation must not complete the account';

    RAISE NOTICE 'TEST 28 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 28 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 28 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 29: A payment with a different mode from its till is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
    v_account      defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 29: A payment with a different mode from its till is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417065::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4109' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4109 was not raised';
    ASSERT v_detail = 'p_allocate_till: payment and till auto_payment flags must agree', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417065;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417065;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417065;
    ASSERT v_account.amount_paid = 20.00, 'Rejected allocation must not change amount_paid';
    ASSERT v_account.account_balance = -100.00, 'Rejected allocation must not change the account balance';
    ASSERT v_account.version_number = 1, 'Rejected allocation must not change the account version';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected allocation must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected allocation must not complete the account';

    RAISE NOTICE 'TEST 29 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 29 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 29 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 30: A zero payment is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
    v_account      defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 30: A zero payment is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417066::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4108' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4108 was not raised';
    ASSERT v_detail = 'p_allocate_till: payment_amount must be greater than zero', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417066;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 0.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417066;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417066;
    ASSERT v_account.amount_paid = 20.00, 'Rejected allocation must not change amount_paid';
    ASSERT v_account.account_balance = -100.00, 'Rejected allocation must not change the account balance';
    ASSERT v_account.version_number = 1, 'Rejected allocation must not change the account version';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected allocation must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected allocation must not complete the account';

    RAISE NOTICE 'TEST 30 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 30 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 30 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 31: A negative payment is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
    v_account      defendant_accounts%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 31: A negative payment is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417067::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4108' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4108 was not raised';
    ASSERT v_detail = 'p_allocate_till: payment_amount must be greater than zero', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417067;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = -1.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417067;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';
    SELECT * INTO STRICT v_account FROM defendant_accounts WHERE defendant_account_id = 9999003417067;
    ASSERT v_account.amount_paid = 20.00, 'Rejected allocation must not change amount_paid';
    ASSERT v_account.account_balance = -100.00, 'Rejected allocation must not change the account balance';
    ASSERT v_account.version_number = 1, 'Rejected allocation must not change the account version';
    ASSERT v_account.last_movement_date = TIMESTAMP '2020-01-01', 'Rejected allocation must not change last_movement_date';
    ASSERT v_account.completed_date IS NULL, 'Rejected allocation must not complete the account';

    RAISE NOTICE 'TEST 31 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 31 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 31 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 32: A null till ID is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 32: A null till ID is rejected ===';

    BEGIN
        CALL p_allocate_till(NULL::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4101' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4101 was not raised';
    ASSERT v_detail = 'p_allocate_till: pi_till_id is required', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417083;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417083;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 32 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 32 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 32 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 33: A null business unit is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 33: A null business unit is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417083::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, NULL::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4102' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4102 was not raised';
    ASSERT v_detail = 'p_allocate_till: pi_business_unit_id is required', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417083;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417083;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 33 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 33 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 33 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 34: A blank posting user is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 34: A blank posting user is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417083::BIGINT, ' '::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4103' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4103 was not raised';
    ASSERT v_detail = 'p_allocate_till: pi_posted_by is required', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417083;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417083;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 34 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 34 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 34 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 35: A null posting name is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 35: A null posting name is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417083::BIGINT, 'PO3417UT'::VARCHAR, NULL::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4104' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4104 was not raised';
    ASSERT v_detail = 'p_allocate_till: pi_posted_by_name is required', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417083;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417083;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 35 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 35 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 35 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 36: An unknown till is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 36: An unknown till is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417999::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4105' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4105 was not raised';
    ASSERT v_detail = 'p_allocate_till: no till exists for pi_till_id', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417083;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417083;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 36 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 36 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 36 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 37: A till business unit mismatch is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 37: A till business unit mismatch is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417083::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9418::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4106' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4106 was not raised';
    ASSERT v_detail = 'p_allocate_till: till business unit 9417; supplied business unit 9418', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417083;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417083;
    ASSERT v_till.status = 'Created', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 37 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 37 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 37 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 38: An allocated till containing a pending payment is rejected
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught BOOLEAN := FALSE;
    v_detail       TEXT;
    v_payment      payments_in%ROWTYPE;
    v_till         tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 38: An allocated till containing a pending payment is rejected ===';

    BEGIN
        CALL p_allocate_till(9999003417072::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);
    EXCEPTION
        WHEN SQLSTATE 'P4107' THEN
            RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            GET STACKED DIAGNOSTICS v_detail = PG_EXCEPTION_DETAIL;
            v_error_caught := TRUE;
    END;
    ASSERT v_error_caught = TRUE, 'Expected SQLSTATE P4107 was not raised';
    ASSERT v_detail = 'p_allocate_till: inconsistent allocated till state', 'Validation DETAIL must be preserved';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417072;
    ASSERT v_payment.allocated = FALSE, 'Rejected payment must remain unallocated';
    ASSERT v_payment.payment_amount = 25.00, 'Rejected payment amount must remain unchanged';
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417072;
    ASSERT v_till.status = 'Allocated', 'Rejected allocation must preserve the till status';

    RAISE NOTICE 'TEST 38 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 38 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 38 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 39: A legacy automatic JSON string supplies the payment reference
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction defendant_transactions%ROWTYPE;
    v_allocation  allocations%ROWTYPE;
    v_payment     payments_in%ROWTYPE;
    v_till        tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 39: A legacy automatic JSON string supplies the payment reference ===';

    CALL p_allocate_till(9999003417080::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_transaction FROM defendant_transactions WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417080';
    ASSERT v_transaction.transaction_amount = 25.00, 'Payment amount must be allocated';
    ASSERT v_transaction.payment_reference = 'LEGACY-REF', 'Reference must use the expected stored payment value';
    ASSERT v_transaction.source = 'NATWEST', 'Source must use the expected automatic or manual value';
    SELECT * INTO STRICT v_allocation FROM allocations WHERE imposition_id = 9999003417080;
    ASSERT v_allocation.allocation_function = 'Automatic Cash Input', 'Allocation function must match the effective payment mode';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417080;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417080;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 39 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 39 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 39 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 40: Root JSON supplies a manual payment reference and source
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_transaction defendant_transactions%ROWTYPE;
    v_allocation  allocations%ROWTYPE;
    v_payment     payments_in%ROWTYPE;
    v_till        tills%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 40: Root JSON supplies a manual payment reference and source ===';

    CALL p_allocate_till(9999003417081::BIGINT, 'PO3417UT'::VARCHAR, 'Unit Test User'::VARCHAR, 9417::SMALLINT);

    SELECT * INTO STRICT v_transaction FROM defendant_transactions WHERE associated_record_type = 'payments_in' AND associated_record_id = '9999003417081';
    ASSERT v_transaction.transaction_amount = 25.00, 'Payment amount must be allocated';
    ASSERT v_transaction.payment_reference = 'ROOT-REF', 'Reference must use the expected stored payment value';
    ASSERT v_transaction.source = 'OTHER', 'Source must use the expected automatic or manual value';
    SELECT * INTO STRICT v_allocation FROM allocations WHERE imposition_id = 9999003417081;
    ASSERT v_allocation.allocation_function = 'Manual Cash Input', 'Allocation function must match the effective payment mode';

    SELECT * INTO STRICT v_payment FROM payments_in WHERE payment_in_id = 9999003417081;
    SELECT * INTO STRICT v_till FROM tills WHERE till_id = 9999003417081;
    ASSERT v_payment.allocated = TRUE, 'Payment must be allocated';
    ASSERT v_till.status = 'Allocated', 'Till must be allocated';

    RAISE NOTICE 'TEST 40 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 40 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 40 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 41: Allocated receipts equal the defendant and suspense amounts
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_received    NUMERIC;
    v_defendant   NUMERIC;
    v_suspense    NUMERIC;
    v_allocations NUMERIC;
BEGIN
    RAISE NOTICE '=== TEST 41: Allocated receipts equal the defendant and suspense amounts ===';

    SELECT SUM(payment_amount) INTO v_received FROM payments_in
     WHERE till_id BETWEEN 9999003417001 AND 9999003417083
       AND allocated = TRUE;

    SELECT SUM(transaction_amount) INTO v_defendant FROM defendant_transactions
     WHERE defendant_account_id BETWEEN 9999003417001 AND 9999003417083
       AND associated_record_type = 'payments_in';

    SELECT SUM(st.amount) INTO v_suspense
      FROM suspense_transactions st JOIN suspense_items si USING (suspense_item_id)
     WHERE si.suspense_account_id = 9999003417000;

    SELECT SUM(a.allocated_amount) INTO v_allocations
      FROM allocations a JOIN defendant_transactions dt USING (defendant_transaction_id)
     WHERE dt.defendant_account_id BETWEEN 9999003417001 AND 9999003417083
       AND dt.associated_record_type = 'payments_in';

    ASSERT v_received = v_defendant + v_suspense, 'Every allocated receipt must be fully accounted for';
    ASSERT v_allocations = v_defendant, 'Defendant payments must equal the total imposition allocations';

    RAISE NOTICE 'TEST 41 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 41 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 41 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Cleanup data after tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data after tests ===';

    DELETE FROM notes
     WHERE associated_record_type = 'suspense_items'
       AND associated_record_id IN (
           SELECT suspense_item_id::TEXT FROM suspense_items WHERE suspense_account_id = 9999003417000
       );
    DELETE FROM court_fees_received WHERE business_unit_id IN (9417, 9418);
    DELETE FROM suspense_transactions
     WHERE suspense_item_id IN (
           SELECT suspense_item_id FROM suspense_items WHERE suspense_account_id = 9999003417000
       );
    DELETE FROM allocations
     WHERE imposition_id IN (
           SELECT imposition_id FROM impositions
            WHERE defendant_account_id BETWEEN 9999003417001 AND 9999003417083
       );
    DELETE FROM creditor_transactions WHERE creditor_account_id = 9999003417000;
    DELETE FROM defendant_transactions WHERE defendant_account_id BETWEEN 9999003417001 AND 9999003417083;
    DELETE FROM payments_in
     WHERE till_id IN (
           SELECT till_id FROM tills WHERE business_unit_id = 9417
       );
    DELETE FROM impositions WHERE defendant_account_id BETWEEN 9999003417001 AND 9999003417083;
    DELETE FROM defendant_accounts WHERE defendant_account_id BETWEEN 9999003417001 AND 9999003417083;
    DELETE FROM suspense_items WHERE suspense_account_id = 9999003417000;
    DELETE FROM suspense_item_number_index WHERE business_unit_id = 9417;
    DELETE FROM tills WHERE business_unit_id = 9417;
    DELETE FROM interface_files WHERE interface_file_id IN (9999003417000, 9999003417001);
    DELETE FROM interface_jobs WHERE interface_job_id IN (9999003417000, 9999003417001);
    DELETE FROM court_fees WHERE court_fee_id IN (9999003417000, 9999003417001);
    DELETE FROM suspense_accounts WHERE suspense_account_id = 9999003417000;
    DELETE FROM creditor_accounts WHERE creditor_account_id = 9999003417000;
    DELETE FROM configuration_items WHERE configuration_item_id = 9999003417000;
    DELETE FROM results WHERE result_id IN ('U41701', 'U41700', 'U417IN');
    DELETE FROM business_units WHERE business_unit_id IN (9417, 9418);

    RAISE NOTICE 'Cleanup completed';
END $$;

DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE 'All p_allocate_till unit tests passed';
END $$;
