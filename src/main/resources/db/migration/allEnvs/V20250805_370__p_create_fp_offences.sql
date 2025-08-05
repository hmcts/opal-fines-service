CREATE OR REPLACE PROCEDURE p_create_fp_offences (
    IN  pi_defendant_account_id     defendant_accounts.defendant_account_id%TYPE,
    IN  pi_account_type             defendant_accounts.account_type%TYPE,
    IN  pi_fp_ticket_detail_json    JSON
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_fp_offences.sql
*
* DESCRIPTION : Insert record into FIXED_PENALTY_OFFENCES for the defendant.
*               Throws 'P2011 - Missing ticket number' exception if account type is 'fixed penalty' and the ticket number is not present in the passed Json object.
*
* PARAMETERS  : pi_defendant_account_id  - The Opal defendant account id associated with the fp_ticket_detail Json object.
*               pi_account_type          - The account type of the associated defendant_accounts record.
*               pi_fp_ticket_detail_json - The fixed penalty offence Json object related to the dedendant.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------
* 25/07/2025    TMc         1.0         PO-1037 - Insert record into FIXED_PENALTY_OFFENCES for the defendant. 
*
**/
DECLARE
    v_pg_exception_detail   TEXT;
    v_vehicle_fixed_penalty BOOLEAN;
    v_ticket_number         VARCHAR := NULL;
BEGIN
    
    v_vehicle_fixed_penalty := (LOWER(pi_account_type) = 'fixed penalty');
    v_ticket_number := pi_fp_ticket_detail_json ->> 'notice_number';

    --Check if the account type is Fixed Penalty and a ticket_number (i.e. notice_number) is present in the passed Json object
    IF v_vehicle_fixed_penalty AND v_ticket_number IS NULL THEN

        --Raise custom exception
        RAISE EXCEPTION 'Missing ticket number' 
            USING ERRCODE = 'P2011'
                , DETAIL = 'p_create_fp_offences: pi_defendant_account_id = ' || pi_defendant_account_id || ', pi_account_type = ' || pi_account_type;

    END IF;

    --Check if passed Json is NULL
    IF pi_fp_ticket_detail_json IS NULL OR JSON_TYPEOF(pi_fp_ticket_detail_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_fp_offences: There were no FP ticket details to process for defendant_account_id = %', pi_defendant_account_id;

    ELSE 
    
        --Insert the FIXED_PENALTY_OFFENCES record
        INSERT INTO fixed_penalty_offences (
              defendant_account_id
            , ticket_number
            , vehicle_registration
            , offence_location
            , notice_number
            , issued_date
            , licence_number
            , vehicle_fixed_penalty
            , offence_date
            , offence_time)
        VALUES (
              pi_defendant_account_id
            , v_ticket_number
            , pi_fp_ticket_detail_json ->> 'fp_registration_number'
            , pi_fp_ticket_detail_json ->> 'place_of_offence'
            , pi_fp_ticket_detail_json ->> 'notice_to_owner_hirer'
            , TO_TIMESTAMP(pi_fp_ticket_detail_json ->> 'date_of_issue', 'YYYY-MM-DD')
            , pi_fp_ticket_detail_json ->> 'fp_driving_licence_number'
            , v_vehicle_fixed_penalty
            , NULL  --offence_date
            , pi_fp_ticket_detail_json ->> 'time_of_issue' --offence_time
            --, TO_DATE(pi_fp_ticket_detail_json ->> 'date_of_offence', 'YYYY-MM-DD')  --offence_date: Json needs updating before doing this
            --, pi_fp_ticket_detail_json ->> 'time_of_offence'                         --offence_time: Json needs updating before doing this 
        );
        
        RAISE INFO 'p_create_fp_offences: Created fixed_penalty_offences record for defendant_account_id = %', pi_defendant_account_id;
    END IF;

EXCEPTION
    WHEN SQLSTATE 'P2011' THEN
        --When custom exception just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_fp_offences: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        --Log error and re-raise the exception ensuring the caller has access to the complete exception details.
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_fp_offences: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_fp_offences: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_fp_offences 
    IS 'Procedure to insert a record into FIXED_PENALTY_OFFENCES for the defendant fp_ticket_detail Json object.';
