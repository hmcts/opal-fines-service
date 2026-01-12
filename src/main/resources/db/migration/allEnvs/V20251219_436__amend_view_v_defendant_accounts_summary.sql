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
* 17/10/2025    CL          1.2         PO-2312 Amend view to to return Aliases as columns in a single row
* 18/12/2025    C Cho       1.3         PO-2304 For multiple payment terms only return active one
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
         , r_le.result_title              AS last_enf_title
         , da.collection_order
         , da.account_comments
         , da.account_note_1
         , da.account_note_2
         , da.account_note_3
         , da.jail_days
         , da.enf_override_result_id
         , da.enf_override_enforcer_id    AS enforcer_id
         , e.name                         AS enforcer_name
         , da.enf_override_tfo_lja_id     AS lja_id
         , lja.name                       AS lja_name
         , r_eo.result_title              AS enf_override_title
         , da.last_movement_date
         , dap.association_type           AS debtor_type
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
         -- Aliases fields (up to 5)
         , CASE WHEN p.organisation 
                THEN a1.alias_id||'|'||a1.sequence_number||'|'||a1.organisation_name
                ELSE NULLIF(a1.alias_id||'|'||a1.sequence_number||'|'||TRIM(COALESCE(a1.forenames, '') || ' ' || COALESCE(a1.surname, '')), '')
           END AS alias_1
         , CASE WHEN p.organisation 
                THEN a2.alias_id||'|'||a2.sequence_number||'|'||a2.organisation_name
                ELSE NULLIF(a2.alias_id||'|'||a2.sequence_number||'|'||TRIM(COALESCE(a2.forenames, '') || ' ' || COALESCE(a2.surname, '')), '')
           END AS alias_2
         , CASE WHEN p.organisation 
                THEN a3.alias_id||'|'||a3.sequence_number||'|'||a3.organisation_name
                ELSE NULLIF(a3.alias_id||'|'||a3.sequence_number||'|'||TRIM(COALESCE(a3.forenames, '') || ' ' || COALESCE(a3.surname, '')), '')
           END AS alias_3
         , CASE WHEN p.organisation 
                THEN a4.alias_id||'|'||a4.sequence_number||'|'||a4.organisation_name
                ELSE NULLIF(a4.alias_id||'|'||a4.sequence_number||'|'||TRIM(COALESCE(a4.forenames, '') || ' ' || COALESCE(a4.surname, '')), '')
           END AS alias_4
         , CASE WHEN p.organisation 
                THEN a5.alias_id||'|'||a5.sequence_number||'|'||a5.organisation_name
                ELSE NULLIF(a5.alias_id||'|'||a5.sequence_number||'|'||TRIM(COALESCE(a5.forenames, '') || ' ' || COALESCE(a5.surname, '')), '')
           END AS alias_5
         , pt.terms_type_code
         , pt.instalment_period
         , pt.instalment_amount
         , pt.instalment_lump_sum
         , pt.effective_date
      FROM defendant_accounts da
      JOIN defendant_account_parties dap
        ON da.defendant_account_id = dap.defendant_account_id
       AND dap.debtor IS TRUE        
 LEFT JOIN results r_le
        ON da.last_enforcement = r_le.result_id
 LEFT JOIN results r_eo
        ON da.enf_override_result_id = r_eo.result_id          
 LEFT JOIN debtor_detail dd
        ON dap.party_id = dd.party_id         
 LEFT JOIN payment_terms pt
        ON da.defendant_account_id = pt.defendant_account_id
 LEFT JOIN enforcers e
        ON da.enf_override_enforcer_id = e.enforcer_id
 LEFT JOIN local_justice_areas lja
        ON da.enf_override_tfo_lja_id = lja.local_justice_area_id
 LEFT JOIN parties p
        ON dap.party_id = p.party_id
 LEFT JOIN aliases a1
        ON a1.party_id = p.party_id
       AND a1.sequence_number = 1
 LEFT JOIN aliases a2
        ON a2.party_id = p.party_id
       AND a2.sequence_number = 2
 LEFT JOIN aliases a3
        ON a3.party_id = p.party_id
       AND a3.sequence_number = 3
 LEFT JOIN aliases a4
        ON a4.party_id = p.party_id
       AND a4.sequence_number = 4
 LEFT JOIN aliases a5
        ON a5.party_id = p.party_id
       AND a5.sequence_number = 5     
    WHERE pt.active IS TRUE
;

COMMENT ON VIEW v_defendant_accounts_summary IS 'Retrieves defendant account summary information for the At a Glance section';