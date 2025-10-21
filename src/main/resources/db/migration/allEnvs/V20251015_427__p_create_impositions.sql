DROP PROCEDURE IF EXISTS p_create_impositions;

CREATE OR REPLACE PROCEDURE p_create_impositions (
    IN pi_defendant_account_id  defendant_accounts.defendant_account_id%TYPE,
    IN pi_business_unit_id      draft_accounts.business_unit_id%TYPE,
    IN pi_posted_by             impositions.posted_by%TYPE,            
    IN pi_posted_by_name        impositions.posted_by_name%TYPE,
    IN pi_offences_json         JSON,
    OUT po_da_account_balance   defendant_accounts.account_balance%TYPE
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_impositions.sql
*
* DESCRIPTION : Process the Offences Json object for the related defendant.
*               Tables inserted into: IMPOSITIONS, CONTROL_TOTALS, DEFENDANT_TRANSACTIONS, ALLOCATIONS, DOCUMENT_INSTANCES (compensation notice)
*                                     and CREDITOR_ACCOUNTS and PARTIES (minor creditor) if necessary
*               It also updates DEFENDANT_ACCOUNTS (amount_imposed, amount_paid, amount_balance)
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_business_unit_id     - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*               pi_posted_by            - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*               pi_posted_by_name       - The user that is submitting the request to be passed in by the backend
*               pi_offences_json        - The dedendant offences Json array object
*               po_da_account_balance   - The calculated defendant account balance
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------
* 28/07/2025    TMc         1.0         PO-1038, PO-1039, PO-1040 - Processes the Offences Json object for the related defendant.
* 28/08/2025    TMc         1.1         PO-2096 - Added pi_originator_name and populate new column IMPOSITIONS.ORIGINATOR_NAME with the passed value.
* 13/10/2025    CL          2.0         PO-2291 - Removed the originator_name parameter from signature and insert statement, as the column has been 
*                                                 removed from the impositions table
*
**/
DECLARE
	v_pg_exception_detail           TEXT;
    v_pg_exception_constraint       TEXT;
    v_custom_exception_code         TEXT;
    v_custom_exception_msg          TEXT;

    v_offence_json                  JSON;
    v_offence_count                 INTEGER := 0;
    v_impositions_count             INTEGER := 0;
    v_impositions_json              JSON;
    v_imposition_json               JSON;
    v_imposition_id                 impositions.imposition_id%TYPE;
    v_impositions_result_id         impositions.result_id%TYPE;
    v_offence_id                    offences.offence_id%TYPE;
    v_offence_cjs_code              offences.cjs_code%TYPE;
    v_offence_offence_title         offences.offence_title%TYPE;
    v_offence_imposing_court_id     impositions.imposing_court_id%TYPE;

    v_imposed_amount                impositions.imposed_amount%TYPE;
    v_imposed_amount_total          impositions.imposed_amount%TYPE := 0;
    v_paid_amount                   impositions.paid_amount%TYPE;
    v_paid_amount_total             impositions.paid_amount%TYPE    := 0;

    v_defendant_transactions_id     defendant_transactions.defendant_transaction_id%TYPE := NULL;
    v_dt_updated_transaction_amount defendant_transactions.transaction_amount%TYPE;

    v_creditor_account_id           creditor_accounts.creditor_account_id%TYPE;
    v_is_minor_creditor             BOOLEAN;
    v_results_mapped_item_number    control_totals.item_number%TYPE;
BEGIN

    --Process each Offence object within the Offences array
    IF pi_offences_json IS NULL OR JSON_TYPEOF(pi_offences_json) = 'null' THEN

        --Do nothing for now

    ELSE
        <<offences_loop>>
        FOR v_offence_json IN SELECT json_array_elements(pi_offences_json)
        LOOP
            v_offence_count             := v_offence_count + 1;
            v_offence_id                := (v_offence_json ->> 'offence_id')::BIGINT;
            v_offence_imposing_court_id := (v_offence_json ->> 'imposing_court_id')::BIGINT;

            --Retrieve the Offence details from OFFENCES. Raise an exception if not found or more than 1 record is returned.
            BEGIN
                SELECT cjs_code
                     , offence_title
                  INTO STRICT v_offence_cjs_code
                            , v_offence_offence_title
                  FROM offences
                 WHERE offence_id = v_offence_id;
            EXCEPTION
                --When NO_DATA_FOUND or TOO_MANY_ROWS raise custom exception otherwise re-raise the original
                WHEN SQLSTATE 'P0002' OR SQLSTATE 'P0003' THEN
                    RAISE EXCEPTION 'Offence % not found', v_offence_id 
                        USING ERRCODE = 'P2009'
                            , DETAIL = 'p_create_impositions: defendant_account_id = ' || pi_defendant_account_id || ', offence_id = ' || v_offence_id;
                WHEN OTHERS THEN
                    RAISE;
            END;

            --Process the impositions Json
            v_impositions_json := v_offence_json ->> 'impositions';

            <<impositions_loop>>
            FOR v_imposition_json IN SELECT json_array_elements(v_impositions_json)
            LOOP
                v_impositions_count     := v_impositions_count + 1;
                v_impositions_result_id := v_imposition_json ->> 'result_id';
                v_imposed_amount        := (v_imposition_json ->> 'amount_imposed')::NUMERIC(18,2);
                v_paid_amount           := (v_imposition_json ->> 'amount_paid')::NUMERIC(18,2);

                --Keep a running total of imposed_amount and paid_amount to update DEFENDANT_ACCOUNTS and DEFENDANT_TRANSACTIONS (transaction_amount) tables later
                v_imposed_amount_total := v_imposed_amount_total + v_imposed_amount;
                v_paid_amount_total    := v_paid_amount_total + v_paid_amount;

                -- Get CREDITOR_ACCOUNT 
                CALL p_get_creditor_account ( pi_business_unit_id,
                                              v_imposition_json,
                                              v_creditor_account_id,
                                              v_is_minor_creditor,
                                              v_results_mapped_item_number
                );

                --Insert IMPOSITIONS record
                INSERT INTO impositions (
                      imposition_id
                    , defendant_account_id
                    , posted_date
                    , posted_by
                    , posted_by_name
                    , original_posted_date
                    , result_id
                    , imposing_court_id
                    , imposed_date
                    , imposed_amount
                    , paid_amount
                    , offence_id
                    , offence_title
                    , offence_code
                    , creditor_account_id
                    , unit_fine_adjusted
                    , unit_fine_units
                    , completed                    
                    , original_imposition_id
                )
                VALUES (
                      NEXTVAL('imposition_id_seq')
                    , pi_defendant_account_id
                    , CLOCK_TIMESTAMP()
                    , pi_posted_by
                    , pi_posted_by_name
                    , CLOCK_TIMESTAMP()
                    , v_impositions_result_id               --If this doesn't exist in RESULTS then a FK violation exception will be raised
                    , v_offence_imposing_court_id           --If this is present and doesn't exist in COURTS then a FK violation exception will be raised
                    , TO_TIMESTAMP(v_offence_json ->> 'date_of_sentence', 'YYYY-MM-DD')  --imposed_date
                    , 0 - v_imposed_amount                  --Store as a negative value
                    , v_paid_amount
                    , v_offence_id                          --If this is present and doesn't exist in OFFENCES then a FK violation exception will be raised
                    , v_offence_offence_title
                    , v_offence_cjs_code
                    , v_creditor_account_id
                    , NULL  --unit_fine_adjusted
                    , NULL  --unit_fine_units
                    , FALSE --completed                    
                    , NULL  --original_imposition_id
                )
                RETURNING imposition_id
                INTO      v_imposition_id;

                --Insert CONTROL_TOTALS record for the imposition
                INSERT INTO control_totals (
                      control_total_id
                    , business_unit_id
                    , item_number
                    , amount
                    , associated_record_type
                    , associated_record_id
                    , ct_report_instance_id
                    , qe_report_instance_id
                )
                VALUES (
                      NEXTVAL('control_total_id_seq')
                    , pi_business_unit_id
                    , v_results_mapped_item_number
                    , (0 - v_imposed_amount) + v_paid_amount
                    , 'impositions'
                    , v_imposition_id
                    , NULL  --ct_report_instance_id
                    , NULL  --qe_report_instance_id
                );

                --Insert ALLOCATIONS record and DEFENDANT_TRANSACTIONS record when needed (i.e. amount_paid > 0). Only create 1 record
                IF v_paid_amount > 0 THEN

                    --Insert DEFENDANT_TRANSACTIONS record, if not already created. 
                    --TRANSACTION_AMOUNT is initially set to 0 and is updated after all Impositions has been processed
                    IF v_defendant_transactions_id IS NULL THEN
                     
                        INSERT INTO defendant_transactions (
                              defendant_transaction_id
                            , defendant_account_id
                            , posted_date
                            , posted_by
                            , transaction_type
                            , transaction_amount
                            , payment_method
                            , payment_reference
                            , "text"
                            , status
                            , status_date
                            , status_amount
                            , write_off_code
                            , associated_record_type
                            , associated_record_id
                            , imposed_amount
                            , posted_by_name
                        )
                        VALUES (
                              NEXTVAL('defendant_transaction_id_seq')
                            , pi_defendant_account_id
                            , CLOCK_TIMESTAMP()
                            , pi_posted_by
                            , 'TFO IN'
                            , 0                 --This will be updated once all Impositions have been processed
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , pi_posted_by_name
                        )
                        RETURNING defendant_transaction_id
                        INTO      v_defendant_transactions_id;
                    END IF;

                    --Insert ALLOCATIONS record
                    INSERT INTO allocations (
                          allocation_id
                        , imposition_id
                        , allocated_date
                        , allocated_amount
                        , transaction_type
                        , allocation_function
                        , defendant_transaction_id
                    )
                    VALUES (
                          NEXTVAL('allocation_id_seq')
                        , v_imposition_id
                        , CURRENT_TIMESTAMP
                        , v_paid_amount
                        , 'TFO IN'
                        , 'MAC'
                        , v_defendant_transactions_id
                    );
                    
                END IF;

                --Insert DOCUMENT_INSTANCES record (compensation notice) if result_id = 'FCOMP' and the creditor is a minor creditor
                IF v_impositions_result_id = 'FCOMP' AND v_is_minor_creditor THEN

                    INSERT INTO document_instances (
                          document_instance_id
                        , document_id
                        , business_unit_id
                        , generated_date
                        , generated_by
                        , associated_record_type
                        , associated_record_id
                        , status
                        , printed_date
                        , document_content
                    )
                    VALUES (
                          NEXTVAL('document_instance_id_seq')
                        , 'COMPLETT'            --document_id
                        , pi_business_unit_id
                        , CURRENT_TIMESTAMP     --generated_date
                        , pi_posted_by          --generated_by
                        , 'impositions'         --associated_record_type
                        , v_imposition_id       --associated_record_id
                        , 'New'                 --status
                        , NULL                  --printed_date
                        , NULL                  --document_content
                    );
                END IF;
                
            END LOOP impositions_loop;

        END LOOP offences_loop;

        RAISE INFO 'p_create_impositions: Processed % offences, % impositions', v_offence_count, v_impositions_count;

        --Update DEFENDANT_TRANSACTIONS record. Set TRANSACTION_AMOUNT to the AMOUNT_PAID total if it's GT 0
        IF v_paid_amount_total > 0 THEN

            UPDATE defendant_transactions
               SET transaction_amount = v_paid_amount_total
             WHERE defendant_transaction_id = v_defendant_transactions_id
            RETURNING transaction_amount INTO STRICT v_dt_updated_transaction_amount;   --Returning STRICT to ensure 1 record is found and updated

            RAISE INFO 'p_create_impositions: defendant_transactions has been updated. defendant_transaction_id = %, transaction_amount = %', v_defendant_transactions_id, v_paid_amount_total;
        END IF;
 
        --Update DEFENDANT_ACCOUNTS (amount_imposed, amount_paid, amount_balance)
        UPDATE defendant_accounts
           SET amount_imposed  = (0 - v_imposed_amount_total)
             , amount_paid     = v_paid_amount_total
             , account_balance = (0 - v_imposed_amount_total) + v_paid_amount_total
         WHERE defendant_account_id = pi_defendant_account_id
        RETURNING account_balance INTO STRICT po_da_account_balance;   --Returning STRICT to ensure 1 record is found and updated

        RAISE INFO 'p_create_impositions: defendant_accounts has been updated. defendant_account_id = %', pi_defendant_account_id;
    END IF;

    --If no offences were found then raise custom error
    IF v_offence_count = 0 THEN

        --Raise custom exception
        RAISE EXCEPTION 'Offence not found' 
            USING ERRCODE = 'P2009'
                , DETAIL = 'p_create_impositions: There were no Offences to process. defendant_account_id = %' || pi_defendant_account_id;

    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2009' OR SQLSTATE 'P2005' OR SQLSTATE 'P2010' OR SQLSTATE 'P2004' OR SQLSTATE 'P2006' THEN
        --When custom exceptions just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_impositions: % - %', SQLSTATE, SQLERRM;
        RAISE;
    
    WHEN FOREIGN_KEY_VIOLATION THEN
        --Check for specific FK violations (i.e. imposing_court_id, offence_id, result_id)
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL, v_pg_exception_constraint = CONSTRAINT_NAME;
        RAISE NOTICE 'Error in p_create_impositions: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;

        --IF v_pg_exception_constraint = 'imp_defendant_account_id_fk' THEN 

        --    v_custom_exception_code := NULL;
        --    v_custom_exception_msg  := format('defendant_account_id %L does not exist!', v_defendant_account_id);

        IF v_pg_exception_constraint = 'imp_result_id_fk' THEN 

            v_custom_exception_code := 'P2004';
            v_custom_exception_msg  := format('Result %L is not valid', v_impositions_result_id);

        ELSIF v_pg_exception_constraint = 'imp_imposing_court_id_fk' THEN 

            v_custom_exception_code := 'P2008';
            v_custom_exception_msg  := format('Imposing court %L not found', v_offence_imposing_court_id);

        ELSIF v_pg_exception_constraint = 'imp_offence_id_fk' THEN

            v_custom_exception_code := 'P2009';
            v_custom_exception_msg  := format('Offence %L not found', v_offence_id);

        ELSE
            --Any other FK violation then construct standard message
            v_custom_exception_code := NULL;
            v_custom_exception_msg  := format('Error in p_create_impositions: %s - %s', SQLSTATE, SQLERRM);

        END IF;
        
        IF v_custom_exception_code IS NULL THEN  
            --Raise generic exception
            RAISE EXCEPTION  
                USING MESSAGE = v_custom_exception_msg
                    , DETAIL = v_pg_exception_detail;
        ELSE
            --Raise custom exception
            RAISE EXCEPTION 
                USING MESSAGE = v_custom_exception_msg
                    , ERRCODE = v_custom_exception_code
                    , DETAIL = 'p_create_impositions: ' || v_pg_exception_detail;
        END IF;

    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_impositions: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_impositions: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_impositions
    IS 'Processes the Offences Json object for the related defendant.';
