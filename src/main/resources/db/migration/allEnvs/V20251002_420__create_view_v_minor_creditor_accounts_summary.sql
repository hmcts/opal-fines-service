/**
* CGI OPAL Program
*
* MODULE      : create_view_v_minor_creditor_accounts_summary.sql
*
* DESCRIPTION : Create view to retrieve minor creditors accounts summary information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 25/09/2025    CL          1.0         PO-1927 - Create view to retrieve minor creditor accounts summary information
*
**/
CREATE OR REPLACE VIEW v_minor_creditor_accounts_summary
AS 
    SELECT DISTINCT
           t.creditor_account_id    
         , t.creditor_account_number
         , t.pay_by_bacs
         , t.version_number
         , t.hold_payout
         , p_mc.party_id
         , p_mc.title                 AS creditor_title
         , p_mc.forenames             AS creditor_forenames     
         , p_mc.surname               AS creditor_surname
         , p_mc.organisation          AS creditor_organisation
         , p_mc.organisation_name     AS creditor_organisation_name
         , p_mc.address_line_1        AS creditor_address_line_1
         , p_mc.address_line_2        AS creditor_address_line_2
         , p_mc.address_line_3        AS creditor_address_line_3
         , p_mc.address_line_4        AS creditor_address_line_4
         , p_mc.address_line_5        AS creditor_address_line_5
         , p_mc.postcode              AS creditor_postcode
         , da.defendant_account_id
         , da.account_number          AS defendant_account_number
         , p_da.title                 AS defendant_title
         , p_da.forenames             AS defendant_forenames
         , p_da.surname               AS defendant_surname         
      FROM ( SELECT ca.creditor_account_id    
                , ca.account_number AS creditor_account_number
                , ca.pay_by_bacs
                , ca.version_number
                , ca.hold_payout
                , ca.minor_creditor_party_id                
             FROM creditor_accounts ca
            WHERE ca.creditor_account_type = 'MN'
           ) t    
      JOIN parties p_mc
        ON t.minor_creditor_party_id = p_mc.party_id    
 LEFT JOIN impositions i
        ON t.creditor_account_id = i.creditor_account_id
 LEFT JOIN defendant_accounts da
        ON i.defendant_account_id = da.defendant_account_id
 LEFT JOIN defendant_account_parties dap
        ON da.defendant_account_id = dap.defendant_account_id
       AND dap.association_type = 'Defendant'
 LEFT JOIN parties p_da
        ON dap.party_id = p_da.party_id;

COMMENT ON VIEW v_minor_creditor_accounts_summary IS 'Retrieves minor creditor accounts summary information';
    

  