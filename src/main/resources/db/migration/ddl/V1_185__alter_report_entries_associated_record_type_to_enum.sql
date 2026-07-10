/**
* OPAL Program
*
* MODULE      : alter_report_entries_associated_record_type_to_enum.sql
*
* DESCRIPTION : Alter report_entries.associated_record_type to use PostgreSQL enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3844 - Update columns on REPORT_ENTRIES table to use PostgreSQL ENUM
*
**/

ALTER TABLE report_entries
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum
    USING associated_record_type::text::t_associated_record_type_enum;

COMMENT ON COLUMN report_entries.associated_record_type IS 'Type of record identified by associated_record_id. Specific values can be found in the DB LLD on Confluence.';
