/**
* CGI OPAL Program
*
* MODULE      : alter_tables_for_r1a_enums_4.sql
*
* DESCRIPTION : Alter specific columns on tables to an ENUM data type. Dependant Views are dropped first.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    --------------------------------------------------------------------------------------------------------------
* 16/03/2026    T McCallion    1.0         PO-2868 - Update columns on DEFENDANT_ACCOUNTS table to use postgresql enum instead of varchar
*                                          PO-2906 - Update columns on DEBTOR_DETAIL table to use postgresql enum instead of varchar
*                                          PO-2910 - Update columns on PAYMENT_TERMS table to use postgresql enum instead of varchar 
*                                          PO-2930 - Update columns on DOCUMENT_INSTANCES table to use postgresql enum instead of varchar
*                                          PO-2933 - Update columns on ACCOUNT_NUMBER_INDEX table to use postgresql enum instead of varchar
**/

--Drop views, recreated via other script(s)
DROP VIEW IF EXISTS v_defendant_accounts_header;                --For PO-2868
DROP VIEW IF EXISTS v_enforcement_status;                       --For PO-2868
DROP VIEW IF EXISTS v_search_defendant_accounts_consolidation;  --For PO-2868
DROP VIEW IF EXISTS v_search_defendant_accounts;                --For PO-2868
DROP VIEW IF EXISTS v_audit_defendant_accounts;                 --For PO-2906
DROP VIEW IF EXISTS v_defendant_accounts_summary;               --For PO-2906, PO-2910
--PO-2930 & PO-2933 = No views needed


--DEFENDANT_ACCOUNTS - PO-2868
ALTER TABLE defendant_accounts 
    DROP CONSTRAINT IF EXISTS da_account_type_cc;

ALTER TABLE defendant_accounts  
    ALTER COLUMN account_status TYPE t_da_account_status_enum 
        USING account_status::t_da_account_status_enum,
    ALTER COLUMN account_type TYPE t_da_account_type_enum
        USING account_type::t_da_account_type_enum,
    ALTER COLUMN consolidated_account_type TYPE t_consolidated_account_type_enum
        USING consolidated_account_type::t_consolidated_account_type_enum,
    ALTER COLUMN originator_type TYPE t_originator_type_enum
        USING originator_type::t_originator_type_enum;

COMMENT ON COLUMN defendant_accounts.account_status IS 'The status of the account. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN defendant_accounts.account_type IS 'The type of the account. Specific values can be found in the DB LLD on Confluence. One of Fixed Penalty, Fine, Conditional Caution, Confiscation.';
COMMENT ON COLUMN defendant_accounts.consolidated_account_type IS 'If the account has been subject to a consolidation. M for Master or C for Child. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN defendant_accounts.originator_type IS 'How the account originated. Specific values can be found in the DB LLD on Confluence.';

--DEBTOR_DETAILS - PO-2906
ALTER TABLE debtor_detail  
    ALTER COLUMN document_language TYPE t_language_enum 
        USING document_language::t_language_enum,
    ALTER COLUMN hearing_language TYPE t_language_enum
        USING hearing_language::t_language_enum;

COMMENT ON COLUMN debtor_detail.document_language IS 'Document language preference (CY or EN). Specific values can be found in the DB LLD on Confluence.'; 
COMMENT ON COLUMN debtor_detail.hearing_language IS 'Hearing language preference (CY or EN). Specific values can be found in the DB LLD on Confluence.';

--PAYMENT_TERMS - PO-2910
ALTER TABLE payment_terms 
    ALTER COLUMN terms_type_code TYPE t_terms_type_code_enum 
        USING terms_type_code::t_terms_type_code_enum,
    ALTER COLUMN instalment_period TYPE t_instalment_period_enum
        USING instalment_period::t_instalment_period_enum;

COMMENT ON COLUMN payment_terms.terms_type_code IS 'The terms type. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN payment_terms.instalment_period IS 'The instalment period or NULL if not instalments. Specific values can be found in the DB LLD on Confluence.';

--DOCUMENT_INSTANCES - PO-2930
ALTER TABLE document_instances 
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum 
        USING associated_record_type::t_associated_record_type_enum,
    ALTER COLUMN status TYPE t_di_status_enum
        USING status::t_di_status_enum;

COMMENT ON COLUMN document_instances.associated_record_type IS 'Type of record identified by associated_record_id. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN document_instances.status IS 'The status of the document instance. Specific values can be found in the DB LLD on Confluence.';

DROP INDEX IF EXISTS di_bu_document_status_date_idx;
CREATE INDEX di_bu_document_status_date_idx ON document_instances (business_unit_id, document_id, status, generated_date);

--ACCOUNT_NUMBER_INDEX - PO-2933
ALTER TABLE account_number_index 
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum 
        USING associated_record_type::t_associated_record_type_enum;

COMMENT ON COLUMN account_number_index.associated_record_type IS 'The target table the account is intended for. Specific values can be found in the DB LLD on Confluence.';