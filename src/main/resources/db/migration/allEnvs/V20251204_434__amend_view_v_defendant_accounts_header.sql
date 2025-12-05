/**
* CGI OPAL Program
*
* MODULE      : amend_view_v_defendant_accounts_header.sql
*
* DESCRIPTION : Amend view to retrieve defendant account header information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 29/07/2025    C Cho       1.0         PO-1641 Create view to retrieve defendant header summary information
* 01/12/2025    C Cho       1.1         PO-2338 Add has_consolidated_accounts flag
*
**/
CREATE OR REPLACE VIEW v_defendant_accounts_header
AS 
    SELECT DISTINCT
           da.defendant_account_id
         , da.version_number
         , da.account_number
         , da.prosecutor_case_reference
         , da.account_status
         , da.account_type
         , da.amount_paid AS paid_written_off
         , da.account_balance
         , da.amount_imposed
         , f_arrears(da.defendant_account_id) AS arrears
         , dap_def.defendant_account_party_id
         , CASE WHEN dap_def.debtor IS TRUE THEN dap_def.association_type ELSE NULL END AS debtor_type
         , p_def.party_id
         , p_def.title
         , p_def.forenames
         , p_def.surname
         , p_def.birth_date
         , p_def.organisation
         , p_def.organisation_name
         , bu.business_unit_id
         , bu.business_unit_name
         , bu.business_unit_code
         , fpo.ticket_number
         , dap_pg.defendant_account_party_id AS parent_guardian_account_party_id
         , CASE WHEN dap_pg.defendant_account_id IS NOT NULL THEN TRUE ELSE FALSE END AS has_parent_guardian
         , CASE WHEN dap_pg.debtor IS TRUE THEN dap_pg.association_type ELSE NULL END AS parent_guardian_debtor_type
         , CASE WHEN dt.defendant_account_id IS NOT NULL THEN TRUE ELSE FALSE END AS has_consolidated_accounts
      FROM defendant_accounts da
      JOIN business_units bu
        ON da.business_unit_id = bu.business_unit_id                
      JOIN defendant_account_parties dap_def
        ON da.defendant_account_id = dap_def.defendant_account_id
       AND dap_def.association_type = 'Defendant'
      JOIN parties p_def
        ON dap_def.party_id = p_def.party_id
 LEFT JOIN fixed_penalty_offences fpo
        ON da.defendant_account_id = fpo.defendant_account_id
 LEFT JOIN defendant_account_parties dap_pg
        ON da.defendant_account_id = dap_pg.defendant_account_id
       AND dap_pg.association_type = 'Parent/Guardian'
 LEFT JOIN ( SELECT DISTINCT defendant_account_id
               FROM defendant_transactions
              WHERE transaction_type = 'CONSOL'
                AND associated_record_type = 'defendant_accounts'
           ) dt
        ON da.defendant_account_id = dt.defendant_account_id
;

COMMENT ON VIEW v_defendant_accounts_header IS 'Retrieves defendant account header information for the Defendant Header Summary';