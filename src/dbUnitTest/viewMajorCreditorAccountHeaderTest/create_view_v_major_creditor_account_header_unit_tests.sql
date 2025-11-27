/**
* CGI OPAL Program
*
* MODULE      : create_view_v_major_creditor_account_header_unit_tests.sql
*
* DESCRIPTION : Unit tests for v_major_creditor_account_header view
*               Verifies creditor account balance calculations and related fields for various scenarios
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 19/11/2025    C Larkin    1.0         PO-2122 - Updated test description, data and assertions to cover additional scenarios
*
**/

-- Start timing measurements for test execution
\timing

DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Clear down data (delete dependent tables first)
    DELETE FROM creditor_transactions WHERE creditor_transaction_id IN (99911,99912,99913,99914,99915,99916,99917,99918,99919) OR creditor_account_id IN (9996,9997,9998,9999);
    DELETE FROM creditor_accounts WHERE creditor_account_id IN (9996,9997,9998,9999);
    DELETE FROM major_creditors WHERE major_creditor_id IN (9991,9992);
    DELETE FROM configuration_items WHERE configuration_item_id IN (9991,9992);
    -- Ensure test business_units in the 999 range are removed so inserts can proceed without ON CONFLICT
    DELETE FROM business_units WHERE business_unit_id IN (999,1000,1001);

    RAISE NOTICE '=== Test cleanup completed ===';

END $$;

DO LANGUAGE 'plpgsql' $$
DECLARE
    -- only the variables used in these compact tests
    v_creditor_account_id         BIGINT;
    v_creditor_account_number     VARCHAR(50);
    v_creditor_account_type       VARCHAR(10);
    v_creditor_version            INTEGER;
    v_business_unit_id            SMALLINT;
    v_business_unit_name          VARCHAR(200);
    v_name                        VARCHAR(200);
    v_awaiting_payout             NUMERIC(18,2);
    v_expected_name               VARCHAR(200);
    v_expected_awaiting_payout    NUMERIC(18,2);
    rec                           RECORD;    
    i                             INTEGER := 0;  
BEGIN

    RAISE NOTICE '=== Setting up test data for v_major_creditor_account_header tests ===';

    -- Insert Business Units    
    FOR i IN 0..2 LOOP
        INSERT INTO business_units
        (
          business_unit_id
        , business_unit_name
        , business_unit_code 
        , business_unit_type
        , welsh_language
        )
        VALUES
        (
          999 + i
        , 'Test Audit Business Unit'
        , 'TBU' || (i + 1)
        , 'Accounting'
        , FALSE
        );
    END LOOP;

    -- Insert into major_creditors
    INSERT INTO major_creditors
    (
        major_creditor_id,
        business_unit_id,
        name
    )
    VALUES
    -- Scenario 1 - used by MJ creditor_account with single PAYMNT transaction
    (
        9991,
        999,
        'Major Creditor A'
    ),
    -- Scenario 3 - different major creditor
    (
        9992,
        1000,
        'Major Creditor B'
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
    -- Scenario 1: MJ creditor_account with single PAYMNT transaction
    (9996, 999, 'CA006', 'MJ', 9991, FALSE, NULL, FALSE, FALSE, TRUE, '777888', '77889900', 'MajorCredAcct', 'REF006', '1'),
    -- Scenario 2: CF creditor_account with single PAYMNT transaction  
    (9997, 999, 'CA007', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '888999', '88990011', 'CentralFundAcct', 'REF007', '3'),
    -- Scenario 3: MJ creditor_account with 3 transactions (one 'Other')
    (9998, 1000, 'CA998', 'MJ', 9992, FALSE, NULL, FALSE, FALSE, TRUE, '111222', '33344455', 'MJScenarioAcct', 'REF998', '1'),
    -- Scenario 4: CF creditor_account with 4 PAYMNT transactions
    (9999, 1001, 'CA999', 'CF', NULL, FALSE, NULL, FALSE, FALSE, TRUE, '444555', '55667788', 'CFScenarioAcct', 'REF999', '3');

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
    -- Scenario 1 - single PAYMNT transaction of 700
    (99911, 9996, CURRENT_TIMESTAMP - INTERVAL '5 days', 'DRJ11', NULL, 'PAYMNT', 700, NULL, FALSE, 9811, NULL, CURRENT_TIMESTAMP + INTERVAL '5 days', NULL, NULL),

    -- Scenario 2 - single PAYMNT transaction of 300
    (99912, 9997, CURRENT_TIMESTAMP - INTERVAL '3 days', 'DRJ12', NULL, 'PAYMNT', 300, NULL, FALSE, 9812, NULL, CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, NULL),

    -- Scenario 3 - 3 transactions (2 PAYMNT + 1 Other), total awaiting_payout = 400
    (99913, 9998, CURRENT_TIMESTAMP - INTERVAL '6 days', 'DRJ13', NULL, 'PAYMNT', 100, NULL, FALSE, 9913, NULL, CURRENT_TIMESTAMP + INTERVAL '6 days', NULL, NULL),
    (99914, 9998, CURRENT_TIMESTAMP - INTERVAL '4 days', 'DRJ14', NULL, 'Other', 200, NULL, FALSE, 9914, NULL, CURRENT_TIMESTAMP + INTERVAL '4 days', NULL, NULL),
    (99915, 9998, CURRENT_TIMESTAMP - INTERVAL '2 days', 'DRJ15', NULL, 'PAYMNT', 300, NULL, FALSE, 9915, NULL, CURRENT_TIMESTAMP + INTERVAL '2 days', NULL, NULL),

    -- Scenario 4 - 4 PAYMNT transactions, one processed; total awaiting_payout = 325
    (99916, 9999, CURRENT_TIMESTAMP - INTERVAL '8 days', 'DRJ16', NULL, 'PAYMNT', 50, NULL, FALSE, 9916, NULL, CURRENT_TIMESTAMP + INTERVAL '8 days', NULL, NULL),
    (99917, 9999, CURRENT_TIMESTAMP - INTERVAL '7 days', 'DRJ17', NULL, 'PAYMNT', 75, NULL, FALSE, 9917, NULL, CURRENT_TIMESTAMP + INTERVAL '7 days', NULL, NULL),
    (99918, 9999, CURRENT_TIMESTAMP - INTERVAL '5 days', 'DRJ18', NULL, 'PAYMNT', 125, NULL, TRUE, 9918, NULL, CURRENT_TIMESTAMP + INTERVAL '5 days', NULL, NULL),
    (99919, 9999, CURRENT_TIMESTAMP - INTERVAL '3 days', 'DRJ19', NULL, 'PAYMNT', 200, NULL, FALSE, 9919, NULL, CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, NULL);
        
    FOR rec IN
        SELECT creditor_account_id, expected_name, expected_awaiting_payout
        FROM ( VALUES
                   (9996::BIGINT, 'Major Creditor A'::TEXT, 700::NUMERIC),
                   (9997::BIGINT, 'Central Fund A'::TEXT, 300::NUMERIC),
                   (9998::BIGINT, 'Major Creditor B'::TEXT, 400::NUMERIC),
                   (9999::BIGINT, 'Central Fund B'::TEXT, 325::NUMERIC)
             ) AS t(creditor_account_id, expected_name, expected_awaiting_payout)
    LOOP
        v_creditor_account_id := rec.creditor_account_id;

        SELECT
            creditor_account_id,
            creditor_account_number,
            creditor_account_type,
            version_number,
            business_unit_id,
            business_unit_name,
            name,
            awaiting_payout
        INTO
            v_creditor_account_id,
            v_creditor_account_number,
            v_creditor_account_type,
            v_creditor_version,
            v_business_unit_id,
            v_business_unit_name,
            v_name,
            v_awaiting_payout
        FROM v_major_creditor_account_header
        WHERE creditor_account_id = v_creditor_account_id;

        i := i + 1;

        RAISE NOTICE '--- Running test scenario % ---', i;
        
        v_expected_name := rec.expected_name;
        v_expected_awaiting_payout := rec.expected_awaiting_payout;

        -- Why format?
        ASSERT v_name = v_expected_name, FORMAT('Name mismatch for creditor_account_id %s: expected="%s" actual="%s"', v_creditor_account_id, v_expected_name, v_name);
        ASSERT v_awaiting_payout = v_expected_awaiting_payout, FORMAT('Awaiting payout mismatch for creditor_account_id %s: expected=%s actual=%s', v_creditor_account_id, v_expected_awaiting_payout, v_awaiting_payout);

    END LOOP;

    RAISE NOTICE '=== All tests completed and passed for v_major_creditor_account_header view ===';
    
END
$$;

-- Cleanup to remove test rows
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Final targeted cleanup: removing test rows ===';
    DELETE FROM creditor_transactions WHERE creditor_transaction_id IN (99911,99912,99913,99914,99915,99916,99917,99918,99919);
    DELETE FROM creditor_accounts WHERE creditor_account_id IN (9996,9997,9998,9999);
    DELETE FROM major_creditors WHERE major_creditor_id IN (9991,9992);
    DELETE FROM configuration_items WHERE configuration_item_id IN (9991,9992);
    RAISE NOTICE '=== Final targeted cleanup completed ===';
END $$;

\timing