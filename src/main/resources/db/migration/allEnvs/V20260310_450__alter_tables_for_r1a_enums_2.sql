/**
* CGI OPAL Program
*
* MODULE      : alter_tables_for_r1a_enums_2.sql
*
* DESCRIPTION : Alter specific columns on tables to an ENUM data type. Dependant Views are dropped first.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    --------------------------------------------------------------------------------------------------------------
* 05/03/2026    T McCallion    1.0         PO-2870 - Update column defendant_account_parties.association_type to use a PostgreSQL Enum instead of varchar 
*                                          PO-2908 - Update column creditor_accounts.creditor_account_type to use a PostgreSQL Enum instead of varchar
*                                          PO-2916 - Update columns on CREDITOR_TRANSACTIONS table to use postgresql enum instead of varchar
**/

--Drop views, recreated via other script(s)
DROP VIEW IF EXISTS v_audit_defendant_accounts;                 --For PO-2870
DROP VIEW IF EXISTS v_enforcement_status;                       --For PO-2870
DROP VIEW IF EXISTS v_search_minor_creditor_accounts;           --For PO-2870, PO-2916
DROP VIEW IF EXISTS v_search_defendant_accounts_consolidation;  --For PO-2870
DROP VIEW IF EXISTS v_search_defendant_accounts;                --For PO-2870
DROP VIEW IF EXISTS v_minor_creditor_accounts_summary;          --For PO-2870, PO-2908
DROP VIEW IF EXISTS v_defendant_accounts_header;                --For PO-2870
DROP VIEW IF EXISTS v_defendant_accounts_summary;               --For PO-2870
DROP VIEW IF EXISTS v_consolidated_accounts;                    --For PO-2870
DROP VIEW IF EXISTS v_audit_creditor_accounts;                  --For PO-2908
DROP VIEW IF EXISTS v_minor_creditor_account_header;            --For PO-2908
DROP VIEW IF EXISTS v_major_creditor_account_header;            --For PO-2908, PO-2916
DROP VIEW IF EXISTS v_major_creditor_account_at_a_glance;       --For PO-2908

   
--DEFENDANT_ACCOUNT_PARTIES - PO-2870
ALTER TABLE defendant_account_parties
   ALTER COLUMN association_type TYPE t_association_type_enum 
   USING association_type::t_association_type_enum; 

DROP INDEX IF EXISTS dap_daid_at_idx;
CREATE INDEX dap_daid_at_idx ON defendant_account_parties (defendant_account_id, association_type);

--CREDITOR_ACCOUNTS - PO-2908
ALTER TABLE creditor_accounts
   ALTER COLUMN creditor_account_type TYPE t_creditor_account_type_enum 
   USING creditor_account_type::t_creditor_account_type_enum; 
   
DROP INDEX IF EXISTS ca_bus_unit_acc_type_idx; 
CREATE INDEX ca_bus_unit_acc_type_idx ON creditor_accounts (business_unit_id, creditor_account_type); 

--CREDITOR_TRANSACTIONS - PO-2916
ALTER TABLE creditor_transactions  
    ALTER COLUMN transaction_type TYPE t_creditor_transaction_type_enum 
        USING transaction_type::t_creditor_transaction_type_enum,
    ALTER COLUMN status TYPE t_creditor_transaction_status_enum
        USING status::t_creditor_transaction_status_enum,
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum
        USING associated_record_type::t_associated_record_type_enum;

COMMENT ON COLUMN creditor_transactions.transaction_type IS 'The code that determines the type of transaction. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN creditor_transactions.status IS 'Indicates if a transaction has been Reversed, partially-reversed, dishonoured, cancelled or cleared/presented. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN creditor_transactions.associated_record_type IS 'Table where relating record that caused this amount is stored. Specific values can be found in the DB LLD on Confluence.';

DROP INDEX IF EXISTS ct_caid_tt_pp_idx;
CREATE INDEX ct_caid_tt_pp_idx ON creditor_transactions (creditor_account_id, transaction_type, payment_processed);
