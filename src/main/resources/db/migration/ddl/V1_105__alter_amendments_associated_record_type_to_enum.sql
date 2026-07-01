/**
* OPAL Program
*
* MODULE      : alter_amendments_associated_record_type_to_enum.sql
*
* DESCRIPTION : Alter amendments.associated_record_type to use PostgreSQL enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3619 - Update columns on AMENDMENTS table to use PostgreSQL ENUM
*
**/

ALTER TABLE amendments
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum
    USING associated_record_type::t_associated_record_type_enum;

COMMENT ON COLUMN amendments.associated_record_type IS 'Type of record that is identified by associated_record_id. Specific values can be found in the DB LLD on Confluence.';
