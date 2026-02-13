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
*            : pi_business_unit_id       - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*            : pi_posted_by              - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*            : pi_posted_by_name         - The user that is submitting the request to be passed in by the backend
*            : po_account_number         - The Opal account number to be generated and returned to the backend
*            : po_defendant_account_id   - The Opal defendant account id to be generated and returned to the backend
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
* 01/08/2024    TMc         4.0         Call to new procedure (p_create_impositions) to process:
*                                               PO-1039 offences and impositions, 
*                                               PO-1043 creditor_account, minor creditor, 
*                                               PO-1039 control_totals, 
*                                               PO-1040 allocations, 
*                                               PO-1038 defendant_transactions, 
*                                               PO-1041 document_instances (compensation notice)
*                                               Also updates defendant_accounts (amount_imposed, amount_paid, amount_balance)
*                                       PO-1036 Added code for payment_card_requests and call to new procedure (p_create_payment_terms) to process payment terms.
*                                       PO-1034 Call to new procedure (p_create_enforcements) to process enforcements.
*                                               Also updates defendant_accounts (last_enforcement)
*                                       PO-1035 Call to new procedure (p_create_account_notes) to process acocunt notes.
*                                       PO-1041 Added code to process document_instances ('TFO Order' then 'TFO Letter')
*                                       PO-1042 Added code to process report_entries
*                                       Changes commented with 'v4.0'
* 28/08/2025    TMc         4.1         PO-2096 Added originator_name parameter when calling p_create_impositions
* 28/08/2025    TMc         4.2         PO-2099 Populate DEFENDANT_ACCOUNTS.VERSION_NUMBER with 1 instead of NULL
* 03/09/2025    TMc         4.3         PO-1044 Populate DEFENDANT_ACCOUNTS.CHEQUE_CLEARANCE_PERIOD, CREDIT_TRANS_CLEARANCE_PERIOD 
*                                               Values for CHEQUE_CLEARANCE_PERIOD and CREDIT_TRANS_CLEARANCE_PERIOD are retrieved from the CONFIGURATION_ITEMS table.
*                                       PO-2118 Populate DEFENDANT_ACCOUNTS.PAYMENT_CARD_REQUESTED_BY_NAME
* 13/10/2025    CL          5.0         PO-2291 - Removed the originator_name parameter from the call to p_create_impositions, as the column has been 
*                                                 removed from the impositions table
* 03/02/2026    TMc         6.0         PO-2751 - Populate new column DEFENDANT_ACCOUNTS.IMPOSED_BY_NAME and amend how DEFENDANT_ACCOUNTS.ORIGINATOR_TYPE is populated.
**/
DECLARE
    c_ci_cheque_clearance_period       CONSTANT    configuration_items.item_name%TYPE := 'DEFAULT_CHEQUE_CLEARANCE_PERIOD';         --v4.3
    c_ci_credit_trans_clearance_period CONSTANT    configuration_items.item_name%TYPE := 'DEFAULT_CREDIT_TRANS_CLEARANCE_PERIOD';   --v4.3
    
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
    
    v_originator_type                defendant_accounts.originator_type%TYPE;  -- v4.0
    v_cheque_clearance_period        defendant_accounts.cheque_clearance_period%TYPE;        -- v4.3
    v_credit_trans_clearance_period  defendant_accounts.credit_trans_clearance_period%TYPE;  -- v4.3
    v_imposed_by_name                defendant_accounts.imposed_by_name%TYPE;  -- v6.0

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
         , account ->> 'originator_type'        -- v6.0
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
       , v_originator_type                      -- v6.0
       , v_fp_ticket_detail_json
       , v_collection_order
       , v_collection_order_date
       , v_suspended_committal_date
       , v_payment_card_requested
       , v_prosecutor_case_reference
       , v_defendant_json                       -- v3.0
       , v_account_notes_json_array             -- v4.0
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

    --Retrieve values for CHEQUE_CLEARANCE_PERIOD and CREDIT_TRANS_CLEARANCE_PERIOD from CONFIGURATION_ITEMS
    --Both SELECT statements added in v4.3
    SELECT item_value 
      INTO STRICT v_cheque_clearance_period         --STRICT to ensure 1 row is returned
      FROM configuration_items
     WHERE item_name = c_ci_cheque_clearance_period;
      
    SELECT item_value 
      INTO STRICT v_credit_trans_clearance_period   --STRICT to ensure 1 row is returned
      FROM configuration_items
     WHERE item_name = c_ci_credit_trans_clearance_period;
    
    -- v6.0 - Added IF statement for imposed_by_name
    IF LOWER(v_account_type) = 'fixed penalty' THEN
        SELECT lja.name
          INTO v_imposed_by_name
          FROM courts c 
          JOIN local_justice_areas lja 
            ON lja.local_justice_area_id = c.local_justice_area_id
         WHERE c.court_id = v_enforcement_court_id;
    ELSE
        v_imposed_by_name := v_originator_name;
    END IF;    

    -- Determine report_id - If originator_type is FP then 'fp_register' else 'tfo_in_register'  - v6.0
    IF v_originator_type = 'FP' THEN 
        v_report_id := 'fp_register';
    ELSE 
        v_report_id := 'tfo_in_register';
    END IF;

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
          , payment_card_requested_by_name  --v4.3
          , prosecutor_case_reference
          , enforcement_case_status
          , account_comments            --v3.0
          , account_note_1              --v3.0
          , account_note_2              --v3.0
          , account_note_3              --v3.0
          , jail_days                   --v3.0
          , version_number              --v4.0
          , imposed_by_name             --v6.0
          )
    VALUES( 
            NEXTVAL('defendant_account_id_seq')
          , pi_business_unit_id
          , f_get_account_number(pi_business_unit_id, 'defendant_accounts')  -- Use the generated account number from f_get_account_number
          , v_account_type
          , v_account_sentence_date
          , NULL                    -- imposing_court_id should be NULL
          , 0.00                    -- amount_imposed  - this will be updated by a later procedure - v4.0: Updated by p_create_impositions
          , 0.00                    -- amount_paid     - this will be updated by a later procedure - v4.0: Updated by p_create_impositions
          , 0.00                    -- account_balance - this will be updated by a later procedure - v4.0: Updated by p_create_impositions
          , 'L'                     -- account_status should be L for Live
          , NULL                    -- completed_date should be NULL
          , v_enforcement_court_id  -- If this doesn't exist in COURTS then a FK violation exception will be raised 
          , NULL                    -- last_hearing_court_id should be NULL
          , NULL                    -- last_hearing_date should be NULL
          --, NULL                    -- last_movement_date should be NULL      v3.0 - Commented out
          , CURRENT_TIMESTAMP       -- last_movement_date                       v3.0 - Added
          , NULL                    -- last_changed_date should be NULL
          , NULL                    -- last_enforcement will be updated when determined below when enforcements are processed - v4.0: Updated by p_create_enforcements
          , v_originator_name
          , v_originator_id
          /*  v4.0 Commented out
          , CASE                    -- originator_type is FP if fp_ticket_detail exists, TFO if no fp_ticket_detail exists
                WHEN v_fp_ticket_detail_json IS NOT NULL THEN 'FP'    
                ELSE 'TFO'
            END
          */
          , v_originator_type       -- v4.0 - Added
          , TRUE                    -- allow_writeoffs should be TRUE
          , TRUE                    -- allow_cheques should be TRUE
          , v_cheque_clearance_period        -- cheque_clearance_period           Amended in v4.3. Was being set to NULL
          , v_credit_trans_clearance_period  -- credit_trans_clearance_period     Amended in v4.3. Was being set to NULL
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
          , CASE                    -- payment_card_requested_by_name should be pi_posted_by_name if payment_card_requested is TRUE   v4.3
                WHEN v_payment_card_requested = TRUE THEN pi_posted_by_name     
                ELSE NULL
            END
          , v_prosecutor_case_reference 
          , NULL                    -- enforcement_case_status should be NULL
          , NULL                    -- account_comments                 v4.0 - Updated by p_create_account_notes
          , NULL                    -- account_note_1 should be NULL    v4.0
          , NULL                    -- account_note_2 should be NULL    v4.0
          , NULL                    -- account_note_3 should be NULL    v4.0
          , NULL                    -- jail_days                        v4.0 - Updated by p_create_payment_terms
          , 1                       -- version_number                   v4.0 - v4.2 changed from NULL to 1
          , v_imposed_by_name       --v6.0
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

    

    -- PROCESS OFFENCES  (impositions, creditor_accounts, minor creditor, control_totals, allocations, defendant_transactions, document_instances (compensation notice))
    --                    Also updates defendant_accounts (amount_imposed, amount_paid, amount_balance)
    -- v4.0 - Added call to p_create_impositions
    CALL p_create_impositions ( po_defendant_account_id,
                                pi_business_unit_id,
                                pi_posted_by,
                                pi_posted_by_name,                                
                                v_offences_json_array,
                                v_da_account_balance
    );

    -- PAYMENT_CARD_REQUESTS
    -- v4.0 - Added IF statement
    IF v_payment_card_requested THEN

        INSERT INTO payment_card_requests (defendant_account_id)
        VALUES (po_defendant_account_id)
        ON CONFLICT(defendant_account_id) DO NOTHING;   --Ignore if defendant_account_id already exists in the table

    END IF;

    -- PAYMENT_TERMS  -  Must be done after p_create_impositions (i.e. after defendant_transactions POSTED_DATE)
    --                   Also updates defendant_accounts (jail_days)
    -- v4.0 - Added call to p_create_payment_terms
    CALL p_create_payment_terms ( po_defendant_account_id,
                                  v_account_type,
                                  v_da_account_balance,
                                  pi_posted_by,
                                  pi_posted_by_name,
                                  v_payment_terms_json
    );

    -- ENFORCEMENTS  -  Must be done after p_create_payment_terms (i.e. after payment_terms POSTED_DATE)
    --                  Also updates defendant_accounts (last_enforcement)
    -- v4.0 - Added call to p_create_enforcements
    CALL p_create_enforcements ( po_defendant_account_id,
                                 pi_posted_by,
                                 pi_posted_by_name,
                                 v_enforcements_json_array,
                                 v_last_enforcement
    );

    -- ACCOUNT NOTES  -  Must be done after p_create_enforcements (i.e. after enforcements POSTED_DATE)
    --                   Also updates defendant_accounts (account_comments)
    -- v4.0 - Added call to p_create_account_notes
    CALL p_create_account_notes ( po_defendant_account_id,
                                  pi_posted_by,
                                  pi_posted_by_name,
                                  v_account_notes_json_array
    );

    -- DOCUMENT_INSTANCES ('TFO Order' and 'TFO Letter'. 'compensation notice' is done by p_create_impositions)
    -- v4.0 - Added SELECT, both IF and INSERT statement

    --TFO Order - Retrieve the document_language for the debtor and work out the document_id
    SELECT document_language
      INTO STRICT v_debtor_document_language        --STRICT because there should be one debtor_detail record for the debtor
      FROM debtor_detail dt
      JOIN defendant_account_parties dap
        ON dt.party_id = dap.party_id
     WHERE dap.defendant_account_id = po_defendant_account_id
       AND dap.debtor = TRUE;

    IF LOWER(v_account_type) = 'fixed penalty' THEN
        v_document_id := 'FINOR';
    ELSE
        v_document_id := 'FINOT';
    END IF;

    IF v_debtor_document_language = 'CY' THEN
        v_document_id := 'CY_' || v_document_id;
    END IF;

    --Insert DOCUMENT_INSTANCES records. 'TFO Order' then 'TFO Letter'
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
        , v_document_id
        , pi_business_unit_id
        , CURRENT_TIMESTAMP        --generated_date
        , pi_posted_by             --generated_by
        , 'defendant_account'      --associated_record_type
        , po_defendant_account_id  --associated_record_id
        , 'New'                    --status
        , NULL                     --printed_date
        , NULL                     --document_content
    ),
    (
        NEXTVAL('document_instance_id_seq')
        , 'FINOTA'
        , pi_business_unit_id
        , CURRENT_TIMESTAMP        --generated_date
        , pi_posted_by             --generated_by
        , 'defendant_account'      --associated_record_type
        , po_defendant_account_id  --associated_record_id
        , 'New'                    --status
        , NULL                     --printed_date
        , NULL                     --document_content
    );

    RAISE INFO 'p_create_defendant_account: Created document_instances records for defendant_account_id = %', po_defendant_account_id;

    --Insert REPORT_ENTRIES record.
    -- v4.0 - Added IF and INSERT statements    
    INSERT INTO report_entries (
          report_entry_id
        , business_unit_id
        , report_id
        , entry_timestamp
        , reported_timestamp
        , associated_record_type
        , associated_record_id
        , report_instance_id
    )
    VALUES (
          NEXTVAL('report_entry_id_seq')
        , pi_business_unit_id
        , v_report_id
        , CLOCK_TIMESTAMP()        --entry_timestamp
        , NULL                     --reported_timestamp        --PO-1995 make REPORTED_TIMESTAMP nullable
        , 'defendant_accounts'     --associated_record_type
        , po_defendant_account_id  --associated_record_id
        , NULL                     --report_instance_id
    );

    RAISE INFO 'p_create_defendant_account: Created report_entries record for defendant_account_id = %, report_id = %', po_defendant_account_id, v_report_id;

EXCEPTION
    WHEN SQLSTATE 'P2002' OR SQLSTATE 'P2003' OR SQLSTATE 'P2004' OR SQLSTATE 'P2005' OR SQLSTATE 'P2006' OR    --v3.0
         SQLSTATE 'P2008' OR SQLSTATE 'P2009' OR SQLSTATE 'P2010' OR SQLSTATE 'P2011' OR SQLSTATE 'P2012' OR SQLSTATE 'P2013' THEN   --v4.0
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
                    , DETAIL = 'p_create_defendant_account: %' || v_pg_exception_detail;
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