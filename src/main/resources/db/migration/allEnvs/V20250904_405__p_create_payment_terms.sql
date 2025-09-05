CREATE OR REPLACE PROCEDURE p_create_payment_terms (
    IN  pi_defendant_account_id defendant_accounts.defendant_account_id%TYPE,
    IN  pi_account_type         defendant_accounts.account_type%TYPE,
    IN  pi_da_account_balance   defendant_accounts.account_balance%TYPE,
    IN  pi_posted_by            payment_terms.posted_by%TYPE,            
    IN  pi_posted_by_name       payment_terms.posted_by_name%TYPE,
    IN  pi_payment_terms_json   JSON
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_payment_terms.sql
*
* DESCRIPTION : Insert into the PAYMENT_TERMS table and update JAIL_DAYS on DEFENDANT_ACCOUNTS table.
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_account_type         - The account type of the associated defendant_accounts record
*               pi_da_account_balance   - The calculated defendant account balance
*               pi_posted_by            - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*               pi_posted_by_name       - The user that is submitting the request to be passed in by the backend
*               pi_payment_terms_json   - The payment_terms Json object for the defendant account
*
* VERSION HISTORY:
*
* Date          Author               Version     Nature of Change
* ----------    -----------------    --------    ----------------------------------------------------------------------------------------------
* 29/07/2025    TMc and A Dennis     1.0         PO-1036 - Insert into the PAYMENT_TERMS table and update JAIL_DAYS on DEFENDANT_ACCOUNTS table
* 02/09/2025    TMc                  2.0         PO-2115 - Removed time component when populating PAYMENT_TERMS.EFFECTIVE_DATE
*
**/
DECLARE
	v_pg_exception_detail	TEXT;

    v_payment_terms_type_code   payment_terms.terms_type_code%TYPE;
    v_effective_date            payment_terms.effective_date%TYPE;
    v_instalment_amount         payment_terms.instalment_amount%TYPE;
    v_jail_days                 payment_terms.jail_days%TYPE;
    v_is_fixed_penalty          BOOLEAN;
    v_payment_term_condition    BOOLEAN; --Flag to check if payment terms are valid
BEGIN

    --Insert the PAYMENT_TERMS record, if the Json passed is not NULL
    IF pi_payment_terms_json IS NULL OR JSON_TYPEOF(pi_payment_terms_json) = 'null' THEN
        
        --Raise exception - payment_terms Json is required
        RAISE EXCEPTION 'Missing payment terms' 
            USING DETAIL = 'p_create_payment_terms: Passed payment_terms Json was missing or empty.';

    ELSE

        --Parse payment_terms fields
        v_payment_terms_type_code := pi_payment_terms_json ->> 'payment_terms_type_code';
        v_effective_date          := TO_TIMESTAMP(pi_payment_terms_json ->> 'effective_date', 'YYYY-MM-DD');
        v_instalment_amount       := (pi_payment_terms_json ->> 'instalment_amount')::NUMERIC(18,2);
        v_jail_days               := (pi_payment_terms_json ->> 'default_days_in_jail')::INTEGER;
        v_is_fixed_penalty        := (LOWER(pi_account_type) = 'fixed penalty'); 
        v_payment_term_condition  := FALSE; --Flag to check if payment terms are valid 

        IF v_is_fixed_penalty 
        THEN 
            IF (v_payment_terms_type_code = 'B' AND v_effective_date IS NULL)  -- effective date not required for fixed penalty accounts
            THEN
                v_payment_term_condition  := TRUE;
            END IF;
        ELSE
            IF (v_payment_terms_type_code = 'B' AND v_effective_date IS NOT NULL AND NOT v_is_fixed_penalty) OR  -- A non fixed penalty account can have payment type code of B
               (v_payment_terms_type_code = 'I' AND v_effective_date IS NOT NULL AND v_instalment_amount IS NOT NULL) OR
               (v_payment_terms_type_code = 'P') 
            THEN
                v_payment_term_condition := TRUE;
            END IF;
        END IF;

        IF v_payment_term_condition 
        THEN
            
            --Payment terms are valid

            IF v_is_fixed_penalty THEN
                --Add 28 days to the current_date
                --v_effective_date := CURRENT_TIMESTAMP + INTERVAL '28 days';  --v2.0 Commented out
                v_effective_date := (CURRENT_DATE + INTERVAL '28 days')::timestamp;    --v2.0 Added
            END IF;
            
            INSERT INTO payment_terms (
                  payment_terms_id
                , defendant_account_id
                , posted_date
                , posted_by
                , terms_type_code
                , effective_date
                , instalment_period
                , instalment_amount
                , instalment_lump_sum
                , jail_days
                , "extension"
                , account_balance
                , posted_by_name
                , active
            )
            VALUES (
                  NEXTVAL('payment_terms_id_seq')
                , pi_defendant_account_id
                , CLOCK_TIMESTAMP()
                , pi_posted_by
                , v_payment_terms_type_code
                , v_effective_date
                , pi_payment_terms_json ->> 'instalment_period'
                , v_instalment_amount
                , (pi_payment_terms_json ->> 'lump_sum_amount')::NUMERIC(18,2)
                , v_jail_days
                , FALSE --extension
                , pi_da_account_balance
                , pi_posted_by_name
                , TRUE  --active
            );

            RAISE INFO 'p_create_payment_terms: Created payment_terms record for defendant_account_id = %', pi_defendant_account_id;

            --Update DEFENDANT_ACCOUNTS.JAIL_DAYS column
            UPDATE defendant_accounts
               SET jail_days = v_jail_days
             WHERE defendant_account_id = pi_defendant_account_id;

            RAISE INFO 'p_create_payment_terms: defendant_accounts.jail_days [%] has been updated. defendant_account_id = %', v_jail_days, pi_defendant_account_id;

        ELSE
            --Payment terms are invalid. Raise exception
            RAISE EXCEPTION 'Invalid payment terms' 
                USING ERRCODE = 'P2003'
                    , DETAIL = 'p_create_payment_terms: Passed payment_terms Json = ' || pi_payment_terms_json;     

        END IF;
    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2003' THEN 
        --When custom exception just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_payment_terms: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_payment_terms: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_payment_terms: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_payment_terms 
    IS 'Procedure to insert into the PAYMENT_TERMS table.';
