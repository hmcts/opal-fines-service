/**
* OPAL Program
*
* MODULE      : modify_local_justice_areas.sql
*
* DESCRIPTION : Modify the LOCAL_JUSTICE_AREAS table in order to be able to load Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------
* 05/05/2024    A Dennis    1.0         PO-305 Modify the LOCAL_JUSTICE_AREAS table in order to be able to load Reference Data from data held in Excel spreadsheet. 
*
**/
ALTER TABLE local_justice_areas
ADD COLUMN lja_code        varchar(4),
ADD COLUMN address_line_4  varchar(35),
ADD COLUMN address_line_5  varchar(35),
ADD COLUMN end_date        timestamp;

COMMENT ON COLUMN local_justice_areas.lja_code IS 'LJA Code';
COMMENT ON COLUMN local_justice_areas.address_line_4 IS 'LJA Address line 4';
COMMENT ON COLUMN local_justice_areas.address_line_5 IS 'LJA Address line 5';
COMMENT ON COLUMN local_justice_areas.end_date IS 'The end date of the record';

ALTER TABLE local_justice_areas
ALTER COLUMN name TYPE varchar(100);

ALTER TABLE account_transfers
DROP constraint IF EXISTS at_local_justice_area_id_fk;

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS res_enf_override_tfo_lja_id_fk;
