CREATE OR REPLACE PROCEDURE p_int_payment_card_requests(
    IN pi_interface_job_id interface_jobs.interface_job_id%TYPE)
AS $$
/**
* OPAL Program
*
* MODULE      : p_int_payment_card_requests.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
DECLARE
    k_interface_name text := 'PAYMENT_CARD_REQUESTS';
	k_char_exclusion_range text := '[^\u0020-\u00FF]';
    k_excluded_chars text := '[%()*+<=>?\[\]{}]';
    v_err_context text;
    v_json_data json;
    v_interface_file_id interface_files.interface_file_id%TYPE;
	v_file_name text;
	v_defendant_account_list bigint[] := '{}';
BEGIN
    SELECT  array_to_json(
			    array_agg(
				    json_build_object(
                        'business_unit_id', da.business_unit_id,
                        'account', da.account_number,
                        'debtor_title_and_initials', regexp_replace(regexp_replace(case when p.title is not null then TRIM(p.title || ' ' || p.initials) else null end,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'debtor_name', regexp_replace(regexp_replace(substr(case when p.organisation then p.organisation_name else p.surname end,1,19),k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'name_on_card', regexp_replace(regexp_replace(substr(case when p.title is not null then TRIM(p.title || ' ' || p.initials) else '' end || ' ' || case when p.organisation then p.organisation_name else p.surname end,1,27),k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'address_line_1', regexp_replace(regexp_replace(p.address_line_1,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'address_line_2', regexp_replace(regexp_replace(p.address_line_2,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'address_line_3', regexp_replace(regexp_replace(p.address_line_3,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'address_line_4', regexp_replace(regexp_replace(p.address_line_4,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'postcode', p.postcode
                    )
                )
		    ) AS json_array,
	        array_agg(pcr.defendant_account_id)
    INTO    v_json_data, v_defendant_account_list 
    FROM    payment_card_requests pcr
    JOIN    defendant_account_parties dap ON dap.defendant_account_id = pcr.defendant_account_id
    JOIN    parties p ON p.party_id = dap.party_id
	JOIN    defendant_accounts da ON da.defendant_account_id = dap.defendant_account_id
	WHERE   dap.debtor = true;
	IF v_json_data IS NOT NULL THEN
        v_interface_file_id := nextval('interface_file_id_seq');
        SELECT  'PCR_' || TO_CHAR(CURRENT_DATE, 'DDMMYYYY') || '_' || TO_CHAR(COUNT(1), 'FM000')
        INTO    v_file_name
        FROM    interface_files if
        JOIN    interface_jobs ij ON ij.interface_job_id = if.interface_job_id
        WHERE   if.created_datetime::date = CURRENT_DATE
        AND     ij.interface_name = k_interface_name;
        INSERT INTO interface_files (interface_file_id, interface_job_id, file_name, created_datetime, records)
        VALUES      (v_interface_file_id, pi_interface_job_id, v_file_name , CURRENT_TIMESTAMP, v_json_data);
        DELETE FROM payment_card_requests 
        WHERE       defendant_account_id = ANY(v_defendant_account_list);
    END IF;
END;
$$ LANGUAGE plpgsql;
