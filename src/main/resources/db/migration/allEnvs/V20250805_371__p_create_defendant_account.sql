CREATE OR REPLACE PROCEDURE p_create_defendant_account(
    IN pi_draft_account_id          draft_accounts.draft_account_id%TYPE,
    IN pi_business_unit_id          draft_accounts.business_unit_id%TYPE,
    IN pi_posted_by                 defendant_transactions.posted_by%TYPE,            
    IN pi_posted_by_name            defendant_transactions.posted_by_name%TYPE, 
    OUT po_account_number           draft_accounts.account_number%TYPE,
    OUT po_defendant_account_id     draft_accounts.account_id%TYPE
)
LANGUAGE 'plpgsql'
AS 
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_defendant_account.sql
*
* DESCRIPTION : The interface procedure to create manual account. It will parse the account json to insert into the substantive tables.
*
* PARAMETERS : pi_draft_account_id       - The draft account id from the DRAFT_ACCOUNTS table to be passed in by the backend to identify the account json to be processed
*			 : pi_business_unit_id       - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*			 : pi_posted_by              - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*			 : pi_posted_by_name         - The user that is submitting the request to be passed in by the backend
*			 : po_account_number         - The Opal account number to be generated and returned to the backend
*			 : po_defendant_account_id   - The Opal defendant account id to be generated and returned to the backend
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 30/06/2025    A Dennis    1.0         PO-1905 Initial version - Create manual defendant account.
* 21/07/2025    C Cho       2.0         PO-1044 Create Defendant Account 
* 22/07/2025    TMc         3.0         PO-1043 Call new procedure (p_create_defendant_parties) to process the defendant Json and insert records into
*                                               PARTIES, DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAIL and ALIASES for the defendant and parent/guardian.
*                                       PO-1037 Call new procedure (p_create_fp_offences) to insert a record into FIXED_PENALTY_OFFENCES for the defendant.
*                                       Amended exception handling to raise a custom exception when the enforcing court ID is not valid and to re-raise all custom exceptions without manipulating them.
*                                       All changes commented with 'v3.0' 
*
**/
DECLARE
    v_account_type                   defendant_accounts.account_type%TYPE;
    v_account_sentence_date          defendant_accounts.imposed_hearing_date%TYPE;
    v_enforcement_court_id           defendant_accounts.enforcing_court_id%TYPE;
    v_enforcements_json_array        json := NULL;
    --v_enforcements_json_length       smallint;    -- v3.0 Commented out 
    v_offences_json_array            json := NULL;
    v_payment_terms_json             json := NULL;
    /* v3.0 - Commented out
    v_payment_term_type_code         payment_terms.terms_type_code%TYPE;
    v_payment_term_eff_date          payment_terms.effective_date%TYPE := NULL;
    v_payment_term_instal_period     payment_terms.instalment_period%TYPE;
    v_payment_term_instal_amount     payment_terms.instalment_amount%TYPE := NULL;
    v_payment_term_instal_lump_sum   payment_terms.instalment_lump_sum%TYPE;
    v_payment_term_jail_days         payment_terms.jail_days%TYPE;
    */
    v_originator_name                defendant_accounts.originator_name%TYPE;
    v_originator_id                  defendant_accounts.originator_id%TYPE;
    v_fp_ticket_detail_json          json := NULL;
    v_collection_order               defendant_accounts.collection_order%TYPE;
    v_collection_order_date          defendant_accounts.collection_order_date%TYPE;
    v_suspended_committal_date       defendant_accounts.suspended_committal_date%TYPE;
    v_payment_card_requested         defendant_accounts.payment_card_requested%TYPE;
    v_prosecutor_case_reference      defendant_accounts.prosecutor_case_reference%TYPE;
    --v_account_number                 defendant_accounts.account_number%TYPE;          -- v3.0 Commented out 

    -- v3.0 - Start
    v_pg_exception_detail            TEXT;
    v_pg_exception_constraint        TEXT;
    v_defendant_type                 VARCHAR;  --Needed for parties and defendant_account_parties
    v_defendant_json                 json := NULL;
    v_da_account_balance             defendant_accounts.account_balance%TYPE; --Returned from p_create_impositions
    v_last_enforcement               defendant_accounts.last_enforcement%TYPE;
    v_account_notes_json_array       json := NULL;
    v_debtor_document_language       debtor_detail.document_language%TYPE;
    v_document_id                    document_instances.document_id%TYPE;
    v_report_id                      reports.report_id%TYPE;
    -- v3.0 - End
    
BEGIN
    -- Get the account json from the DRAFT_ACCOUNTS table using the draft_account_id passed in
    SELECT account ->> 'account_type'
         , account ->> 'defendant_type'         -- v3.0
         , account ->> 'account_sentence_date'
         , account ->> 'offences'
         , account ->> 'enforcement_court_id'
         , account ->> 'payment_terms'          -- v3.0
         /* v3.0 - Commented out 
         , account ->  'payment_terms' ->> 'payment_terms_type_code'
         , account ->  'payment_terms' ->> 'effective_date'
         , account ->  'payment_terms' ->> 'instalment_period'
         , account ->  'payment_terms' ->> 'instalment_amount'
         , account ->  'payment_terms' ->> 'lump_sum_amount'
         , account ->  'payment_terms' ->> 'jail_days'
         */
         , account ->  'payment_terms' ->> 'enforcements'
         , account ->> 'originator_name'
         , account ->> 'originator_id' 
         , account ->> 'fp_ticket_detail'
         , account ->> 'collection_order_made'
         , account ->> 'collection_order_date'
         , account ->> 'suspended_committal_date'
         , account ->> 'payment_card_request'
         , account ->> 'prosecutor_case_reference'
         , account ->> 'defendant'              -- v3.0
         , account ->> 'account_notes'          -- v3.0
    INTO STRICT v_account_type                  -- STRICT to raise an exception if the value is NULL. One row needs to be returned otherwise an exception is thrown.
       , v_defendant_type                       -- v3.0
       , v_account_sentence_date
       , v_offences_json_array
       , v_enforcement_court_id
       , v_payment_terms_json                   -- v3.0  
       /* v3.0 - Commented out 
       , v_payment_term_type_code
       , v_payment_term_eff_date
       , v_payment_term_instal_period
       , v_payment_term_instal_amount
       , v_payment_term_instal_lump_sum
       , v_payment_term_jail_days
       */ 
       , v_enforcements_json_array
       , v_originator_name
       , v_originator_id
       , v_fp_ticket_detail_json
       , v_collection_order
       , v_collection_order_date
       , v_suspended_committal_date
       , v_payment_card_requested
       , v_prosecutor_case_reference
       , v_defendant_json                       -- v3.0  
    FROM draft_accounts
    WHERE draft_account_id = pi_draft_account_id
      AND account_number IS NULL;  --v3.0 - Ensures that the draft account is only processed once

    RAISE INFO 'v_account_type                = %', v_account_type;
    RAISE INFO 'v_defendant_type              = %', v_defendant_type;           -- v3.0
    RAISE INFO 'v_account_sentence_date       = %', v_account_sentence_date;
    RAISE INFO 'v_enforcement_court_id        = %', v_enforcement_court_id;
    RAISE INFO 'v_originator_name             = %', v_originator_name;
    RAISE INFO 'v_originator_id               = %', v_originator_id;
    
    --RAISE INFO 'Generated account number: %', v_account_number;  -- v3.0 Commented out

    -- Insert into DEFENDANT_ACCOUNTS table
    INSERT INTO defendant_accounts(
            defendant_account_id
          , business_unit_id
          , account_number
          , account_type
          , imposed_hearing_date
          , imposing_court_id
          , amount_imposed
          , amount_paid
          , account_balance
          , account_status 
          , completed_date
          , enforcing_court_id
          , last_hearing_court_id
          , last_hearing_date
          , last_movement_date
          , last_changed_date
          , last_enforcement
          , originator_name
          , originator_id
          , originator_type
          , allow_writeoffs
          , allow_cheques
          , cheque_clearance_period
          , credit_trans_clearance_period
          , enf_override_result_id
          , enf_override_enforcer_id
          , enf_override_tfo_lja_id
          , unit_fine_detail
          , unit_fine_value
          , collection_order
          , collection_order_date
          , further_steps_notice_date
          , confiscation_order_date
          , fine_registration_date
          , suspended_committal_date
          , consolidated_account_type
          , payment_card_requested
          , payment_card_requested_date
          , payment_card_requested_by
          , prosecutor_case_reference
          , enforcement_case_status
          , account_comments            --v3.0
          , account_note_1              --v3.0
          , account_note_2              --v3.0
          , account_note_3              --v3.0
          , jail_days                   --v3.0
          )
    VALUES( 
            NEXTVAL('defendant_account_id_seq')
          , pi_business_unit_id
          , f_get_account_number(pi_business_unit_id, 'defendant_accounts')  -- Use the generated account number from f_get_account_number
          , v_account_type
          , v_account_sentence_date
          , NULL                    -- imposing_court_id should be NULL
          , 0.00                    -- amount_imposed  - this will be updated by a later procedure - v3.0: Will be updated by p_create_impositions
          , 0.00                    -- amount_paid     - this will be updated by a later procedure - v3.0: Will be updated by p_create_impositions
          , 0.00                    -- account_balance - this will be updated by a later procedure - v3.0: Will be updated by p_create_impositions
          , 'L'                     -- account_status should be L for Live
          , NULL                    -- completed_date should be NULL
          , v_enforcement_court_id
          , NULL                    -- last_hearing_court_id should be NULL
          , NULL                    -- last_hearing_date should be NULL
          --, NULL                    -- last_movement_date should be NULL      v3.0 - Commented out
          , CURRENT_TIMESTAMP       -- last_movement_date                       v3.0 - Added
          , NULL                    -- last_changed_date should be NULL
          , NULL                    -- last_enforcement will be updated when determined below when enforcements are processed - v3.0: Will be updated by p_create_enforcements
          , v_originator_name
          , v_originator_id
          , CASE                    -- originator_type is FP if fp_ticket_detail exists, TFO if no fp_ticket_detail exists
                WHEN v_fp_ticket_detail_json IS NOT NULL THEN 'FP'    
                ELSE 'TFO'
            END
          , TRUE                    -- allow_writeoffs should be TRUE
          , TRUE                    -- allow_cheques should be TRUE
          , NULL                    -- cheque_clearance_period XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX JIRA TICKET PO-1103
          , NULL                    -- credit_trans_clearance_period should be NULL
          , NULL                    -- enf_override_result_id should be NULL
          , NULL                    -- enf_override_enforcer_id should be NULL
          , NULL                    -- enf_override_tfo_lja_id should be NULL
          , NULL                    -- unit_fine_detail should be NULL
          , NULL                    -- unit_fine_value should be NULL
          , v_collection_order
          , v_collection_order_date
          , NULL                    -- further_steps_notice_date should be NULL
          , NULL                    -- confiscation_order_date should be NULL
          , NULL                    -- fine_registration_date should be NULL
          , v_suspended_committal_date
          , NULL                    -- consolidated_account_type should be NULL
          , v_payment_card_requested
          , CASE                    -- payment_card_requested_date should be the current timestamp if payment_card_requested is TRUE
                WHEN v_payment_card_requested = TRUE THEN CURRENT_TIMESTAMP
                ELSE NULL
            END
          , CASE                    -- payment_card_requested_by should be the user id if payment_card_requested is TRUE  
                WHEN v_payment_card_requested = TRUE THEN pi_posted_by     
                ELSE NULL
            END
          , v_prosecutor_case_reference 
          , NULL                    -- enforcement_case_status should be NULL
          , NULL                    -- account_comments     v3.0 - Needs updating once it's provided in the Json
          , NULL                    -- account_note_1       v3.0 - Needs updating once it's provided in the Json
          , NULL                    -- account_note_2       v3.0 - Needs updating once it's provided in the Json
          , NULL                    -- account_note_3       v3.0 - Needs updating once it's provided in the Json
          , NULL                    -- jail_days            v3.0 - Needs updating once it's provided in the Json
          )
    RETURNING 
            defendant_account_id
          , account_number 
    INTO 
            po_defendant_account_id
          , po_account_number;

    RAISE INFO 'p_create_defendant_account: defendant_account_id: %. Generated account number: %', po_defendant_account_id, po_account_number;  -- v3.0

    -- Process PARTIES, including DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAILS and ALIASES
    -- v3.0 - Added call to p_create_defendant_parties
    CALL p_create_defendant_parties ( po_defendant_account_id,
                                      v_defendant_type,
                                      v_defendant_json
    );

    -- FIXED_PENALTY_OFFENCES
    -- v3.0 - Added call to p_create_fp_offences
    CALL p_create_fp_offences ( po_defendant_account_id,
                                v_account_type,
                                v_fp_ticket_detail_json
    );

EXCEPTION
    WHEN SQLSTATE 'P2002' OR SQLSTATE 'P2003' OR SQLSTATE 'P2004' OR SQLSTATE 'P2005' OR SQLSTATE 'P2006' OR    --v3.0
         SQLSTATE 'P2008' OR SQLSTATE 'P2009' OR SQLSTATE 'P2010' OR SQLSTATE 'P2011' THEN
        --When custom exceptions just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN FOREIGN_KEY_VIOLATION THEN     --v3.0
        --Check for specific FK violations (i.e. enforcing_court_id)
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL, v_pg_exception_constraint = CONSTRAINT_NAME;
        RAISE NOTICE 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;

        IF v_pg_exception_constraint = 'da_enforcing_court_id_fk' THEN 
            --Raise custom exception
            RAISE EXCEPTION 'Enforcement court % not found', v_enforcement_court_id 
                USING ERRCODE = 'P2007'
                    , DETAIL = 'p_create_defendant_account: pi_defendant_account_id = ' || pi_defendant_account_id;
        ELSE
            --Any other FK violation then construct standard exception
            RAISE EXCEPTION 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM 
                USING DETAIL = v_pg_exception_detail;
        END IF;
    WHEN OTHERS THEN
        --v3.0 Output full exception details and added DETAIL to RAISE EXCEPTION
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;           --v3.0 
        RAISE NOTICE 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM;  --v3.0
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;                        --v3.0 
        --RAISE EXCEPTION 'Error in p_create_defendant_account: %', ' SQLSTATE: ' || SQLSTATE || '   SQLERRM: ' || SQLERRM;  --v3.0 - Commented out
        RAISE EXCEPTION 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;  --v3.0
END;
$BODY$;

COMMENT ON PROCEDURE p_create_defendant_account
    IS 'The interface procedure to create manual account. It parses the account Json to insert into the substantive tables.';