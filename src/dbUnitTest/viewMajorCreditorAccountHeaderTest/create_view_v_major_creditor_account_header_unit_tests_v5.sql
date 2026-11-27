/**
* CGI OPAL Program
*
* MODULE      : create_view_v_major_creditor_account_header_unit_tests_v5.sql
*
* DESCRIPTION : Unit tests for v_major_creditor_account_header view
*               Verifies creditor account header fields and the PO-5774 Awaiting Payout calculation.
*
*               PO-11608 appends business_units.welsh_language as welsh_language to both the MJ and CF
*               branches of the view. Test business units now use a mix of TRUE, FALSE and NULL so that
*               every value is exercised through both branches. Existing scenarios and expected values
*               (name, awaiting_payout, business_unit_code) are unchanged to prove existing results are
*               preserved, and column structure / one-row-per-account checks are added.
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
* 24/11/2026    S Abbasi    5.0         PO-11608 - Add welsh_language assertions (TRUE, FALSE and NULL) for both
*                                                MJ and CF branches, plus column structure and row count checks.
*                                                Test business_unit_ids moved from 999-1005 to 9981-9987 because
*                                                1001-1005 are seeded reference business units (V1_3) and the v4
*                                                cleanup fails on a migrated database (FK from configuration_items).
*                                                creditor_accounts.from_suspense renamed to repayment (V1_245).
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
    -- Ensure test business_units in the 9981-9987 range are removed so inserts can proceed without ON CONFLICT
    DELETE FROM business_units WHERE business_unit_id IN (9981,9982,9983,9984,9985,9986,9987);

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
    v_welsh_language              BOOLEAN;
    v_expected_welsh_language     BOOLEAN;
    v_row_count                   INTEGER;
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
    -- PO-11608: welsh_language varied so TRUE, FALSE and NULL are each exercised in both MJ and CF branches:
    --   MJ: 9981 TRUE, 9982 FALSE, 9984 NULL, 9986 TRUE
    --   CF: 9981 TRUE, 9983 NULL, 9985 TRUE, 9987 FALSE
    VALUES
    (9981,  'Test Audit Business Unit', 'TBU1', 'Accounting Division', TRUE),
    (9982, 'Test Audit Business Unit', 'TBU2', 'Accounting Division', FALSE),
    (9983, 'Test Audit Business Unit', 'TBU3', 'Accounting Division', NULL),
    (9984, 'Test Audit Business Unit', 'TBU4', 'Accounting Division', NULL),
    (9985, 'Test Audit Business Unit', 'TBU5', 'Accounting Division', TRUE),
    (9986, 'Test Audit Business Unit', 'TBU6', 'Accounting Division', TRUE),
    (9987, 'Test Audit Business Unit', 'TBU7', 'Accounting Division', FALSE);

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
        9981,
        'Major Creditor A'
    ),
    -- Scenario 3 - MJ creditor_account used to prove non-PAYMNT rows are now included
    (
        9992,
        9982,
        'Major Creditor B'
    ),
    -- Scenario 5 - PO-5774 new MJ scenario with a BACS cutoff
    (
        9993,
        9984,
        'Major Creditor C'
    ),
    -- Scenario 7 - PO-5774 new MJ scenario with both BACS and CHEQUE cutoffs
    (
        9994,
        9986,
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
        9981,
        'CENTRAL_FUND_ACCOUNT',
        '{"name": "Central Fund A"}'::JSONB
    ),
    (
        9992,
        9983,
        'CENTRAL_FUND_ACCOUNT',
        '{"name": "Central Fund B"}'::JSONB
    ),
    -- Scenario 6 - PO-5774 new CF scenario with a CHEQUE cutoff
    (
        9993,
        9985,
        'CENTRAL_FUND_ACCOUNT',
        '{"name": "Central Fund C"}'::JSONB
    ),
    -- Scenario 8 - PO-5774 new CF scenario with no transactions after latest BACS/CHEQUE
    (
        9994,
        9987,
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
        repayment,
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
    (9996, 9981, 'CA006', 'MJ', 9991, FALSE, NULL, FALSE, FALSE, TRUE, '777888', '77889900', 'MajorCredAcct', 'REF006', '1'),
    -- Scenario 2: CF creditor_account with no BACS/CHEQUE cutoff; all transactions are summed
    (9997, 9981, 'CA007', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '888999', '88990011', 'CentralFundAcct', 'REF007', '3'),
    -- Scenario 3: MJ creditor_account with non-PAYMNT transaction included by PO-5774
    (9998, 9982, 'CA998', 'MJ', 9992, FALSE, NULL, FALSE, FALSE, TRUE, '111222', '33344455', 'MJScenarioAcct', 'REF998', '1'),
    -- Scenario 4: CF creditor_account with processed transaction included by PO-5774
    (9999, 9983, 'CA999', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '444555', '55667788', 'CFScenarioAcct', 'REF999', '3'),
    -- Scenario 5: MJ creditor_account with BACS cutoff
    (10000, 9984, 'CA10000', 'MJ', 9993, FALSE, NULL, FALSE, FALSE, TRUE, '121212', '34343434', 'MJBacsCutoffAcct', 'REF000', '1'),
    -- Scenario 6: CF creditor_account with CHEQUE cutoff
    (10001, 9985, 'CA10001', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '565656', '78787878', 'CFChequeCutoffAcct', 'REF001', '3'),
    -- Scenario 7: MJ creditor_account with both BACS and CHEQUE; latest one must be used
    (10002, 9986, 'CA10002', 'MJ', 9994, FALSE, NULL, FALSE, FALSE, TRUE, '909090', '12121212', 'MJLatestCutoffAcct', 'REF002', '1'),
    -- Scenario 8: CF creditor_account with no transactions after latest BACS/CHEQUE; expected payout is zero
    (10003, 9987, 'CA10003', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '343434', '56565656', 'CFNoPostCutoffAcct', 'REF003', '3');

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
        SELECT creditor_account_id, expected_account_type, expected_name, expected_awaiting_payout,
               expected_business_unit_code, expected_welsh_language
        FROM ( VALUES
                   -- Scenario 1 - unchanged: no BACS/CHEQUE cutoff, single transaction is included
                   -- PO-11608: MJ branch, welsh_language TRUE
                   (9996::BIGINT, 'MJ'::TEXT, 'Major Creditor A'::TEXT, 700::NUMERIC, 'TBU1'::TEXT, TRUE::BOOLEAN),
                   -- Scenario 2 - unchanged: no BACS/CHEQUE cutoff, single transaction is included
                   -- PO-11608: CF branch, welsh_language TRUE
                   (9997::BIGINT, 'CF'::TEXT, 'Central Fund A'::TEXT, 300::NUMERIC, 'TBU1'::TEXT, TRUE::BOOLEAN),
                   -- Scenario 3 - PO-5774 changed from 400 to 600 because all transaction types are included
                   -- PO-11608: MJ branch, welsh_language FALSE
                   (9998::BIGINT, 'MJ'::TEXT, 'Major Creditor B'::TEXT, 600::NUMERIC, 'TBU2'::TEXT, FALSE::BOOLEAN),
                   -- Scenario 4 - PO-5774 changed from 325 to 450 because payment_processed is ignored
                   -- PO-11608: CF branch, welsh_language NULL
                   (9999::BIGINT, 'CF'::TEXT, 'Central Fund B'::TEXT, 450::NUMERIC, 'TBU3'::TEXT, NULL::BOOLEAN),
                   -- Scenario 5 - PO-5774 new: MJ branch uses latest BACS as cutoff
                   -- PO-11608: MJ branch, welsh_language NULL
                   (10000::BIGINT, 'MJ'::TEXT, 'Major Creditor C'::TEXT, 100::NUMERIC, 'TBU4'::TEXT, NULL::BOOLEAN),
                   -- Scenario 6 - PO-5774 new: CF branch uses latest CHEQUE as cutoff
                   -- PO-11608: CF branch, welsh_language TRUE
                   (10001::BIGINT, 'CF'::TEXT, 'Central Fund C'::TEXT, 50::NUMERIC, 'TBU5'::TEXT, TRUE::BOOLEAN),
                   -- Scenario 7 - PO-5774 new: latest of BACS/CHEQUE is used as cutoff
                   -- PO-11608: MJ branch, welsh_language TRUE
                   (10002::BIGINT, 'MJ'::TEXT, 'Major Creditor D'::TEXT, 25::NUMERIC, 'TBU6'::TEXT, TRUE::BOOLEAN),
                   -- Scenario 8 - PO-5774 new: no transactions after latest BACS/CHEQUE returns zero
                   -- PO-11608: CF branch, welsh_language FALSE
                   (10003::BIGINT, 'CF'::TEXT, 'Central Fund D'::TEXT, 0::NUMERIC, 'TBU7'::TEXT, FALSE::BOOLEAN)
             ) AS t(creditor_account_id, expected_account_type, expected_name, expected_awaiting_payout,
                    expected_business_unit_code, expected_welsh_language)
    LOOP
        v_creditor_account_id := rec.creditor_account_id;

        -- PO-11608: Confirm row selection is unchanged (exactly one row per creditor account)
        SELECT COUNT(*)
          INTO v_row_count
          FROM v_major_creditor_account_header
         WHERE creditor_account_id = v_creditor_account_id;

        SELECT
            creditor_account_id,
            creditor_account_number,
            creditor_account_type,
            version_number,
            business_unit_id,
            business_unit_code,
            business_unit_name,
            name,
            awaiting_payout,
            welsh_language
        INTO
            v_creditor_account_id,
            v_creditor_account_number,
            v_creditor_account_type,
            v_creditor_version,
            v_business_unit_id,
            v_business_unit_code,
            v_business_unit_name,
            v_name,
            v_awaiting_payout,
            v_welsh_language
        FROM v_major_creditor_account_header
        WHERE creditor_account_id = v_creditor_account_id;

        i := i + 1;

        RAISE NOTICE '--- Running test scenario % ---', i;

        v_expected_name := rec.expected_name;
        v_expected_awaiting_payout := rec.expected_awaiting_payout;
        v_expected_business_unit_code := rec.expected_business_unit_code;
        v_expected_welsh_language := rec.expected_welsh_language;

        ASSERT v_row_count = 1, FORMAT('Row count mismatch for creditor_account_id %s: expected=1 actual=%s', v_creditor_account_id, v_row_count);
        ASSERT v_creditor_account_type::TEXT = rec.expected_account_type, FORMAT('Account type mismatch for creditor_account_id %s: expected="%s" actual="%s"', v_creditor_account_id, rec.expected_account_type, v_creditor_account_type);
        ASSERT v_name = v_expected_name, FORMAT('Name mismatch for creditor_account_id %s: expected="%s" actual="%s"', v_creditor_account_id, v_expected_name, v_name);
        ASSERT v_awaiting_payout = v_expected_awaiting_payout, FORMAT('Awaiting payout mismatch for creditor_account_id %s: expected=%s actual=%s', v_creditor_account_id, v_expected_awaiting_payout, v_awaiting_payout);
        ASSERT v_business_unit_code = v_expected_business_unit_code, FORMAT('Business unit code mismatch for creditor_account_id %s: expected="%s" actual="%s"', v_creditor_account_id, v_expected_business_unit_code, v_business_unit_code);
        -- PO-11608: IS NOT DISTINCT FROM so that NULL is compared as a value
        ASSERT v_welsh_language IS NOT DISTINCT FROM v_expected_welsh_language, FORMAT('Welsh language mismatch for creditor_account_id %s: expected=%s actual=%s', v_creditor_account_id, COALESCE(v_expected_welsh_language::TEXT, 'NULL'), COALESCE(v_welsh_language::TEXT, 'NULL'));

    END LOOP;

    RAISE NOTICE '=== All tests completed and passed for v_major_creditor_account_header view ===';

END
$$;

-- PO-11608: Existing columns retain names, types and order; welsh_language is appended as boolean
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_expected_columns  TEXT[] := ARRAY[
        'creditor_account_id', 'creditor_account_number', 'creditor_account_type', 'version_number',
        'business_unit_id', 'business_unit_name', 'name', 'awaiting_payout', 'business_unit_code',
        'welsh_language'
    ];
    v_actual_columns    TEXT[];
    v_welsh_type        TEXT;
BEGIN
    RAISE NOTICE '=== PO-11608: Checking column names, order and welsh_language type ===';

    SELECT array_agg(column_name::TEXT ORDER BY ordinal_position)
      INTO v_actual_columns
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND table_name = 'v_major_creditor_account_header';

    ASSERT v_actual_columns = v_expected_columns,
        FORMAT('Column list/order mismatch: expected=%s actual=%s', v_expected_columns, v_actual_columns);

    SELECT data_type
      INTO v_welsh_type
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND table_name = 'v_major_creditor_account_header'
       AND column_name = 'welsh_language';

    ASSERT v_welsh_type = 'boolean', FORMAT('welsh_language should be boolean, actual=%s', v_welsh_type);

    RAISE NOTICE '=== PO-11608: Column structure checks passed ===';
END $$;

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
    DELETE FROM business_units WHERE business_unit_id IN (9981,9982,9983,9984,9985,9986,9987);

    RAISE NOTICE '=== Final targeted cleanup completed ===';
END $$;

\timing
