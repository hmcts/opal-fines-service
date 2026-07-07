/**
* OPAL Program
*
* MODULE      : alter_standard_letters_associated_record_type_to_enum.sql
*
* DESCRIPTION : Alter standard_letters.associated_record_type to use PostgreSQL enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3847 - Update columns on STANDARD_LETTERS table to use PostgreSQL ENUM
*
**/

ALTER TABLE standard_letters
    ALTER COLUMN associated_record_type TYPE t_associated_record_type_enum
    USING associated_record_type::text::t_associated_record_type_enum;

COMMENT ON COLUMN standard_letters.associated_record_type IS 'The type of record for which this letter is generated. Specific values can be found in the DB LLD on Confluence.';
