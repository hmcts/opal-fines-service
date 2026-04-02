/**
* CGI OPAL Program
*
* MODULE      : alter_tables_for_r1a_enums_3.sql
*
* DESCRIPTION : Alter specific columns on tables to an ENUM data type. Dependant Views are dropped first.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    --------------------------------------------------------------------------------------------------------------
* 05/03/2026    T McCallion    1.0         PO-2920 - Update columns on NOTES table to use postgresql enum instead of varchar
*                                          PO-2931 - Update column on DEFENDANT_TRANSACTIONS table to use postgresql enum instead of varchar
*                                          PO-2932 - Update columns on DRAFT_ACCOUNTS table to use postgresql enum instead of varchar
**/

--Drop views, recreated via other script(s)
DROP VIEW IF EXISTS v_defendant_accounts_header;  --For PO-2931
DROP VIEW IF EXISTS v_consolidated_accounts;      --For PO-2931


--NOTES - PO-2920
ALTER TABLE notes  
    ALTER COLUMN note_type TYPE t_note_type_enum 
        USING note_type::t_note_type_enum,
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum
        USING associated_record_type::t_associated_record_type_enum;

COMMENT ON COLUMN notes.note_type IS 'The type of note. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN notes.associated_record_type IS 'The type of record this note relates to. Specific values can be found in the DB LLD on Confluence.';

--DEFENDANT_TRANSACTIONS - PO-2931
ALTER TABLE defendant_transactions 
    ALTER COLUMN transaction_type TYPE t_defendant_transaction_type_enum 
        USING transaction_type::t_defendant_transaction_type_enum,
    ALTER COLUMN status TYPE t_defendant_transaction_status_enum
        USING status::t_defendant_transaction_status_enum,
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum
        USING associated_record_type::t_associated_record_type_enum,
    ALTER COLUMN payment_method TYPE t_payment_method_enum
        USING payment_method::t_payment_method_enum,
    ALTER COLUMN write_off_code TYPE t_write_off_code_enum
        USING write_off_code::t_write_off_code_enum;

COMMENT ON COLUMN defendant_transactions.transaction_type IS 'The code that determines the type of transaction. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN defendant_transactions.payment_method IS 'The method of paying. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN defendant_transactions.status IS 'Indicates the status of the transaction. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN defendant_transactions.write_off_code IS 'Code of write-off category applicable. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN defendant_transactions.associated_record_type IS 'Type of record that is identified by associated_record_id. Specific values can be found in the DB LLD on Confluence.';

--DRAFT_ACCOUNTS - PO-2932
ALTER TABLE draft_accounts 
    ALTER COLUMN account_type TYPE t_da_account_type_enum 
        USING account_type::t_da_account_type_enum,
    ALTER COLUMN account_status TYPE t_dra_account_status_enum
        USING account_status::t_dra_account_status_enum;

COMMENT ON COLUMN draft_accounts.account_status IS 'The status of the draft account. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN draft_accounts.account_type IS 'Type of account. Specific values can be found in the DB LLD on Confluence.';

DROP INDEX IF EXISTS dra_account_status_idx;
CREATE INDEX dra_account_status_idx ON draft_accounts (account_status);

DROP INDEX IF EXISTS dra_submitted_bu_status_idx;
CREATE INDEX dra_submitted_bu_status_idx ON draft_accounts (submitted_by, business_unit_id, account_status);