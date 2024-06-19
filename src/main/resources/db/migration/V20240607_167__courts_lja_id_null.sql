/**
* OPAL Program
*
* MODULE      : courts_lja_id_null.sql
*
* DESCRIPTION : Make courts.local_justice_area_id nullable to allow COURTS reference data load, some of which have null LJA in the spreadsheet
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------------------------------
* 07/06/2024    A Dennis    1.0         PO-308 Make courts.local_justice_area_id nullable to allow COURTS reference data load, some of which have null LJA in the spreadsheet
*/

ALTER TABLE courts
ALTER COLUMN local_justice_area_id DROP NOT NULL;
