/**
* CGI OPAL Program
*
* MODULE      : create_view_v_audit_creditor_accounts.sql
*
* DESCRIPTION : Create view to retrieve audit creditor account information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 15/08/2025    C Cho       1.0         PO-1664 Create view to retrieve audit creditor account information
*
**/
CREATE OR REPLACE VIEW v_audit_creditor_accounts
AS 
    SELECT DISTINCT
           -- Party fields
           TRIM(COALESCE(p.title, '') || ' ' || COALESCE(p.forenames, '') || ' ' || COALESCE(p.surname, '')) AS name
         , p.address_line_1
         , p.address_line_2
         , p.address_line_3
         , p.postcode
           -- Creditor Accounts fields
         , ca.creditor_account_id
         , ca.hold_payout
         , ca.pay_by_bacs
         , ca.bank_sort_code
         , ca.bank_account_type
         , ca.bank_account_number
         , ca.bank_account_name
         , ca.bank_account_reference
      FROM creditor_accounts ca
 LEFT JOIN parties p
        ON ca.minor_creditor_party_id = p.party_id AND ca.creditor_account_type = 'MN'
;

COMMENT ON VIEW v_audit_creditor_accounts IS 'Retrieves audit creditor account information with related party details';