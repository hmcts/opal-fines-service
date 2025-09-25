/**
* CGI OPAL Program
*
* MODULE      : create_view_v_minor_creditor_account_header.sql
*
* DESCRIPTION : Create view to retrieve minor creditors accounts header information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 18/09/2025    CL          1.0         PO-1926 - Create view to retrieve minor creditor accounts header information
*
**/
CREATE OR REPLACE VIEW v_minor_creditor_account_header
AS 
    SELECT DISTINCT
           t.creditor_account_id    
         , t.creditor_account_number
         , t.creditor_account_type
         , t.version_number
         , p.party_id
         , p.title
         , p.forenames
         , p.surname         
         , p.organisation
         , p.organisation_name        
         , bu.business_unit_id
         , bu.business_unit_name
         , bu.welsh_language
         , COALESCE((SELECT SUM(imposed_amount)
                       FROM impositions i
                      WHERE i.creditor_account_id = t.creditor_account_id), 0) AS awarded
         , COALESCE((SELECT SUM(transaction_amount)
                       FROM creditor_transactions ct
                      WHERE ct.creditor_account_id = t.creditor_account_id
                        AND payment_processed IS TRUE), 0)                     AS paid_out
         , COALESCE((SELECT SUM(ct.transaction_amount)
                       FROM creditor_transactions ct
                      WHERE ct.creditor_account_id = t.creditor_account_id
                        AND ct.payment_processed IS FALSE ), 0)                AS awaiting_payment         
         , COALESCE((SELECT SUM(i.imposed_amount) - SUM(i.paid_amount)
                       FROM impositions i
                      WHERE i.creditor_account_id = t.creditor_account_id), 0) AS outstanding         
    FROM ( SELECT ca.creditor_account_id    
                , ca.account_number AS creditor_account_number
                , ca.creditor_account_type
                , ca.version_number
                , ca.minor_creditor_party_id
                , ca.business_unit_id
             FROM creditor_accounts ca
            WHERE ca.creditor_account_type = 'MN'
         ) t    
      JOIN parties p
        ON t.minor_creditor_party_id = p.party_id    
      JOIN business_units bu
        ON t.business_unit_id = bu.business_unit_id;
        
COMMENT ON VIEW v_minor_creditor_account_header IS 'Retrieves minor creditor accounts header information';
    

  