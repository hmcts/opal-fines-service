CREATE OR REPLACE PROCEDURE p_audit_finalise(
    IN pi_associated_account_id     BIGINT,
    IN pi_record_type               CHARACTER VARYING,
    IN pi_business_unit_id          business_units.business_unit_id%TYPE,
    IN pi_posted_by                 defendant_transactions.posted_by%TYPE,
    IN pi_case_reference            amendments.case_reference%TYPE,
    IN pi_function_code             amendments.function_code%TYPE
)
LANGUAGE 'plpgsql'
AS 
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_audit_finalise.sql
*
* DESCRIPTION : Procedure to be called to fetch the current state of values of the auditable amendment data item fields,
*               depending on the record type passed in, from the corresponding database view in the same session as when 
*               p_audit_initialise was called. The values of the fields fetched will be compared with the values in the 
*               audit amendment list that have been stored in the temporary table. Those that have changed will be 
*               inserted into the AMENDMENTS table.
*
* PARAMETERS : pi_associated_account_id  - The Opal defendant account id or creditor account id
*            : pi_record_type            - Can be 'defendant_accounts' or 'creditor_accounts'
*            : pi_business_unit_id       - Business unit ID for the amendment
*            : pi_posted_by              - User ID who made the amendment
*            : pi_case_reference         - Case reference if set by Case Management
*            : pi_function_code          - Function code where amendment was made
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 28/08/2025    C Cho       1.0         PO-1677 Initial version - Create audit finalise procedure.
*
**/
DECLARE
    v_pg_exception_detail            TEXT;
    v_record_count                   INTEGER;
    v_amendment_count                INTEGER := 0;
    v_current_record                 RECORD;
    v_temp_record                    RECORD;
    v_field_code                     SMALLINT;
    v_update_defendant_last_changed  BOOLEAN := FALSE;
    v_update_creditor_last_changed   BOOLEAN := FALSE;
    v_field_name                     TEXT;
    v_old_value                      TEXT;
    v_new_value                      TEXT;
    v_fields_to_check                TEXT[];
    v_last_changed_fields            TEXT[];
    v_column_exists                  BOOLEAN;
    
BEGIN
    RAISE INFO 'p_audit_finalise: Starting with pi_associated_account_id = %, pi_record_type = %', pi_associated_account_id, pi_record_type;

    -- Validate input parameters
    IF pi_associated_account_id IS NULL THEN
        RAISE EXCEPTION 'Associated account ID cannot be null'
            USING ERRCODE = 'P3005'
                , DETAIL = 'p_audit_finalise: pi_associated_account_id is required';
    END IF;

    IF pi_record_type IS NULL OR pi_record_type NOT IN ('defendant_accounts', 'creditor_accounts') THEN
        RAISE EXCEPTION 'Invalid record type: %. Must be defendant_accounts or creditor_accounts', pi_record_type
            USING ERRCODE = 'P3006'
                , DETAIL = 'p_audit_finalise: pi_record_type must be defendant_accounts or creditor_accounts';
    END IF;

    IF pi_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'Business unit ID cannot be null'
            USING ERRCODE = 'P3007'
                , DETAIL = 'p_audit_finalise: pi_business_unit_id is required';
    END IF;

    IF pi_posted_by IS NULL THEN
        RAISE EXCEPTION 'Posted by cannot be null'
            USING ERRCODE = 'P3008'
                , DETAIL = 'p_audit_finalise: pi_posted_by is required';
    END IF;

    -- Process based on record type
    IF pi_record_type = 'defendant_accounts' THEN
        
        -- Check if temporary table exists
        IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'temp_def_ac_amendment_list') THEN
            RAISE EXCEPTION 'Temporary table temp_def_ac_amendment_list does not exist. Call p_audit_initialise first.'
                USING ERRCODE = 'P3009'
                    , DETAIL = 'p_audit_finalise: temp_def_ac_amendment_list not found';
        END IF;

        -- Get current values from view
        SELECT INTO v_current_record
            defendant_account_id,
            cheque_clearance_period,
            allow_cheques,
            credit_trans_clearance_period,
            allow_writeoffs,
            enf_override_enforcer_id,
            enf_override_result_id,
            enf_override_tfo_lja_id,
            enforcing_court_id,
            collection_order,
            suspended_committal_date,
            account_comments,
            account_note_1,
            account_note_2,
            account_note_3,
            name,
            birth_date,
            age,
            address_line_1,
            address_line_2,
            address_line_3,
            postcode,
            national_insurance_number,
            telephone_home,
            telephone_business,
            telephone_mobile,
            email_1,
            email_2,
            pname,
            paddr1,
            paddr2,
            paddr3,
            pbdate,
            pninumber,
            alias1,
            alias2,
            alias3,
            alias4,
            alias5,
            document_language,
            hearing_language,
            vehicle_make,
            vehicle_registration,
            employee_reference,
            employer_name,
            employer_address_line_1,
            employer_address_line_2,
            employer_address_line_3,
            employer_address_line_4,
            employer_address_line_5,
            employer_postcode,
            employer_telephone,
            employer_email
        FROM v_audit_defendant_accounts
        WHERE defendant_account_id = pi_associated_account_id;

        GET DIAGNOSTICS v_record_count = ROW_COUNT;
        
        IF v_record_count = 0 THEN
            RAISE EXCEPTION 'No defendant account found with ID: %', pi_associated_account_id
                USING ERRCODE = 'P3010'
                    , DETAIL = 'p_audit_finalise: defendant_account_id not found';
        END IF;

        -- Define all fields to check
        v_fields_to_check := ARRAY[
            'cheque_clearance_period', 'allow_cheques', 'credit_trans_clearance_period', 'allow_writeoffs',
            'enf_override_enforcer_id', 'enf_override_result_id', 'enf_override_tfo_lja_id', 'enforcing_court_id',
            'collection_order', 'suspended_committal_date', 'account_comments', 'account_note_1', 'account_note_2',
            'account_note_3', 'name', 'birth_date', 'age', 'address_line_1', 'address_line_2', 'address_line_3',
            'postcode', 'national_insurance_number', 'telephone_home', 'telephone_business', 'telephone_mobile',
            'email_1', 'email_2', 'pname', 'paddr1', 'paddr2', 'paddr3', 'pbdate', 'pninumber', 'alias1',
            'alias2', 'alias3', 'alias4', 'alias5', 'document_language', 'hearing_language', 'vehicle_make',
            'vehicle_registration', 'employee_reference', 'employer_name', 'employer_address_line_1',
            'employer_address_line_2', 'employer_address_line_3', 'employer_address_line_4', 'employer_address_line_5',
            'employer_postcode', 'employer_telephone', 'employer_email'
        ];

        -- Fields that require last_changed_date update
        v_last_changed_fields := ARRAY['name', 'birth_date', 'address_line_1', 'postcode', 'alias1',
            'alias2', 'alias3', 'alias4', 'alias5', 'credit_trans_clearance_period', 'account_comments', 
            'allow_writeoffs', 'enforcing_court_id'];

        -- Loop through all fields and compare
        FOREACH v_field_name IN ARRAY v_fields_to_check
        LOOP
            BEGIN
                -- Get old value directly from temporary table
                EXECUTE format('SELECT %I::TEXT FROM temp_def_ac_amendment_list LIMIT 1', v_field_name) INTO v_old_value;
                -- Get new value directly from view
                EXECUTE format('SELECT %I::TEXT FROM v_audit_defendant_accounts WHERE defendant_account_id = %s LIMIT 1', v_field_name, pi_associated_account_id) INTO v_new_value;
                
                -- Compare values
                IF COALESCE(v_new_value, '') != COALESCE(v_old_value, '') THEN
                    -- Get field code
                    SELECT field_code INTO v_field_code FROM audit_amendment_fields WHERE data_item = v_field_name;
                    
                    -- Only insert if field_code was found
                    IF v_field_code IS NOT NULL THEN
                        -- Insert amendment record
                        INSERT INTO amendments (amendment_id, associated_record_type, associated_record_id, field_code, old_value, new_value, business_unit_id, amended_by, amended_date, case_reference, function_code)
                        VALUES (nextval('amendment_id_seq'), pi_record_type, pi_associated_account_id, v_field_code, v_old_value, v_new_value, pi_business_unit_id, pi_posted_by, CURRENT_TIMESTAMP, pi_case_reference, pi_function_code);
                        
                        v_amendment_count := v_amendment_count + 1;
                        
                        -- Check if this field requires last_changed_date update
                        IF v_field_name = ANY(v_last_changed_fields) THEN
                            v_update_defendant_last_changed := TRUE;
                        END IF;
                    ELSE
                        RAISE NOTICE 'p_audit_finalise: Field code not found for data_item: %', v_field_name;
                    END IF;
                END IF;
            EXCEPTION
                WHEN OTHERS THEN
                    -- Log the error and continue with the next field
                    RAISE NOTICE 'p_audit_finalise: Error processing field %, skipping: % - %', v_field_name, SQLSTATE, SQLERRM;
                    CONTINUE;
            END;
        END LOOP;

        -- Update last_changed_date if any relevant fields have changed
        IF v_update_defendant_last_changed THEN
            UPDATE defendant_accounts
            SET last_changed_date = CURRENT_TIMESTAMP
            WHERE defendant_account_id = pi_associated_account_id;
        END IF;

        RAISE INFO 'p_audit_finalise: Processed defendant account with % amendments recorded', v_amendment_count;

    ELSIF pi_record_type = 'creditor_accounts' THEN
        
        -- Check if temporary table exists
        IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'temp_cred_ac_amendment_list') THEN
            RAISE EXCEPTION 'Temporary table temp_cred_ac_amendment_list does not exist. Call p_audit_initialise first.'
                USING ERRCODE = 'P3011'
                    , DETAIL = 'p_audit_finalise: temp_cred_ac_amendment_list not found';
        END IF;

        -- Get current values from view
        SELECT INTO v_current_record
            name,
            address_line_1,
            address_line_2,
            address_line_3,
            postcode,
            creditor_account_id,
            hold_payout,
            pay_by_bacs,
            bank_sort_code,
            bank_account_type,
            bank_account_number,
            bank_account_name,
            bank_account_reference
        FROM v_audit_creditor_accounts
        WHERE creditor_account_id = pi_associated_account_id;

        GET DIAGNOSTICS v_record_count = ROW_COUNT;
        
        IF v_record_count = 0 THEN
            RAISE EXCEPTION 'No creditor account found with ID: %', pi_associated_account_id
                USING ERRCODE = 'P3012'
                    , DETAIL = 'p_audit_finalise: creditor_account_id not found';
        END IF;

        -- Define all fields to check
        v_fields_to_check := ARRAY[
            'name', 'address_line_1', 'address_line_2', 'address_line_3', 'postcode',
            'hold_payout', 'pay_by_bacs', 'bank_sort_code', 'bank_account_type',
            'bank_account_number', 'bank_account_name', 'bank_account_reference'
        ];

        -- Fields that require last_changed_date update
        v_last_changed_fields := ARRAY['name', 'address_line_1', 'postcode', 'hold_payout', 
                                    'pay_by_bacs', 'bank_sort_code', 'bank_account_type',
                                    'bank_account_number', 'bank_account_name', 'bank_account_reference'];

        -- Loop through all fields and compare
        FOREACH v_field_name IN ARRAY v_fields_to_check
        LOOP
            BEGIN
                -- Get old value directly from temporary table
                EXECUTE format('SELECT %I::TEXT FROM temp_cred_ac_amendment_list LIMIT 1', v_field_name) INTO v_old_value;
                -- Get new value directly from view
                EXECUTE format('SELECT %I::TEXT FROM v_audit_creditor_accounts WHERE creditor_account_id = %s LIMIT 1', v_field_name, pi_associated_account_id) INTO v_new_value;
                
                -- Compare values
                IF COALESCE(v_new_value, '') != COALESCE(v_old_value, '') THEN
                    -- Get field code
                    SELECT field_code INTO v_field_code FROM audit_amendment_fields WHERE data_item = v_field_name;
                    
                    -- Only insert if field_code was found
                    IF v_field_code IS NOT NULL THEN
                        -- Insert amendment record
                        INSERT INTO amendments (amendment_id, associated_record_type, associated_record_id, field_code, old_value, new_value, business_unit_id, amended_by, amended_date, case_reference, function_code)
                        VALUES (nextval('amendment_id_seq'), pi_record_type, pi_associated_account_id, v_field_code, v_old_value, v_new_value, pi_business_unit_id, pi_posted_by, CURRENT_TIMESTAMP, pi_case_reference, pi_function_code);
                        
                        v_amendment_count := v_amendment_count + 1;
                        
                        -- Check if this field requires last_changed_date update
                        IF v_field_name = ANY(v_last_changed_fields) THEN
                            v_update_creditor_last_changed := TRUE;
                        END IF;
                    ELSE
                        RAISE NOTICE 'p_audit_finalise: Field code not found for data_item: %', v_field_name;
                    END IF;
                END IF;
            EXCEPTION
                WHEN OTHERS THEN
                    -- Log the error and continue with the next field
                    RAISE NOTICE 'p_audit_finalise: Error processing field %, skipping: % - %', v_field_name, SQLSTATE, SQLERRM;
                    CONTINUE;
            END;
        END LOOP;

        -- Update last_changed_date if any relevant fields have changed
        IF v_update_creditor_last_changed THEN
            UPDATE creditor_accounts
            SET last_changed_date = CURRENT_TIMESTAMP
            WHERE creditor_account_id = pi_associated_account_id;
        END IF;

        RAISE INFO 'p_audit_finalise: Processed creditor account with % amendments recorded', v_amendment_count;

    END IF;

    -- Clean up temporary tables
    IF pi_record_type = 'defendant_accounts' THEN
        DROP TABLE IF EXISTS temp_def_ac_amendment_list;
    ELSIF pi_record_type = 'creditor_accounts' THEN
        DROP TABLE IF EXISTS temp_cred_ac_amendment_list;
    END IF;

    RAISE INFO 'p_audit_finalise: Successfully completed with % total amendments recorded', v_amendment_count;

EXCEPTION
    WHEN SQLSTATE 'P3005' OR SQLSTATE 'P3006' OR SQLSTATE 'P3007' OR SQLSTATE 'P3008' OR 
         SQLSTATE 'P3009' OR SQLSTATE 'P3010' OR SQLSTATE 'P3011' OR SQLSTATE 'P3012' THEN
        -- When custom exceptions just re-raise them so they're not manipulated
        RAISE NOTICE 'Error in p_audit_finalise: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        -- Output full exception details
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_audit_finalise: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_audit_finalise: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_audit_finalise
    IS 'Procedure to fetch current values of auditable amendment data fields, compare with stored initial values, and record changes in the amendments table.';