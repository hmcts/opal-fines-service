CREATE OR REPLACE PROCEDURE p_int_payments_in(
    IN pi_interface_job_id interface_jobs.interface_job_id%TYPE)
AS $$
/**
* OPAL Program
*
* MODULE      : p_int_payments_in.sql
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
    k_valid_transaction_codes varchar[] := ARRAY['00','68','99'];
    k_status_cs text := 'CS';
    k_status_ta text := 'TA';
    k_status_to text := 'TO';
    k_status_ts text := 'TS';
    k_status_wo text := 'WO';
    k_msg_bank interface_messages.message_text%TYPE := 'Unknown bank details';
    k_msg_tran interface_messages.message_text%TYPE := 'Invalid transaction code';
    k_msg_nf interface_messages.message_text%TYPE := 'Account not found';
    k_msg_cs interface_messages.message_text%TYPE := 'Account consolidated';
    k_msg_ta interface_messages.message_text%TYPE := 'Account transferred out (acknowledged)';
    k_msg_to interface_messages.message_text%TYPE := 'Account transferred out';
    k_msg_ts interface_messages.message_text%TYPE := 'Account transferred out to Scotland/NI';
    k_msg_wo interface_messages.message_text%TYPE := 'Account written off';
    k_msg_bal interface_messages.message_text%TYPE := 'Account has a zero balance';
    k_msg_enf interface_messages.message_text%TYPE := 'Account last enforcement inhibits payments';
    k_msg_type_info interface_messages.message_type%TYPE := 'Info';
    k_msg_type_warn interface_messages.message_type%TYPE := 'Warning';
    k_msg_type_exc interface_messages.message_type%TYPE := 'Exception';
    k_tbl_defendant_accounts text := 'defendant_accounts';
    k_dest_fines text := 'F';
    k_dest_suspense text := 'S';
    k_alloc_unidentified text := 'UN';
    k_unidentified_ref_label text := ' - Auto Cash Input';
    k_summary_till text :=  'Till allocated: ';
    k_summary_processed text := 'Payment records processed: ';
    k_summary_accepted text := 'Payment records accepted: ';
    k_summary_fines text := 'Payment records (fines): ';
    k_summary_suspense text := 'Payment records (suspense): ';
    k_summary_rejected text := 'Payment records rejected: ';
    k_summary_ignored text := 'Payment records ignored: ';
    k_summary_value text := ', value: '||chr(163);
    r_master_account record;
    r_payment record;
    v_msg_text interface_messages.message_text%TYPE;
    v_msg_type interface_messages.message_type%TYPE;
    v_record_detail interface_messages.record_detail%TYPE;
    v_till_id tills.till_id%TYPE;
    v_till_number tills.till_number%TYPE;
    v_count_ignored bigint := 0;
    v_count_processed bigint := 0;
    v_count_rejected bigint := 0;
    v_count_accepted bigint := 0;
    v_count_fines bigint := 0;
    v_count_suspense bigint := 0;
    v_total_processed bigint := 0;
    v_total_rejected bigint := 0;
    v_total_accepted bigint := 0;
    v_total_fines bigint := 0;
    v_total_suspense bigint := 0;
BEGIN
    FOR r_payment IN (
         SELECT             rec.rec_index,
                            j.business_unit_id,
                            f.interface_file_id,
                            CASE WHEN ba->>'account_number' IS NOT NULL THEN true ELSE false END AS is_bu_bank,
                            rec.obj->>'receiving_sort_code' AS receiving_sort_code,
                            rec.obj->>'receiving_bank_account_number' AS receiving_bank_account_number,
                            rec.obj->>'receiving_account_type' AS receiving_account_type,
                            rec.obj->>'transaction_code' AS transaction_code,
                            rec.obj->>'originator_sort_code' AS originator_sort_code,
                            rec.obj->>'originator_bank_account_number' AS originator_bank_account_number,
                            (rec.obj->>'amount_pence')::bigint AS amount_pence,
                            rec.obj->>'originator_name' AS originator_name,
                            rec.obj->>'originator_reference' AS originator_reference,
                            rec.obj->>'originator_beneficiary_name' AS originator_beneficiary_name,
                            da.defendant_account_id,
                            da.account_status
        FROM                interface_jobs j
        INNER JOIN          interface_files f ON f.interface_job_id = j.interface_job_id
        INNER JOIN          configuration_items ci ON ci.business_unit_id = j.business_unit_id AND ci.item_name = 'BANKLIST'
        CROSS JOIN LATERAL  json_array_elements(f.records) WITH ORDINALITY AS rec(obj, rec_index)
        LEFT JOIN LATERAL   json_array_elements(ci.item_values) AS ba
                                ON ba->>'account_number' = rec.obj->>'receiving_bank_account_number'
                                AND ba->>'sort_code' = rec.obj->>'receiving_sort_code'
        LEFT JOIN           defendant_accounts da ON da.business_unit_id = j.business_unit_id AND da.account_number = rec.obj->>'originator_reference'
        WHERE               j.interface_job_id = pi_interface_job_id
        ORDER BY            rec.rec_index)
    LOOP
        v_count_processed := v_count_processed + 1;
        v_total_processed := v_total_processed + r_payment.amount_pence;
        IF r_payment.amount_pence > 0 THEN
            v_record_detail := r_payment.receiving_sort_code
                ||','||r_payment.receiving_bank_account_number
                ||','||r_payment.receiving_account_type
                ||','||r_payment.transaction_code
                ||','||r_payment.originator_sort_code
                ||','||r_payment.originator_bank_account_number
                ||','||round((r_payment.amount_pence/100.00),2)::text
                ||','||r_payment.originator_name
                ||','||r_payment.originator_reference
                ||','||r_payment.originator_beneficiary_name;
            IF NOT r_payment.is_bu_bank THEN
                -- rejected records
                v_msg_text := k_msg_bank;
                v_msg_type := k_msg_type_exc;
                v_count_rejected := v_count_rejected + 1;
                v_total_rejected := v_total_rejected + r_payment.amount_pence;
            ELSE
                -- accepted records (might still have warnings)
                SELECT              da.defendant_account_id,
                                    da.account_number,
                                    da.last_enforcement,
                                    da.account_status,
                                    da.account_balance,
                                    (res IS NOT NULL) AS enf
                INTO                r_master_account
                FROM                defendant_accounts da
                LEFT JOIN           configuration_items ci ON ci.business_unit_id = da.business_unit_id AND ci.item_name = 'INHIBIT_PAYMENT_RESULTS'
                LEFT JOIN LATERAL   json_array_elements_text(ci.item_values) AS res ON res = da.last_enforcement
                WHERE               da.defendant_account_id = f_get_master_account_id(r_payment.defendant_account_id);
                v_msg_text := CASE
                    WHEN r_payment.transaction_code != ALL(k_valid_transaction_codes) THEN k_msg_tran
                    WHEN r_master_account.defendant_account_id IS NULL THEN k_msg_nf
                    WHEN r_master_account.account_status = k_status_cs THEN k_msg_cs
                    WHEN r_master_account.account_status = k_status_ta THEN k_msg_ta
                    WHEN r_master_account.account_status = k_status_to THEN k_msg_to
                    WHEN r_master_account.account_status = k_status_ts THEN k_msg_ts
                    WHEN r_master_account.account_status = k_status_wo THEN k_msg_wo
                    WHEN r_master_account.enf THEN k_msg_enf
                    WHEN r_master_account.account_balance = 0 THEN k_msg_bal
                    ELSE NULL END;
                IF v_msg_text IS NOT NULL THEN
                    v_msg_type = k_msg_type_warn;
                END IF;
                -- create payment in (first payment requires till to be created)
                IF v_till_id IS NULL THEN
                    CALL p_insert_till(v_till_id, v_till_number, r_payment.business_unit_id);
                END IF;
                CALL p_insert_payment_in(
                    pi_till_id := v_till_id,
                    pi_payment_amount := round((r_payment.amount_pence/100.00),2),
                    pi_payment_method := 'CT'::text,
                    pi_destination_type := CASE WHEN v_msg_text IS NULL THEN k_dest_fines ELSE k_dest_suspense END,
                    pi_allocation_type := CASE WHEN v_msg_text IS NULL THEN NULL ELSE k_alloc_unidentified END,
                    pi_associated_record_type := CASE WHEN v_msg_text IS NULL THEN k_tbl_defendant_accounts ELSE NULL::text END,
                    pi_associated_record_id := CASE WHEN v_msg_text IS NULL THEN r_master_account.defendant_account_id::text ELSE NULL::text END,
                    pi_additional_information := r_payment.originator_reference||k_unidentified_ref_label,
                    pi_auto_payment := true);
                v_count_accepted := v_count_accepted + 1;
                v_total_accepted := v_total_accepted + r_payment.amount_pence;
                IF v_msg_text IS NULL THEN
                    v_count_fines := v_count_fines + 1;
                    v_total_fines := v_total_fines + r_payment.amount_pence;
                ELSE
                    v_count_suspense := v_count_suspense + 1;
                    v_total_suspense := v_total_suspense + r_payment.amount_pence;
                END IF;
            END IF;
            IF v_msg_text IS NOT NULL THEN
                CALL p_insert_interface_message(pi_interface_job_id, v_msg_type, v_msg_text, r_payment.interface_file_id, r_payment.rec_index, v_record_detail);
            END IF;
        ELSE
            v_count_ignored := v_count_ignored + 1;
        END IF;
    END LOOP;
    -- job summary messages
    IF v_till_number IS NOT NULL THEN
        CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_till||v_till_number::text);
    END IF;
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_processed||v_count_processed::text||k_summary_value||round(v_total_processed/100.00,2)::text);
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_accepted||v_count_accepted::text||k_summary_value||round(v_total_accepted/100.00,2)::text);
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_fines||v_count_fines::text||k_summary_value||round(v_total_fines/100.00,2)::text);
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_suspense||v_count_suspense::text||k_summary_value||round(v_total_suspense/100.00,2)::text);
    IF v_count_rejected > 0 THEN
        CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_rejected||v_count_rejected::text||k_summary_value||round(v_total_rejected/100.00,2)::text);
    END IF;
    IF v_count_ignored > 0 THEN
        CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_ignored||v_count_ignored::text);
    END IF;
END;
$$ LANGUAGE plpgsql;
