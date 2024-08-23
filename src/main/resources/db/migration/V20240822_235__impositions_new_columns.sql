/** 
* 
* OPAL Program 
* 
* MODULE      : impositions_new_columns.sql 
* 
* DESCRIPTION : Add new columns to the IMPOSITIONS table.  
* 
* VERSION HISTORY: 
* 
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------- 
* 22/08/2024    I Readman    1.0         PO-643 Add new columns to the IMPOSITIONS table 
* 
**/ 
-- Add new columns but preserve order in the data model spreadsheet
-- Impositions table is currently empty so no data loss
ALTER TABLE impositions DROP COLUMN creditor_account_id;
ALTER TABLE impositions DROP COLUMN unit_fine_adjusted;
ALTER TABLE impositions DROP COLUMN unit_fine_units;
ALTER TABLE impositions DROP COLUMN completed;
ALTER TABLE impositions ADD COLUMN offence_title varchar(120);
ALTER TABLE impositions ADD COLUMN offence_code varchar(10);
ALTER TABLE impositions ADD COLUMN creditor_account_id bigint NOT NULL;
ALTER TABLE impositions ADD COLUMN unit_fine_adjusted boolean;
ALTER TABLE impositions ADD COLUMN unit_fine_units smallint;
ALTER TABLE impositions ADD COLUMN completed boolean;
ALTER TABLE impositions ALTER COLUMN offence_id DROP NOT NULL;

ALTER TABLE impositions ADD CONSTRAINT imp_creditor_account_id_fk FOREIGN KEY (creditor_account_id) REFERENCES creditor_accounts (creditor_account_id);

COMMENT ON COLUMN impositions.offence_title IS 'Offence title where id unavailable (local offences TFO''d)';
COMMENT ON COLUMN impositions.offence_code IS 'Offence code where id unavailable (local offences TFO''d)';
COMMENT ON COLUMN impositions.creditor_account_id IS 'ID of the creditor account to be allocated payments received against this imposition';
COMMENT ON COLUMN impositions.unit_fine_adjusted IS 'Whether a "Unit Fine Adjustment under s.18(7) CJA 1991" was made';
COMMENT ON COLUMN impositions.unit_fine_units IS 'Number of units';
COMMENT ON COLUMN impositions.completed IS 'If the imposition has been paid in full';