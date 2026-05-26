/**
* OPAL Program
*
* MODULE      : alter_audit_procedures_add_posted_by_name.sql
*
* DESCRIPTION : Amend audit procedures to store the user name responsible for amendment events.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 22/05/2026    C Cho       1.0         PO-2596 Amend audit procedures to populate AMENDMENTS.AMENDED_BY_NAME.
*
**/

CREATE OR REPLACE PROCEDURE public.p_audit_finalise(
    IN pi_associated_account_id bigint,
    IN pi_record_type character varying,
    IN pi_business_unit_id smallint,
    IN pi_posted_by character varying,
    IN pi_posted_by_name character varying,
    IN pi_case_reference character varying,
    IN pi_function_code character varying
)
    LANGUAGE plpgsql
    AS $$
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
*            : pi_posted_by_name         - User name who made the amendment
*            : pi_case_reference         - Case reference if set by Case Management
*            : pi_function_code          - Function code where amendment was made
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 28/08/2025    C Cho       1.0         PO-1677 Initial version - Create audit finalise procedure.
* 22/05/2026    C Cho       2.0         PO-2596 Add pi_posted_by_name and populate AMENDMENTS.AMENDED_BY_NAME.
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
                    -- Map database column name to audit field name
                    DECLARE
                        v_audit_field_name TEXT;
                    BEGIN
                        CASE v_field_name
                            WHEN 'cheque_clearance_period' THEN v_audit_field_name := 'Cheque Clearance Period';
                            WHEN 'allow_cheques' THEN v_audit_field_name := 'Cheque Hold';
                            WHEN 'credit_trans_clearance_period' THEN v_audit_field_name := 'Credit Transfer Clearance Period';
                            WHEN 'allow_writeoffs' THEN v_audit_field_name := 'Inhibit Write Off';
                            WHEN 'enf_override_enforcer_id' THEN v_audit_field_name := 'Override Enforcer';
                            WHEN 'enf_override_result_id' THEN v_audit_field_name := 'Enforcement Override';
                            WHEN 'enf_override_tfo_lja_id' THEN v_audit_field_name := 'TFOOUT LJA Code';
                            WHEN 'enforcing_court_id' THEN v_audit_field_name := 'Enforcement Court';
                            WHEN 'collection_order' THEN v_audit_field_name := 'Collection Order';
                            WHEN 'suspended_committal_date' THEN v_audit_field_name := 'SC Date';
                            WHEN 'account_comments' THEN v_audit_field_name := 'Comment';
                            WHEN 'account_note_1' THEN v_audit_field_name := 'Free Text Notes 1';
                            WHEN 'account_note_2' THEN v_audit_field_name := 'Free Text Notes 2';
                            WHEN 'account_note_3' THEN v_audit_field_name := 'Free Text Notes 3';
                            WHEN 'name' THEN v_audit_field_name := 'Name';
                            WHEN 'birth_date' THEN v_audit_field_name := 'Date of Birth';
                            WHEN 'age' THEN v_audit_field_name := 'Age';
                            WHEN 'address_line_1' THEN v_audit_field_name := 'Address Line 1';
                            WHEN 'address_line_2' THEN v_audit_field_name := 'Address Line 2';
                            WHEN 'address_line_3' THEN v_audit_field_name := 'Address Line 3';
                            WHEN 'postcode' THEN v_audit_field_name := 'Postcode';
                            WHEN 'national_insurance_number' THEN v_audit_field_name := 'National Insurance Number';
                            WHEN 'telephone_home' THEN v_audit_field_name := 'Home Phone Number';
                            WHEN 'telephone_business' THEN v_audit_field_name := 'Business Phone Number';
                            WHEN 'telephone_mobile' THEN v_audit_field_name := 'Mobile Phone Number';
                            WHEN 'email_1' THEN v_audit_field_name := 'Email Address 1';
                            WHEN 'email_2' THEN v_audit_field_name := 'Email Address 2';
                            WHEN 'pname' THEN v_audit_field_name := 'Parent Name';
                            WHEN 'paddr1' THEN v_audit_field_name := 'Parent Address Line 1';
                            WHEN 'paddr2' THEN v_audit_field_name := 'Parent Address Line 2';
                            WHEN 'paddr3' THEN v_audit_field_name := 'Parent Address Line 3';
                            WHEN 'pbdate' THEN v_audit_field_name := 'Parent Date of Birth';
                            WHEN 'pninumber' THEN v_audit_field_name := 'Parent NI Number';
                            WHEN 'alias1' THEN v_audit_field_name := 'AKA Name 1';
                            WHEN 'alias2' THEN v_audit_field_name := 'AKA Name 2';
                            WHEN 'alias3' THEN v_audit_field_name := 'AKA Name 3';
                            WHEN 'alias4' THEN v_audit_field_name := 'AKA Name 4';
                            WHEN 'alias5' THEN v_audit_field_name := 'AKA Name 5';
                            WHEN 'document_language' THEN v_audit_field_name := 'Document Language';
                            WHEN 'hearing_language' THEN v_audit_field_name := 'Hearing Language';
                            WHEN 'vehicle_make' THEN v_audit_field_name := 'Vehicle Make';
                            WHEN 'vehicle_registration' THEN v_audit_field_name := 'Vehicle Registration';
                            WHEN 'employee_reference' THEN v_audit_field_name := 'Employee Reference';
                            WHEN 'employer_name' THEN v_audit_field_name := 'Employer Name';
                            WHEN 'employer_address_line_1' THEN v_audit_field_name := 'Employer Address Line 1';
                            WHEN 'employer_address_line_2' THEN v_audit_field_name := 'Employer Address Line 2';
                            WHEN 'employer_address_line_3' THEN v_audit_field_name := 'Employer Address Line 3';
                            WHEN 'employer_address_line_4' THEN v_audit_field_name := 'Employer Address Line 4';
                            WHEN 'employer_address_line_5' THEN v_audit_field_name := 'Employer Address Line 5';
                            WHEN 'employer_postcode' THEN v_audit_field_name := 'Employer Postcode';
                            WHEN 'employer_telephone' THEN v_audit_field_name := 'Employer Phone Number';
                            WHEN 'employer_email' THEN v_audit_field_name := 'Employer Email';
                            ELSE v_audit_field_name := NULL;
                        END CASE;

                        -- Get field code using audit field name
                        SELECT field_code INTO v_field_code FROM audit_amendment_fields WHERE data_item = v_audit_field_name;
                    END;

                    -- Only insert if field_code was found
                    IF v_field_code IS NOT NULL THEN
                        -- Insert amendment record
                        INSERT INTO amendments (
                            amendment_id,
                            associated_record_type,
                            associated_record_id,
                            field_code,
                            old_value,
                            new_value,
                            business_unit_id,
                            amended_by,
                            amended_by_name,
                            amended_date,
                            case_reference,
                            function_code
                        )
                        VALUES (
                            nextval('amendment_id_seq'),
                            pi_record_type,
                            pi_associated_account_id,
                            v_field_code,
                            v_old_value,
                            v_new_value,
                            pi_business_unit_id,
                            pi_posted_by,
                            pi_posted_by_name,
                            CURRENT_TIMESTAMP,
                            pi_case_reference,
                            pi_function_code
                        );

                        v_amendment_count := v_amendment_count + 1;

                        -- Check if this field requires last_changed_date update
                        IF v_field_name = ANY(v_last_changed_fields) THEN
                            v_update_defendant_last_changed := TRUE;
                        END IF;
                    ELSE
                        RAISE NOTICE 'p_audit_finalise: Field code not found for data_item: % (column: %)', v_audit_field_name, v_field_name;
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
                    -- Map database column name to audit field name
                    DECLARE
                        v_audit_field_name TEXT;
                    BEGIN
                        CASE v_field_name
                            WHEN 'name' THEN v_audit_field_name := 'Name';
                            WHEN 'address_line_1' THEN v_audit_field_name := 'Address Line 1';
                            WHEN 'address_line_2' THEN v_audit_field_name := 'Address Line 2';
                            WHEN 'address_line_3' THEN v_audit_field_name := 'Address Line 3';
                            WHEN 'postcode' THEN v_audit_field_name := 'Postcode';
                            WHEN 'hold_payout' THEN v_audit_field_name := 'Hold Pay Out';
                            WHEN 'pay_by_bacs' THEN v_audit_field_name := 'Pay by BACS';
                            WHEN 'bank_sort_code' THEN v_audit_field_name := 'BACS Sort Code';
                            WHEN 'bank_account_type' THEN v_audit_field_name := 'BACS Account Type';
                            WHEN 'bank_account_number' THEN v_audit_field_name := 'BACS Account Number';
                            WHEN 'bank_account_name' THEN v_audit_field_name := 'BACS Account Name';
                            WHEN 'bank_account_reference' THEN v_audit_field_name := 'BACS Account Reference';
                            ELSE v_audit_field_name := NULL;
                        END CASE;

                        -- Get field code using audit field name
                        SELECT field_code INTO v_field_code FROM audit_amendment_fields WHERE data_item = v_audit_field_name;
                    END;

                    -- Only insert if field_code was found
                    IF v_field_code IS NOT NULL THEN
                        -- Insert amendment record
                        INSERT INTO amendments (
                            amendment_id,
                            associated_record_type,
                            associated_record_id,
                            field_code,
                            old_value,
                            new_value,
                            business_unit_id,
                            amended_by,
                            amended_by_name,
                            amended_date,
                            case_reference,
                            function_code
                        )
                        VALUES (
                            nextval('amendment_id_seq'),
                            pi_record_type,
                            pi_associated_account_id,
                            v_field_code,
                            v_old_value,
                            v_new_value,
                            pi_business_unit_id,
                            pi_posted_by,
                            pi_posted_by_name,
                            CURRENT_TIMESTAMP,
                            pi_case_reference,
                            pi_function_code
                        );

                        v_amendment_count := v_amendment_count + 1;

                        -- Check if this field requires last_changed_date update
                        IF v_field_name = ANY(v_last_changed_fields) THEN
                            v_update_creditor_last_changed := TRUE;
                        END IF;
                    ELSE
                        RAISE NOTICE 'p_audit_finalise: Field code not found for data_item: % (column: %)', v_audit_field_name, v_field_name;
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
$$;

COMMENT ON PROCEDURE public.p_audit_finalise(
    IN pi_associated_account_id bigint,
    IN pi_record_type character varying,
    IN pi_business_unit_id smallint,
    IN pi_posted_by character varying,
    IN pi_posted_by_name character varying,
    IN pi_case_reference character varying,
    IN pi_function_code character varying
) IS 'Procedure to fetch current values of auditable amendment data fields, compare with stored initial values, and record changes in the amendments table.';

CREATE OR REPLACE PROCEDURE public.p_add_defendant_account_enforcement(
    IN pi_result_id character varying,
    IN pi_defendant_account_id bigint,
    IN pi_business_unit_id smallint,
    IN pi_record_type character varying,
    IN pi_case_reference character varying,
    IN pi_function_code character varying,
    IN pi_jail_days integer,
    IN pi_posted_by character varying,
    IN pi_posted_by_name character varying,
    IN pi_reason character varying,
    IN pi_enforcer_id bigint,
    IN pi_result_responses json,
    IN pi_earliest_release_date timestamp without time zone,
    IN pi_version_number bigint,
    OUT po_enforcement_id bigint
)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_add_defendant_account_enforcement.sql
*
* DESCRIPTION : Procedure to add a new enforcement action for a defendant account.
*               Creates enforcement records and updates defendant account fields as required.
*               Calls audit procedures to track changes to auditable fields.
*               Implements concurrency check via version number checking.
*
* PARAMETERS : pi_result_id               - The incoming result_id which is the same as the enforcement action
*            : pi_defendant_account_id    - The Opal defendant account id
*            : pi_business_unit_id        - Business unit identifier
*            : pi_record_type             - For audit, this will be 'defendant_accounts'
*            : pi_case_reference          - For audit, case reference if set by Case Management
*            : pi_function_code           - For audit, the function from which the Amendment was made
*            : pi_jail_days               - Number of days in jail the defendant will spend in default of payment
*            : pi_posted_by               - User ID submitting the request
*            : pi_posted_by_name          - User name submitting the request
*            : pi_reason                  - The reason for this enforcement action
*            : pi_enforcer_id             - The enforcer/process server for this enforcement action
*            : pi_result_responses        - The result parameters
*            : pi_earliest_release_date   - The earliest release date for a PRIS enforcement action
*            : pi_version_number          - Current version number for concurrency check
*            : po_enforcement_id          - Returns the ID of the new enforcement created
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------
* 12/09/2025    C Cho       1.0         PO-1790 Initial version - Create add defendant account enforcement procedure.
* 24/09/2025    C Cho       1.1         PO-2130 Add concurrency check via version number parameter.
* 17/03/2026    TMc         2.0         PO-2930 Amended INSERT statements for DOCUMENT_INSTANCES and REPORT_ENTRIES,
*                                               set ASSOCIATED_RECORD_TYPE to a valid ENUM value (enforcements)
* 22/05/2026    C Cho       2.1         PO-2596 Pass pi_posted_by_name to p_audit_finalise.
*
**/
DECLARE
    v_pg_exception_detail            TEXT;
    v_enforcement_id                 BIGINT;
    v_enforcement_action             VARCHAR(6);
    v_generates_warrant              BOOLEAN := FALSE;
    v_enforcer_code                  VARCHAR(4);
    v_current_warrant_ref            VARCHAR(20);
    v_warrant_reference              VARCHAR(20);
    v_current_year                   VARCHAR(2);
    v_current_ref_year               VARCHAR(2);
    v_warrant_register_id            BIGINT;
    v_document_id                    VARCHAR(12);
    v_document_instance_id           BIGINT;
    v_fsn_date                       TIMESTAMP;
    v_fine_reg_date                  TIMESTAMP;
    v_confiscation_date              TIMESTAMP;
    v_serial_part                    VARCHAR(5);
    v_current_serial                 INTEGER;
    v_rows_updated                   INTEGER;

BEGIN
    RAISE INFO 'p_add_defendant_account_enforcement: Starting with pi_result_id = %, pi_defendant_account_id = %, pi_version_number = %', pi_result_id, pi_defendant_account_id, pi_version_number;

    -- Validate input parameters
    IF pi_result_id IS NULL THEN
        RAISE EXCEPTION 'Result ID cannot be null'
            USING ERRCODE = 'P4001'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_result_id is required';
    END IF;

    IF pi_defendant_account_id IS NULL THEN
        RAISE EXCEPTION 'Defendant account ID cannot be null'
            USING ERRCODE = 'P4002'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_defendant_account_id is required';
    END IF;

    IF pi_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'Business unit ID cannot be null'
            USING ERRCODE = 'P4003'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_business_unit_id is required';
    END IF;

    IF pi_record_type IS NULL OR pi_record_type != 'defendant_accounts' THEN
        RAISE EXCEPTION 'Invalid record type: %. Must be defendant_accounts', pi_record_type
            USING ERRCODE = 'P4004'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_record_type must be defendant_accounts';
    END IF;

    IF pi_posted_by IS NULL THEN
        RAISE EXCEPTION 'Posted by cannot be null'
            USING ERRCODE = 'P4005'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_posted_by is required';
    END IF;

    IF pi_version_number IS NULL THEN
        RAISE EXCEPTION 'Version number cannot be null'
            USING ERRCODE = 'P4007'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_version_number is required for concurrency control';
    END IF;

    -- The incoming result_id is also the enforcement action
    v_enforcement_action := pi_result_id;

    -- Call audit initialise procedure
    CALL p_audit_initialise(pi_defendant_account_id, pi_record_type);

    -- Get next enforcement ID
    v_enforcement_id := nextval('enforcement_id_seq');
    po_enforcement_id := v_enforcement_id;

    -- Check if this result generates a warrant
    SELECT generates_warrant INTO v_generates_warrant
    FROM results
    WHERE result_id = pi_result_id;

    -- Generate warrant reference if needed
    IF v_generates_warrant = TRUE AND pi_enforcer_id IS NOT NULL THEN
        -- Get enforcer code and current warrant reference sequence
        SELECT enforcer_code, warrant_reference_sequence
        INTO v_enforcer_code, v_current_warrant_ref
        FROM enforcers
        WHERE enforcer_id = pi_enforcer_id;

        IF v_enforcer_code IS NULL THEN
            RAISE EXCEPTION 'Enforcer not found with ID: %', pi_enforcer_id
                USING ERRCODE = 'P4006'
                    , DETAIL = 'p_add_defendant_account_enforcement: enforcer_id not found';
        END IF;

        -- Get current year suffix (last 2 digits)
        v_current_year := RIGHT(EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::TEXT, 2);

        -- Check if we have a current warrant reference and extract year and sequence
        IF v_current_warrant_ref IS NOT NULL AND LENGTH(v_current_warrant_ref) > 0 THEN
            -- Extract the year part from current warrant reference (format: 101/24/00008)
            v_current_ref_year := SPLIT_PART(v_current_warrant_ref, '/', 2);

            -- Check if the year has changed
            IF v_current_ref_year = v_current_year THEN
                -- Same year, increment the sequence number
                v_serial_part := SPLIT_PART(v_current_warrant_ref, '/', 3);
                v_current_serial := v_serial_part::INTEGER + 1;
            ELSE
                -- Different year, reset sequence to 1
                v_current_serial := 1;
            END IF;
        ELSE
            -- No previous warrant reference, start with 1
            v_current_serial := 1;
        END IF;

        -- Generate warrant reference (format: 101/25/00001)
        v_warrant_reference := LPAD(v_enforcer_code, 3, '0') || '/' || v_current_year || '/' || LPAD(v_current_serial::TEXT, 5, '0');

        -- Update enforcer warrant sequence
        UPDATE enforcers
        SET warrant_reference_sequence = v_warrant_reference
        WHERE enforcer_id = pi_enforcer_id;
    END IF;

    -- Insert into ENFORCEMENTS table
    INSERT INTO enforcements (
        enforcement_id,
        defendant_account_id,
        result_id,
        posted_date,
        posted_by,
        posted_by_name,
        reason,
        enforcer_id,
        result_responses,
        warrant_reference,
        earliest_release_date,
        jail_days
    ) VALUES (
        v_enforcement_id,
        pi_defendant_account_id,
        pi_result_id,
        CURRENT_TIMESTAMP,
        pi_posted_by,
        pi_posted_by_name,
        pi_reason,
        pi_enforcer_id,
        pi_result_responses,
        v_warrant_reference,
        CASE WHEN v_enforcement_action = 'PRIS' THEN pi_earliest_release_date ELSE NULL END,
        pi_jail_days
    );

    -- Update DEFENDANT_ACCOUNTS table based on enforcement action
    SELECT further_steps_notice_date, fine_registration_date, confiscation_order_date
    INTO v_fsn_date, v_fine_reg_date, v_confiscation_date
    FROM defendant_accounts
    WHERE defendant_account_id = pi_defendant_account_id;

    UPDATE defendant_accounts
    SET
        last_enforcement = pi_result_id,
        last_movement_date = CURRENT_TIMESTAMP,
        jail_days = CASE WHEN pi_jail_days IS NOT NULL THEN pi_jail_days ELSE jail_days END,
        collection_order = CASE WHEN v_enforcement_action = 'COLLO' THEN TRUE ELSE collection_order END,
        collection_order_date = CASE WHEN v_enforcement_action = 'COLLO' THEN CURRENT_TIMESTAMP ELSE collection_order_date END,
        further_steps_notice_date = CASE
            WHEN v_enforcement_action = 'FSN' AND v_fsn_date IS NULL THEN CURRENT_TIMESTAMP
            ELSE further_steps_notice_date
        END,
        suspended_committal_date = CASE WHEN v_enforcement_action = 'SC' THEN CURRENT_TIMESTAMP ELSE suspended_committal_date END,
        fine_registration_date = CASE
            WHEN v_enforcement_action = 'REGF' AND v_fine_reg_date IS NULL THEN CURRENT_TIMESTAMP
            ELSE fine_registration_date
        END,
        confiscation_order_date = CASE
            WHEN v_enforcement_action = 'CONF' AND v_confiscation_date IS NULL THEN CURRENT_TIMESTAMP
            ELSE confiscation_order_date
        END
    WHERE defendant_account_id = pi_defendant_account_id;

    -- Insert into WARRANT_REGISTER if warrant is generated
    IF v_generates_warrant = TRUE AND pi_enforcer_id IS NOT NULL THEN
        v_warrant_register_id := nextval('warrant_register_id_seq');

        INSERT INTO warrant_register (
            warrant_register_id,
            business_unit_id,
            enforcer_id,
            enforcement_id
        ) VALUES (
            v_warrant_register_id,
            pi_business_unit_id,
            pi_enforcer_id,
            v_enforcement_id
        );
    END IF;

    -- Insert into DOCUMENT_INSTANCES for any result documents
    FOR v_document_id IN (
        SELECT document_id
        FROM result_documents
        WHERE result_id = pi_result_id
    ) LOOP
        v_document_instance_id := nextval('document_instance_id_seq');

        INSERT INTO document_instances (
            document_instance_id,
            document_id,
            business_unit_id,
            generated_date,
            generated_by,
            associated_record_type,
            associated_record_id,
            status,
            printed_date,
            document_content
        ) VALUES (
            v_document_instance_id,
            v_document_id,
            pi_business_unit_id,
            CURRENT_TIMESTAMP,
            pi_posted_by,
            'enforcements',
            v_enforcement_id,
            'New',
            NULL,
            NULL
        );
    END LOOP;

    -- Insert into REPORT_ENTRIES if warrant is generated
    IF v_generates_warrant = TRUE THEN
        INSERT INTO report_entries (
            report_entry_id,
            business_unit_id,
            report_id,
            entry_timestamp,
            associated_record_type,
            associated_record_id
        ) VALUES (
            nextval('report_entry_id_seq'),
            pi_business_unit_id,
            'warrant_register',
            CURRENT_TIMESTAMP,
            'enforcements',
            v_enforcement_id
        );
    END IF;

    -- Call audit finalise procedure
    CALL p_audit_finalise(
        pi_defendant_account_id,
        pi_record_type,
        pi_business_unit_id,
        pi_posted_by,
        pi_posted_by_name,
        pi_case_reference,
        pi_function_code
    );

    -- Increment version number by 1
    UPDATE defendant_accounts
    SET version_number = version_number + 1
    WHERE defendant_account_id = pi_defendant_account_id
      AND version_number = pi_version_number;

    GET DIAGNOSTICS v_rows_updated = ROW_COUNT;

    IF v_rows_updated = 0 THEN
        RAISE EXCEPTION 'Some information on this page may be out of date.'
            USING ERRCODE = 'P4008'
                , DETAIL = 'p_add_defendant_account_enforcement: Concurrency check failed - version number mismatch';
    END IF;

    RAISE INFO 'p_add_defendant_account_enforcement: Successfully completed. Created enforcement_id = %', v_enforcement_id;

EXCEPTION
    WHEN SQLSTATE 'P4001' OR SQLSTATE 'P4002' OR SQLSTATE 'P4003' OR
         SQLSTATE 'P4004' OR SQLSTATE 'P4005' OR SQLSTATE 'P4006' OR
         SQLSTATE 'P4007' OR SQLSTATE 'P4008' THEN
        -- When custom exceptions just re-raise them so they're not manipulated
        RAISE NOTICE 'Error in p_add_defendant_account_enforcement: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        -- Output full exception details
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_add_defendant_account_enforcement: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_add_defendant_account_enforcement: % - %', SQLSTATE, SQLERRM
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_add_defendant_account_enforcement(IN pi_result_id character varying, IN pi_defendant_account_id bigint, IN pi_business_unit_id smallint, IN pi_record_type character varying, IN pi_case_reference character varying, IN pi_function_code character varying, IN pi_jail_days integer, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_reason character varying, IN pi_enforcer_id bigint, IN pi_result_responses json, IN pi_earliest_release_date timestamp without time zone, IN pi_version_number bigint, OUT po_enforcement_id bigint); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_add_defendant_account_enforcement(IN pi_result_id character varying, IN pi_defendant_account_id bigint, IN pi_business_unit_id smallint, IN pi_record_type character varying, IN pi_case_reference character varying, IN pi_function_code character varying, IN pi_jail_days integer, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_reason character varying, IN pi_enforcer_id bigint, IN pi_result_responses json, IN pi_earliest_release_date timestamp without time zone, IN pi_version_number bigint, OUT po_enforcement_id bigint) IS 'Procedure to add a new enforcement action for a defendant account with audit tracking, associated record creation, and concurrency check.';
