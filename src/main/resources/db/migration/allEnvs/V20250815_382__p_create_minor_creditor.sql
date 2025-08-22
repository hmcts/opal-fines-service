CREATE OR REPLACE PROCEDURE p_create_minor_creditor (
    IN  pi_business_unit_id     business_units.business_unit_id%TYPE,
    IN  pi_minor_creditor_json  JSON,
    OUT po_creditor_account_id  creditor_accounts.creditor_account_id%TYPE
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_minor_creditor.sql
*
* DESCRIPTION : Create new CREDITOR_ACCOUNTS and PARTIES records for a minor creditor.
*
* PARAMETERS  : pi_business_unit_id    - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*               pi_minor_creditor_json - The minor_creditor Json object used to create the CREDITOR_ACCOUNT and PARTIES records
*               po_creditor_account_id - The creditor_account_id to be generated and returned
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------
* 29/07/2025    TMc         1.0         PO-1043 - Create new CREDITOR_ACCOUNTS and PARTIES records for a minor creditor.
*
**/
DECLARE
    c_account_type_creditor CONSTANT    parties.account_type%TYPE := 'Creditor';
	v_pg_exception_detail	TEXT;

    v_party_id              parties.party_id%TYPE := NULL;
    v_pay_by_bacs           creditor_accounts.pay_by_bacs%TYPE;
    v_bank_sort_code        creditor_accounts.bank_sort_code%TYPE;
    v_bank_account_number   creditor_accounts.bank_account_number%TYPE;
    v_bank_account_name     creditor_accounts.bank_account_name%TYPE;
    v_bank_account_ref      creditor_accounts.bank_account_reference%TYPE;
    v_bank_account_type     creditor_accounts.bank_account_type%TYPE;
BEGIN

    --Check to ensure the passed Json is not NULL
    IF pi_minor_creditor_json IS NULL OR JSON_TYPEOF(pi_minor_creditor_json) = 'null' THEN

        --Raise custom exception
        RAISE EXCEPTION 'Missing creditor' 
            USING ERRCODE = 'P2005'
                , DETAIL = 'p_create_minor_creditor: Passed minor_creditor Json was missing or empty.';

    ELSE 
        --Check bank account details are present if pay_by_bacs is TRUE
        v_pay_by_bacs := (pi_minor_creditor_json ->> 'pay_by_bacs')::BOOLEAN;

        v_bank_sort_code      := pi_minor_creditor_json ->> 'bank_sort_code';
        v_bank_account_number := pi_minor_creditor_json ->> 'bank_account_number';
        v_bank_account_name   := pi_minor_creditor_json ->> 'bank_account_name';
        v_bank_account_ref    := pi_minor_creditor_json ->> 'bank_account_ref';
        v_bank_account_type   := pi_minor_creditor_json ->> 'bank_account_type';

        IF COALESCE(v_pay_by_bacs, TRUE) THEN 
            IF v_pay_by_bacs IS NULL OR
               COALESCE(LENGTH(TRIM(v_bank_sort_code)), 0)      = 0 OR
               COALESCE(LENGTH(TRIM(v_bank_account_number)), 0) = 0 OR
               COALESCE(LENGTH(TRIM(v_bank_account_name)), 0)   = 0 OR
               COALESCE(LENGTH(TRIM(v_bank_account_ref)), 0)    = 0 OR
               COALESCE(LENGTH(TRIM(v_bank_account_type)), 0)   = 0 THEN 

                --Raise an exception. One or more bank details are missing or not populated
                RAISE EXCEPTION 'Missing bank detail' 
                    USING ERRCODE = 'P2010'
                        , DETAIL = 'p_create_minor_creditor: Passed bank details: ' || json_build_object(
                                                                                            'pay_by_bacs', v_pay_by_bacs,
                                                                                            'bank_sort_code', v_bank_sort_code,
                                                                                            'bank_account_number', v_bank_account_number,
                                                                                            'bank_account_name', v_bank_account_name,
                                                                                            'bank_account_ref', v_bank_account_ref,
                                                                                            'bank_account_type', v_bank_account_type
                                                                                       );
            END IF;
        END IF;

        --Insert the PARTIES record for the minor creditor
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
            , (pi_minor_creditor_json ->> 'company_flag')::BOOLEAN
            , pi_minor_creditor_json ->> 'company_name'
            , pi_minor_creditor_json ->> 'surname'
            , pi_minor_creditor_json ->> 'forenames'
            --, NULL  --Initials not required
            , pi_minor_creditor_json ->> 'title'
            , pi_minor_creditor_json ->> 'address_line_1'
            , pi_minor_creditor_json ->> 'address_line_2'
            , pi_minor_creditor_json ->> 'address_line_3'
            , pi_minor_creditor_json ->> 'address_line_4'
            , pi_minor_creditor_json ->> 'address_line_5'
            , pi_minor_creditor_json ->> 'post_code'
            , c_account_type_creditor
            , TO_TIMESTAMP(pi_minor_creditor_json ->> 'dob', 'YYYY-MM-DD')
            , NULL  --age
            , NULL  --national_insurance_number
            , pi_minor_creditor_json ->> 'telephone'   --telephone_home
            , NULL  --telephone_business
            , NULL  --telephone_mobile
            , pi_minor_creditor_json ->> 'email_address'
            , NULL  --email_2
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
        INTO      v_party_id;

        RAISE INFO 'p_create_minor_creditor: Created parties record for the minor_creditor. party_id = %', v_party_id;

        --Insert the CREDITOR_ACCOUNTS record for the minor creditor
        INSERT INTO creditor_accounts (
              creditor_account_id
            , business_unit_id
            , account_number
            , creditor_account_type
            , prosecution_service
            , major_creditor_id
            , minor_creditor_party_id
            , from_suspense
            , hold_payout
            , pay_by_bacs
            , bank_sort_code
            , bank_account_number
            , bank_account_name
            , bank_account_reference
            , bank_account_type
            , last_changed_date
        )
        VALUES (
              NEXTVAL('creditor_account_id_seq')
            , pi_business_unit_id
            , f_get_account_number(pi_business_unit_id, 'creditor_accounts')
            , 'MN'        --creditor_account_type
            , FALSE       --prosecution_service
            , NULL        --major_creditor_id
            , v_party_id  --minor_creditor_party_id
            , FALSE       --from_suspense
            , (pi_minor_creditor_json ->> 'payout_hold')::BOOLEAN
            , v_pay_by_bacs
            , v_bank_sort_code
            , v_bank_account_number
            , v_bank_account_name
            , v_bank_account_ref
            , v_bank_account_type
            , NULL        --last_changed_date'
        )
        RETURNING creditor_account_id
        INTO      po_creditor_account_id;

        RAISE INFO 'p_create_minor_creditor: Created creditor_accounts record for the minor_creditor. creditor_account_id = %', po_creditor_account_id;

    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2005' OR SQLSTATE 'P2010' THEN
        --When custom exception just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_minor_creditor: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_minor_creditor: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_minor_creditor: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_minor_creditor
    IS 'Procedure to create new CREDITOR_ACCOUNTS and PARTIES records for a minor creditor.';
