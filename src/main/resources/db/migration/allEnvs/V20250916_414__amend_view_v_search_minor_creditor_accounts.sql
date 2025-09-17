/**
* CGI OPAL Program
*
* MODULE      : create_view_v_search_minor_creditor_accounts.sql
*
* DESCRIPTION : Create view to retrieve minor creditors information for Search and Matches
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 11/07/2025    TMc         1.0         PO-1586 - Create view to retrieve minor creditors information for Search and Matches
* 12/08/2025    C Cho       1.1         PO-2070 - Remove XFER from creditor_account_balance calculation
* 02/09/2025    C Larkin    1.2         PO-2114 - Default creditor_account_balance to zero if no data found
**/

CREATE OR REPLACE VIEW v_search_minor_creditor_accounts
AS 
    SELECT DISTINCT
           ca.creditor_account_id
         , ca.account_number
         , bu.business_unit_id
         , bu.business_unit_name
         , p_ca.party_id
         , p_ca.organisation
         , p_ca.organisation_name
         , p_ca.address_line_1
         , p_ca.postcode
         , p_ca.forenames
         , p_ca.surname
         , i.defendant_account_id
         , p_da.organisation_name AS defendant_organisation_name
         , p_da.forenames         AS defendant_forenames
         , p_da.surname           AS defendant_surname
         , (SELECT COALESCE(SUM(ct.transaction_amount), 0::NUMERIC(18,2))
              FROM creditor_transactions ct
             WHERE ct.creditor_account_id = ca.creditor_account_id
               AND ct.transaction_type = 'PAYMNT'
               AND ct.payment_processed = FALSE
              ) AS creditor_account_balance
      FROM creditor_accounts ca
      JOIN business_units bu
        ON ca.business_unit_id = bu.business_unit_id                
      JOIN parties p_ca
        ON ca.minor_creditor_party_id = p_ca.party_id
 LEFT JOIN impositions i
        ON ca.creditor_account_id = i.creditor_account_id
 LEFT JOIN defendant_account_parties dap
        ON i.defendant_account_id = dap.defendant_account_id
       AND dap.association_type = 'Defendant'
 LEFT JOIN parties p_da
        ON dap.party_id = p_da.party_id
;

COMMENT ON VIEW v_search_minor_creditor_accounts IS 'Retrieves minor creditors information for Search and Matches';