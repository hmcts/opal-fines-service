/**
* CGI OPAL Program
*
* MODULE      : prisons.sql
*
* DESCRIPTION : Creates the PRISONS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 06/12/2023    A Dennis    1.0         PO-127 Creates the PRISONS table for the Fines model
*
**/
CREATE TABLE prisons 
(
 prison_id             bigint       not null
,business_unit_id      smallint     not null
,prison_code           varchar(4)   not null
,name                  varchar(35)  not null
,address_line_1        varchar(35)  not null
,address_line_2        varchar(35)  
,address_line_3        varchar(35)  
,postcode              varchar(8)
,CONSTRAINT prisons_pk PRIMARY KEY 
 (
   prison_id	
 ) 
);

ALTER TABLE prisons
ADD CONSTRAINT pri_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN prisons.prison_id IS 'Unique ID of this record';
COMMENT ON COLUMN prisons.business_unit_id IS 'ID of the relating till to which this till belongs';
COMMENT ON COLUMN prisons.prison_code IS 'Prison code unique within the business unit';
COMMENT ON COLUMN prisons.name IS 'Prison name';
COMMENT ON COLUMN prisons.address_line_1 IS 'Prison address line 1';
COMMENT ON COLUMN prisons.address_line_2 IS 'Prison address line 2';
COMMENT ON COLUMN prisons.address_line_3 IS 'Prison address line 3';
COMMENT ON COLUMN prisons.postcode IS 'Prison postcode';
