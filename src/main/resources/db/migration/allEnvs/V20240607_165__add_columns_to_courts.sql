/**
* OPAL Program
*
* MODULE      : add_columns_to_courts.sql
*
* DESCRIPTION : Add new columns, from the reference data spreadsheet, to the COURTS table in order to be able to load Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------
* 07/06/2024    A Dennis    1.0         PO-308 Add new columns to the COURTS table in order to be able to load Reference Data from data held in Excel spreadsheet.
*
**/
ALTER TABLE courts
ADD COLUMN gob_enforcing_court_code        smallint,
ADD COLUMN lja                             smallint,
ADD COLUMN court_type                      varchar(2),
ADD COLUMN division                        varchar(2),
ADD COLUMN session                         varchar(2),
ADD COLUMN start_time                      varchar(8),
ADD cOLUMN max_load                        bigint,
ADD COLUMN record_session_times            varchar(1),
ADD COLUMN max_court_duration              bigint,
ADD COLUMN group_code                      varchar(24);

COMMENT ON COLUMN courts.gob_enforcing_court_code IS 'The GoB enforcing court code';
COMMENT ON COLUMN courts.lja IS 'GoB local justice area code';
COMMENT ON COLUMN courts.court_type IS 'GoB Court Type';
COMMENT ON COLUMN courts.division IS 'GoB Division';
COMMENT ON COLUMN courts.session IS 'AM or PM session';
COMMENT ON COLUMN courts.start_time IS 'The start time';
COMMENT ON COLUMN courts.max_load IS 'Maximum load';
COMMENT ON COLUMN courts.record_session_times IS 'Y or N whether session time is recorded';
COMMENT ON COLUMN courts.max_court_duration IS 'The maximum court duration';
COMMENT ON COLUMN courts.group_code IS 'The group this court belongs to';
