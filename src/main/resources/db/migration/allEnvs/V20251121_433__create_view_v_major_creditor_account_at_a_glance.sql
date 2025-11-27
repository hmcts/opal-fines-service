/**
* CGI OPAL Program
*
* MODULE      : create_view_v_major_creditor_account_at_a_glance.sql
*
* DESCRIPTION : Create view to retrieve major creditor account at a glance information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 14/11/2025    C Cho       1.0         PO-2123 Create view to retrieve major creditor account at a glance information
*
**/
CREATE OR REPLACE VIEW v_major_creditor_account_at_a_glance
AS 
    SELECT 
           -- Major Creditor fields
           ca.creditor_account_id
         , CASE WHEN ca.pay_by_bacs IS TRUE 
                THEN 'PROVIDED' 
                ELSE 'NOT PROVIDED' 
           END AS bacs_details
         , mj.name
         , mj.address_line_1
         , mj.address_line_2
         , mj.address_line_3
         , mj.postcode
      FROM creditor_accounts ca
      JOIN major_creditors mj
        ON ca.major_creditor_id = mj.major_creditor_id
     WHERE ca.creditor_account_type = 'MJ'

    UNION

    SELECT 
           -- Central Fund fields
           ca.creditor_account_id
         , CASE WHEN ca.pay_by_bacs IS TRUE 
                THEN 'PROVIDED' 
                ELSE 'NOT PROVIDED' 
           END AS bacs_details
         , ci.item_values ->> 'name' AS name
         , ci.item_values ->> 'address_line_1' AS address_line_1
         , ci.item_values ->> 'address_line_2' AS address_line_2
         , ci.item_values ->> 'address_line_3' AS address_line_3
         , NULL AS postcode
      FROM creditor_accounts ca
      JOIN business_units bu
        ON ca.business_unit_id = bu.business_unit_id
      JOIN configuration_items ci
        ON ci.business_unit_id = bu.business_unit_id
       AND ci.item_name = 'CENTRAL_FUND_ACCOUNT'
     WHERE ca.creditor_account_type = 'CF'
;

COMMENT ON VIEW v_major_creditor_account_at_a_glance IS 'Retrieves major creditor account at a glance information for both Major Creditors and Central Fund accounts';