/**
* CGI OPAL Program
*
* MODULE      : tills.sql
*
* DESCRIPTION : Creates the TILLS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the TILLS table for the Fines model
*
**/
CREATE TABLE tills 
(
 till_id                 bigint       not null
,business_unit_id        smallint     not null
,till_number             smallint     not null
,owned_by                varchar(20)  not null
,CONSTRAINT tills_pk PRIMARY KEY 
 (
   till_id	
 ) 
);

ALTER TABLE tills
ADD CONSTRAINT till_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN tills.till_id IS 'Unique ID of this record';
COMMENT ON COLUMN tills.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN tills.till_number IS 'Till number unique within the business unit';
COMMENT ON COLUMN tills.owned_by IS 'ID of the user that owns this till';
