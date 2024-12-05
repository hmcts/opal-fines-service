/**
*
* OPAL Program
*
* MODULE      : standard_letters_report_entries_fix.sql
*
* DESCRIPTION : Rename constraint for STANDARD_LETTERS and set REPORT_ENTRIES columns to be not null.
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 09/07/2024    I Readman    1.0         PO-501 Fix minor issues with the CREATE TABLE scripts
*
**/       

-- Rename Constraint
ALTER TABLE standard_letters RENAME CONSTRAINT ct_business_unit_id_fk TO sl_business_unit_id_fk;

-- Set columns to NOT NULL
ALTER TABLE report_entries ALTER business_unit_id SET NOT NULL;
ALTER TABLE report_entries ALTER report_id SET NOT NULL;
ALTER TABLE report_entries ALTER entry_timestamp SET NOT NULL;
ALTER TABLE report_entries ALTER reported_timestamp SET NOT NULL;
ALTER TABLE report_entries ALTER associated_record_type SET NOT NULL;
ALTER TABLE report_entries ALTER associated_record_id SET NOT NULL;