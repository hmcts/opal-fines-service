CREATE OR REPLACE PROCEDURE p_create_aliases (
    IN pi_party_id       parties.party_id%TYPE,
    IN pi_aliases_json   JSON
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_aliases.sql
*
* DESCRIPTION : Insert records into ALIASES for the defendant or parent/guardian debtor_details --> aliases Json object.
*
* PARAMETERS  : pi_party_id     - The Opal party id of the associated party of the parent debtor details Json object.
*               pi_aliases_json - The dedendant or parent/guardian debtor_details --> aliases Json object
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------------
* 24/07/2025    TMc         1.0         PO-1043 - Insert records into ALIASES for the defendant or parent/guardian debtor_details --> aliases Json object. 
*
**/
DECLARE
    v_pg_exception_detail   TEXT;
    v_alias_json            JSON;
    v_alias_count           INTEGER := 0;
BEGIN

    --Insert the ALIASES record(s), if the Json passed is not NULL
    IF pi_aliases_json IS NULL OR JSON_TYPEOF(pi_aliases_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_aliases: There were no aliases to process for party_id = %', pi_party_id;

    ELSE 
    
        FOR v_alias_json IN SELECT JSON_ARRAY_ELEMENTS(pi_aliases_json)
        LOOP
            v_alias_count := v_alias_count + 1;
            
            INSERT INTO aliases (
                  alias_id
                , party_id
                , surname
                , forenames
                --, initials
                , sequence_number
                , organisation_name)
            VALUES (
                  NEXTVAL('alias_id_seq')
                , pi_party_id
                , v_alias_json ->> 'alias_surname'
                , v_alias_json ->> 'alias_forenames'
                --, NULL  --Initials not required
                , v_alias_count --sequence_number
                , v_alias_json ->> 'alias_company_name'
            );
        END LOOP;
        
        RAISE INFO 'p_create_aliases: Created aliases for party_id = % - Alias count = %', pi_party_id, v_alias_count;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        --Log error and re-raise the exception ensuring the caller has access to the complete exception details.
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_aliases: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        --RAISE;
        RAISE EXCEPTION 'Error in p_create_aliases: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_aliases 
    IS 'Procedure to insert records into ALIASES for the defendant or parent/guardian debtor_details-aliases Json object.';
