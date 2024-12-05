/**
* CGI OPAL Program
*
* MODULE      : major_creditors.sql
*
* DESCRIPTION : Creates the MAJOR CREDITORS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/04/2024    A Dennis    1.0         PO-200 Creates the MAJOR CREDITORS table for the Fines model
*
**/
CREATE TABLE major_creditors 
(
 major_creditor_id       bigint       not null
,business_unit_id        smallint     not null
,major_creditor_code     varchar(4)
,name                    varchar(35)  not null
,address_line_1          varchar(35)     
,address_line_2          varchar(35)
,address_line_3          varchar(35)
,postcode                varchar(8)
,CONSTRAINT major_creditors_pk PRIMARY KEY 
 (
   major_creditor_id	
 ) 
);

ALTER TABLE major_creditors
ADD CONSTRAINT mc_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN major_creditors.major_creditor_id IS 'Unique ID of this record';
COMMENT ON COLUMN major_creditors.business_unit_id IS 'ID of the relating business unit to which this major creditor belongs';
COMMENT ON COLUMN major_creditors.major_creditor_code IS 'Major creditor code unique within the business unit';
COMMENT ON COLUMN major_creditors.name IS 'Major creditor name';
COMMENT ON COLUMN major_creditors.address_line_1 IS 'Major creditor address line 1';
COMMENT ON COLUMN major_creditors.address_line_2 IS 'Major creditor address line 2';
COMMENT ON COLUMN major_creditors.address_line_3 IS 'Major creditor address line 3';
COMMENT ON COLUMN major_creditors.postcode IS 'Major creditor postcode';
