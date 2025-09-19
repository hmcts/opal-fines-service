/**
* CGI OPAL Program
*
* MODULE      : create_view_v_defendant_accounts_summary.sql
*
* DESCRIPTION : Create view to retrieve defendant account summary information for At a Glance
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 31/07/2025    C Cho       1.0         PO-1641 Create view to retrieve defendant at a glance summary information
* 11/09/2025    P Brumby    1.1         PO-2138 Add more enforcer and LJA fields to v_defendant_accounts_summary - 
*                                               enforcer_id, lja_id, age, enforcer_name, lja_name
*
**/

-- Drop view first to allow new columns to be added to existing view
DROP VIEW v_defendant_accounts_summary;

CREATE OR REPLACE VIEW v_defendant_accounts_summary
AS 
    SELECT DISTINCT
           da.defendant_account_id
         , da.version_number
         , da.account_number
         , da.last_enforcement
         , r_le.result_title AS last_enf_title
         , da.collection_order
         , da.account_comments
         , da.account_note_1
         , da.account_note_2
         , da.account_note_3
         , da.jail_days
         , da.enf_override_result_id
         , da.enf_override_enforcer_id AS enforcer_id
         , e.name AS enforcer_name
         , da.enf_override_tfo_lja_id AS lja_id
         , lja.name AS lja_name
         , r_eo.result_title AS enf_override_title
         , da.last_movement_date
         , dap.association_type AS debtor_type
         , dd.document_language
         , dd.hearing_language
         , p.party_id
         , p.title
         , p.forenames
         , p.surname
         , p.birth_date
         , p.age
         , p.organisation
         , p.organisation_name
         , p.address_line_1
         , p.address_line_2
         , p.address_line_3
         , p.address_line_4
         , p.address_line_5
         , p.postcode
         , p.national_insurance_number
         , a.alias_id
         , a.sequence_number         
         , a.organisation_name AS alias_org_name
         , a.forenames AS alias_forenames
         , a.surname AS alias_surname         
         , pt.terms_type_code
         , pt.instalment_period
         , pt.instalment_amount
         , pt.instalment_lump_sum
         , pt.effective_date
      FROM defendant_accounts da
 LEFT JOIN results r_le
        ON da.last_enforcement = r_le.result_id
 LEFT JOIN results r_eo
        ON da.enf_override_result_id = r_eo.result_id
      JOIN defendant_account_parties dap
        ON da.defendant_account_id = dap.defendant_account_id
       AND dap.debtor IS TRUE
 LEFT JOIN parties p
        ON dap.party_id = p.party_id
 LEFT JOIN debtor_detail dd
        ON dap.party_id = dd.party_id
 LEFT JOIN aliases a
        ON p.party_id = a.party_id
 LEFT JOIN payment_terms pt
        ON da.defendant_account_id = pt.defendant_account_id
 LEFT JOIN enforcers e
        ON da.enf_override_enforcer_id = e.enforcer_id
 LEFT JOIN local_justice_areas lja
        ON da.enf_override_tfo_lja_id = lja.local_justice_area_id
;

COMMENT ON VIEW v_defendant_accounts_summary IS 'Retrieves defendant account summary information for the At a Glance section';