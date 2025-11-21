/**
* CGI OPAL Program
*
* MODULE      : create_view_v_major_creditor_account_header.sql
*
* DESCRIPTION : Creates the v_major_creditor_account_header view for the Fines database
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------
* 17/11/2025    CL          1.0         PO-2122 - Unit Test for v_major_creditor_account_header view for the Fines database
*
**/

CREATE OR REPLACE VIEW v_major_creditor_account_header
AS
    SELECT ca.creditor_account_id
         , ca.account_number            AS creditor_account_number
         , ca.creditor_account_type
         , ca.version_number
         , bu.business_unit_id
         , bu.business_unit_name
         , mj.name                      AS name         
         , (SELECT COALESCE(SUM(ct.transaction_amount), 0)
              FROM creditor_transactions ct
             WHERE ct.creditor_account_id = ca.creditor_account_id
               AND ct.transaction_type = 'PAYMNT'
               AND ct.payment_processed = FALSE ) AS awaiting_payout
      FROM creditor_accounts ca
      JOIN business_units bu 
        ON bu.business_unit_id = ca.business_unit_id
      JOIN major_creditors mj 
        ON mj.major_creditor_id = ca.major_creditor_id     
     WHERE ca.creditor_account_type = 'MJ'           
     UNION 
    SELECT ca.creditor_account_id
         , ca.account_number            AS creditor_account_number
         , ca.creditor_account_type
         , ca.version_number
         , bu.business_unit_id
         , bu.business_unit_name         
         , ci.item_values ->> 'name'    AS name   
         , (SELECT COALESCE(SUM(ct.transaction_amount), 0)
              FROM creditor_transactions ct
             WHERE ct.creditor_account_id = ca.creditor_account_id
               AND ct.transaction_type = 'PAYMNT'
               AND ct.payment_processed = FALSE ) AS awaiting_payout
      FROM creditor_accounts ca
      JOIN business_units bu 
        ON bu.business_unit_id = ca.business_unit_id
      JOIN configuration_items ci 
        ON bu.business_unit_id = ci.business_unit_id
       AND ci.item_name = 'CENTRAL_FUND_ACCOUNT'
     WHERE ca.creditor_account_type = 'CF';

COMMENT ON VIEW v_major_creditor_account_header IS 'Retrieves major creditor account header information';
    

  
