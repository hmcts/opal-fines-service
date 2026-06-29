/**
* OPAL Program
*
* MODULE      : alter_allocations_transaction_type_to_enum.sql
*
* DESCRIPTION : Alter allocations.transaction_type to use PostgreSQL enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3618 - Update columns on ALLOCATIONS table to use PostgreSQL ENUM
*
**/

ALTER TABLE allocations
    ALTER COLUMN transaction_type TYPE t_allocation_transaction_type_enum
    USING transaction_type::t_allocation_transaction_type_enum;

COMMENT ON COLUMN allocations.transaction_type IS 'The type of transaction this allocation is associated with. Specific values can be found in the DB LLD on Confluence.';
