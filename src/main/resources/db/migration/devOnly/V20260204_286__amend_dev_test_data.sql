/**
* CGI OPAL Program
*
* MODULE      : amend_dev_test_data.sql
*
* DESCRIPTION : Amend the DRAFT_ACCOUNTS test data, to be used by Frontend for development testing, to include originator_type (set to NEW) in the account json. 
*               Update DEFENDANT_ACCOUNTS test data for records related to those that are going to be deleted from LOCAL_JUSTICE_AREAS.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -------------------------------------------------------------------------------------------------------------------------------
* 10/02/2026    T McCallion    1.0         PO-2751 - Populate new column DEFENDANT_ACCOUNTS.IMPOSED_BY_NAME and amend how DEFENDANT_ACCOUNTS.ORIGINATOR_TYPE is populated.
*                                          Original script: V20260116_285__create_dev_test_data.sql
*                                          PO-2752 - Amend reference data in the LOCAL_JUSTICE_AREAS table
*
**/

--Update FK column on defendant_accounts for related local_justice_areas records that are going to be deleted
UPDATE defendant_accounts 
   SET enf_override_tfo_lja_id = 401
 WHERE enf_override_tfo_lja_id = 101;
 
UPDATE defendant_accounts 
   SET enf_override_tfo_lja_id = 402
 WHERE enf_override_tfo_lja_id = 102;
 
UPDATE defendant_accounts 
   SET enf_override_tfo_lja_id = 403
 WHERE enf_override_tfo_lja_id = 103;
 
UPDATE defendant_accounts 
   SET enf_override_tfo_lja_id = 404
 WHERE enf_override_tfo_lja_id = 106;


DO $$
DECLARE
    v_rec_da            RECORD;
    v_originator_type   defendant_accounts.originator_type%TYPE;
    v_new_json          JSON;
BEGIN

    FOR v_rec_da IN (SELECT draft_account_id, account FROM draft_accounts) 
    LOOP 
        v_originator_type := v_rec_da.account ->> 'originator_type';

        IF v_originator_type IS NULL THEN 
            
            v_new_json := jsonb_set(v_rec_da.account::jsonb, '{originator_type}', to_jsonb('NEW'::TEXT), true)::json;

            --RAISE INFO 'From new JSON: originator_type = %', v_new_json ->> 'originator_type';
            --RAISE INFO '   Old JSON = %', v_rec_da.account;
            --RAISE INFO '   New JSON = %', v_new_json;

            UPDATE draft_accounts 
               SET account = v_new_json
             WHERE draft_account_id = v_rec_da.draft_account_id;
            
        END IF;
    END LOOP;

END $$;
