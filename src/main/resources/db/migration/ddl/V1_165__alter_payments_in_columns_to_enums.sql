/**
* OPAL Program
*
* MODULE      : alter_payments_in_columns_to_enums.sql
*
* DESCRIPTION : Alter payments_in enum-backed columns
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    --------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3839 - Update columns on PAYMENTS_IN table to use PostgreSQL ENUM
*
**/

ALTER TABLE payments_in
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum
        USING associated_record_type::text::t_associated_record_type_enum,
    ALTER COLUMN destination_type TYPE t_pi_destination_type_enum
        USING destination_type::text::t_pi_destination_type_enum,
    ALTER COLUMN payment_method TYPE t_payment_method_enum
        USING payment_method::text::t_payment_method_enum;

COMMENT ON COLUMN payments_in.associated_record_type IS 'Type of record identified by associated_record_id. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN payments_in.destination_type IS 'Allocation destination (e.g. F (Fines), S (Suspense), C (Court Fee)). Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN payments_in.payment_method IS 'Payment method. Specific values can be found in the DB LLD on Confluence.';
