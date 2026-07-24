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
* 06/03/2026    T McCallion 1.1         Need to recreate view as part of new ENUMs work
*                                       Taken from: V20251121_432__create_view_v_major_creditor_account_header.sql
*                                                   Only change: Added DROP statement
* 02/06/2026    P Brumby    1.2         PO-5774 Amend how Awaiting Payout is determined in the database view v_major_creditor_account_header.
* 13/07/2026    P Brumby    1.3         PO-7422 Amend view v_major_creditor_account_header to fetch business_units.business_unit_code for frontend consumption.
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
         , (SELECT COALESCE(sum(ct.transaction_amount), (0)::numeric) AS "coalesce"
              FROM creditor_transactions ct
             WHERE ct.creditor_account_id = ca.creditor_account_id 
               AND ct.posted_date > COALESCE(( SELECT max(ct_last.posted_date) AS max
                                                  FROM creditor_transactions ct_last
                                                 WHERE ct_last.creditor_account_id = ca.creditor_account_id 
                                                   AND ct_last.transaction_type IN ('BACS'::t_creditor_transaction_type_enum,
                                                                                    'CHEQUE'::t_creditor_transaction_type_enum))
                                              , '-infinity'::timestamp without time zone)) AS awaiting_payout 
         , bu.business_unit_code
      FROM creditor_accounts ca
      JOIN business_units bu 
        ON bu.business_unit_id = ca.business_unit_id
      JOIN major_creditors mj 
        ON mj.major_creditor_id = ca.major_creditor_id     
     WHERE ca.creditor_account_type = 'MJ'::t_creditor_account_type_enum  
     UNION 
    SELECT ca.creditor_account_id
         , ca.account_number            AS creditor_account_number
         , ca.creditor_account_type
         , ca.version_number
         , bu.business_unit_id
         , bu.business_unit_name         
         , ci.item_values ->> 'name'    AS name   
         , (SELECT COALESCE(sum(ct.transaction_amount), (0)::numeric) AS "coalesce"
              FROM creditor_transactions ct
             WHERE ct.creditor_account_id = ca.creditor_account_id 
               AND ct.posted_date > COALESCE(( SELECT max(ct_last.posted_date) AS max
                                                  FROM creditor_transactions ct_last
                                                 WHERE ct_last.creditor_account_id = ca.creditor_account_id 
                                                   AND ct_last.transaction_type IN ('BACS'::t_creditor_transaction_type_enum,
                                                                                    'CHEQUE'::t_creditor_transaction_type_enum))
                                              , '-infinity'::timestamp without time zone)) AS awaiting_payout 
         , bu.business_unit_code
      FROM creditor_accounts ca
      JOIN business_units bu 
        ON bu.business_unit_id = ca.business_unit_id
      JOIN configuration_items ci 
        ON bu.business_unit_id = ci.business_unit_id
       AND ci.item_name = 'CENTRAL_FUND_ACCOUNT'
     WHERE ca.creditor_account_type = 'CF'::t_creditor_account_type_enum;

COMMENT ON VIEW v_major_creditor_account_header IS 'Retrieves major creditor account header information';
