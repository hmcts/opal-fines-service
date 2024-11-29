DO $$
DECLARE
test_result integer := 0;

BEGIN
	--Script to test Presented Cheques interface
	-- clear down jobs
	DELETE FROM interface_messages; 
	DELETE FROM interface_files;
	DELETE FROM interface_jobs;
	

	--cheques / creditor_transactions / defendant_accounts
	DELETE FROM cheques WHERE cheque_id < 100000;
	DELETE FROM creditor_transactions WHERE creditor_transaction_id < 1000000;


	-- Cheque 1 (should get status P)
	INSERT INTO public.creditor_transactions(
		creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_user_id, transaction_type, transaction_amount, imposition_result_id, 
			payment_processed, payment_reference, status, status_date, associated_record_type, associated_record_id)
		VALUES (1000, 20040000000001, (CURRENT_TIMESTAMP-INTERVAL '60 days'), 'DRJ', null, 'C', 5000, 1, null, 976, null, (CURRENT_TIMESTAMP+INTERVAL '60 days'), null, null);
	INSERT INTO public.cheques(
		cheque_id, business_unit_id, cheque_number, issue_date, creditor_transaction_id, defendant_transaction_id, amount, allocation_type, reminder_date, status)
		VALUES (01, 4, 8888, (CURRENT_TIMESTAMP-INTERVAL '60 days'), 1000, null, 5000, 'A', (CURRENT_TIMESTAMP+INTERVAL '60 days'), 'U');
	

	-- Cheque 2(Query amount)
	INSERT INTO public.creditor_transactions(
		creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_user_id, transaction_type, transaction_amount, imposition_result_id, 
			payment_processed, payment_reference, status, status_date, associated_record_type, associated_record_id)
		VALUES (1050, 20040000000370, (CURRENT_TIMESTAMP-INTERVAL '60 days'), 'DRJ2', null, 'C', 1050, 1, null, 976, null, (CURRENT_TIMESTAMP+INTERVAL '60 days'), null, null);
	INSERT INTO public.cheques(
		cheque_id, business_unit_id, cheque_number, issue_date, creditor_transaction_id, defendant_transaction_id, amount, allocation_type, reminder_date, status)
		VALUES (02, 4, 7777, (CURRENT_TIMESTAMP-INTERVAL '60 days'), 1050, null, 1050, 'A', (CURRENT_TIMESTAMP+INTERVAL '60 days'), 'U');

-- Cheque 3 (is not entered to test Cheque not found)

-- Cheque 4 (awaiting deletion)
	INSERT INTO public.cheques(
		cheque_id, business_unit_id, cheque_number, issue_date, creditor_transaction_id, defendant_transaction_id, amount, allocation_type, reminder_date, status)
		VALUES (04, 4, 5555, (CURRENT_TIMESTAMP-INTERVAL '60 days'), 20040000000052, null, 300, 'A', (CURRENT_TIMESTAMP+INTERVAL '60 days'), 'X');
-- Cheque 5 (cheque destroyed)
	INSERT INTO public.cheques(
		cheque_id, business_unit_id, cheque_number, issue_date, creditor_transaction_id, defendant_transaction_id, amount, allocation_type, reminder_date, status)
		VALUES (05, 4, 4444, (CURRENT_TIMESTAMP-INTERVAL '60 days'), 20040000002535, null, 300, 'A', (CURRENT_TIMESTAMP+INTERVAL '60 days'), 'D');
-- Cheque 6 (cheque withdrawn)
	INSERT INTO public.cheques(
		cheque_id, business_unit_id, cheque_number, issue_date, creditor_transaction_id, defendant_transaction_id, amount, allocation_type, reminder_date, status)
		VALUES (06, 4, 3333, (CURRENT_TIMESTAMP-INTERVAL '60 days'), 20040000002499, null, 300, 'A', (CURRENT_TIMESTAMP+INTERVAL '60 days'), 'W');
-- Cheque 7 (cheque presented)
	INSERT INTO public.cheques(
		cheque_id, business_unit_id, cheque_number, issue_date, creditor_transaction_id, defendant_transaction_id, amount, allocation_type, reminder_date, status)
		VALUES (07, 4, 2222, (CURRENT_TIMESTAMP-INTERVAL '60 days'), 20040000002466, null, 300, 'A', (CURRENT_TIMESTAMP+INTERVAL '60 days'), 'P');							
	-- jobs
	INSERT INTO interface_jobs(interface_job_id,business_unit_id,interface_name)
		VALUES      (99,4,'presented_cheques');
	INSERT INTO interface_files(interface_file_id, interface_job_id,file_name,records)
		VALUES      (66, 99,'test-presented-cheques',
		'[{"account_number":"24000001A","receiving_sort_code":"27049027","receiving_bank_account_number":"27049027","cheque_number":"8888","transaction_code":"11","amount_pence":"5000","entry_date":"311024","originator_reference":"24000001A"},
		  {"account_number":"24000002A","receiving_sort_code":"27049027","receiving_bank_account_number":"27049027","cheque_number":"7777","transaction_code":"11","amount_pence":"1000","entry_date":"311024","originator_reference":"24000002A"},
		  {"account_number":"24000003A","receiving_sort_code":"27049027","receiving_bank_account_number":"27049027","cheque_number":"6666","transaction_code":"11","amount_pence":"6666","entry_date":"311024","originator_reference":"24000003A"},
		  {"account_number":"24000004A","receiving_sort_code":"27049027","receiving_bank_account_number":"27049027","cheque_number":"5555","transaction_code":"11","amount_pence":"5555","entry_date":"311024","originator_reference":"24000004A"},
		  {"account_number":"24000005A","receiving_sort_code":"27049027","receiving_bank_account_number":"27049027","cheque_number":"4444","transaction_code":"11","amount_pence":"4444","entry_date":"311024","originator_reference":"24000005A"},
		  {"account_number":"24000006A","receiving_sort_code":"27049027","receiving_bank_account_number":"27049027","cheque_number":"3333","transaction_code":"11","amount_pence":"3333","entry_date":"311024","originator_reference":"24000006A"},
		  {"account_number":"24000007A","receiving_sort_code":"27049027","receiving_bank_account_number":"27049027","cheque_number":"2222","transaction_code":"11","amount_pence":"3333","entry_date":"311024","originator_reference":"24000007A"}]');
		RAISE NOTICE 'Data setup. Beginning job...';
		
		-- Now run all the cheques via the sp and check the results 
		--call p_run_interface_job('presented_cheques'::text, 4::smallint); -- Job Id , Business Unit
		call p_int_presented_cheques(99);

-- Check the results
	BEGIN
		
		-- Test 1 - Good cheque (should be presented)
		test_result  := (select count(*) from creditor_transactions ct 
			left join cheques c on ct.creditor_transaction_id = c.creditor_transaction_Id
			where ct.creditor_transaction_id = 1000 and ct.status = 'C' and c.status = 'P')::integer ;
		RAISE NOTICE 'Test 1: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;
		
		--Test 2 - Cheque not presented - should mark Cheque as Query and leave Creditor_Transaction status null 
		test_result  := (select count(*)  from creditor_transactions ct 
			left join cheques c on ct.creditor_transaction_id = c.creditor_transaction_Id
			where ct.creditor_transaction_id = 1050 and ct.status is null and c.status = 'Q')::integer ;
		RAISE NOTICE 'Test 2: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;
				
		--Test 3 - Cheque not found - cheque number doesn't exist in DB and interface message written
		test_result  := (select count(*)  from interface_Messages 
			where message_text = 'Cheque not found' AND record_detail like '%6666%' and
			0 = (select count(*) from cheques where cheque_number = 6666))::integer ;
		RAISE NOTICE 'Test 3: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;
		
		--Test 4 - Cheque awaiting deletion (not processed - status remains X)
		test_result  := (select count(*) from cheques where cheque_number = 5555 and status = 'X')::integer ;
		RAISE NOTICE 'Test 4: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;

		-- Test 5 - Cheque marked for deletion not processed - marked in Interface Messages
		test_result  := (select count(*) from interface_messages where message_text = 'Cheque not found' AND record_detail like '%5555%')::integer ;
		RAISE NOTICE 'Test 5: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;
	
		--Test 6 - Cheque destroyed (not processed - status remains D)
		test_result  := (select count(*) from cheques where cheque_number = 4444 and status = 'D')::integer ;
		RAISE NOTICE 'Test 6: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;

		-- Test 7 - Cheque destroyed not processed - marked in Interface Messages
		test_result  := (select count(*) from interface_messages where message_text = 'Cheque destroyed' AND record_detail like '%4444%')::integer ;
		RAISE NOTICE 'Test 7: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;
		
		--Test 8 - Cheque widthdrawn (not processed - status remains W)
		test_result  := (select count(*) from cheques where cheque_number = 3333 and status = 'W')::integer ;
		RAISE NOTICE 'Test 8: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;

		-- Test 9 - Cheque withdrawn not processed - marked in Interface Messages
		test_result  := (select count(*) from interface_messages where message_text = 'Cheque withdrawn' AND record_detail like '%3333%')::integer ;
		RAISE NOTICE 'Test 9: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;
		
		--Test 10 - Cheque already presented (not processed - status remains P)
		test_result  := (select count(*) from cheques where cheque_number = 2222 and status = 'P')::integer ;
		RAISE NOTICE 'Test 10: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;

		-- Test 11 - Cheque already presented -  not processed - marked in Interface Messages
		test_result  := (select count(*) from interface_messages where message_text = 'Cheque already presented' AND record_detail like '%2222%')::integer ;
		RAISE NOTICE 'Test 11: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;
		
		--Test 12 - Check the total presented interface message is correct
		test_result  := (select count(*) from interface_messages where message_text = 'Cheques presented: 1, value: £50.00')::integer ;
		RAISE NOTICE 'Test 12: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;
		
		--Test 13 - Check the total not presented interface message is correct
		test_result  := (select count(*) from interface_messages where message_text = 'Cheques not presented: 6, value: £243.31')::integer ;
		RAISE NOTICE 'Test 13: %', CASE WHEN test_result = 1 THEN 'Passed' ELSE 'Failed' END;
	END;
END;
$$;