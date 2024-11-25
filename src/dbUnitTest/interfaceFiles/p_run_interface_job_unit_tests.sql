DO $$
DECLARE
    v_buid smallint := 4;
    v_jobs_aft_pc bigint;
    v_jobs_aft_pi bigint;
    v_jobs_aft_pcr bigint;
    v_jobs_bef_pc bigint;
    v_jobs_bef_pi bigint;
    v_jobs_bef_pcr bigint;
    v_days integer;
    v_sp text;
    v_test_status text;
BEGIN
    /* run interfaces */
    /* run payment card requests */
    SELECT  COUNT(*)
    INTO    v_jobs_bef_pcr
    FROM    interface_jobs
    WHERE   interface_name = 'PAYMENT_CARD_REQUESTS' AND 
            status IN ('Completed','Failed') AND
            completed_datetime < (CURRENT_DATE - 100::integer);
    DELETE FROM payment_card_requests;
    INSERT INTO payment_card_requests SELECT MAX(defendant_account_id) FROM defendant_accounts;
    CALL p_run_interface_job('PAYMENT_CARD_REQUESTS');
    SELECT  COUNT(*)
    INTO    v_jobs_aft_pcr
    FROM    interface_jobs
    WHERE   interface_name = 'PAYMENT_CARD_REQUESTS' AND 
            status IN ('Completed','Failed') AND
            completed_datetime < (CURRENT_DATE - 100::integer);
    /* run presented cheques */
    SELECT  COUNT(*)
    INTO    v_jobs_bef_pc
    FROM    interface_jobs
    WHERE   interface_name = 'PRESENTED_CHEQUES' AND 
            status IN ('Completed','Failed') AND
            completed_datetime < (CURRENT_DATE - 100::integer);    
    CALL p_run_interface_job('PRESENTED_CHEQUES',v_buid);
    SELECT  COUNT(*)
    INTO    v_jobs_aft_pc
    FROM    interface_jobs
    WHERE   interface_name = 'PRESENTED_CHEQUES' AND 
            status IN ('Completed','Failed') AND
            completed_datetime < (CURRENT_DATE - 100::integer);
    /* run payments in */
    SELECT  COUNT(*)
    INTO    v_jobs_bef_pi
    FROM    interface_jobs
    WHERE   interface_name = 'PAYMENTS_IN' AND 
            status IN ('Completed','Failed') AND
            completed_datetime < (CURRENT_DATE - 100::integer);
    CALL p_run_interface_job('PAYMENTS_IN',v_buid);
    SELECT  COUNT(*)
    INTO    v_jobs_aft_pi
    FROM    interface_jobs
    WHERE   interface_name = 'PAYMENTS_IN' AND 
            status IN ('Completed','Failed') AND
            completed_datetime < (CURRENT_DATE - 100::integer);
    -- Test 1
    -- To verify that the specified interface is run for all business units (payment card requests only)
    SELECT  'Passed'
    INTO    v_test_status
    FROM    interface_files; -- if pcr ran it would have created a file as we had a request
    RAISE NOTICE 'Test 1: %',COALESCE(v_test_status,'Failed');
    -- Test 2
    -- To verify that the specified interface is run for the specified business units (presented cheques and payments in only)
    SELECT  'Passed'
    INTO v_test_status
    WHERE   (
        SELECT COUNT(*)
        FROM interface_jobs
        WHERE interface_name IN ('PRESENTED_CHEQUES','PAYMENTS_IN')
            AND business_unit_id = 4
        ) = 2;
    RAISE NOTICE 'Test 2: %',COALESCE(v_test_status,'Failed');
    -- Test 3
    -- To verify that an interface job is created when running an outbound interface (payment card requests only)
    SELECT  'Passed'
    INTO v_test_status
    WHERE   (
        SELECT COUNT(*)
        FROM interface_jobs
        WHERE interface_name = 'PAYMENT_CARD_REQUESTS'
        ) = 4; -- there was 5 before this test (but 2 would have been deleted)
    RAISE NOTICE 'Test 3: %',COALESCE(v_test_status,'Failed');
    -- Test 4
    -- To verify that an outbound job status is set to Written on completion when an interface file is created
    SELECT  'Passed'
    INTO v_test_status
    WHERE   (
        SELECT COUNT(*)
        FROM interface_jobs
        WHERE interface_name = 'PAYMENT_CARD_REQUESTS'
            AND status = 'Written'
        ) = 2; -- there was 1 before this test
    RAISE NOTICE 'Test 4: %',COALESCE(v_test_status,'Failed');
    -- Test 5
    -- To verify that an outbound job status is set to No data on completion when if no interface file was created
    -- run interface job again - there should be nothing to process
    CALL p_run_interface_job('PAYMENT_CARD_REQUESTS');
    SELECT  'Passed'
    INTO v_test_status
    WHERE   (
        SELECT COUNT(*)
        FROM interface_jobs
        WHERE interface_name = 'PAYMENT_CARD_REQUESTS'
            AND status = 'No data'
        ) = 1;
    RAISE NOTICE 'Test 5: %',COALESCE(v_test_status,'Failed');
    -- Test 6
    -- To verify that an inbound job status is set to Completed on completion
    SELECT  'Passed'
    INTO v_test_status
    WHERE   (
        SELECT COUNT(*)
        FROM interface_jobs
        WHERE interface_name IN ('PRESENTED_CHEQUES','PAYMENTS_IN')
            AND business_unit_id = 4
            AND status = 'Completed'
        ) = 2; -- there were none for BU 4 before
    RAISE NOTICE 'Test 6: %',COALESCE(v_test_status,'Failed');
    -- Test 7
    -- To verify that old completed or failed jobs for the specified interface are deleted
    SELECT  'Passed'
    INTO v_test_status
    WHERE (
        v_jobs_bef_pcr = 2 AND v_jobs_aft_pcr = 0 AND
        v_jobs_bef_pc = 2 AND v_jobs_aft_pc = 0 AND
        v_jobs_bef_pi = 4 AND v_jobs_aft_pi = 0
    );
    RAISE NOTICE 'Test 7: %', COALESCE(v_test_status,'Failed');
    -- Test 8
    -- To verify that an Error type interface message is created and status set to failed when a job fails
    RAISE NOTICE 'Test 8: Must be run manually';
END $$;

/*
-- Test 8
-- To verify that an Error type interface message is created and status set to failed when a job fails

STEP 1:
DROP PROCEDURE p_int_payment_card_requests(bigint);

STEP2:
CALL p_run_interface_job('PAYMENT_CARD_REQUESTS');

STEP3: (result)
SELECT 'Passed'
FROM interface_jobs j
INNER JOIN interface_messages m ON m.interface_job_id = j.interface_job_id AND message_type = 'Error'
WHERE interface_name = 'PAYMENT_CARD_REQUESTS'
    AND status = 'Failed';

STEP 4 
restore procedure p_int_payment_card_requests
*/
