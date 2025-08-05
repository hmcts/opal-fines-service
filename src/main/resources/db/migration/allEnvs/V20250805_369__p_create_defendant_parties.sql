CREATE OR REPLACE PROCEDURE p_create_defendant_parties (
    IN  pi_defendant_account_id defendant_accounts.defendant_account_id%TYPE,
    IN  pi_defendant_type       VARCHAR,
    IN  pi_defendant_json       JSON
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_defendant_parties.sql
*
* DESCRIPTION : Process the defendant Json and insert PARTIES, DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAIL and ALIASES records for the defendant and parent/guardian.
*               Throws 'P2002 - Missing parent/guardian' exception if defendant_type = 'parentOrGuardianToPay' and the parent_guardian Json object is not present.
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_defendant_type       - Type of the defendant account - adultOrYouthOnly, parentOrGuardianToPay, company
*               pi_defendant_json       - The dedendant Json object from the DRAFT_ACCOUNTS.ACCOUNT Json
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------
* 22/07/2025    TMc         1.0         PO-1043 - Process the defendant Json and insert PARTIES, DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAIL and ALIASES
*                                                 records for the defendant and parent/guardian.
*                           1.1         Corrected typo in INSERT INTO parties statement
*
**/
DECLARE
    c_account_type_defendant        CONSTANT    parties.account_type%TYPE := 'Defendant';
    c_association_type_defendant    CONSTANT    defendant_account_parties.association_type%TYPE := 'Defendant';
    c_association_type_pg           CONSTANT    defendant_account_parties.association_type%TYPE := 'Parent/Guardian';
    c_defendant_type_pgToPay        CONSTANT    VARCHAR := 'parentOrGuardianToPay';

    v_pg_exception_detail           TEXT;
    v_party_id_defendant            parties.party_id%TYPE := NULL;
    v_party_id_pg                   parties.party_id%TYPE := NULL;

    v_def_pg_json                   JSON := NULL;
    v_debtor_detail_def_json        JSON := NULL;
    v_debtor_detail_pg_json         JSON := NULL;
BEGIN

    -----------------------------------------
    --Process the defendant information
    -----------------------------------------

    --Insert record for the defendant into the PARTIES table
    INSERT INTO parties (
          party_id
        , organisation
        , organisation_name
        , surname
        , forenames
        --, initials
        , title
        , address_line_1
        , address_line_2
        , address_line_3
        , address_line_4
        , address_line_5
        , postcode
        , account_type
        , birth_date
        , age
        , national_insurance_number
        , telephone_home
        , telephone_business
        , telephone_mobile
        , email_1
        , email_2
        , last_changed_date
        /*  Not required for Release 1A
         , driving_licence_number
         , pnc_id
         , nationality1
         , nationality2
         , self_defined_ethnicity
         , observed_ethnicity
         , cro_number
         , occupation
         , gender
         , custody_status
         , prison_number
         , interpreter_language_needs
        */
    )
    VALUES ( 
          NEXTVAL('party_id_seq')
        , (pi_defendant_json ->> 'company_flag')::BOOLEAN
        , pi_defendant_json ->> 'company_name'
        , pi_defendant_json ->> 'surname'
        , pi_defendant_json ->> 'forenames'
        --, NULL  --Initials not required
        , pi_defendant_json ->> 'title'
        , pi_defendant_json ->> 'address_line_1'
        , pi_defendant_json ->> 'address_line_2'
        , pi_defendant_json ->> 'address_line_3'
        , pi_defendant_json ->> 'address_line_4'
        , pi_defendant_json ->> 'address_line_5'
        , pi_defendant_json ->> 'post_code'
        , c_account_type_defendant
        , TO_TIMESTAMP(pi_defendant_json ->> 'dob', 'YYYY-MM-DD')
        , NULL --age
        , pi_defendant_json ->> 'national_insurance_number'
        , pi_defendant_json ->> 'telephone_number_home'
        , pi_defendant_json ->> 'telephone_number_business'
        , pi_defendant_json ->> 'telephone_number_mobile'
        , pi_defendant_json ->> 'email_address_1'
        , pi_defendant_json ->> 'email_address_2'
        , NULL --last_changed_date
        /*  Not required for Release 1A
         , pi_defendant_json ->> 'driving_licence_number'
         , pi_defendant_json ->> 'pnc_id'
         , pi_defendant_json ->> 'nationality_1'
         , pi_defendant_json ->> 'nationality_2'
         , pi_defendant_json ->> 'ethnicity_self_defined'
         , pi_defendant_json ->> 'ethnicity_observed'
         , pi_defendant_json ->> 'cro_number'
         , pi_defendant_json ->> 'occupation'
         , pi_defendant_json ->> 'gender'
         , pi_defendant_json ->> 'custody_status'
         , pi_defendant_json ->> 'prison_number'
         , pi_defendant_json ->> 'interpreter_lang'
        */
    )
    RETURNING party_id
    INTO      v_party_id_defendant;

    RAISE INFO 'p_create_defendant_parties: Created parties record for the defendant. pi_defendant_account_id = %, party_id = %', pi_defendant_account_id, v_party_id_defendant;

    --Insert the related DEFENDANT_ACCOUNT_PARTIES record for the defendant
    INSERT INTO defendant_account_parties (
          defendant_account_party_id
        , defendant_account_id
        , party_id
        , association_type
        , debtor
    )
    VALUES (
          NEXTVAL('defendant_account_party_id_seq')
        , pi_defendant_account_id
        , v_party_id_defendant
        , c_association_type_defendant
        , (pi_defendant_type != c_defendant_type_pgToPay)
    );

    RAISE INFO 'p_create_defendant_parties: Created defendant_account_parties record for the defendant. pi_defendant_account_id = %, party_id = %', pi_defendant_account_id, v_party_id_defendant;

    --Call p_create_debtor_details to insert the related DEBTOR_DETAIL record, including ALIASES, for the defendant, if the Json object exists
    v_debtor_detail_def_json := json_extract_path(pi_defendant_json, 'debtor_detail');

    CALL p_create_debtor_details( v_party_id_defendant,
                                  v_debtor_detail_def_json
    );

    -----------------------------------------
    --Process the parent/guardian information
    -----------------------------------------

    --Insert record for the parent/guardian into the PARTIES table if defendant_type = 'parentOrGuardianToPay'
    IF pi_defendant_type = c_defendant_type_pgToPay THEN

        v_def_pg_json := json_extract_path(pi_defendant_json, 'parent_guardian');
        
        --Check if parent_guardian Json object exists
        IF v_def_pg_json IS NULL OR JSON_TYPEOF(v_def_pg_json) = 'null' THEN

            --Raise custom exception
            RAISE EXCEPTION 'Missing parent/guardian' 
                USING ERRCODE = 'P2002'
                    , DETAIL = 'p_create_defendant_parties: pi_defendant_account_id = ' || pi_defendant_account_id || ', pi_defendant_type = ' || pi_defendant_type;

        ELSE
            --Insert the parent/guardian record into PARTIES table
            INSERT INTO parties (
                  party_id
                , organisation
                , organisation_name
                , surname
                , forenames
                --, initials
                , title
                , address_line_1
                , address_line_2
                , address_line_3
                , address_line_4
                , address_line_5
                , postcode
                , account_type
                , birth_date
                , age
                , national_insurance_number
                , telephone_home
                , telephone_business
                , telephone_mobile
                , email_1
                , email_2
                , last_changed_date
                /*  Not required for Release 1A
                 , driving_licence_number
                 , pnc_id
                 , nationality1
                 , nationality2
                 , self_defined_ethnicity
                 , observed_ethnicity
                 , cro_number
                 , occupation
                 , gender
                 , custody_status
                 , prison_number
                 , interpreter_language_needs
                */
            )
            VALUES ( 
                  NEXTVAL('party_id_seq')
                , (v_def_pg_json ->> 'company_flag')::BOOLEAN
                , v_def_pg_json ->> 'company_name'
                , v_def_pg_json ->> 'surname'
                , v_def_pg_json ->> 'forenames'
                --, NULL  --Initials not required
                , NULL  --title
                , v_def_pg_json ->> 'address_line_1'
                , v_def_pg_json ->> 'address_line_2'
                , v_def_pg_json ->> 'address_line_3'
                , v_def_pg_json ->> 'address_line_4'
                , v_def_pg_json ->> 'address_line_5'
                , v_def_pg_json ->> 'post_code'
                , c_account_type_defendant
                , TO_TIMESTAMP(v_def_pg_json ->> 'dob', 'YYYY-MM-DD')
                , NULL  --age
                , v_def_pg_json ->> 'national_insurance_number'
                , v_def_pg_json ->> 'telephone_number_home'
                , v_def_pg_json ->> 'telephone_number_business'
                , v_def_pg_json ->> 'telephone_number_mobile'
                , v_def_pg_json ->> 'email_address_1'
                , v_def_pg_json ->> 'email_address_2'
                , NULL  --last_changed_date
                /*  Not required for Release 1A
                 , NULL --driving_licence_number
                 , NULL --pnc_id
                 , NULL --nationality_1
                 , NULL --nationality_2
                 , NULL --ethnicity_self_defined
                 , NULL --ethnicity_observed
                 , NULL --cro_number
                 , NULL --occupation
                 , NULL --gender
                 , NULL --custody_status
                 , NULL --prison_number
                 , NULL --interpreter_lang
                */
            )
            RETURNING party_id
            INTO      v_party_id_pg;

            RAISE INFO 'p_create_defendant_parties: Created parties record for the parent_guardian. pi_defendant_account_id = %, party_id = %', pi_defendant_account_id, v_party_id_pg;

            --Insert the related DEFENDANT_ACCOUNT_PARTIES record for the parent/guardian
            INSERT INTO defendant_account_parties (
                  defendant_account_party_id
                , defendant_account_id
                , party_id
                , association_type
                , debtor
            )
            VALUES (
                  NEXTVAL('defendant_account_party_id_seq')
                , pi_defendant_account_id
                , v_party_id_pg
                , c_association_type_pg
                , (pi_defendant_type = c_defendant_type_pgToPay)
            );

            RAISE INFO 'p_create_defendant_parties: Created defendant_account_parties record for the parent_guardian. pi_defendant_account_id = %, party_id = %', pi_defendant_account_id, v_party_id_pg;

            --Call p_create_debtor_details to insert the related DEBTOR_DETAIL record, including ALIASES, for the parent/guardian, if the Json object exists
            v_debtor_detail_pg_json := json_extract_path(v_def_pg_json, 'debtor_detail');
        
            CALL p_create_debtor_details( v_party_id_pg,
                                          v_debtor_detail_pg_json
            );

        END IF;
    END IF;

EXCEPTION
    WHEN SQLSTATE 'P2002' THEN 
        --When custom exception just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_defendant_parties: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        --Log error and re-raise the exception ensuring the caller has access to the complete exception details.
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_defendant_parties: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_defendant_parties: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_defendant_parties 
    IS 'Procedure to process the defendant Json and insert PARTIES, DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAIL and ALIASES records for the defendant and parent/guardian.';
