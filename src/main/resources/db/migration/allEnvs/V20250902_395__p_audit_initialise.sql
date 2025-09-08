CREATE OR REPLACE PROCEDURE p_audit_initialise(
    IN pi_associated_account_id     BIGINT,
    IN pi_record_type               CHARACTER VARYING
)
LANGUAGE 'plpgsql'
AS 
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_audit_initialise.sql
*
* DESCRIPTION : Procedure to be called at the start of the session to fetch the initial values of the auditable amendment data item fields.
*               Creates temporary tables to store initial data values which will later be used by p_audit_finalise to check for changes.
*
* PARAMETERS : pi_associated_account_id  - The Opal defendant account id or creditor account id
*            : pi_record_type            - Can be 'defendant_accounts' or 'creditor_accounts' to determine which view to query
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 28/08/2025    C Cho       1.0         PO-1666 Initial version - Create audit initialise procedure.
*
**/
DECLARE
    v_pg_exception_detail            TEXT;
    v_record_count                   INTEGER;
    
BEGIN
    RAISE INFO 'p_audit_initialise: Starting with pi_associated_account_id = %, pi_record_type = %', pi_associated_account_id, pi_record_type;

    -- Validate input parameters
    IF pi_associated_account_id IS NULL THEN
        RAISE EXCEPTION 'Associated account ID cannot be null'
            USING ERRCODE = 'P3001'
                , DETAIL = 'p_audit_initialise: pi_associated_account_id is required';
    END IF;

    IF pi_record_type IS NULL OR pi_record_type NOT IN ('defendant_accounts', 'creditor_accounts') THEN
        RAISE EXCEPTION 'Invalid record type: %. Must be defendant_accounts or creditor_accounts', pi_record_type
            USING ERRCODE = 'P3002'
                , DETAIL = 'p_audit_initialise: pi_record_type must be defendant_accounts or creditor_accounts';
    END IF;

    -- Process based on record type
    IF pi_record_type = 'defendant_accounts' THEN
        
        -- Drop temp table if it exists from previous session
        DROP TABLE IF EXISTS temp_def_ac_amendment_list;
        
        -- Create temporary table for defendant accounts
        CREATE TEMP TABLE temp_def_ac_amendment_list AS
        SELECT 
            -- Defendant Accounts fields
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
            -- Defendant Party fields
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
            -- Parent/Guardian Party fields
            pname,
            paddr1,
            paddr2,
            paddr3,
            pbdate,
            pninumber,
            -- Aliases fields
            alias1,
            alias2,
            alias3,
            alias4,
            alias5,
            -- Debtor Details fields
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
                USING ERRCODE = 'P3003'
                    , DETAIL = 'p_audit_initialise: defendant_account_id not found';
        END IF;

        RAISE INFO 'p_audit_initialise: Created temp_def_ac_amendment_list with % records', v_record_count;

    ELSIF pi_record_type = 'creditor_accounts' THEN
        
        -- Drop temp table if it exists from previous session
        DROP TABLE IF EXISTS temp_cred_ac_amendment_list;
        
        -- Create temporary table for creditor accounts
        CREATE TEMP TABLE temp_cred_ac_amendment_list AS
        SELECT 
            -- Party fields
            name,
            address_line_1,
            address_line_2,
            address_line_3,
            postcode,
            -- Creditor Accounts fields
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
                USING ERRCODE = 'P3004'
                    , DETAIL = 'p_audit_initialise: creditor_account_id not found';
        END IF;

        RAISE INFO 'p_audit_initialise: Created temp_cred_ac_amendment_list with % records', v_record_count;

    END IF;

    RAISE INFO 'p_audit_initialise: Successfully completed for record_type = %', pi_record_type;

EXCEPTION
    WHEN SQLSTATE 'P3001' OR SQLSTATE 'P3002' OR SQLSTATE 'P3003' OR SQLSTATE 'P3004' THEN
        -- When custom exceptions just re-raise them so they're not manipulated
        RAISE NOTICE 'Error in p_audit_initialise: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        -- Output full exception details
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_audit_initialise: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_audit_initialise: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_audit_initialise
    IS 'Procedure to fetch initial values of auditable amendment data fields and store them in temporary tables for later comparison.';