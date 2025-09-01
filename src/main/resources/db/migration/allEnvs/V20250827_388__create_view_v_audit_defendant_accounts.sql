/**
* CGI OPAL Program
*
* MODULE      : create_view_v_audit_defendant_accounts.sql
*
* DESCRIPTION : Create view to retrieve audit defendant account information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 18/08/2025    C Cho       1.0         Create view to retrieve audit defendant account information
*
**/
CREATE OR REPLACE VIEW v_audit_defendant_accounts
AS 
    SELECT DISTINCT
           -- Defendant Accounts fields
           da.defendant_account_id
         , da.cheque_clearance_period
         , da.allow_cheques
         , da.credit_trans_clearance_period
         , da.allow_writeoffs
         , da.enf_override_enforcer_id
         , da.enf_override_result_id
         , da.enf_override_tfo_lja_id
         , da.enforcing_court_id
         , da.collection_order
         , da.suspended_committal_date
         , da.account_comments
         , da.account_note_1
         , da.account_note_2
         , da.account_note_3
         -- Defendant Party fields
         , CASE WHEN p_def.organisation IS TRUE 
                THEN p_def.organisation_name 
                ELSE TRIM(COALESCE(p_def.title, '') || ' ' || COALESCE(p_def.forenames, '') || ' ' || COALESCE(p_def.surname, ''))
           END AS name
         , p_def.birth_date
         , p_def.age
         , p_def.address_line_1
         , p_def.address_line_2
         , p_def.address_line_3
         , p_def.postcode
         , p_def.national_insurance_number
         , p_def.telephone_home
         , p_def.telephone_business
         , p_def.telephone_mobile
         , p_def.email_1
         , p_def.email_2
         -- Parent/Guardian Party fields
         , CASE WHEN p_pg.organisation IS TRUE 
                THEN p_pg.organisation_name 
                ELSE TRIM(COALESCE(p_pg.title, '') || ' ' || COALESCE(p_pg.forenames, '') || ' ' || COALESCE(p_pg.surname, ''))
           END AS pname
         , p_pg.address_line_1 AS paddr1
         , p_pg.address_line_2 AS paddr2
         , p_pg.address_line_3 AS paddr3
         , p_pg.birth_date AS pbdate
         , p_pg.national_insurance_number AS pninumber
         -- Aliases fields (up to 5)
         , a1.alias_name AS alias1
         , a2.alias_name AS alias2
         , a3.alias_name AS alias3
         , a4.alias_name AS alias4
         , a5.alias_name AS alias5
         -- Debtor Details fields
         , dd.document_language
         , dd.hearing_language
         , dd.vehicle_make
         , dd.vehicle_registration
         , dd.employee_reference
         , dd.employer_name
         , dd.employer_address_line_1
         , dd.employer_address_line_2
         , dd.employer_address_line_3
         , dd.employer_address_line_4
         , dd.employer_address_line_5
         , dd.employer_postcode
         , dd.employer_telephone
         , dd.employer_email
      FROM defendant_accounts da
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
 LEFT JOIN debtor_detail dd
        ON p_def.party_id = dd.party_id
 LEFT JOIN (
        SELECT a.party_id,
               CASE WHEN p.organisation IS TRUE 
                    THEN a.organisation_name 
                    ELSE TRIM(COALESCE(a.forenames, '') || ' ' || COALESCE(a.surname, ''))
               END AS alias_name
          FROM aliases a
          JOIN parties p ON a.party_id = p.party_id
          AND a.sequence_number = 1
       ) a1 ON p_def.party_id = a1.party_id
 LEFT JOIN (
        SELECT a.party_id,
               CASE WHEN p.organisation IS TRUE 
                    THEN a.organisation_name 
                    ELSE TRIM(COALESCE(a.forenames, '') || ' ' || COALESCE(a.surname, ''))
               END AS alias_name
          FROM aliases a
          JOIN parties p ON a.party_id = p.party_id
          AND a.sequence_number = 2
       ) a2 ON p_def.party_id = a2.party_id
 LEFT JOIN (
        SELECT a.party_id,
               CASE WHEN p.organisation IS TRUE 
                    THEN a.organisation_name 
                    ELSE TRIM(COALESCE(a.forenames, '') || ' ' || COALESCE(a.surname, ''))
               END AS alias_name
          FROM aliases a
          JOIN parties p ON a.party_id = p.party_id
          AND a.sequence_number = 3
       ) a3 ON p_def.party_id = a3.party_id
 LEFT JOIN (
        SELECT a.party_id,
               CASE WHEN p.organisation IS TRUE 
                    THEN a.organisation_name 
                    ELSE TRIM(COALESCE(a.forenames, '') || ' ' || COALESCE(a.surname, ''))
               END AS alias_name
          FROM aliases a
          JOIN parties p ON a.party_id = p.party_id
          AND a.sequence_number = 4
       ) a4 ON p_def.party_id = a4.party_id
 LEFT JOIN (
        SELECT a.party_id,
               CASE WHEN p.organisation IS TRUE 
                    THEN a.organisation_name 
                    ELSE TRIM(COALESCE(a.forenames, '') || ' ' || COALESCE(a.surname, ''))
               END AS alias_name
          FROM aliases a
          JOIN parties p ON a.party_id = p.party_id
          AND a.sequence_number = 5
       ) a5 ON p_def.party_id = a5.party_id
;

COMMENT ON VIEW v_audit_defendant_accounts IS 'Retrieves audit defendant account information with related party, alias, and debtor details';