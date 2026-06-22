/**
* OPAL Program
*
* MODULE      : alter_interface_files_add_new_columns.sql
*
* DESCRIPTION : Add source, override inhibits, record count, and total amount to INTERFACE_FILES.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 10/06/2026    C Cho       1.0         PO-2573 Add summary columns to INTERFACE_FILES.
*
**/

ALTER TABLE interface_files
    ADD COLUMN source t_interface_file_source_enum,
    ADD COLUMN override_inhibits BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN record_count SMALLINT,
    ADD COLUMN total_amount DECIMAL(18,2);

COMMENT ON COLUMN interface_files.source IS 'The party that sent the associated interface file. Specific values can be found in the DB LLD on Confluence, t_interface_file_source_enum.';
COMMENT ON COLUMN interface_files.override_inhibits IS 'Determines whether or not to override inhibits. When it is FALSE then carry out the existing payment inhibit functionality.';
COMMENT ON COLUMN interface_files.record_count IS 'To be populated by the external interface function that creates the data in the table, it is the number of records (payment entries) in the Interface File, to be displayed on the Till Summary Report / Interface File Summary screen.';
COMMENT ON COLUMN interface_files.total_amount IS 'To be populated by the external interface function that creates the data in the table, it is the sum of all the records (payment entries) in the Interface File, to be displayed on the Till Summary Report / Interface File Summary screen.';
