/**
* CGI OPAL Program
*
* MODULE      : local_justice_areas.sql
*
* DESCRIPTION : Creates the LOCAL_JUSTICE_AREAS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the LOCAL_JUSTICE_AREAS table for the Fines model
*
**/
CREATE TABLE local_justice_areas 
(
 local_justice_area_id     smallint     not null
,name                      varchar(35)  not null
,address_line_1            varchar(35)  not null
,address_line_2            varchar(35)
,address_line_3            varchar(35)
,postcode                  varchar(8)
,CONSTRAINT local_justice_area_id_pk PRIMARY KEY 
 (
   local_justice_area_id	
 ) 
);

COMMENT ON COLUMN local_justice_areas.local_justice_area_id IS 'Unique ID of this record';
COMMENT ON COLUMN local_justice_areas.name IS 'LJA name';
COMMENT ON COLUMN local_justice_areas.address_line_1 IS 'LJA address line 1';
COMMENT ON COLUMN local_justice_areas.address_line_2 IS 'LJA address line 2';
COMMENT ON COLUMN local_justice_areas.address_line_3 IS 'LJA address line 3';
COMMENT ON COLUMN local_justice_areas.postcode IS 'LJA postcode';
