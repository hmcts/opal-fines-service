/**
* OPAL Program
*
* MODULE      : alter_cheques_columns_to_enums.sql
*
* DESCRIPTION : Alter cheques, allocation_type and status, columns to enums
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3621 - Update columns on CHEQUES table to use PostgreSQL ENUM
*
**/

ALTER TABLE cheques
    ALTER COLUMN allocation_type TYPE t_cheque_allocation_type_enum
        USING allocation_type::t_cheque_allocation_type_enum,
    ALTER COLUMN status TYPE t_cheque_status_enum
        USING status::t_cheque_status_enum;

COMMENT ON COLUMN cheques.allocation_type IS 'Indicates what this cheque payment is in respect of, for example, COMP or REPAYW. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN cheques.status IS 'The cheque status. Specific values can be found in the DB LLD on Confluence.';