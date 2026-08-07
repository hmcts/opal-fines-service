/**
* CGI OPAL Program
*
* MODULE      : create_view_v_major_creditor_account_header_unit_tests_v4.sql
*
* DESCRIPTION : Unit tests for v_major_creditor_account_header view
*               Verifies creditor account header fields and the PO-5774 Awaiting Payout calculation.
*
*               PO-5774 changes awaiting_payout from:
*                   sum unprocessed PAYMNT transactions
*               to:
*                   sum all creditor_transactions for the creditor_account_id
*                   where posted_date is after the most recent BACS or CHEQUE transaction.
*
*               This test script assumes the updated v_major_creditor_account_header DDL has already
*               been applied before the script is run.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 19/11/2025    C Larkin    1.0         PO-2122 - Updated test description, data and assertions to cover additional scenarios
* 06/03/2026    TMc         2.0         Updates for new ENUM data types:
*                                           PO-2856 - business_units.business_unit_type
*                                           PO-2908 - creditor_accounts.creditor_account_type
*                                           PO-2916 - creditor_transactions.transaction_type
* 01/06/2026    P Brumby    3.0         PO-5774 - Updated expected awaiting_payout values and added cutoff scenarios
*                                                for latest BACS/CHEQUE posted_date logic.
* 13/07/2026    P Brumby    4.0         PO-7422 - Add business_unit_code processing and assertions for
*                                                v_major_creditor_account_header.
*
**/

-- Start timing measurements for test execution
\timing

DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

    -- PO-5774: Expanded cleanup to include the additional v3 scenarios.
    DELETE FROM creditor_transactions
     WHERE creditor_transaction_id IN (
        99911,99912,99913,99914,99915,99916,99917,99918,99919,
        99920,99921,99922,99923,99924,99925,99926,99927,99928,99929,
        99930,99931,99932,99933,99934,99935,99936
     )
        OR creditor_account_id IN (9996,9997,9998,9999,10000,10001,10002,10003);

    DELETE FROM creditor_accounts WHERE creditor_account_id IN (9996,9997,9998,9999,10000,10001,10002,10003);
    DELETE FROM major_creditors WHERE major_creditor_id IN (9991,9992,9993,9994);
    DELETE FROM configuration_items WHERE configuration_item_id IN (9991,9992,9993,9994);
    -- Ensure test business_units in the 999 range are removed so inserts can proceed without ON CONFLICT
    DELETE FROM business_units WHERE business_unit_id IN (999,1000,1001,1002,1003,1004,1005);

    RAISE NOTICE '=== Test cleanup completed ===';

END $$;

DO LANGUAGE 'plpgsql' $$
DECLARE
    -- only the variables used in these compact tests
    v_creditor_account_id         BIGINT;
    v_creditor_account_number     VARCHAR(50);
    v_creditor_account_type       t_creditor_account_type_enum;
    v_creditor_version            INTEGER;
    v_business_unit_id            SMALLINT;
    v_business_unit_code          VARCHAR(4);
    v_business_unit_name          VARCHAR(200);
    v_name                        VARCHAR(200);
    v_awaiting_payout             NUMERIC(18,2);
    v_expected_name               VARCHAR(200);
    v_expected_awaiting_payout    NUMERIC(18,2);
    v_expected_business_unit_code VARCHAR(4);
    rec                           RECORD;
    i                             INTEGER := 0;
BEGIN

    RAISE NOTICE '=== Setting up test data for v_major_creditor_account_header tests ===';

    -- PO-5774: Insert additional Business Units for the new cutoff scenarios.
    INSERT INTO business_units
    (
      business_unit_id
    , business_unit_name
    , business_unit_code
    , business_unit_type
    , welsh_language
    )
    VALUES
    (999,  'Test Audit Business Unit', 'TBU1', 'Accounting Division', FALSE),
    (1000, 'Test Audit Business Unit', 'TBU2', 'Accounting Division', FALSE),
    (1001, 'Test Audit Business Unit', 'TBU3', 'Accounting Division', FALSE),
    (1002, 'Test Audit Business Unit', 'TBU4', 'Accounting Division', FALSE),
    (1003, 'Test Audit Business Unit', 'TBU5', 'Accounting Division', FALSE),
    (1004, 'Test Audit Business Unit', 'TBU6', 'Accounting Division', FALSE),
    (1005, 'Test Audit Business Unit', 'TBU7', 'Accounting Division', FALSE);

    -- Insert into major_creditors
    INSERT INTO major_creditors
    (
        major_creditor_id,
        business_unit_id,
        name
    )
    VALUES
    -- Scenario 1 - MJ creditor_account with no BACS/CHEQUE cutoff
    (
        9991,
        999,
        'Major Creditor A'
    ),
    -- Scenario 3 - MJ creditor_account used to prove non-PAYMNT rows are now included
    (
        9992,
        1000,
        'Major Creditor B'
    ),
    -- Scenario 5 - PO-5774 new MJ scenario with a BACS cutoff
    (
        9993,
        1002,
        'Major Creditor C'
    ),
    -- Scenario 7 - PO-5774 new MJ scenario with both BACS and CHEQUE cutoffs
    (
        9994,
        1004,
        'Major Creditor D'
    );

    -- Insert into configuration_items (required for CF account name lookup)
    INSERT INTO configuration_items
    (
        configuration_item_id,
        business_unit_id,
        item_name,
        item_values
    )
    VALUES
    (
        9991,
        999,
        'CENTRAL_FUND_ACCOUNT',
        '{"name": "Central Fund A"}'::JSONB
    ),
    (
        9992,
        1001,
        'CENTRAL_FUND_ACCOUNT',
        '{"name": "Central Fund B"}'::JSONB
    ),
    -- Scenario 6 - PO-5774 new CF scenario with a CHEQUE cutoff
    (
        9993,
        1003,
        'CENTRAL_FUND_ACCOUNT',
        '{"name": "Central Fund C"}'::JSONB
    ),
    -- Scenario 8 - PO-5774 new CF scenario with no transactions after latest BACS/CHEQUE
    (
        9994,
        1005,
        'CENTRAL_FUND_ACCOUNT',
        '{"name": "Central Fund D"}'::JSONB
    );

    -- Insert creditor_accounts (must exist before inserting transactions due to FK)
    INSERT INTO creditor_accounts
    (
        creditor_account_id,
        business_unit_id,
        account_number,
        creditor_account_type,
        major_creditor_id,
        prosecution_service,
        minor_creditor_party_id,
        from_suspense,
        hold_payout,
        pay_by_bacs,
        bank_sort_code,
        bank_account_number,
        bank_account_name,
        bank_account_reference,
        bank_account_type
    )
    VALUES
    -- Scenario 1: MJ creditor_account with no BACS/CHEQUE cutoff; all transactions are summed
    (9996, 999, 'CA006', 'MJ', 9991, FALSE, NULL, FALSE, FALSE, TRUE, '777888', '77889900', 'MajorCredAcct', 'REF006', '1'),
    -- Scenario 2: CF creditor_account with no BACS/CHEQUE cutoff; all transactions are summed
    (9997, 999, 'CA007', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '888999', '88990011', 'CentralFundAcct', 'REF007', '3'),
    -- Scenario 3: MJ creditor_account with non-PAYMNT transaction included by PO-5774
    (9998, 1000, 'CA998', 'MJ', 9992, FALSE, NULL, FALSE, FALSE, TRUE, '111222', '33344455', 'MJScenarioAcct', 'REF998', '1'),
    -- Scenario 4: CF creditor_account with processed transaction included by PO-5774
    (9999, 1001, 'CA999', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '444555', '55667788', 'CFScenarioAcct', 'REF999', '3'),
    -- Scenario 5: MJ creditor_account with BACS cutoff
    (10000, 1002, 'CA10000', 'MJ', 9993, FALSE, NULL, FALSE, FALSE, TRUE, '121212', '34343434', 'MJBacsCutoffAcct', 'REF000', '1'),
    -- Scenario 6: CF creditor_account with CHEQUE cutoff
    (10001, 1003, 'CA10001', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '565656', '78787878', 'CFChequeCutoffAcct', 'REF001', '3'),
    -- Scenario 7: MJ creditor_account with both BACS and CHEQUE; latest one must be used
    (10002, 1004, 'CA10002', 'MJ', 9994, FALSE, NULL, FALSE, FALSE, TRUE, '909090', '12121212', 'MJLatestCutoffAcct', 'REF002', '1'),
    -- Scenario 8: CF creditor_account with no transactions after latest BACS/CHEQUE; expected payout is zero
    (10003, 1005, 'CA10003', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '343434', '56565656', 'CFNoPostCutoffAcct', 'REF003', '3');

    -- Consolidated multi-row INSERT for all creditor_transactions (preserve scenario comments)
    INSERT INTO creditor_transactions
    (
        creditor_transaction_id,
        creditor_account_id,
        posted_date,
        posted_by,
        posted_by_name,
        transaction_type,
        transaction_amount,
        imposition_result_id,
        payment_processed,
        payment_reference,
        status,
        status_date,
        associated_record_type,
        associated_record_id
    )
    VALUES
    -- Scenario 1 - unchanged from v2: no BACS/CHEQUE, single PAYMNT transaction of 700
    (99911, 9996, CURRENT_TIMESTAMP - INTERVAL '5 days', 'DRJ11', NULL, 'PAYMNT', 700, NULL, FALSE, 9811, NULL, CURRENT_TIMESTAMP + INTERVAL '5 days', NULL, NULL),

    -- Scenario 2 - unchanged from v2: no BACS/CHEQUE, single PAYMNT transaction of 300
    (99912, 9997, CURRENT_TIMESTAMP - INTERVAL '3 days', 'DRJ12', NULL, 'PAYMNT', 300, NULL, FALSE, 9812, NULL, CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, NULL),

    -- Scenario 3 - PO-5774 changed expectation from 400 to 600 because XFER is now included when no BACS/CHEQUE cutoff exists
    (99913, 9998, CURRENT_TIMESTAMP - INTERVAL '6 days', 'DRJ13', NULL, 'PAYMNT', 100, NULL, FALSE, 9913, NULL, CURRENT_TIMESTAMP + INTERVAL '6 days', NULL, NULL),
    (99914, 9998, CURRENT_TIMESTAMP - INTERVAL '4 days', 'DRJ14', NULL, 'XFER', 200, NULL, FALSE, 9914, NULL, CURRENT_TIMESTAMP + INTERVAL '4 days', NULL, NULL),
    (99915, 9998, CURRENT_TIMESTAMP - INTERVAL '2 days', 'DRJ15', NULL, 'PAYMNT', 300, NULL, FALSE, 9915, NULL, CURRENT_TIMESTAMP + INTERVAL '2 days', NULL, NULL),

    -- Scenario 4 - PO-5774 changed expectation from 325 to 450 because payment_processed is no longer filtered out
    (99916, 9999, CURRENT_TIMESTAMP - INTERVAL '8 days', 'DRJ16', NULL, 'PAYMNT', 50, NULL, FALSE, 9916, NULL, CURRENT_TIMESTAMP + INTERVAL '8 days', NULL, NULL),
    (99917, 9999, CURRENT_TIMESTAMP - INTERVAL '7 days', 'DRJ17', NULL, 'PAYMNT', 75, NULL, FALSE, 9917, NULL, CURRENT_TIMESTAMP + INTERVAL '7 days', NULL, NULL),
    (99918, 9999, CURRENT_TIMESTAMP - INTERVAL '5 days', 'DRJ18', NULL, 'PAYMNT', 125, NULL, TRUE, 9918, NULL, CURRENT_TIMESTAMP + INTERVAL '5 days', NULL, NULL),
    (99919, 9999, CURRENT_TIMESTAMP - INTERVAL '3 days', 'DRJ19', NULL, 'PAYMNT', 200, NULL, FALSE, 9919, NULL, CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, NULL),

    -- Scenario 5 - PO-5774 new MJ BACS cutoff:
    -- transactions before and on the BACS date are excluded; transactions after BACS total 60 + 40 = 100
    (99920, 10000, CURRENT_TIMESTAMP - INTERVAL '10 days', 'DRJ20', NULL, 'PAYMNT', 80, NULL, FALSE, 9920, NULL, CURRENT_TIMESTAMP + INTERVAL '10 days', NULL, NULL),
    (99921, 10000, CURRENT_TIMESTAMP - INTERVAL '8 days', 'DRJ21', NULL, 'BACS', 80, NULL, TRUE, 9921, NULL, CURRENT_TIMESTAMP + INTERVAL '8 days', NULL, NULL),
    (99922, 10000, CURRENT_TIMESTAMP - INTERVAL '6 days', 'DRJ22', NULL, 'PAYMNT', 60, NULL, FALSE, 9922, NULL, CURRENT_TIMESTAMP + INTERVAL '6 days', NULL, NULL),
    (99923, 10000, CURRENT_TIMESTAMP - INTERVAL '4 days', 'DRJ23', NULL, 'XFER', 40, NULL, FALSE, 9923, NULL, CURRENT_TIMESTAMP + INTERVAL '4 days', NULL, NULL),

    -- Scenario 6 - PO-5774 new CF CHEQUE cutoff:
    -- transactions before and on the CHEQUE date are excluded; transactions after CHEQUE total 35 + 15 = 50
    (99924, 10001, CURRENT_TIMESTAMP - INTERVAL '9 days', 'DRJ24', NULL, 'PAYMNT', 90, NULL, FALSE, 9924, NULL, CURRENT_TIMESTAMP + INTERVAL '9 days', NULL, NULL),
    (99925, 10001, CURRENT_TIMESTAMP - INTERVAL '7 days', 'DRJ25', NULL, 'CHEQUE', 90, NULL, TRUE, 9925, NULL, CURRENT_TIMESTAMP + INTERVAL '7 days', NULL, NULL),
    (99926, 10001, CURRENT_TIMESTAMP - INTERVAL '5 days', 'DRJ26', NULL, 'PAYMNT', 35, NULL, FALSE, 9926, NULL, CURRENT_TIMESTAMP + INTERVAL '5 days', NULL, NULL),
    (99927, 10001, CURRENT_TIMESTAMP - INTERVAL '3 days', 'DRJ27', NULL, 'MADJ', 15, NULL, FALSE, 9927, NULL, CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, NULL),

    -- Scenario 7 - PO-5774 new MJ latest-cutoff scenario:
    -- BACS is older than CHEQUE, so the later CHEQUE date is used; only the final PAYMNT of 25 is included
    (99928, 10002, CURRENT_TIMESTAMP - INTERVAL '12 days', 'DRJ28', NULL, 'PAYMNT', 120, NULL, FALSE, 9928, NULL, CURRENT_TIMESTAMP + INTERVAL '12 days', NULL, NULL),
    (99929, 10002, CURRENT_TIMESTAMP - INTERVAL '10 days', 'DRJ29', NULL, 'BACS', 120, NULL, TRUE, 9929, NULL, CURRENT_TIMESTAMP + INTERVAL '10 days', NULL, NULL),
    (99930, 10002, CURRENT_TIMESTAMP - INTERVAL '8 days', 'DRJ30', NULL, 'PAYMNT', 70, NULL, FALSE, 9930, NULL, CURRENT_TIMESTAMP + INTERVAL '8 days', NULL, NULL),
    (99931, 10002, CURRENT_TIMESTAMP - INTERVAL '6 days', 'DRJ31', NULL, 'CHEQUE', 70, NULL, TRUE, 9931, NULL, CURRENT_TIMESTAMP + INTERVAL '6 days', NULL, NULL),
    (99932, 10002, CURRENT_TIMESTAMP - INTERVAL '2 days', 'DRJ32', NULL, 'PAYMNT', 25, NULL, FALSE, 9932, NULL, CURRENT_TIMESTAMP + INTERVAL '2 days', NULL, NULL),

    -- Scenario 8 - PO-5774 new CF zero-after-cutoff scenario:
    -- latest BACS/CHEQUE is the final creditor transaction, so COALESCE(sum(...), 0) should return 0
    (99933, 10003, CURRENT_TIMESTAMP - INTERVAL '9 days', 'DRJ33', NULL, 'PAYMNT', 45, NULL, FALSE, 9933, NULL, CURRENT_TIMESTAMP + INTERVAL '9 days', NULL, NULL),
    (99934, 10003, CURRENT_TIMESTAMP - INTERVAL '7 days', 'DRJ34', NULL, 'CHEQUE', 45, NULL, TRUE, 9934, NULL, CURRENT_TIMESTAMP + INTERVAL '7 days', NULL, NULL),
    (99935, 10003, CURRENT_TIMESTAMP - INTERVAL '5 days', 'DRJ35', NULL, 'PAYMNT', 30, NULL, FALSE, 9935, NULL, CURRENT_TIMESTAMP + INTERVAL '5 days', NULL, NULL),
    (99936, 10003, CURRENT_TIMESTAMP - INTERVAL '3 days', 'DRJ36', NULL, 'BACS', 30, NULL, TRUE, 9936, NULL, CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, NULL);

    FOR rec IN
        SELECT creditor_account_id, expected_name, expected_awaiting_payout, expected_business_unit_code
        FROM ( VALUES
                   -- Scenario 1 - unchanged: no BACS/CHEQUE cutoff, single transaction is included
                   (9996::BIGINT, 'Major Creditor A'::TEXT, 700::NUMERIC, 'TBU1'::TEXT),
                   -- Scenario 2 - unchanged: no BACS/CHEQUE cutoff, single transaction is included
                   (9997::BIGINT, 'Central Fund A'::TEXT, 300::NUMERIC, 'TBU1'::TEXT),
                   -- Scenario 3 - PO-5774 changed from 400 to 600 because all transaction types are included
                   (9998::BIGINT, 'Major Creditor B'::TEXT, 600::NUMERIC, 'TBU2'::TEXT),
                   -- Scenario 4 - PO-5774 changed from 325 to 450 because payment_processed is ignored
                   (9999::BIGINT, 'Central Fund B'::TEXT, 450::NUMERIC, 'TBU3'::TEXT),
                   -- Scenario 5 - PO-5774 new: MJ branch uses latest BACS as cutoff
                   (10000::BIGINT, 'Major Creditor C'::TEXT, 100::NUMERIC, 'TBU4'::TEXT),
                   -- Scenario 6 - PO-5774 new: CF branch uses latest CHEQUE as cutoff
                   (10001::BIGINT, 'Central Fund C'::TEXT, 50::NUMERIC, 'TBU5'::TEXT),
                   -- Scenario 7 - PO-5774 new: latest of BACS/CHEQUE is used as cutoff
                   (10002::BIGINT, 'Major Creditor D'::TEXT, 25::NUMERIC, 'TBU6'::TEXT),
                   -- Scenario 8 - PO-5774 new: no transactions after latest BACS/CHEQUE returns zero
                   (10003::BIGINT, 'Central Fund D'::TEXT, 0::NUMERIC, 'TBU7'::TEXT)
             ) AS t(creditor_account_id, expected_name, expected_awaiting_payout, expected_business_unit_code)
    LOOP
        v_creditor_account_id := rec.creditor_account_id;

        SELECT
            creditor_account_id,
            creditor_account_number,
            creditor_account_type,
            version_number,
            business_unit_id,
            business_unit_code,
            business_unit_name,
            name,
            awaiting_payout
        INTO
            v_creditor_account_id,
            v_creditor_account_number,
            v_creditor_account_type,
            v_creditor_version,
            v_business_unit_id,
            v_business_unit_code,
            v_business_unit_name,
            v_name,
            v_awaiting_payout
        FROM v_major_creditor_account_header
        WHERE creditor_account_id = v_creditor_account_id;

        i := i + 1;

        RAISE NOTICE '--- Running test scenario % ---', i;

        v_expected_name := rec.expected_name;
        v_expected_awaiting_payout := rec.expected_awaiting_payout;
        v_expected_business_unit_code := rec.expected_business_unit_code;

        ASSERT v_name = v_expected_name, FORMAT('Name mismatch for creditor_account_id %s: expected="%s" actual="%s"', v_creditor_account_id, v_expected_name, v_name);
        ASSERT v_awaiting_payout = v_expected_awaiting_payout, FORMAT('Awaiting payout mismatch for creditor_account_id %s: expected=%s actual=%s', v_creditor_account_id, v_expected_awaiting_payout, v_awaiting_payout);
        ASSERT v_business_unit_code = v_expected_business_unit_code, FORMAT('Business unit code mismatch for creditor_account_id %s: expected="%s" actual="%s"', v_creditor_account_id, v_expected_business_unit_code, v_business_unit_code);

    END LOOP;

    RAISE NOTICE '=== All tests completed and passed for v_major_creditor_account_header view ===';

END
$$;

-- Cleanup to remove test rows
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Final targeted cleanup: removing test rows ===';

    -- PO-5774: Expanded final cleanup to include the additional v3 scenarios.
    DELETE FROM creditor_transactions
     WHERE creditor_transaction_id IN (
        99911,99912,99913,99914,99915,99916,99917,99918,99919,
        99920,99921,99922,99923,99924,99925,99926,99927,99928,99929,
        99930,99931,99932,99933,99934,99935,99936
     );

    DELETE FROM creditor_accounts WHERE creditor_account_id IN (9996,9997,9998,9999,10000,10001,10002,10003);
    DELETE FROM major_creditors WHERE major_creditor_id IN (9991,9992,9993,9994);
    DELETE FROM configuration_items WHERE configuration_item_id IN (9991,9992,9993,9994);
    DELETE FROM business_units WHERE business_unit_id IN (999,1000,1001,1002,1003,1004,1005);

    RAISE NOTICE '=== Final targeted cleanup completed ===';
END $$;

\timing
