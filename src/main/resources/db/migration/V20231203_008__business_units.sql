/**
* CGI OPAL Program
*
* MODULE      : business_units.sql
*
* DESCRIPTION : Creates the BUSINESS_UNITS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/12/2023    A Dennis    1.0         PO-127 Creates the BUSINESS_UNITS table for the Fines model
*
**/
CREATE TABLE business_units 
(
 business_unit_id	         smallint     not null
,business_unit_name	       varchar(200) not null
,business_unit_code        varchar(4)
,business_unit_type        varchar(20)  not null            
,account_number_prefix     varchar(2)
,parent_business_unit_id   smallint     
,CONSTRAINT business_unit_id_pk PRIMARY KEY 
 (
   business_unit_id	
 ) 
);
COMMENT ON COLUMN business_units.business_unit_id IS 'Unique ID of this record';
COMMENT ON COLUMN business_units.business_unit_name IS 'Business Unit name';
COMMENT ON COLUMN business_units.business_unit_code IS 'Business unit code';
COMMENT ON COLUMN business_units.business_unit_type IS 'Area or Accounting Division'; 
COMMENT ON COLUMN business_units.account_number_prefix IS 'Accounting division code that appears before account numbers';
COMMENT ON COLUMN business_units.parent_business_unit_id IS 'ID of the business unit that is the parent for this one';
