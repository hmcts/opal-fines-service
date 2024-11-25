--Test 1.  Run interface for all accounts and check format of JSON

DO $$
BEGIN
	delete from public.payment_card_requests;
	insert into public.payment_card_requests
	select defendant_account_id from public.defendant_accounts;
    CALL public.p_run_interface_job('PAYMENT_CARD_REQUESTS',4::smallint);

END
$$;


SELECT * 
  FROM interface_jobs ij1
 WHERE ij1.interface_name = 'PAYMENT_CARD_REQUESTS'
   AND ij1.interface_job_id = (
                               SELECT max(ij2.interface_job_id)
							     FROM interface_jobs ij2
                                WHERE ij2.interface_name = ij1.interface_name
                              );

SELECT *
  FROM interface_files if
 WHERE if.interface_job_id = (
                               SELECT max(ij2.interface_job_id)
							     FROM interface_jobs ij2
                                WHERE ij2.interface_name = 'PAYMENT_CARD_REQUESTS'
                              );

--Test 2. Verify that the payment_card_requests table is empty after running the interface.

SELECT count(*) FROM payment_card_requests;


--Test 3. Verify that special characters are removed from a string. 
--        Apply following SQL and the rerun test1
	update parties
	set surname = 'TestName1%()*+<=>?[]{}Ā'
	WHERE party_id = 10600000001596;

--Test 4. Verify that a company name appears in the JSON. 
--        Apply following SQL and the rerun test1

	update parties
	set surname = NULL, title = null,
	    organisation_name = 'Bob Wright Extreme Tours Ltd',
		organisation = true
	WHERE party_id = 11060000004428;

