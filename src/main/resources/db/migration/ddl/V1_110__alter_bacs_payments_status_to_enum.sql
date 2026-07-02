/**
* OPAL Program
*
* MODULE      : alter_bacs_payments_status_to_enum.sql
*
* DESCRIPTION : Alter bacs_payments.status to use PostgreSQL enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3620 - Update columns on BACS_PAYMENTS table to use PostgreSQL ENUM
*
**/

ALTER TABLE bacs_payments
    ALTER COLUMN status TYPE t_bacs_status_enum
    USING status::t_bacs_status_enum;

COMMENT ON COLUMN bacs_payments.status IS 'BACS status. Specific values can be found in the DB LLD on Confluence.';
