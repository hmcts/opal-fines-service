/**
* OPAL Program
*
* MODULE      : update_courts_lja_data.sql
*
* DESCRIPTION : Update the local justice areas foreign key ids in COURTS table to match the new LJA Reference Data load from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------
* 08/05/2024    A Dennis    1.0         PO-342  Update the local justice areas foreign key ids in COURTS table to match the new LJA Reference Data load from data held in Excel spreadsheet. 
*
**/

UPDATE courts
SET local_justice_area_id = 5739
WHERE local_justice_area_id = 2022;

UPDATE courts
SET local_justice_area_id = 5737
WHERE local_justice_area_id = 2216;

UPDATE courts
SET local_justice_area_id = 5576
WHERE local_justice_area_id = 2232;

UPDATE courts
SET local_justice_area_id = 5578
WHERE local_justice_area_id = 2651;

UPDATE courts
SET local_justice_area_id = 5580
WHERE local_justice_area_id = 2078;

UPDATE courts
SET local_justice_area_id = 5582
WHERE local_justice_area_id = 1755;

UPDATE courts
SET local_justice_area_id = 5715
WHERE local_justice_area_id = 2320;

UPDATE courts
SET local_justice_area_id = 5717
WHERE local_justice_area_id = 2160;

UPDATE courts
SET local_justice_area_id = 5719
WHERE local_justice_area_id = 3119;

UPDATE courts
SET local_justice_area_id = 5721
WHERE local_justice_area_id = 2831;
