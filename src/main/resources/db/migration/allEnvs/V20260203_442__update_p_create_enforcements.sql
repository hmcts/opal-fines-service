CREATE OR REPLACE PROCEDURE p_create_enforcements (
    IN pi_defendant_account_id  defendant_accounts.defendant_account_id%TYPE,
    IN pi_posted_by             enforcements.posted_by%TYPE,            
    IN pi_posted_by_name        enforcements.posted_by_name%TYPE,
    IN pi_enforcements_json     JSON,
    OUT po_last_enforcement     defendant_accounts.last_enforcement%TYPE
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_enforcements.sql
*
* DESCRIPTION : Process the enforcements Json object for the related defendant
*               It also updates DEFENDANT_ACCOUNTS (last_enforcement) as well as returning it
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_posted_by            - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*               pi_posted_by_name       - The user that is submitting the request to be passed in by the backend
*               pi_enforcements_json    - The defendant enforcements Json array object
*               po_last_enforcement     - The last result_id processed
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------
* 30/07/2025    TMc         1.0         PO-1034 - Process the enforcements Json object for the related defendant
* 03/02/2026    C Cho       1.1         PO-2454, PO-2455 - Replace account_type with enforcement_account_type.
*
**/
DECLARE
	v_pg_exception_detail	TEXT;
    v_enforcement_item_json JSON;
    v_result_id             enforcements.result_id%TYPE;
    v_enforcements_count    INTEGER := 0;
BEGIN

    --Check if the passed Json is NULL
    IF pi_enforcements_json IS NULL OR JSON_TYPEOF(pi_enforcements_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_enforcements: There were no enforcements to process for defendant_account_id = %', pi_defendant_account_id;

    ELSE 
        
        --Loop through each enforcement, in result_id order: COLLO, PRIS, NOENF
        FOR v_enforcement_item_json IN
            SELECT value
              FROM JSON_ARRAY_ELEMENTS(pi_enforcements_json) AS t(value)
            ORDER BY CASE value ->> 'result_id'
                        WHEN 'COLLO' THEN 1
                        WHEN 'PRIS'  THEN 2
                        WHEN 'NOENF' THEN 3
                        ELSE 999
                     END
        LOOP
            v_enforcements_count := v_enforcements_count + 1;
            --Keep track of the result_id for the last enforcement
            v_result_id := v_enforcement_item_json ->> 'result_id';
            
            --Insert ENFORCEMENTS record
            INSERT INTO enforcements (
                  enforcement_id
                , defendant_account_id
                , posted_date
                , posted_by
                , result_id
                , reason
                , enforcer_id
                , jail_days
                , result_responses
                , warrant_reference
                , case_reference
                , hearing_date
                , hearing_court_id
                , enforcement_account_type
                , posted_by_name
            )
            VALUES (
                  NEXTVAL('enforcement_id_seq')
                , pi_defendant_account_id
                , CLOCK_TIMESTAMP()
                , pi_posted_by
                , v_result_id
                , NULL --reason
                , NULL --enforcer_id
                , NULL --jail_days
                , v_enforcement_item_json -> 'enforcement_result_responses'   --This stores only the Json array, without the "enforcement_result_responses" wrapper
                , NULL --warrant_reference
                , NULL --case_reference
                , NULL --hearing_date
                , NULL --hearing_court_id
                , NULL --enforcement_account_type
                , pi_posted_by_name
            );
            
        END LOOP;

        RAISE INFO 'p_create_enforcements: last enforcement result ID = %', v_result_id;
        RAISE INFO 'p_create_enforcements: Inserted % enforcements records for defendant_account_id = %', v_enforcements_count, pi_defendant_account_id;

        --Update the DEFENDANT_ACCOUNTS table with last_enforcement
        UPDATE defendant_accounts
           SET last_enforcement = v_result_id
         WHERE defendant_account_id = pi_defendant_account_id
        RETURNING last_enforcement INTO STRICT po_last_enforcement;  --Returning STRICT to ensure 1 record is found and updated;

        RAISE INFO 'p_create_enforcements: defendant_accounts.last_enforcement [%] has been updated. defendant_account_id = %', po_last_enforcement, pi_defendant_account_id;

    END IF;

EXCEPTION 
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_enforcements: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_enforcements: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_enforcements
    IS 'Procedure to process the enforcements Json object for the related defendant';
