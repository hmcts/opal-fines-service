/**
* OPAL Program
*
* MODULE      : add_lja_fk_to_courts.sql
*
* DESCRIPTION : Add local justice areas foreign key to COURTS table as part of LJA Reference Data load from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------
* 08/05/2024    A Dennis    1.0         PO-342  Add local justice areas foreign key to COURTS table as part of LJA Reference Data load from data held in Excel spreadsheet. . 
*
**/

ALTER TABLE courts
ADD CONSTRAINT crt_local_justice_area_id_fk FOREIGN KEY
(
  local_justice_area_id 
)
REFERENCES local_justice_areas
(
  local_justice_area_id 
);
