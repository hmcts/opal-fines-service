/**
* CGI OPAL Program
*
* MODULE      : create_view_v_enforcement_status.sql
*
* DESCRIPTION : Create view to retrieve enforcement status information with defendant account details
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 02/09/2025    C Cho       1.0         PO- 1703 Create view to retrieve enforcement status information with defendant account details
*
**/
CREATE OR REPLACE VIEW v_enforcement_status
AS 
    SELECT 
           -- Defendant Accounts fields
           da.defendant_account_id
         , da.account_status
         , da.collection_order
         , da.jail_days AS days_in_default
         , da.enforcing_court_id
         , da.last_enforcement
         , da.enf_override_result_id
         , da.enf_override_enforcer_id
         , da.enf_override_tfo_lja_id
           -- Enforcement fields
         , e.reason
         , e.enforcer_id
         , e.posted_date
         , e.hearing_date
         , e.hearing_court_id
         , e.result_responses
         , e.warrant_reference
         , e.jail_days
           -- Defendant Account Parties fields
         , dap.association_type
           -- Party fields (for Youth/Adult/Organisation determination)
         , p.birth_date
         , p.age
         , p.organisation
           -- Result fields
         , r.result_id
         , r.result_title
         , r.enf_next_permitted_actions
         , CASE WHEN dd.employer_name IS NOT NULL THEN TRUE ELSE FALSE END AS employer_flag
      FROM defendant_accounts da
 LEFT JOIN defendant_account_parties dap
        ON da.defendant_account_id = dap.defendant_account_id
 LEFT JOIN parties p
        ON dap.party_id = p.party_id
 LEFT JOIN enforcements e
        ON da.defendant_account_id = e.defendant_account_id
 LEFT JOIN results r
        ON da.enf_override_result_id = r.result_id
 LEFT JOIN debtor_detail dd
        ON dap.party_id = dd.party_id
;

COMMENT ON VIEW v_enforcement_status IS 'Retrieves enforcement status information with defendant account details and related party information for Youth/Adult/Organisation determination';