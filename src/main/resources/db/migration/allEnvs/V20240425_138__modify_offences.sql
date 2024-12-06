/**
* OPAL Program
*
* MODULE      : modify_offences.sql
*
* DESCRIPTION : Modify the OFFENCES table in order to be able to load Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------
* 25/04/2024    A Dennis    1.0         PO-303 Modify the OFFENCES table in order to be able to load Reference Data from data held in Excel spreadsheet
*
**/
ALTER TABLE offences
ADD COLUMN date_used_to    timestamp,
ADD COLUMN offence_oas     text,
ADD COLUMN offence_oas_cy  text;

ALTER TABLE offences
ALTER COLUMN offence_id TYPE bigint
USING offence_id::bigint;

ALTER TABLE IF EXISTS offences
DROP constraint IF EXISTS off_business_unit_id_fk;

ALTER TABLE offences
ALTER COLUMN business_unit_id DROP NOT NULL;

ALTER TABLE offences
ALTER COLUMN offence_title DROP NOT NULL;

ALTER TABLE offences
ALTER COLUMN offence_title_cy DROP NOT NULL;

ALTER TABLE impositions
ALTER COLUMN offence_id TYPE bigint
USING offence_id::bigint;

COMMENT ON COLUMN offences.date_used_to IS 'The date the offence was in use till. Null of a date in the future means still in use';
COMMENT ON COLUMN offences.offence_oas IS 'The English Offence Act and Section/Legislation';
COMMENT ON COLUMN offences.offence_oas_cy IS 'The Welsh Offence Act and Section/Legislation';
