/**
* OPAL Program
*
* MODULE      : alter_suspense_items_enum_columns.sql
*
* DESCRIPTION : Alter suspense_items enum-backed columns
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3848 - Update columns on SUSPENSE_ITEMS table to use PostgreSQL ENUM
*
**/

ALTER TABLE suspense_items
    ALTER COLUMN suspense_item_type TYPE t_suspense_item_type_enum
        USING suspense_item_type::text::t_suspense_item_type_enum,
    ALTER COLUMN payment_method TYPE t_payment_method_enum
        USING payment_method::text::t_payment_method_enum;

COMMENT ON COLUMN suspense_items.suspense_item_type IS 'Type of this suspense item. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN suspense_items.payment_method IS 'The method of payment. Specific values can be found in the DB LLD on Confluence.';
