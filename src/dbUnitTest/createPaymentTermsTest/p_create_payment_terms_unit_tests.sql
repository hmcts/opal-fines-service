/**
* CGI OPAL Program
*
* MODULE      : p_create_payment_terms_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_create_payment_terms.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------
* 02/09/2025    TMc         1.0         Unit tests for p_create_payment_terms
*
**/

\timing

-- Clear out tables
DO $$
DECLARE

BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Delete all test accounts created by these tests
    DELETE FROM payment_terms WHERE defendant_account_id = 999901;
    DELETE FROM defendant_accounts WHERE defendant_account_id = 999901;
    DELETE FROM business_units WHERE business_unit_id = 9999;

    COMMIT;

    RAISE NOTICE 'Data cleanup before tests completed';
END $$;

DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
BEGIN
    RAISE NOTICE '=== Setting up test data ===';
    
    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_type
    ) VALUES (
        v_business_unit_id,
        'Test BU',
        'Accounting Division'
    );

    INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number, amount_imposed, amount_paid, account_balance, account_status, account_type, jail_days)
    VALUES ( 
            v_defendant_account_id
          , v_business_unit_id
          , v_account_number
          , 0
          , 0
          , 0
          , 'L'
          , 'Fine'
          , 0
    );
    
    COMMIT;

    RAISE NOTICE 'Test data setup completed: defendant_account_id = %', v_defendant_account_id;
END $$;

--Test 1 - valid payment_terms: acount_type NOT 'Fixed Penalty', type = 'B', effective_date IS NOT NULL
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_result_payment_terms_id       payment_terms.payment_terms_id%TYPE; 
    v_result_posted_date            payment_terms.posted_date%TYPE;
    v_result_posted_by              payment_terms.posted_by%TYPE;
    v_result_terms_type_code        payment_terms.terms_type_code%TYPE;
    v_result_effective_date         payment_terms.effective_date%TYPE;
    v_result_instalment_period      payment_terms.instalment_period%TYPE;
    v_result_instalment_amount      payment_terms.instalment_amount%TYPE;
    v_result_instalment_lump_sum    payment_terms.instalment_lump_sum%TYPE;
    v_result_jail_days              payment_terms.jail_days%TYPE;
    v_result_extension              payment_terms.extension%TYPE;
    v_result_account_balance        payment_terms.account_balance%TYPE;
    v_result_posted_by_name         payment_terms.posted_by_name%TYPE;
    v_result_active                 payment_terms.active%TYPE;
    --v_result_reason_for_extension   payment_terms.reason_for_extension%TYPE;
BEGIN

    v_account_type := 'Fine';

    v_payment_terms_json := '{
            "payment_terms_type_code": "B",
            "effective_date": "2025-01-01",
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 10
    }';

    RAISE NOTICE '=== TEST 1: acount_type NOT Fixed Penalty, type = B, effective_date IS NOT NULL ===';

    CALL p_create_payment_terms ( v_defendant_account_id,
                                  v_account_type,
                                  v_account_balance,
                                  v_posted_by,
                                  v_posted_by_name,
                                  v_payment_terms_json
    );

    SELECT payment_terms_id, posted_date, posted_by, terms_type_code, effective_date, instalment_period
         , instalment_amount, instalment_lump_sum, jail_days, "extension", account_balance, posted_by_name, active
         --, reason_for_extension
      INTO v_result_payment_terms_id, v_result_posted_date, v_result_posted_by, v_result_terms_type_code, v_result_effective_date, v_result_instalment_period
         , v_result_instalment_amount, v_result_instalment_lump_sum, v_result_jail_days, v_result_extension, v_result_account_balance, v_result_posted_by_name
         , v_result_active
         --, v_result_reason_for_extension
      FROM payment_terms
     WHERE defendant_account_id = v_defendant_account_id;

    ASSERT v_result_posted_date         IS NOT NULL, 'posted_date should be set';
    ASSERT v_result_posted_by           = v_posted_by, 'posted_by should match';
    ASSERT v_result_terms_type_code     = 'B', 'terms_type_code should match';
    --ASSERT v_result_effective_date      = (CURRENT_DATE + INTERVAL '28 days')::timestamp, 'effective_date should match';
    ASSERT v_result_effective_date      = ('2025-01-01')::timestamp, 'effective_date should match';
    ASSERT v_result_instalment_period   = 'M', 'instalment_period should match';
    ASSERT v_result_instalment_amount   = 50.00, 'instalment_amount should match';
    ASSERT v_result_instalment_lump_sum = 40.00, 'instalment_lump_sum should match';
    ASSERT v_result_jail_days           = 10, 'jail_days should match';
    ASSERT v_result_extension           = FALSE, 'extension should be FALSE';
    ASSERT v_result_account_balance     = v_account_balance, 'account_balance should match';
    ASSERT v_result_posted_by_name      = v_posted_by_name, 'posted_by_name should match';
    ASSERT v_result_active              = TRUE, 'active should be TRUE';

    --Check that defendant_accounts.jail_days was updated correctly
    ASSERT (SELECT jail_days FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 10, 'Defendant_account Jail days should match';

    RAISE NOTICE 'TEST 1 PASSED';

END $$;

--Test 2 - valid payment_terms: acount_type NOT 'Fixed Penalty', type = 'I', effective_date IS NOT NULL, instalment_amount IS NOT NULL
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_result_payment_terms_id       payment_terms.payment_terms_id%TYPE; 
    v_result_posted_date            payment_terms.posted_date%TYPE;
    v_result_posted_by              payment_terms.posted_by%TYPE;
    v_result_terms_type_code        payment_terms.terms_type_code%TYPE;
    v_result_effective_date         payment_terms.effective_date%TYPE;
    v_result_instalment_period      payment_terms.instalment_period%TYPE;
    v_result_instalment_amount      payment_terms.instalment_amount%TYPE;
    v_result_instalment_lump_sum    payment_terms.instalment_lump_sum%TYPE;
    v_result_jail_days              payment_terms.jail_days%TYPE;
    v_result_extension              payment_terms.extension%TYPE;
    v_result_account_balance        payment_terms.account_balance%TYPE;
    v_result_posted_by_name         payment_terms.posted_by_name%TYPE;
    v_result_active                 payment_terms.active%TYPE;
    --v_result_reason_for_extension   payment_terms.reason_for_extension%TYPE;
BEGIN

    v_account_type := 'Fine';

    v_payment_terms_json := '{
            "payment_terms_type_code": "I",
            "effective_date": "2025-01-02",
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 11
    }';

    RAISE NOTICE '=== TEST 2: acount_type NOT Fixed Penalty, type = I, effective_date IS NOT NULL, instalment_amount IS NOT NULL ===';

    --Delete previously inserted PAYMENT_TERMS record
    DELETE FROM payment_terms WHERE defendant_account_id = v_defendant_account_id; 

    CALL p_create_payment_terms ( v_defendant_account_id,
                                  v_account_type,
                                  v_account_balance,
                                  v_posted_by,
                                  v_posted_by_name,
                                  v_payment_terms_json
    );

    SELECT payment_terms_id, posted_date, posted_by, terms_type_code, effective_date, instalment_period
         , instalment_amount, instalment_lump_sum, jail_days, "extension", account_balance, posted_by_name, active
         --, reason_for_extension
      INTO v_result_payment_terms_id, v_result_posted_date, v_result_posted_by, v_result_terms_type_code, v_result_effective_date, v_result_instalment_period
         , v_result_instalment_amount, v_result_instalment_lump_sum, v_result_jail_days, v_result_extension, v_result_account_balance, v_result_posted_by_name
         , v_result_active
         --, v_result_reason_for_extension
      FROM payment_terms
     WHERE defendant_account_id = v_defendant_account_id;

    ASSERT v_result_posted_date         IS NOT NULL, 'posted_date should be set';
    ASSERT v_result_posted_by           = v_posted_by, 'posted_by should match';
    ASSERT v_result_terms_type_code     = 'I', 'terms_type_code should match';
    --ASSERT v_result_effective_date      = (CURRENT_DATE + INTERVAL '28 days')::timestamp, 'effective_date should match';
    ASSERT v_result_effective_date      = ('2025-01-02')::timestamp, 'effective_date should match';
    ASSERT v_result_instalment_period   = 'M', 'instalment_period should match';
    ASSERT v_result_instalment_amount   = 50.00, 'instalment_amount should match';
    ASSERT v_result_instalment_lump_sum = 40.00, 'instalment_lump_sum should match';
    ASSERT v_result_jail_days           = 11, 'jail_days should match';
    ASSERT v_result_extension           = FALSE, 'extension should be FALSE';
    ASSERT v_result_account_balance     = v_account_balance, 'account_balance should match';
    ASSERT v_result_posted_by_name      = v_posted_by_name, 'posted_by_name should match';
    ASSERT v_result_active              = TRUE, 'active should be TRUE';

    --Check that defendant_accounts.jail_days was updated correctly
    ASSERT (SELECT jail_days FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 11, 'Defendant_account Jail days should match';

    RAISE NOTICE 'TEST 2 PASSED';

END $$;

--Test 3 - valid payment_terms: acount_type NOT 'Fixed Penalty', type = 'P', effective_date IS NOT NULL, instalment_amount IS NOT NULL
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_result_payment_terms_id       payment_terms.payment_terms_id%TYPE; 
    v_result_posted_date            payment_terms.posted_date%TYPE;
    v_result_posted_by              payment_terms.posted_by%TYPE;
    v_result_terms_type_code        payment_terms.terms_type_code%TYPE;
    v_result_effective_date         payment_terms.effective_date%TYPE;
    v_result_instalment_period      payment_terms.instalment_period%TYPE;
    v_result_instalment_amount      payment_terms.instalment_amount%TYPE;
    v_result_instalment_lump_sum    payment_terms.instalment_lump_sum%TYPE;
    v_result_jail_days              payment_terms.jail_days%TYPE;
    v_result_extension              payment_terms.extension%TYPE;
    v_result_account_balance        payment_terms.account_balance%TYPE;
    v_result_posted_by_name         payment_terms.posted_by_name%TYPE;
    v_result_active                 payment_terms.active%TYPE;
    --v_result_reason_for_extension   payment_terms.reason_for_extension%TYPE;
BEGIN

    v_account_type := 'Fine';

    v_payment_terms_json := '{
            "payment_terms_type_code": "P",
            "effective_date": "2025-01-03",
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 12
    }';

    RAISE NOTICE '=== TEST 3: acount_type NOT Fixed Penalty, type = P, effective_date IS NOT NULL, instalment_amount IS NOT NULL ===';

    --Delete previously inserted PAYMENT_TERMS record
    DELETE FROM payment_terms WHERE defendant_account_id = v_defendant_account_id; 

    CALL p_create_payment_terms ( v_defendant_account_id,
                                  v_account_type,
                                  v_account_balance,
                                  v_posted_by,
                                  v_posted_by_name,
                                  v_payment_terms_json
    );

    SELECT payment_terms_id, posted_date, posted_by, terms_type_code, effective_date, instalment_period
         , instalment_amount, instalment_lump_sum, jail_days, "extension", account_balance, posted_by_name, active
         --, reason_for_extension
      INTO v_result_payment_terms_id, v_result_posted_date, v_result_posted_by, v_result_terms_type_code, v_result_effective_date, v_result_instalment_period
         , v_result_instalment_amount, v_result_instalment_lump_sum, v_result_jail_days, v_result_extension, v_result_account_balance, v_result_posted_by_name
         , v_result_active
         --, v_result_reason_for_extension
      FROM payment_terms
     WHERE defendant_account_id = v_defendant_account_id;

    ASSERT v_result_posted_date         IS NOT NULL, 'posted_date should be set';
    ASSERT v_result_posted_by           = v_posted_by, 'posted_by should match';
    ASSERT v_result_terms_type_code     = 'P', 'terms_type_code should match';
    --ASSERT v_result_effective_date      = (CURRENT_DATE + INTERVAL '28 days')::timestamp, 'effective_date should match';
    ASSERT v_result_effective_date      = ('2025-01-03')::timestamp, 'effective_date should match';
    ASSERT v_result_instalment_period   = 'M', 'instalment_period should match';
    ASSERT v_result_instalment_amount   = 50.00, 'instalment_amount should match';
    ASSERT v_result_instalment_lump_sum = 40.00, 'instalment_lump_sum should match';
    ASSERT v_result_jail_days           = 12, 'jail_days should match';
    ASSERT v_result_extension           = FALSE, 'extension should be FALSE';
    ASSERT v_result_account_balance     = v_account_balance, 'account_balance should match';
    ASSERT v_result_posted_by_name      = v_posted_by_name, 'posted_by_name should match';
    ASSERT v_result_active              = TRUE, 'active should be TRUE';

    --Check that defendant_accounts.jail_days was updated correctly
    ASSERT (SELECT jail_days FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 12, 'Defendant_account Jail days should match';

    RAISE NOTICE 'TEST 3 PASSED';

END $$;

--Test 4 - valid payment_terms: acount_type IS 'Fixed Penalty', type = 'B', effective_date IS NULL
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_result_payment_terms_id       payment_terms.payment_terms_id%TYPE; 
    v_result_posted_date            payment_terms.posted_date%TYPE;
    v_result_posted_by              payment_terms.posted_by%TYPE;
    v_result_terms_type_code        payment_terms.terms_type_code%TYPE;
    v_result_effective_date         payment_terms.effective_date%TYPE;
    v_result_instalment_period      payment_terms.instalment_period%TYPE;
    v_result_instalment_amount      payment_terms.instalment_amount%TYPE;
    v_result_instalment_lump_sum    payment_terms.instalment_lump_sum%TYPE;
    v_result_jail_days              payment_terms.jail_days%TYPE;
    v_result_extension              payment_terms.extension%TYPE;
    v_result_account_balance        payment_terms.account_balance%TYPE;
    v_result_posted_by_name         payment_terms.posted_by_name%TYPE;
    v_result_active                 payment_terms.active%TYPE;
    --v_result_reason_for_extension   payment_terms.reason_for_extension%TYPE;
BEGIN

    v_account_type := 'Fixed Penalty';

    v_payment_terms_json := '{
            "payment_terms_type_code": "B",
            "effective_date": null,
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 13
    }';

    RAISE NOTICE '=== TEST 4: acount_type IS Fixed Penalty, type = B, effective_date IS NULL ===';

    --Delete previously inserted PAYMENT_TERMS record
    DELETE FROM payment_terms WHERE defendant_account_id = v_defendant_account_id; 

    CALL p_create_payment_terms ( v_defendant_account_id,
                                  v_account_type,
                                  v_account_balance,
                                  v_posted_by,
                                  v_posted_by_name,
                                  v_payment_terms_json
    );

    SELECT payment_terms_id, posted_date, posted_by, terms_type_code, effective_date, instalment_period
         , instalment_amount, instalment_lump_sum, jail_days, "extension", account_balance, posted_by_name, active
         --, reason_for_extension
      INTO v_result_payment_terms_id, v_result_posted_date, v_result_posted_by, v_result_terms_type_code, v_result_effective_date, v_result_instalment_period
         , v_result_instalment_amount, v_result_instalment_lump_sum, v_result_jail_days, v_result_extension, v_result_account_balance, v_result_posted_by_name
         , v_result_active
         --, v_result_reason_for_extension
      FROM payment_terms
     WHERE defendant_account_id = v_defendant_account_id;

    ASSERT v_result_posted_date         IS NOT NULL, 'posted_date should be set';
    ASSERT v_result_posted_by           = v_posted_by, 'posted_by should match';
    ASSERT v_result_terms_type_code     = 'B', 'terms_type_code should match';
    ASSERT v_result_effective_date      = (CURRENT_DATE + INTERVAL '28 days')::timestamp, 'effective_date should match';
    ASSERT v_result_instalment_period   = 'M', 'instalment_period should match';
    ASSERT v_result_instalment_amount   = 50.00, 'instalment_amount should match';
    ASSERT v_result_instalment_lump_sum = 40.00, 'instalment_lump_sum should match';
    ASSERT v_result_jail_days           = 13, 'jail_days should match';
    ASSERT v_result_extension           = FALSE, 'extension should be FALSE';
    ASSERT v_result_account_balance     = v_account_balance, 'account_balance should match';
    ASSERT v_result_posted_by_name      = v_posted_by_name, 'posted_by_name should match';
    ASSERT v_result_active              = TRUE, 'active should be TRUE';

    --Check that defendant_accounts.jail_days was updated correctly
    ASSERT (SELECT jail_days FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id) = 13, 'Defendant_account Jail days should match';

    RAISE NOTICE 'TEST 4 PASSED';

END $$;

--Test 5 - Invalid payment_terms: payment_terms_type_code is not valid
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_error_caught           BOOLEAN        := FALSE;
    v_expected_errmsg        VARCHAR        := 'Invalid payment terms';
BEGIN

    v_account_type := 'Fine';

    v_payment_terms_json := '{
            "payment_terms_type_code": "Z",
            "effective_date": null,
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 13
    }';

    RAISE NOTICE '=== TEST 5: payment_terms_type_code is not valid ===';

    --Delete previously inserted PAYMENT_TERMS record
    DELETE FROM payment_terms WHERE defendant_account_id = v_defendant_account_id; 
    

    -- Should throw a P2003 exception
    BEGIN
        CALL p_create_payment_terms ( v_defendant_account_id,
                                      v_account_type,
                                      v_account_balance,
                                      v_posted_by,
                                      v_posted_by_name,
                                      v_payment_terms_json
        );
    EXCEPTION
        WHEN SQLSTATE 'P2003' THEN 

            IF SQLERRM = v_expected_errmsg THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Test 5: Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Test 5: Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Test 5: Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'Test 5: A P2003 error, with correct SQLERRM, should have been raised due to invalid payment terms';

    RAISE NOTICE 'TEST 5 PASSED: Error handling works correctly when the passed payment terms are invalid';

END $$;

--Test 6 - Invalid payment_terms: payment_terms_type_code = 'B', effective_date IS NULL but account_type is not 'Fixed Penalty'
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_error_caught           BOOLEAN        := FALSE;
    v_expected_errmsg        VARCHAR        := 'Invalid payment terms';
BEGIN

    v_account_type := 'Fine';

    v_payment_terms_json := '{
            "payment_terms_type_code": "B",
            "effective_date": null,
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 13
    }';

    RAISE NOTICE '=== TEST 6: payment_terms_type_code = B, effective_date IS NULL but account_type is not Fixed Penalty ===';

    --Delete previously inserted PAYMENT_TERMS record
    DELETE FROM payment_terms WHERE defendant_account_id = v_defendant_account_id; 
    

    -- Should throw a P2003 exception
    BEGIN
        CALL p_create_payment_terms ( v_defendant_account_id,
                                      v_account_type,
                                      v_account_balance,
                                      v_posted_by,
                                      v_posted_by_name,
                                      v_payment_terms_json
        );
    EXCEPTION
        WHEN SQLSTATE 'P2003' THEN 

            IF SQLERRM = v_expected_errmsg THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Test 6: Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Test 6: Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Test 6: Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'Test 6: A P2003 error, with correct SQLERRM, should have been raised due to invalid payment terms';

    RAISE NOTICE 'TEST 6 PASSED: Error handling works correctly when the passed payment terms are invalid';

END $$;

--Test 7 - Invalid payment_terms: payment_terms_type_code = 'B', effective_date IS NOT NULL but account_type IS 'Fixed Penalty'
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_error_caught           BOOLEAN        := FALSE;
    v_expected_errmsg        VARCHAR        := 'Invalid payment terms';
BEGIN

    v_account_type := 'Fixed Penalty';

    v_payment_terms_json := '{
            "payment_terms_type_code": "B",
            "effective_date": "2025-01-01",
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 13
    }';

    RAISE NOTICE '=== TEST 7: payment_terms_type_code = B, effective_date IS NOT NULL but account_type IS Fixed Penalty ===';

    --Delete previously inserted PAYMENT_TERMS record
    DELETE FROM payment_terms WHERE defendant_account_id = v_defendant_account_id; 
    

    -- Should throw a P2003 exception
    BEGIN
        CALL p_create_payment_terms ( v_defendant_account_id,
                                      v_account_type,
                                      v_account_balance,
                                      v_posted_by,
                                      v_posted_by_name,
                                      v_payment_terms_json
        );
    EXCEPTION
        WHEN SQLSTATE 'P2003' THEN 

            IF SQLERRM = v_expected_errmsg THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Test 7: Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Test 7: Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Test 7: Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'Test 7: A P2003 error, with correct SQLERRM, should have been raised due to invalid payment terms';

    RAISE NOTICE 'TEST 7 PASSED: Error handling works correctly when the passed payment terms are invalid';

END $$;

--Test 8 - Invalid payment_terms: account_type IS 'Fixed Penalty' but payment_terms_type_code is NOT 'B'
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_error_caught           BOOLEAN        := FALSE;
    v_expected_errmsg        VARCHAR        := 'Invalid payment terms';
BEGIN

    v_account_type := 'Fixed Penalty';

    v_payment_terms_json := '{
            "payment_terms_type_code": "I",
            "effective_date": "2025-01-01",
            "instalment_period": "M",
            "instalment_amount": 50.00,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 13
    }';

    RAISE NOTICE '=== TEST 8: account_type IS Fixed Penalty but payment_terms_type_code is NOT B ===';

    --Delete previously inserted PAYMENT_TERMS record
    DELETE FROM payment_terms WHERE defendant_account_id = v_defendant_account_id; 
    

    -- Should throw a P2003 exception
    BEGIN
        CALL p_create_payment_terms ( v_defendant_account_id,
                                      v_account_type,
                                      v_account_balance,
                                      v_posted_by,
                                      v_posted_by_name,
                                      v_payment_terms_json
        );
    EXCEPTION
        WHEN SQLSTATE 'P2003' THEN 

            IF SQLERRM = v_expected_errmsg THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Test 8: Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Test 8: Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Test 8: Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'Test 8: A P2003 error, with correct SQLERRM, should have been raised due to invalid payment terms';

    RAISE NOTICE 'TEST 8 PASSED: Error handling works correctly when the passed payment terms are invalid';

END $$;

--Test 9 - Invalid payment_terms: payment_terms_type_code = 'I', effective_date IS NOT NULL but instalment_amount IS NULL
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_error_caught           BOOLEAN        := FALSE;
    v_expected_errmsg        VARCHAR        := 'Invalid payment terms';
BEGIN

    v_account_type := 'Fine';

    v_payment_terms_json := '{
            "payment_terms_type_code": "I",
            "effective_date": "2025-01-01",
            "instalment_period": "M",
            "instalment_amount": null,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 13
    }';

    RAISE NOTICE '=== TEST 9: payment_terms_type_code = I, effective_date IS NOT NULL but instalment_amount IS NULL ===';

    --Delete previously inserted PAYMENT_TERMS record
    DELETE FROM payment_terms WHERE defendant_account_id = v_defendant_account_id; 
    

    -- Should throw a P2003 exception
    BEGIN
        CALL p_create_payment_terms ( v_defendant_account_id,
                                      v_account_type,
                                      v_account_balance,
                                      v_posted_by,
                                      v_posted_by_name,
                                      v_payment_terms_json
        );
    EXCEPTION
        WHEN SQLSTATE 'P2003' THEN 

            IF SQLERRM = v_expected_errmsg THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Test 9: Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Test 9: Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Test 9: Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'Test 9: A P2003 error, with correct SQLERRM, should have been raised due to invalid payment terms';

    RAISE NOTICE 'TEST 9 PASSED: Error handling works correctly when the passed payment terms are invalid';

END $$;

--Test 10 - Invalid payment_terms: payment_terms_type_code = 'I', instalment_amount IS NOT NULL but effective_date IS NULL 
DO $$
DECLARE
    v_defendant_account_id   BIGINT         := 999901;
    v_business_unit_id       SMALLINT       := 9999; 
    v_account_number         VARCHAR        := '99000001E';
    v_account_balance        numeric(18,2)  := 10.00;
    v_posted_by              VARCHAR        := 'L045EO';
    v_posted_by_name         VARCHAR        := 'Tester 1';
    
    v_account_type           VARCHAR;
    v_payment_terms_json     JSON;

    v_error_caught           BOOLEAN        := FALSE;
    v_expected_errmsg        VARCHAR        := 'Invalid payment terms';
BEGIN

    v_account_type := 'Fine';

    v_payment_terms_json := '{
            "payment_terms_type_code": "I",
            "effective_date": "2025-01-01",
            "instalment_period": "M",
            "instalment_amount": null,
            "lump_sum_amount": 40.00,
            "default_days_in_jail": 13
    }';

    RAISE NOTICE '=== TEST 10: payment_terms_type_code = I, instalment_amount IS NOT NULL but effective_date IS NULL ===';

    --Delete previously inserted PAYMENT_TERMS record
    DELETE FROM payment_terms WHERE defendant_account_id = v_defendant_account_id; 
    

    -- Should throw a P2003 exception
    BEGIN
        CALL p_create_payment_terms ( v_defendant_account_id,
                                      v_account_type,
                                      v_account_balance,
                                      v_posted_by,
                                      v_posted_by_name,
                                      v_payment_terms_json
        );
    EXCEPTION
        WHEN SQLSTATE 'P2003' THEN 

            IF SQLERRM = v_expected_errmsg THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Test 10: Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Test 10: Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF; 
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Test 10: Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'Test 10: A P2003 error, with correct SQLERRM, should have been raised due to invalid payment terms';

    RAISE NOTICE 'TEST 10 PASSED: Error handling works correctly when the passed payment terms are invalid';

END $$;

-- Cleanup test data
DO $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';
    
    -- Delete all test accounts created by these tests
    DELETE FROM payment_terms WHERE defendant_account_id = 999901;
    DELETE FROM defendant_accounts WHERE defendant_account_id = 999901;
    DELETE FROM business_units WHERE business_unit_id = 9999;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing