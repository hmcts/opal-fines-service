/** 
* 
* OPAL Program 
* 
* MODULE      : enforcement_new_column.sql 
* 
* DESCRIPTION : Add new column to the ENFORCEMENT table.  
* 
* VERSION HISTORY: 
* 
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------- 
* 23/08/2024    I Readman    1.0         PO-644 Add new column to the ENFORCEMENT table 
* 
**/ 
-- Create temporary table to hold enforcement data
CREATE TEMP TABLE enforcements_tmp AS SELECT * FROM enforcements;

-- Drop columns to maintain column order from data model spreadsheet
ALTER TABLE enforcements DROP COLUMN warrant_reference;
ALTER TABLE enforcements DROP COLUMN case_reference;
ALTER TABLE enforcements DROP COLUMN hearing_date;
ALTER TABLE enforcements DROP COLUMN hearing_court_id;
ALTER TABLE enforcements DROP COLUMN account_type;
ALTER TABLE enforcements DROP COLUMN posted_by_user_id;

ALTER TABLE enforcements ADD COLUMN result_responses json;
ALTER TABLE enforcements ADD COLUMN warrant_reference varchar(20);
ALTER TABLE enforcements ADD COLUMN case_reference varchar(40);
ALTER TABLE enforcements ADD COLUMN hearing_date timestamp;
ALTER TABLE enforcements ADD COLUMN hearing_court_id bigint;
ALTER TABLE enforcements ADD COLUMN account_type varchar(20);
ALTER TABLE enforcements ADD COLUMN posted_by_user_id bigint;

ALTER TABLE enforcements ADD CONSTRAINT enf_hearing_court_id_fk FOREIGN KEY (hearing_court_id) REFERENCES courts (court_id);
ALTER TABLE enforcements ADD CONSTRAINT enf_posted_by_user_id_fk FOREIGN KEY (posted_by_user_id) REFERENCES users (user_id);

COMMENT ON COLUMN enforcements.result_responses IS 'Name value pairs for enforcement parameters';
COMMENT ON COLUMN enforcements.warrant_reference IS 'The reference number of the warrant generated from this action';
COMMENT ON COLUMN enforcements.case_reference IS 'The reference number of the case generated from this action';
COMMENT ON COLUMN enforcements.hearing_date IS 'The hearing date of the case generated from this action';
COMMENT ON COLUMN enforcements.hearing_court_id IS 'The hearing court of the case generated from this action';
COMMENT ON COLUMN enforcements.account_type IS 'The enforcement account type that auto-enforcement deemed this to be at the time of it applying this action';
COMMENT ON COLUMN enforcements.posted_by_user_id IS 'The user ID and is the foreign key to Users table but can be NULL, so if a not null value is put then it is enforced';

-- Update information from dropped columns
UPDATE enforcements e SET warrant_reference = (SELECT warrant_reference FROM enforcements_tmp et WHERE e.enforcement_id = et.enforcement_id),
                          case_reference = (SELECT case_reference FROM enforcements_tmp et WHERE e.enforcement_id = et.enforcement_id),
                          hearing_date = (SELECT hearing_date FROM enforcements_tmp et WHERE e.enforcement_id = et.enforcement_id),
                          hearing_court_id = (SELECT hearing_court_id FROM enforcements_tmp et WHERE e.enforcement_id = et.enforcement_id),
                          account_type = (SELECT account_type FROM enforcements_tmp et WHERE e.enforcement_id = et.enforcement_id),
                          posted_by_user_id = (SELECT posted_by_user_id FROM enforcements_tmp et WHERE e.enforcement_id = et.enforcement_id);