CREATE OR REPLACE PROCEDURE p_get_creditor_account (
    IN  pi_business_unit_id            draft_accounts.business_unit_id%TYPE,
    IN  pi_imposition_json             JSON,
    OUT po_creditor_account_id         creditor_accounts.creditor_account_id%TYPE,
    OUT po_is_minor_creditor           BOOLEAN,
    OUT po_results_mapped_item_number  control_totals.item_number%TYPE
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_get_creditor_account.sql
*
* DESCRIPTION : Retrieves or Creates the CREDITOR_ACCOUNT details, including minor_creditor PARTIES record(s).
*
* PARAMETERS  : pi_business_unit_id           - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*               pi_imposition_json            - The offence -> impositions -> imposition Json object
*               po_creditor_account_id        - The found or newly created creditor_account_id
*               po_is_minor_creditor          - Whether or not the creditor account is for a minor creditor
*               po_results_mapped_item_number - The resutls.imposition_category mapped to an item_number and returned
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------------------------
* 29/07/2025    TMc         1.0         PO-1043 - Retrieves or Creates the CREDITOR_ACCOUNT details, including minor_creditor PARTIES record(s).
*
**/
DECLARE
	v_pg_exception_detail	TEXT;
    v_result_id             results.result_id%TYPE;
    v_major_creditor_id     creditor_accounts.major_creditor_id%TYPE;
    v_minor_creditor_json   JSON;

    v_results_imposition_creditor   results.imposition_creditor%TYPE;
BEGIN
    po_is_minor_creditor := FALSE;

    --Extract details from Impositions Json object
    v_result_id           := pi_imposition_json ->> 'result_id';
    v_major_creditor_id   := (pi_imposition_json ->> 'major_creditor_id')::BIGINT;
    v_minor_creditor_json := json_extract_path(pi_imposition_json, 'minor_creditor');

    --Retrieve the RESULTS record and map to an item_number    ***** Should this be in a mapping table as per note on MAC confluence page?
    BEGIN
        SELECT imposition_creditor
             , CASE imposition_category
                    WHEN 'Crown Prosecution Costs'         THEN 301
                    WHEN 'Court Charge'                    THEN 805
                    WHEN 'Costs'                           THEN 213
                    WHEN 'Fines'                           THEN 208
                    WHEN 'Witness Expenses & Central Fund' THEN 212
                    WHEN 'Victim Surcharge'                THEN 405
                    WHEN 'Compensation'                    THEN 214
                    WHEN 'Legal Aid'                       THEN 209
                    ELSE NULL
               END CASE
        INTO STRICT v_results_imposition_creditor, po_results_mapped_item_number
          FROM results
         WHERE result_id = v_result_id
           AND imposition = TRUE;
    EXCEPTION 
        --When NO_DATA_FOUND or TOO_MANY_ROWS raise custom exception otherwise re-raise the original
        WHEN SQLSTATE 'P0002' OR SQLSTATE 'P0003' THEN
            RAISE EXCEPTION 'Result % is not valid', v_result_id
                USING ERRCODE = 'P2004'
                    , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id;
        WHEN OTHERS THEN
            RAISE;
    END;

    --Retrieve the CREDITOR_ACCOUNT record, if possible
    BEGIN
        IF v_results_imposition_creditor = 'CF' THEN
    
            SELECT creditor_account_id
            INTO STRICT po_creditor_account_id
              FROM creditor_accounts  
             WHERE business_unit_id      = pi_business_unit_id 
               AND creditor_account_type = 'CF';
    
        ELSIF v_results_imposition_creditor = 'CPS' THEN
    
            SELECT creditor_account_id
            INTO STRICT po_creditor_account_id
              FROM creditor_accounts  
             WHERE business_unit_id      = pi_business_unit_id 
               AND creditor_account_type = 'MJ'
               AND prosecution_service   = TRUE;
    
        ELSIF v_results_imposition_creditor = '!CPS' THEN
    
            IF v_major_creditor_id IS NULL THEN
                
                --New minor creditor account & parties will be created
                po_creditor_account_id := NULL;

            ELSE
                
                SELECT creditor_account_id
                INTO STRICT po_creditor_account_id
                  FROM creditor_accounts  
                 WHERE business_unit_id      = pi_business_unit_id 
                   AND creditor_account_type = 'MJ'
                   AND prosecution_service   = FALSE
                   AND major_creditor_id     = v_major_creditor_id;

            END IF;
    
        ELSIF v_results_imposition_creditor = 'Any' THEN
    
            IF v_major_creditor_id IS NULL THEN
                
                --New minor creditor account & parties will be created
                po_creditor_account_id := NULL;

            ELSE
                
                SELECT creditor_account_id
                INTO STRICT po_creditor_account_id
                  FROM creditor_accounts  
                 WHERE business_unit_id      = pi_business_unit_id 
                   AND creditor_account_type = 'MJ'
                   AND major_creditor_id     = v_major_creditor_id;

            END IF;
        
        ELSE
            -- Should not happen but raise exception if it does
            RAISE EXCEPTION 'Result % is not valid', v_result_id
                USING ERRCODE = 'P2004'
                    , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id;
        END IF;
    EXCEPTION 
        --When NO_DATA_FOUND or TOO_MANY_ROWS raise custom exception otherwise re-raise the original
        WHEN SQLSTATE 'P0002' OR SQLSTATE 'P0003' THEN
            If v_major_creditor_id IS NULL THEN
                RAISE EXCEPTION 'Creditor not found' 
                    USING ERRCODE = 'P2006'
                        , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id || ', imposition_creditor = ' || v_results_imposition_creditor;
            ELSE 
                RAISE EXCEPTION 'Creditor % not found', v_major_creditor_id
                    USING ERRCODE = 'P2006'
                        , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id || ', imposition_creditor = ' || v_results_imposition_creditor;
            END IF;
        WHEN OTHERS THEN
            RAISE;
    END;

    IF po_creditor_account_id IS NULL THEN

        --Create new minor creditor account & parties, if passed minor_creditor Json is present
        IF v_minor_creditor_json IS NULL OR JSON_TYPEOF(v_minor_creditor_json) = 'null' THEN
            --Raise custom exception
            RAISE EXCEPTION 'Missing creditor' 
                USING ERRCODE = 'P2005'
                    , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id || ', imposition_creditor = ' || v_results_imposition_creditor;
        ELSE
            po_is_minor_creditor := TRUE;            

            CALL p_create_minor_creditor ( pi_business_unit_id,
                                           v_minor_creditor_json,
                                           po_creditor_account_id
            );

            RAISE INFO 'p_get_creditor_account: Created creditor_account_id = % - result_id = %, imposition_creditor = %', po_creditor_account_id, v_result_id, v_results_imposition_creditor;

        END IF;
    ELSE
        RAISE INFO 'p_get_creditor_account: Found creditor_account_id = % - result_id = %, imposition_creditor = %', po_creditor_account_id, v_result_id, v_results_imposition_creditor;
    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2004' OR SQLSTATE 'P2005' OR SQLSTATE 'P2006' OR SQLSTATE 'P2010' THEN
        --When custom exceptions just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_get_creditor_account: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_get_creditor_account: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_get_creditor_account: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_get_creditor_account 
    IS 'Retrieves or Creates the CREDITOR_ACCOUNT details, including minor_creditor PARTIES record(s).';
