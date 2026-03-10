/**
* CGI OPAL Program
*
* MODULE      : create_view_v_consolidated_accounts.sql
*
* DESCRIPTION : Create view to retrieve consolidated account information
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------
* 24/02/2026    CL          1.0         PO-2339 - Create v_consolidated_accounts view to retrieve consolidated account information
*
**/

CREATE OR REPLACE VIEW v_consolidated_accounts AS
   SELECT ca.master_account_id
        , ca.child_account_id
        , da.account_number            AS child_account_number
        , da.prosecutor_case_reference AS child_reference
        , da.imposed_hearing_date      AS child_date_imposed
        , da.imposed_by_name           AS child_imposed_by
        , pa.forenames                 AS child_first_name
        , pa.surname                   AS child_last_name
     FROM ( SELECT dt.defendant_account_id AS master_account_id
                 , dt.associated_record_id AS child_account_id
              FROM defendant_transactions dt
             WHERE dt.transaction_type = 'CONSOL'
               AND dt.associated_record_type = 'defendant_accounts' ) AS ca
     JOIN defendant_accounts da ON ca.child_account_id::bigint = da.defendant_account_id
LEFT JOIN defendant_account_parties dap ON da.defendant_account_id = dap.defendant_account_id
                                        AND dap.association_type = 'Defendant'
LEFT JOIN parties pa ON dap.party_id = pa.party_id;        

COMMENT ON VIEW v_consolidated_accounts IS 'Retrieves consolidated account information with related party details';