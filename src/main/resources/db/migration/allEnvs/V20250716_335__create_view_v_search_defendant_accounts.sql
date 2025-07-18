/**
* CGI OPAL Program
*
* MODULE      : create_view_v_search_defendant_accounts.sql
*
* DESCRIPTION : Create view to retrieve defendant account information for Search and Matches
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------
* 11/07/2025    TMc         1.0         PO-1586 - Create view to retrieve defendant account information for Search and Matches
*
**/
CREATE OR REPLACE VIEW v_search_defendant_accounts
AS 
    SELECT da.defendant_account_id
         , da.account_number
         , da.prosecutor_case_reference
         , da.last_enforcement
         , da.account_status
         , da.account_balance AS defendant_account_balance
         , da.completed_date
         , bu.business_unit_id
         , bu.business_unit_name
         , p_def.party_id
         , p_def.organisation
         , p_def.organisation_name
         , p_def.address_line_1
         , p_def.postcode
         , p_def.title
         , p_def.forenames
         , p_def.surname
         , p_def.birth_date
         , p_def.national_insurance_number
         , p_pg.surname   AS parent_guardian_surname
         , p_pg.forenames AS parent_guardian_forenames
      FROM defendant_accounts da
      JOIN business_units bu
        ON da.business_unit_id = bu.business_unit_id
      JOIN defendant_account_parties dap_def
        ON da.defendant_account_id = dap_def.defendant_account_id
       AND dap_def.association_type = 'Defendant' 
      JOIN parties p_def
        ON dap_def.party_id = p_def.party_id
 LEFT JOIN defendant_account_parties dap_pg
        ON da.defendant_account_id = dap_pg.defendant_account_id
       AND dap_pg.association_type = 'Parent/Guardian' 
 LEFT JOIN parties p_pg
        ON dap_pg.party_id = p_pg.party_id
;

COMMENT ON VIEW v_search_defendant_accounts IS 'Retrieves defendant account information for Search and Matches';