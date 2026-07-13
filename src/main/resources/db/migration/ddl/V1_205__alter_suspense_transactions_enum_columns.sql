/**
* OPAL Program
*
* MODULE      : alter_suspense_transactions_enum_columns.sql
*
* DESCRIPTION : Alter suspense_transactions enum-backed columns
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3849 - Update columns on SUSPENSE_TRANSACTIONS table to use PostgreSQL ENUM
*
**/

ALTER TABLE suspense_transactions
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum
        USING associated_record_type::text::t_associated_record_type_enum,
    ALTER COLUMN transaction_type TYPE t_suspense_transaction_type_enum
        USING transaction_type::text::t_suspense_transaction_type_enum,
    ALTER COLUMN reversed TYPE t_reversed_enum
        USING reversed::text::t_reversed_enum;

COMMENT ON COLUMN suspense_transactions.associated_record_type IS 'Type of record identified by associated_record_id. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN suspense_transactions.transaction_type IS 'Suspense transaction type. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN suspense_transactions.reversed IS 'If this transaction has subsequently been reversed (R) or dishonoured (D). Specific values can be found in the DB LLD on Confluence.';
