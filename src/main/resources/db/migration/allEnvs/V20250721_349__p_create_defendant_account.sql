CREATE OR REPLACE PROCEDURE p_create_defendant_account(
    IN pi_draft_account_id           draft_accounts.draft_account_id%TYPE,
    IN pi_business_unit_id           draft_accounts.business_unit_id%TYPE,
    IN pi_posted_by                  defendant_transactions.posted_by%TYPE,            
    IN pi_posted_by_name             defendant_transactions.posted_by_name%TYPE, 
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
* PARAMETERS : pi_draft_account_id             - The draft account id from the DRAFT_ACCOUNTS table to be passed in by the backend to identify the account json to be processed
*			 : pi_business_unit_id             - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*			 : pi_posted_by                    - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*			 : pi_posted_by_name               - The user that is submitting the request to be passed in by the backend
*			 : po_account_number              - The Opal account number to be generated and returned to the backend
*			 : po_defendant_account_id        - The Opal defendant account id to be generated and returned to the backend
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 30/06/2025    A Dennis    1.0         PO-1905 Initial version - Create manual defendant account.
* 21/07/2025    C Cho       2.0         PO-1044 Create Defendant Account 
*
**/
DECLARE
    v_account_type                   defendant_accounts.account_type%TYPE;
    v_account_sentence_date          defendant_accounts.imposed_hearing_date%TYPE;
    v_enforcement_court_id           defendant_accounts.enforcing_court_id%TYPE;
    v_enforcements_json_array        json;
    v_enforcements_json_length       smallint;
    v_offences_json_array            json := NULL;
    v_payment_term_json              json := NULL;
    v_payment_term_type_code         payment_terms.terms_type_code%TYPE;
    v_payment_term_eff_date          payment_terms.effective_date%TYPE := NULL;
    v_payment_term_instal_period     payment_terms.instalment_period%TYPE;
    v_payment_term_instal_amount     payment_terms.instalment_amount%TYPE := NULL;
    v_payment_term_instal_lump_sum   payment_terms.instalment_lump_sum%TYPE;
    v_payment_term_jail_days         payment_terms.jail_days%TYPE;
    v_originator_name                defendant_accounts.originator_name%TYPE;
    v_originator_id                  defendant_accounts.originator_id%TYPE;
    v_fp_ticket_detail_json          json := NULL;
    v_collection_order               defendant_accounts.collection_order%TYPE;
    v_collection_order_date          defendant_accounts.collection_order_date%TYPE;
    v_suspended_committal_date       defendant_accounts.suspended_committal_date%TYPE;
    v_payment_card_requested         defendant_accounts.payment_card_requested%TYPE;
    v_prosecutor_case_reference      defendant_accounts.prosecutor_case_reference%TYPE;
    v_account_number                 defendant_accounts.account_number%TYPE;
    
BEGIN
    -- Get the account json from the DRAFT_ACCOUNTS table using the draft_account_id passed in
    SELECT account ->> 'account_type'
         , account ->> 'account_sentence_date'
         , account ->> 'offences'
         , account ->> 'enforcement_court_id'
         , account ->  'payment_terms' ->> 'payment_terms_type_code'
         , account ->  'payment_terms' ->> 'effective_date'
         , account ->  'payment_terms' ->> 'instalment_period'
         , account ->  'payment_terms' ->> 'instalment_amount'
         , account ->  'payment_terms' ->> 'lump_sum_amount'
         , account ->  'payment_terms' ->> 'jail_days'
         , account ->  'payment_terms' ->> 'enforcements'
         , account ->> 'originator_name'
         , account ->> 'originator_id' 
         , account ->> 'fp_ticket_detail'
         , account ->> 'collection_order_made'
         , account ->> 'collection_order_date'
         , account ->> 'suspended_committal_date'
         , account ->> 'payment_card_request'
         , account ->> 'prosecutor_case_reference'
    INTO STRICT v_account_type                          -- STRICT to raise an exception if the value is NULL 
       , v_account_sentence_date
       , v_offences_json_array
       , v_enforcement_court_id
       , v_payment_term_type_code
       , v_payment_term_eff_date
       , v_payment_term_instal_period
       , v_payment_term_instal_amount
       , v_payment_term_instal_lump_sum
       , v_payment_term_jail_days
       , v_enforcements_json_array
       , v_originator_name
       , v_originator_id
       , v_fp_ticket_detail_json
       , v_collection_order
       , v_collection_order_date
       , v_suspended_committal_date
       , v_payment_card_requested
       , v_prosecutor_case_reference
    FROM draft_accounts
    WHERE draft_account_id = pi_draft_account_id;

    RAISE INFO 'v_account_type                = %', v_account_type;
    RAISE INFO 'v_account_sentence_date       = %', v_account_sentence_date;
    RAISE INFO 'v_enforcement_court_id        = %', v_enforcement_court_id;
    RAISE INFO 'v_originator_name             = %', v_originator_name;
    RAISE INFO 'v_originator_id               = %', v_originator_id;
    
    RAISE INFO 'Generated account number: %', v_account_number;

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
          )
    VALUES( 
            nextval('defendant_account_id_seq')
          , pi_business_unit_id
          , f_get_account_number(pi_business_unit_id, 'defendant_accounts')  -- Use the generated account number from f_get_account_number
          , v_account_type
          , v_account_sentence_date
          , NULL                     -- imposing_court_id should be NULL
          , 0.00                     -- amount_imposed - this will be updated by a later procedure  XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          , 0.00                     -- amount_paid - this will be updated by a later procedure    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          , 0.00                     -- account_balance - this will be updated by a later procedure  XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          , 'L'                      -- account_status - shoulb be L for Live
          , NULL                     -- completed_date should be NULL
          , v_enforcement_court_id
          , NULL                     -- last_hearing_court_id should be NULL
          , NULL                     -- last_hearing_date should be NULL
          , NULL                     -- last_movement_date should be NULL
          , NULL                     -- last_changed_date should be NULL
          , NULL                     -- last_enforcement will be updated when determined below when enforcements are processed
          , v_originator_name
          , v_originator_id
          , CASE                     -- originator_type is FP if fp_ticket_detail exists, TFO if no fp_ticket_detail exists
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
          )
    RETURNING 
            defendant_account_id
          , account_number 
    INTO 
            po_defendant_account_id
          , po_account_number;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error in p_create_defendant_account: %',' SQLSTATE: '||SQLSTATE ||'   SQLERRM: '|| SQLERRM;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_defendant_account
    IS 'The interface procedure to create manual account. It parses the account json to insert into the substantive tables.';