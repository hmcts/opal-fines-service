SET SCHEMA 'public';
/* 
 * Version: 2.0 - Column ACCOUNT_NUMBER_INDEX.ACCOUNT_INDEX_TYPE renamed to ASSOCIATED_RECORD_TYPE
 * 
 * 
 * Test 1 - Reset sequence when year changes. 
 *          Test data setup: Max account number, for the business unit, is in previous year  (e.g. 24000111W)
 *          Expected result: Account number generated is in current year with minimum value (i.e. 000001)  (e.g. 25000001E)
 *          pi_business_unit_id = 5
 * 
 * Test 2 - Check account number sequence increments correctly in the natural sequence
 *          Test data setup: Max account number, for the business unit, is in current year and NOT at max value (i.e. 999999)  (e.g. 25000010H)
 *          Expected result: Account number generated is next in the sequence (e.g. 25000011D)
 *          pi_business_unit_id = 8
 * 
 * Test 3 - Check gaps are used when account number is at MAX value and there are gaps, but MIN value is NOT available.
 *          Test data setup: Max account number, for the business unit, is in current year, at MAX value (i.e. 999999), MIN value is NOT available but has gaps (e.g. 25999999I)
 *          Expected result: Account number generated is for a gap found (e.g. 25000003T)
 *          pi_business_unit_id = 9
 * 
 * Test 4 - Check gaps are used when account number is at MAX value and there are gaps, and MIN value IS available.
 *          Test data setup: Max account number, for the business unit, is in current year, at MAX value (i.e. 999999), MIN value IS available (e.g. 25999999I)
 *          Expected result: Account number generated is for a gap found and is the MIN value (e.g. 25000001E)
 *          pi_business_unit_id = 10
 * 
 * Test 5 - Check account number is generated correctly when the sequence is already in next year's range.
 *          Test data setup: max account number is in next years range (e.g. 26000001D)
 *          Expected result: Account number generated is next in the sequence (e.g. 26000002W)
 *          pi_business_unit_id = 11
 * 
 * Test 6 - Check account number correctly rolls over to the next years range.  This also tests f_get_check_number
 *          Test data setup: max account number is in current years range, at MAX value and has NO gaps (e.g. 25999999I)
 *          Expected result: Account number generated is the first in next years range (e.g. 26000001D)
 *          pi_business_unit_id = 12
 * 
 * Test 7 - Break test - Non UNIQUE_VIOLATION exception - Business unit does NOT exist
 *          Expected result: Exception is raised. It does NOT retry
 *          pi_business_unit_id = 999
 * 
 */

BEGIN;

DO $$
DECLARE
    v_account_number    account_number_index.account_number%TYPE;
    v_curr_yy           VARCHAR := TO_CHAR(NOW(), 'YY');
    v_prev_yy           VARCHAR := (v_curr_yy::int - 1)::VARCHAR;
    v_next_yy           VARCHAR := (v_curr_yy::int + 1)::VARCHAR;
    v_i                 INTEGER  := 1;

    --BU for each test
    v_bu_test1          draft_accounts.business_unit_id%TYPE := 5;
    v_bu_test2          draft_accounts.business_unit_id%TYPE := 8;
    v_bu_test3          draft_accounts.business_unit_id%TYPE := 9;
    v_bu_test4          draft_accounts.business_unit_id%TYPE := 10;
    v_bu_test5          draft_accounts.business_unit_id%TYPE := 11;
    v_bu_test6          draft_accounts.business_unit_id%TYPE := 12;
    v_bu_test7          draft_accounts.business_unit_id%TYPE := 999;  --Not a valid BU

    --Expected results - without check number suffix
    v_expected_result_test1     VARCHAR := v_curr_yy || '000001';
    v_expected_result_test2     VARCHAR := v_curr_yy || '000011';
    v_expected_result_test3     VARCHAR := v_curr_yy || '000003';
    v_expected_result_test4     VARCHAR := v_curr_yy || '000001';
    v_expected_result_test5     VARCHAR := v_next_yy || '000002';
    v_expected_result_test6     VARCHAR := v_next_yy || '000001';
    v_expected_account_number   account_number_index.account_number%TYPE;
BEGIN
    RAISE INFO 'Current year  = %', v_curr_yy;    
    RAISE INFO 'Previous year = %', v_prev_yy;
    RAISE INFO 'Next year     = %', v_next_yy;

    --Setup test data
    RAISE INFO 'Truncating table account_number_index...'; 
    TRUNCATE TABLE account_number_index RESTART IDENTITY;

    RAISE INFO 'Inserting test data into table account_number_index...'; 
    --Data for tests 1 - 5
    INSERT INTO account_number_index (account_number_index_id, business_unit_id, account_number, associated_record_type)
    VALUES (NEXTVAL('account_number_index_seq'), v_bu_test1, v_prev_yy || '000001F', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test1, v_prev_yy || '000002B', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test2, v_curr_yy || '000009S', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test2, v_curr_yy || '000010H', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test3, v_curr_yy || '000001E', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test3, v_curr_yy || '000002A', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test3, v_curr_yy || '000005L', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test3, v_curr_yy || '999999I', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test4, v_curr_yy || '000002A', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test4, v_curr_yy || '000005L', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test4, v_curr_yy || '999999I', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test5, v_curr_yy || '999999I', 'defendant_accounts')
         , (NEXTVAL('account_number_index_seq'), v_bu_test5, v_next_yy || '000001D', 'defendant_accounts')
    ;
    
    --Data for test 6
    FOR v_i IN 1..999999 LOOP
        v_account_number := v_curr_yy || LPAD(v_i::VARCHAR, 6, '0');
        v_account_number := v_account_number || f_get_check_letter(v_account_number);
    
        INSERT INTO public.account_number_index (account_number_index_id, business_unit_id, account_number, associated_record_type)
            VALUES (NEXTVAL('account_number_index_seq'), v_bu_test6, v_account_number, 'creditor_accounts');
    END LOOP;

    RAISE INFO 'Running tests...';

    --Test 1 - Reset sequence when year changes. 
    BEGIN
        RAISE INFO '---------- TEST 1 ----------';

        v_account_number := f_get_account_number(v_bu_test1, 'defendant_accounts');

        RAISE INFO 'Test 1 - Returned account_number = %', v_account_number;
        v_expected_account_number := v_expected_result_test1 || f_get_check_letter(v_expected_result_test1);

        IF v_account_number = v_expected_account_number THEN
            RAISE INFO 'Test 1 - Passed';
        ELSE
            RAISE WARNING 'Test 1 - Failed';
        END IF;

    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Test 1 - Failed [Error: %]', SQLERRM;
    END;
    
    --Test 2 - Check account number sequence increments correctly in the natural sequence 
    BEGIN
        RAISE INFO '---------- TEST 2 ----------';

        v_account_number := f_get_account_number(v_bu_test2, 'defendant_accounts');

        RAISE INFO 'Test 2 - Returned account_number = %', v_account_number;
        v_expected_account_number := v_expected_result_test2 || f_get_check_letter(v_expected_result_test2);

        IF v_account_number = v_expected_account_number THEN
            RAISE INFO 'Test 2 - Passed';
        ELSE
            RAISE WARNING 'Test 2 - Failed';
        END IF;

    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Test 2 - Failed [Error: %]', SQLERRM;
    END;

    --Test 3 - Check gaps are used when account number is at MAX value and there are gaps, but MIN value is NOT available.
    BEGIN
        RAISE INFO '---------- TEST 3 ----------';

        v_account_number := f_get_account_number(v_bu_test3, 'defendant_accounts');

        RAISE INFO 'Test 3 - Returned account_number = %', v_account_number;
        v_expected_account_number := v_expected_result_test3 || f_get_check_letter(v_expected_result_test3);

        IF v_account_number = v_expected_account_number THEN
            RAISE INFO 'Test 3 - Passed';
        ELSE
            RAISE WARNING 'Test 3 - Failed';
        END IF;

    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Test 3 - Failed [Error: %]', SQLERRM;
    END;

    --Test 4 - Check gaps are used when account number is at MAX value and there are gaps, and MIN value IS available.
    BEGIN
        RAISE INFO '---------- TEST 4 ----------';

        v_account_number := f_get_account_number(v_bu_test4, 'defendant_accounts');

        RAISE INFO 'Test 4 - Returned account_number = %', v_account_number;
        v_expected_account_number := v_expected_result_test4 || f_get_check_letter(v_expected_result_test4);

        IF v_account_number = v_expected_account_number THEN
            RAISE INFO 'Test 4 - Passed';
        ELSE
            RAISE WARNING 'Test 4 - Failed';
        END IF;

    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Test 4 - Failed [Error: %]', SQLERRM;
    END;

    --Test 5 - Check account number is generated correctly when the sequence is already in next year's range.
    BEGIN
        RAISE INFO '---------- TEST 5 ----------';

        v_account_number := f_get_account_number(v_bu_test5, 'defendant_accounts');

        RAISE INFO 'Test 5 - Returned account_number = %', v_account_number;
        v_expected_account_number := v_expected_result_test5 || f_get_check_letter(v_expected_result_test5);

        IF v_account_number = v_expected_account_number THEN
            RAISE INFO 'Test 5 - Passed';
        ELSE
            RAISE WARNING 'Test 5 - Failed';
        END IF;

    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Test 5 - Failed [Error: %]', SQLERRM;
    END;

    --Test 6 - Check account number correctly rolls over to the next years range.  This also tests f_get_check_number
    BEGIN
        RAISE INFO '---------- TEST 6 ----------';

        v_account_number := f_get_account_number(v_bu_test6, 'defendant_accounts');

        RAISE INFO 'Test 6 - Returned account_number = %', v_account_number;
        v_expected_account_number := v_expected_result_test6 || f_get_check_letter(v_expected_result_test6);

        IF v_account_number = v_expected_account_number THEN
            RAISE INFO 'Test 6 - Passed';
        ELSE
            RAISE WARNING 'Test 6 - Failed';
        END IF;

    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Test 6 - Failed [Error: %]', SQLERRM;
    END;

    --Test 7 - Break test - Non UNIQUE_VIOLATION exception - Business unit does NOT exist
    BEGIN
        RAISE INFO '---------- TEST 7 ----------';

        v_account_number := f_get_account_number(v_bu_test7, 'defendant_accounts');
        
        --Shouldn't get here
        RAISE WARNING 'Test 7 - Failed [Returned account_number = %]', v_account_number;

    EXCEPTION WHEN OTHERS THEN
        RAISE INFO 'Test 7 - Passed [Error: %]', SQLERRM;
    END;
END $$;

END;  --End and Commit transaction
--ROLLBACK;
