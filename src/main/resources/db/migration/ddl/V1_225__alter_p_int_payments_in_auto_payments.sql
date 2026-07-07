/**
* OPAL Program
*
* MODULE      : alter_p_int_payments_in_auto_payments.sql
*
* DESCRIPTION : Amend p_int_payments_in for Auto Payments In processing.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 25/06/2026    C Cho       1.0         PO-2616 Amend p_int_payments_in for Auto Payments In processing.
*
**/

DROP PROCEDURE IF EXISTS p_int_payments_in;

CREATE PROCEDURE p_int_payments_in(
    IN pi_interface_job_id interface_jobs.interface_job_id%TYPE,
    IN pi_business_unit_id interface_jobs.business_unit_id%TYPE,
    IN pi_posted_by tills.owned_by%TYPE,
    IN pi_posted_by_name tills.owned_by_name%TYPE,
    OUT po_till_id tills.till_id%TYPE
)
    LANGUAGE plpgsql
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
* 03/06/2026    TMc         2.0         PO-3839 - Update columns on PAYMENTS_IN table to use PostgreSQL ENUM
* 25/06/2026    C Cho       3.0         PO-2616 Amend p_int_payments_in for Auto Payments In processing.
**/
DECLARE
    k_valid_transaction_codes varchar[] := ARRAY['00','68','99'];
    k_status_cs defendant_accounts.account_status%TYPE := 'CS';
    k_status_ta defendant_accounts.account_status%TYPE := 'TA';
    k_status_to defendant_accounts.account_status%TYPE := 'TO';
    k_status_ts defendant_accounts.account_status%TYPE := 'TS';
    k_status_wo defendant_accounts.account_status%TYPE := 'WO';
    k_msg_zero_balance interface_messages.message_text%TYPE := 'zero_balance';
    k_msg_enforcement_action interface_messages.message_text%TYPE := 'enforcement_action';
    k_msg_unknown_bank interface_messages.message_text%TYPE := 'unknown_bank';
    k_msg_invalid_transaction interface_messages.message_text%TYPE := 'invalid_transaction';
    k_msg_account_notfound interface_messages.message_text%TYPE := 'account_notfound';
    k_msg_account_written_off interface_messages.message_text%TYPE := 'account_written_off';
    k_msg_account_consolidated interface_messages.message_text%TYPE := 'account_consolidated';
    k_msg_account_tfo_not_ack interface_messages.message_text%TYPE := 'account_tfo_not_ack';
    k_msg_account_tfo_ack interface_messages.message_text%TYPE := 'account_tfo_ack';
    k_msg_account_tfo_sc_ni interface_messages.message_text%TYPE := 'account_tfo_sc_ni';
    k_msg_records_read interface_messages.message_text%TYPE := 'records_read';
    k_msg_records_accepted interface_messages.message_text%TYPE := 'records_accepted';
    k_msg_records_fines interface_messages.message_text%TYPE := 'records_fines';
    k_msg_records_suspense interface_messages.message_text%TYPE := 'records_suspense';
    k_msg_records_rejected interface_messages.message_text%TYPE := 'records_rejected';
    k_msg_records_ignored interface_messages.message_text%TYPE := 'records_ignored';
    k_msg_records_ignored_rejected interface_messages.message_text%TYPE := 'records_ignored_rejected';
    k_msg_type_info interface_messages.message_type%TYPE := 'Info';
    k_msg_type_warn interface_messages.message_type%TYPE := 'Warning';
    k_msg_type_exc interface_messages.message_type%TYPE := 'Exception';
    k_tbl_defendant_accounts text := 'defendant_accounts';
    k_dest_fines text := 'F';
    k_dest_suspense text := 'S';
    k_alloc_unidentified text := 'UN';
    r_master_account record;
    r_payment record;
    v_job_business_unit_id interface_jobs.business_unit_id%TYPE;
    v_interface_name interface_jobs.interface_name%TYPE;
    v_msg_text interface_messages.message_text%TYPE;
    v_msg_type interface_messages.message_type%TYPE;
    v_msg_data interface_messages.message_data%TYPE;
    v_record_detail interface_messages.record_detail%TYPE;
    v_payment_amount payments_in.payment_amount%TYPE;
    v_count_ignored bigint := 0;
    v_count_processed bigint := 0;
    v_count_rejected bigint := 0;
    v_count_accepted bigint := 0;
    v_count_fines bigint := 0;
    v_count_suspense bigint := 0;
    v_total_processed bigint := 0;
    v_total_rejected bigint := 0;
    v_total_accepted numeric(18,2) := 0;
    v_total_fines numeric(18,2) := 0;
    v_total_suspense numeric(18,2) := 0;
BEGIN
    SELECT business_unit_id,
           interface_name
    INTO   v_job_business_unit_id,
           v_interface_name
    FROM   interface_jobs
    WHERE  interface_job_id = pi_interface_job_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Interface job not found.'
            USING ERRCODE = 'P3201'
                , DETAIL = 'p_int_payments_in: Interface job ' || pi_interface_job_id || ' not found';
    END IF;

    IF v_job_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'Interface job not linked to any BU.'
            USING ERRCODE = 'P3202'
                , DETAIL = 'p_int_payments_in: Interface job ' || pi_interface_job_id || ' has no business unit id';
    END IF;

    IF pi_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'BU parameter pi_business_unit_id is NULL.'
            USING ERRCODE = 'P3203'
                , DETAIL = 'p_int_payments_in: Business unit id must be provided for interface job ' || pi_interface_job_id;
    END IF;

    IF pi_business_unit_id <> v_job_business_unit_id THEN
        RAISE EXCEPTION 'BU parameter not matching the BU linked to the interface job.'
            USING ERRCODE = 'P3204'
                , DETAIL = 'p_int_payments_in: Interface job ' || pi_interface_job_id
                    || ' belongs to business unit ' || v_job_business_unit_id
                    || ', not ' || pi_business_unit_id;
    END IF;

    po_till_id := NULL;

    FOR r_payment IN (
         SELECT             rec.rec_index,
                            rec.obj AS record_json,
                            j.business_unit_id,
                            j.interface_name,
                            f.interface_file_id,
                            f.source,
                            f.override_inhibits,
                            CASE WHEN ba.value->>'account_number' IS NOT NULL THEN true ELSE false END AS is_bu_bank,
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
                            da.defendant_account_id
        FROM                interface_jobs j
        INNER JOIN          interface_files f ON f.interface_job_id = j.interface_job_id
        CROSS JOIN LATERAL  json_array_elements(f.records) WITH ORDINALITY AS rec(obj, rec_index)
        LEFT JOIN LATERAL   (
                                SELECT ci.item_values
                                FROM   configuration_items ci
                                WHERE  ci.business_unit_id = pi_business_unit_id
                                AND    ci.item_name = 'BANK_ACCOUNTS'
                                ORDER BY ci.configuration_item_id
                                LIMIT 1
                            ) bank_ci ON true
        LEFT JOIN LATERAL   json_array_elements(COALESCE(bank_ci.item_values, '[]'::json)) AS ba(value)
                                ON ba.value->>'account_number' = rec.obj->>'receiving_bank_account_number'
                                AND ba.value->>'sort_code' = rec.obj->>'receiving_sort_code'
        LEFT JOIN           defendant_accounts da
                                ON da.business_unit_id = pi_business_unit_id
                                AND da.account_number = rec.obj->>'originator_reference'
        WHERE               j.interface_job_id = pi_interface_job_id
        AND                 rec.obj->>'amount_pence' IS NOT NULL
        ORDER BY            f.interface_file_id,
                            rec.rec_index)
    LOOP
        SELECT NULL::bigint AS defendant_account_id,
               NULL::character varying AS account_number,
               NULL::character varying AS surname,
               NULL::character varying AS last_enforcement,
               NULL::t_da_account_status_enum AS account_status,
               NULL::numeric AS account_balance,
               NULL::boolean AS enf
        INTO   r_master_account;

        v_msg_text := NULL;
        v_msg_type := NULL;
        v_msg_data := NULL;
        v_record_detail := r_payment.record_json::text;
        v_payment_amount := round((r_payment.amount_pence / 100.00), 2);
        v_count_processed := v_count_processed + 1;
        v_total_processed := v_total_processed + r_payment.amount_pence;

        IF r_payment.amount_pence <= 0 THEN
            v_count_ignored := v_count_ignored + 1;
            CONTINUE;
        END IF;

        IF r_payment.is_bu_bank THEN
            SELECT              da.defendant_account_id,
                                da.account_number,
                                p.surname,
                                da.last_enforcement,
                                da.account_status,
                                da.account_balance,
                                (res.value IS NOT NULL AND NOT COALESCE(r_payment.override_inhibits, false)) AS enf
            INTO                r_master_account
            FROM                defendant_accounts da
            LEFT JOIN           defendant_account_parties dap
                                    ON dap.defendant_account_id = da.defendant_account_id
                                    AND dap.association_type = 'Defendant'
            LEFT JOIN           parties p ON p.party_id = dap.party_id
            LEFT JOIN LATERAL   (
                                    SELECT ci.item_values
                                    FROM   configuration_items ci
                                    WHERE  ci.business_unit_id = da.business_unit_id
                                    AND    ci.item_name = 'RESULTS_TO_INHIBIT_PAYMENTS'
                                    ORDER BY ci.configuration_item_id
                                    LIMIT 1
                                ) inhibit_ci ON true
            LEFT JOIN LATERAL   json_array_elements_text(COALESCE(inhibit_ci.item_values, '[]'::json)) AS res(value)
                                    ON res.value = da.last_enforcement
            WHERE               da.defendant_account_id = f_get_master_account_id(r_payment.defendant_account_id);
        END IF;

        IF NOT r_payment.is_bu_bank THEN
            v_msg_text := k_msg_unknown_bank;
            v_msg_type := k_msg_type_exc;
            v_msg_data := json_build_object(
                'court_sort_code', r_payment.receiving_sort_code,
                'court_account_number', r_payment.receiving_bank_account_number,
                'court_account_type', r_payment.receiving_account_type,
                'court_beneficiary_name', r_payment.originator_beneficiary_name,
                'court_transaction', r_payment.transaction_code,
                'court_amount', v_payment_amount,
                'court_transaction_account', r_payment.originator_reference,
                'defendant_sort_code', r_payment.originator_sort_code,
                'defendant_bank_account_number', r_payment.originator_bank_account_number,
                'defendant_account_number', r_payment.originator_bank_account_number,
                'defendant_name', r_payment.originator_name,
                'Error', 'Unknown bank details - records ignored'
            );
        ELSIF r_master_account.defendant_account_id IS NOT NULL AND r_master_account.account_balance = 0 THEN
            v_msg_text := k_msg_zero_balance;
            v_msg_type := k_msg_type_warn;
            v_msg_data := json_build_object(
                'account_number', r_master_account.account_number,
                'surname', r_master_account.surname,
                'value', v_payment_amount,
                'transaction_account', r_payment.originator_reference
            );
        ELSIF r_master_account.enf THEN
            v_msg_text := k_msg_enforcement_action;
            v_msg_type := k_msg_type_warn;
            v_msg_data := json_build_object(
                'account_number', r_master_account.account_number,
                'surname', r_master_account.surname,
                'last_action', r_master_account.last_enforcement,
                'balance', r_master_account.account_balance,
                'value', v_payment_amount,
                'transaction_account', r_payment.originator_reference
            );
        ELSIF r_payment.transaction_code IS NULL OR r_payment.transaction_code != ALL(k_valid_transaction_codes) THEN
            v_msg_text := k_msg_invalid_transaction;
            v_msg_type := k_msg_type_warn;
            v_msg_data := json_build_object(
                'court_sort_code', r_payment.receiving_sort_code,
                'court_account_number', r_payment.receiving_bank_account_number,
                'court_account_type', r_payment.receiving_account_type,
                'court_beneficiary_name', r_payment.originator_beneficiary_name,
                'court_transaction', r_payment.transaction_code,
                'court_amount', v_payment_amount,
                'court_transaction_account', r_payment.originator_reference,
                'defendant_sort_code', r_payment.originator_sort_code,
                'defendant_bank_account_number', r_payment.originator_bank_account_number,
                'defendant_account_number', r_payment.originator_bank_account_number,
                'defendant_name', r_payment.originator_name,
                'Error', 'Invalid credit transfer transaction code'
            );
        ELSIF r_master_account.defendant_account_id IS NULL THEN
            v_msg_text := k_msg_account_notfound;
            v_msg_type := k_msg_type_warn;
            v_msg_data := json_build_object(
                'court_sort_code', r_payment.receiving_sort_code,
                'court_account_number', r_payment.receiving_bank_account_number,
                'court_account_type', r_payment.receiving_account_type,
                'court_beneficiary_name', r_payment.originator_beneficiary_name,
                'court_transaction', r_payment.transaction_code,
                'court_amount', v_payment_amount,
                'court_transaction_account', r_payment.originator_reference,
                'defendant_sort_code', r_payment.originator_sort_code,
                'defendant_bank_account_number', r_payment.originator_bank_account_number,
                'defendant_account_number', r_payment.originator_bank_account_number,
                'defendant_name', r_payment.originator_name,
                'Error', 'No Opal Account'
            );
        ELSIF r_master_account.account_status = k_status_wo THEN
            v_msg_text := k_msg_account_written_off;
            v_msg_type := k_msg_type_warn;
            v_msg_data := json_build_object(
                'court_sort_code', r_payment.receiving_sort_code,
                'court_account_number', r_payment.receiving_bank_account_number,
                'court_account_type', r_payment.receiving_account_type,
                'court_beneficiary_name', r_payment.originator_beneficiary_name,
                'court_transaction', r_payment.transaction_code,
                'court_amount', v_payment_amount,
                'court_transaction_account', r_payment.originator_reference,
                'defendant_sort_code', r_payment.originator_sort_code,
                'defendant_bank_account_number', r_payment.originator_bank_account_number,
                'defendant_account_number', r_payment.originator_bank_account_number,
                'defendant_name', r_payment.originator_name,
                'Error', 'Opal account is written off'
            );
        ELSIF r_master_account.account_status = k_status_cs THEN
            v_msg_text := k_msg_account_consolidated;
            v_msg_type := k_msg_type_warn;
            v_msg_data := json_build_object(
                'court_sort_code', r_payment.receiving_sort_code,
                'court_account_number', r_payment.receiving_bank_account_number,
                'court_account_type', r_payment.receiving_account_type,
                'court_beneficiary_name', r_payment.originator_beneficiary_name,
                'court_transaction', r_payment.transaction_code,
                'court_amount', v_payment_amount,
                'court_transaction_account', r_payment.originator_reference,
                'defendant_sort_code', r_payment.originator_sort_code,
                'defendant_bank_account_number', r_payment.originator_bank_account_number,
                'defendant_account_number', r_payment.originator_bank_account_number,
                'defendant_name', r_payment.originator_name,
                'Error', 'Opal account consolidated'
            );
        ELSIF r_master_account.account_status = k_status_to THEN
            v_msg_text := k_msg_account_tfo_not_ack;
            v_msg_type := k_msg_type_warn;
            v_msg_data := json_build_object(
                'court_sort_code', r_payment.receiving_sort_code,
                'court_account_number', r_payment.receiving_bank_account_number,
                'court_account_type', r_payment.receiving_account_type,
                'court_beneficiary_name', r_payment.originator_beneficiary_name,
                'court_transaction', r_payment.transaction_code,
                'court_amount', v_payment_amount,
                'court_transaction_account', r_payment.originator_reference,
                'defendant_sort_code', r_payment.originator_sort_code,
                'defendant_bank_account_number', r_payment.originator_bank_account_number,
                'defendant_account_number', r_payment.originator_bank_account_number,
                'defendant_name', r_payment.originator_name,
                'Error', 'Opal account transfer out (not acknowledged)'
            );
        ELSIF r_master_account.account_status = k_status_ta THEN
            v_msg_text := k_msg_account_tfo_ack;
            v_msg_type := k_msg_type_warn;
            v_msg_data := json_build_object(
                'court_sort_code', r_payment.receiving_sort_code,
                'court_account_number', r_payment.receiving_bank_account_number,
                'court_account_type', r_payment.receiving_account_type,
                'court_beneficiary_name', r_payment.originator_beneficiary_name,
                'court_transaction', r_payment.transaction_code,
                'court_amount', v_payment_amount,
                'court_transaction_account', r_payment.originator_reference,
                'defendant_sort_code', r_payment.originator_sort_code,
                'defendant_bank_account_number', r_payment.originator_bank_account_number,
                'defendant_account_number', r_payment.originator_bank_account_number,
                'defendant_name', r_payment.originator_name,
                'Error', 'Opal account transfer out (acknowledged)'
            );
        ELSIF r_master_account.account_status = k_status_ts THEN
            v_msg_text := k_msg_account_tfo_sc_ni;
            v_msg_type := k_msg_type_warn;
            v_msg_data := json_build_object(
                'court_sort_code', r_payment.receiving_sort_code,
                'court_account_number', r_payment.receiving_bank_account_number,
                'court_account_type', r_payment.receiving_account_type,
                'court_beneficiary_name', r_payment.originator_beneficiary_name,
                'court_transaction', r_payment.transaction_code,
                'court_amount', v_payment_amount,
                'court_transaction_account', r_payment.originator_reference,
                'defendant_sort_code', r_payment.originator_sort_code,
                'defendant_bank_account_number', r_payment.originator_bank_account_number,
                'defendant_account_number', r_payment.originator_bank_account_number,
                'defendant_name', r_payment.originator_name,
                'Error', 'Opal account transfer out to Scotland/NI'
            );
        END IF;

        IF v_msg_type = k_msg_type_exc THEN
            v_count_rejected := v_count_rejected + 1;
            v_total_rejected := v_total_rejected + r_payment.amount_pence;
        ELSE
            IF po_till_id IS NULL THEN
                INSERT INTO tills (
                    till_id,
                    business_unit_id,
                    till_number,
                    source,
                    interface_file_id,
                    owned_by,
                    owned_by_name,
                    auto_payment)
                VALUES (
                    nextval('till_id_seq'),
                    r_payment.business_unit_id,
                    nextval(('till_number_' || r_payment.business_unit_id::text || '_seq')::regclass),
                    r_payment.source,
                    r_payment.interface_file_id,
                    pi_posted_by,
                    pi_posted_by_name,
                    true)
                RETURNING till_id
                INTO      po_till_id;
            END IF;

            CALL p_insert_payment_in(
                pi_till_id := po_till_id,
                pi_payment_amount := v_payment_amount,
                pi_payment_method := 'CT'::t_payment_method_enum,
                pi_destination_type := (CASE WHEN v_msg_text IS NULL THEN k_dest_fines ELSE k_dest_suspense END)::t_pi_destination_type_enum,
                pi_allocation_type := CASE WHEN v_msg_text IS NULL THEN NULL ELSE k_alloc_unidentified END,
                pi_associated_record_type := CASE WHEN v_msg_text IS NULL THEN k_tbl_defendant_accounts::t_associated_record_type_enum ELSE NULL::t_associated_record_type_enum END,
                pi_associated_record_id := CASE WHEN v_msg_text IS NULL THEN r_master_account.defendant_account_id::text ELSE NULL::text END,
                pi_third_party_payer_name := NULL,
                pi_additional_information := v_interface_name,
                pi_receipt := false,
                pi_auto_payment := true);

            v_count_accepted := v_count_accepted + 1;
            v_total_accepted := v_total_accepted + v_payment_amount;

            IF v_msg_text IS NULL THEN
                v_count_fines := v_count_fines + 1;
                v_total_fines := v_total_fines + v_payment_amount;
            ELSE
                v_count_suspense := v_count_suspense + 1;
                v_total_suspense := v_total_suspense + v_payment_amount;
            END IF;
        END IF;

        IF v_msg_text IS NOT NULL THEN
            CALL p_insert_interface_message(
                pi_interface_job_id,
                v_msg_type,
                v_msg_text,
                r_payment.interface_file_id,
                r_payment.rec_index,
                v_record_detail,
                v_msg_data
            );
        END IF;
    END LOOP;

    IF po_till_id IS NOT NULL THEN
        UPDATE tills
        SET    total_amount = v_total_accepted,
               payments_count = v_count_accepted::smallint,
               status = 'Created'
        WHERE  till_id = po_till_id;
    END IF;

    CALL p_insert_interface_message(
        pi_interface_job_id,
        k_msg_type_info,
        k_msg_records_read,
        NULL,
        NULL,
        NULL,
        json_build_object('number', v_count_processed, 'value', round(v_total_processed / 100.00, 2))
    );
    CALL p_insert_interface_message(
        pi_interface_job_id,
        k_msg_type_info,
        k_msg_records_accepted,
        NULL,
        NULL,
        NULL,
        json_build_object('number', v_count_accepted, 'value', v_total_accepted)
    );
    CALL p_insert_interface_message(
        pi_interface_job_id,
        k_msg_type_info,
        k_msg_records_fines,
        NULL,
        NULL,
        NULL,
        json_build_object('number', v_count_fines, 'value', v_total_fines)
    );
    CALL p_insert_interface_message(
        pi_interface_job_id,
        k_msg_type_info,
        k_msg_records_suspense,
        NULL,
        NULL,
        NULL,
        json_build_object('number', v_count_suspense, 'value', v_total_suspense)
    );

    IF v_count_rejected > 0 THEN
        CALL p_insert_interface_message(
            pi_interface_job_id,
            k_msg_type_info,
            k_msg_records_rejected,
            NULL,
            NULL,
            NULL,
            json_build_object('number', v_count_rejected, 'value', round(v_total_rejected / 100.00, 2))
        );
    END IF;

    IF v_count_ignored > 0 THEN
        CALL p_insert_interface_message(
            pi_interface_job_id,
            k_msg_type_info,
            k_msg_records_ignored,
            NULL,
            NULL,
            NULL,
            json_build_object('number', v_count_ignored, 'value', 0)
        );
    END IF;

    CALL p_insert_interface_message(
        pi_interface_job_id,
        k_msg_type_info,
        k_msg_records_ignored_rejected,
        NULL,
        NULL,
        NULL,
        json_build_object(
            'number', v_count_ignored + v_count_rejected,
            'value', round(v_total_rejected / 100.00, 2)
        )
    );
END;
$$;
