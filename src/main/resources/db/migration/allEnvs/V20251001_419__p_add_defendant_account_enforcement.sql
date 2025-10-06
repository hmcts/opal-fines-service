CREATE OR REPLACE PROCEDURE p_add_defendant_account_enforcement(
    IN pi_result_id                 results.result_id%TYPE,
    IN pi_defendant_account_id      defendant_accounts.defendant_account_id%TYPE,
    IN pi_business_unit_id          business_units.business_unit_id%TYPE,
    IN pi_record_type               CHARACTER VARYING,
    IN pi_case_reference            amendments.case_reference%TYPE,
    IN pi_function_code             amendments.function_code%TYPE,
    IN pi_jail_days                 defendant_accounts.jail_days%TYPE,
    IN pi_posted_by                 VARCHAR(20),
    IN pi_posted_by_name            VARCHAR(100),
    IN pi_reason                    enforcements.reason%TYPE,
    IN pi_enforcer_id               enforcers.enforcer_id%TYPE,
    IN pi_result_responses          enforcements.result_responses%TYPE,
    IN pi_earliest_release_date     enforcements.earliest_release_date%TYPE,
    IN pi_version_number            defendant_accounts.version_number%TYPE,
    OUT po_enforcement_id           enforcements.enforcement_id%TYPE
)
LANGUAGE 'plpgsql'
AS 
$BODY$
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
* ----------    -------     --------    -------------------------------------------------------------------------
* 12/09/2025    C Cho       1.0         PO-1790 Initial version - Create add defendant account enforcement procedure.
* 24/09/2025    C Cho       1.1         PO-2130 Add concurrency check via version number parameter.
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
            'Enforcement',
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
            'Enforcement',
            v_enforcement_id
        );
    END IF;

    -- Call audit finalise procedure
    CALL p_audit_finalise(
        pi_defendant_account_id,
        pi_record_type,
        pi_business_unit_id,
        pi_posted_by,
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
$BODY$;

COMMENT ON PROCEDURE p_add_defendant_account_enforcement
    IS 'Procedure to add a new enforcement action for a defendant account with audit tracking, associated record creation, and concurrency check.';