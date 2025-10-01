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
* ----------    --------    --------    -------------------------------------------------------------------------------------------------
* 11/07/2025    TMc         1.0         PO-1586 - Create view to retrieve defendant account information for Search and Matches
* 23/09/2025    P Brumby    1.1         PO-2236 - Amend database view v_search_defendant_accounts to return Aliases as columns in one row
*
**/
DROP VIEW IF EXISTS v_search_def_account_and_alias; -- this view is no longer required since the alias logic has been incorporated into this view.

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
         -- Aliases fields (up to 5)
         , CASE WHEN p_def.organisation THEN a1.organisation_name
                ELSE NULLIF(TRIM(COALESCE(a1.forenames, '') || ' ' || COALESCE(a1.surname, '')), '')
           END AS alias1
         , CASE WHEN p_def.organisation THEN a2.organisation_name
                ELSE NULLIF(TRIM(COALESCE(a2.forenames, '') || ' ' || COALESCE(a2.surname, '')), '')
           END AS alias2
         , CASE WHEN p_def.organisation THEN a3.organisation_name
                ELSE NULLIF(TRIM(COALESCE(a3.forenames, '') || ' ' || COALESCE(a3.surname, '')), '')
           END AS alias3
         , CASE WHEN p_def.organisation THEN a4.organisation_name
                ELSE NULLIF(TRIM(COALESCE(a4.forenames, '') || ' ' || COALESCE(a4.surname, '')), '')
           END AS alias4
         , CASE WHEN p_def.organisation THEN a5.organisation_name
                ELSE NULLIF(TRIM(COALESCE(a5.forenames, '') || ' ' || COALESCE(a5.surname, '')), '')
           END AS alias5
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
 LEFT JOIN aliases a1
        ON a1.party_id = p_def.party_id
       AND a1.sequence_number = 1
 LEFT JOIN aliases a2
        ON a2.party_id = p_def.party_id
       AND a2.sequence_number = 2
 LEFT JOIN aliases a3
        ON a3.party_id = p_def.party_id
       AND a3.sequence_number = 3
 LEFT JOIN aliases a4
        ON a4.party_id = p_def.party_id
       AND a4.sequence_number = 4
 LEFT JOIN aliases a5
        ON a5.party_id = p_def.party_id
       AND a5.sequence_number = 5;

COMMENT ON VIEW v_search_defendant_accounts IS 'Retrieves defendant account information for Search and Matches';