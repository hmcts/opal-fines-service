/**
* CGI OPAL Program
*
* MODULE      : create_view_v_search_def_account_and_alias.sql
*
* DESCRIPTION : Create view to retrieve defendant account information, including aliases, for Search and Matches
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------------------------------------
* 11/07/2025    TMc         1.0         PO-1586 - Create view to retrieve defendant account information, including aliases, for Search and Matches
*
**/
CREATE OR REPLACE VIEW v_search_def_account_and_alias
AS 
    SELECT vsda.defendant_account_id
         , vsda.account_number
         , vsda.prosecutor_case_reference
         , vsda.last_enforcement
         , vsda.account_status
         , vsda.defendant_account_balance
         , vsda.completed_date
         , vsda.business_unit_id
         , vsda.business_unit_name
         , vsda.party_id
         , vsda.organisation
         , vsda.organisation_name
         , vsda.address_line_1
         , vsda.postcode
         , vsda.title
         , vsda.forenames
         , vsda.surname
         , vsda.birth_date
         , vsda.national_insurance_number
         , vsda.parent_guardian_surname
         , vsda.parent_guardian_forenames
         , a.sequence_number   AS alias_sequence_number
         , a.organisation_name AS alias_organisation_name
         , a.surname           AS alias_surname
         , a.forenames         AS alias_forenames
      FROM v_search_defendant_accounts vsda
 LEFT JOIN aliases a
        ON vsda.party_id = a.party_id
;

COMMENT ON VIEW v_search_def_account_and_alias IS 'Retrieves defendant account information, including aliases, for Search and Matches';
