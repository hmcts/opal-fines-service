CREATE OR REPLACE PROCEDURE p_create_debtor_details (
    IN  pi_party_id             parties.party_id%TYPE,
    IN  pi_debtor_detail_json   JSON
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_debtor_details.sql
*
* DESCRIPTION : Insert a DEBTOR_DETAIL record for the defendant or parent/guardian debtor_details Json object.
*
* PARAMETERS  : pi_party_id           - The Opal party id of the associated debtor details
*               pi_debtor_detail_json - The dedendant or parent/guardian debtor_details Json object
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------------------------
* 23/07/2025    TMc         1.0         PO-1043 - Insert a DEBTOR_DETAIL record for the defendant or parent/guardian debtor_details Json object. 
*
**/
DECLARE
    v_pg_exception_detail   TEXT;
    v_aliases_json          JSON := NULL;
    --v_document_language_src VARCHAR;
    --v_hearing_language_src  VARCHAR;
    v_document_language     VARCHAR;
    v_hearing_language      VARCHAR;
BEGIN

    --Insert the DEBTOR_DETAIL record, if the Json passed is not NULL

    IF pi_debtor_detail_json IS NULL OR JSON_TYPEOF(pi_debtor_detail_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_debtor_details: There were no debtor details to process for party_id = %', pi_party_id;

    ELSE 
        INSERT INTO debtor_detail (
              party_id
            , vehicle_make
            , vehicle_registration
            , employer_name
            , employer_address_line_1
            , employer_address_line_2
            , employer_address_line_3
            , employer_address_line_4
            , employer_address_line_5
            , employer_postcode
            , employee_reference
            , employer_telephone
            , employer_email
            , document_language
            , document_language_date
            , hearing_language
            , hearing_language_date
        )
        VALUES (
              pi_party_id
            , pi_debtor_detail_json ->> 'vehicle_make'
            , pi_debtor_detail_json ->> 'vehicle_registration_mark'
            , pi_debtor_detail_json ->> 'employer_company_name'
            , pi_debtor_detail_json ->> 'employer_address_line_1'
            , pi_debtor_detail_json ->> 'employer_address_line_2'
            , pi_debtor_detail_json ->> 'employer_address_line_3'
            , pi_debtor_detail_json ->> 'employer_address_line_4'
            , pi_debtor_detail_json ->> 'employer_address_line_5'
            , pi_debtor_detail_json ->> 'employer_post_code'
            , pi_debtor_detail_json ->> 'employee_reference'
            , pi_debtor_detail_json ->> 'employer_telephone_number'
            , pi_debtor_detail_json ->> 'employer_email_address'
            , pi_debtor_detail_json ->> 'document_language'
            , CURRENT_TIMESTAMP
            , pi_debtor_detail_json ->> 'hearing_language'
            , CURRENT_TIMESTAMP
        );

        RAISE INFO 'p_create_debtor_details: Created debtor_detail record for party_id = %', pi_party_id;

        --Call p_create_aliases to insert the related ALIASES record(s), for this debtor_details, if the Json object exists
        v_aliases_json := json_extract_path(pi_debtor_detail_json, 'aliases');
    
        CALL p_create_aliases( pi_party_id,
                               v_aliases_json
        );

    END IF;

EXCEPTION
    WHEN OTHERS THEN
        --Log error and re-raise the exception ensuring the caller has access to the complete exception details.
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_debtor_details: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_debtor_details: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_debtor_details 
    IS 'Procedure to insert a DEBTOR_DETAIL record for the defendant or parent/guardian debtor_details Json object.';
