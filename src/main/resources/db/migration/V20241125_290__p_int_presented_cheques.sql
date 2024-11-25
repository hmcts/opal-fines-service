CREATE OR REPLACE PROCEDURE p_int_presented_cheques(
    IN pi_interface_job_id interface_jobs.interface_job_id%TYPE)
AS $$
/**
* OPAL Program
*
* MODULE      : p_int_presented_cheques.sql
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
    k_valid_transaction_code varchar := '11';
    k_msg_type_info interface_messages.message_type%TYPE := 'Info';
    k_msg_type_warn interface_messages.message_type%TYPE := 'Warning';
    k_msg_type_exc interface_messages.message_type%TYPE := 'Exception';
    k_summary_presented text := 'Cheques presented: ';
    k_summary_not_presented text := 'Cheques not presented: ';
    k_summary_ignored text := 'Cheque records ignored: ';
    k_summary_value text := ', value: '||chr(163);
	k_status_cheque_awaiting_deletion text := 'X';
	k_status_cheque_destroyed text := 'D';
	k_status_cheque_withdrawn text := 'W';
	k_status_cheque_presented text := 'P';
	k_status_cheque_query text := 'Q';
	k_status_cheque_cleared text := 'C';
	k_updated_cheque_status text := null;
	k_msg_bank interface_messages.message_text%TYPE := 'Unknown bank details';
	k_msg_cheque_not_found interface_messages.message_text%TYPE := 'Cheque not found';
	k_msg_cheque_destroyed interface_messages.message_text%TYPE := 'Cheque destroyed';
	k_msg_cheque_withdrawn interface_messages.message_text%TYPE := 'Cheque withdrawn';
	k_msg_cheque_presented interface_messages.message_text%TYPE := 'Cheque already presented';
	k_msg_cheque_amount_mismatch interface_messages.message_text%TYPE := 'Cheque amount mismatch'; 
    r_file_record record;
	r_cheque record;
    v_msg_text interface_messages.message_text%TYPE;
    v_msg_type interface_messages.message_type%TYPE;
    v_record_detail interface_messages.record_detail%TYPE;
    v_count_presented integer := 0;
    v_count_not_presented integer := 0;
	v_count_ignored integer := 0;
    v_total_presented integer := 0;
    v_total_not_presented integer := 0;
BEGIN
    FOR r_file_record IN (
         SELECT             rec.rec_index,
                            j.business_unit_id,
                            f.interface_file_id,
                            CASE WHEN ba->>'account_number' IS NOT NULL THEN true ELSE false END AS is_bu_bank,
                            rec.obj->>'receiving_sort_code' AS receiving_sort_code,
                            rec.obj->>'receiving_bank_account_number' AS receiving_bank_account_number,
                            rec.obj->>'transaction_code' AS transaction_code,
                            (rec.obj->>'amount_pence')::bigint AS amount_pence,
							(rec.obj->>'cheque_number')::bigint AS cheque_number,
                            rec.obj->>'entry_date' AS entry_date
        FROM                interface_jobs j
        INNER JOIN          interface_files f ON f.interface_job_id = j.interface_job_id
        INNER JOIN          configuration_items ci ON ci.business_unit_id = j.business_unit_id AND ci.item_name = 'BANKLIST'
        CROSS JOIN LATERAL  json_array_elements(f.records) WITH ORDINALITY AS rec(obj, rec_index)
        LEFT JOIN LATERAL   json_array_elements(ci.item_values) AS ba
                                ON ba->>'account_number' = rec.obj->>'receiving_bank_account_number'
                                AND ba->>'sort_code' = rec.obj->>'receiving_sort_code'
        WHERE               j.interface_job_id = pi_interface_job_id
        ORDER BY            rec.rec_index)
    LOOP
	    v_msg_text := NULL;
	    v_msg_type := NULL;
        IF r_file_record.transaction_code = k_valid_transaction_code AND r_file_record.amount_pence > 0 THEN
            v_record_detail := 'Cheque number: ' || r_file_record.cheque_number::text
				|| ', Value: ' ||chr(163) || round((r_file_record.amount_pence/100.00),2)::text
				|| ', Date: ' || r_file_record.entry_date;
            IF NOT r_file_record.is_bu_bank THEN
                -- rejected records
				v_msg_text := k_msg_bank;
				v_msg_type := k_msg_type_exc;
				v_count_not_presented := v_count_not_presented +1;
				v_total_not_presented := v_total_not_presented + r_file_record.amount_pence;
            ELSE				
                -- Read Cheque and check status
				SELECT  cheque_id, creditor_transaction_id, amount, status
				INTO    r_cheque
				FROM    cheques 
				WHERE   cheque_number = r_file_record.cheque_number AND
					    business_unit_id = r_file_record.business_unit_id;
				-- check the Status of the cheque
				IF r_cheque.status IS NULL OR r_cheque.status = k_status_cheque_awaiting_deletion THEN
					v_msg_type := k_msg_type_exc;
					v_msg_text := k_msg_cheque_not_found;
				ELSIF r_cheque.status = k_status_cheque_destroyed THEN
					v_msg_type := k_msg_type_exc;
					v_msg_text := k_msg_cheque_destroyed;
				ELSIF r_cheque.status = k_status_cheque_withdrawn THEN
					v_msg_type := k_msg_type_exc;
					v_msg_text := k_msg_cheque_withdrawn;
				ELSIF r_cheque.status = k_status_cheque_presented THEN
					v_msg_type := k_msg_type_exc;
					v_msg_text := k_msg_cheque_presented;
				ELSIF round((r_file_record.amount_pence/100.00),2) != round((r_cheque.amount/100.00),2) THEN
					v_msg_type := k_msg_type_warn;
					v_msg_text := k_msg_cheque_amount_mismatch;
				END IF;
				-- update cheque/creditor_transaction (if no exception)
				IF v_msg_type IS NULL OR v_msg_type != k_msg_type_exc THEN
					-- update cheque status
					r_cheque.status = CASE WHEN v_msg_text IS NULL THEN 
							k_status_cheque_presented ELSE k_status_cheque_query END;
					CALL p_update_cheque_status(r_cheque.cheque_id, r_cheque.status);
					-- Only when cheque is cleared (status c) update the creditor transaction
					IF r_cheque.status = k_status_cheque_presented THEN
						CALL p_update_creditor_transaction_status (r_cheque.creditor_transaction_id, k_status_cheque_cleared);
					END IF;
				END IF;
				--Update counts/totals
				IF v_msg_text IS NULL THEN
					v_count_presented := v_count_presented +1;
					v_total_presented := v_total_presented + r_file_record.amount_pence;
				ELSE
					v_count_not_presented := v_count_not_presented +1;
					v_total_not_presented := v_total_not_presented + r_file_record.amount_pence;
				END IF;
            END IF;
            IF v_msg_text IS NOT NULL THEN
                CALL p_insert_interface_message(pi_interface_job_id, v_msg_type, v_msg_text, r_file_record.interface_file_id, r_file_record.rec_index, v_record_detail);
            END IF;
        ELSE
            v_count_ignored := v_count_ignored +1;
        END IF;
    END LOOP;
    -- job summary messages
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_presented||v_count_presented::text||k_summary_value||round(v_total_presented/100.00,2)::text);
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_not_presented||v_count_not_presented::text||k_summary_value||round(v_total_not_presented/100.00,2)::text);
    IF v_count_ignored > 0 THEN
        CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_ignored||v_count_ignored::text);
    END IF;
END;
$$ LANGUAGE plpgsql;
