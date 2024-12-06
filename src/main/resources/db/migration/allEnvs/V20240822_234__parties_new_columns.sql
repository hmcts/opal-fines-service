/** 
* 
* OPAL Program 
* 
* MODULE      : parties_new_columns.sql 
* 
* DESCRIPTION : Add new columns to the PARTIES table.  
* 
* VERSION HISTORY: 
* 
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------- 
* 22/08/2024    I Readman    1.0         PO-642 Add new columns to the PARTIES table 
* 
**/ 
-- Add new columns but keep last_changed_date as the final column in the table
ALTER TABLE parties DROP COLUMN last_changed_date;
ALTER TABLE parties ADD COLUMN telephone_home varchar(35);
ALTER TABLE parties ADD COLUMN telephone_business varchar(35);
ALTER TABLE parties ADD COLUMN telephone_mobile varchar(35);
ALTER TABLE parties ADD COLUMN email_1 varchar(80);
ALTER TABLE parties ADD COLUMN email_2 varchar(80);
ALTER TABLE parties ADD COLUMN last_changed_date timestamp; 

COMMENT ON COLUMN parties.telephone_home IS 'Home telephone number';
COMMENT ON COLUMN parties.telephone_business IS 'Business telephone number';
COMMENT ON COLUMN parties.telephone_mobile IS 'Mobile telephone number';
COMMENT ON COLUMN parties.email_1 IS 'Primary e-mail address';
COMMENT ON COLUMN parties.email_2 IS 'Secondary e-mail address';
COMMENT ON COLUMN parties.last_changed_date IS 'Date this party was last changed in Account Maintenance.';

-- Restore data that was lost when the column was dropped
UPDATE parties set last_changed_date = '2020-10-11' where party_id = 500000007;
UPDATE parties set last_changed_date = '2023-03-12' where party_id = 500000009;