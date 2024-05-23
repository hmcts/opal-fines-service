/**
* OPAL Program
*
* MODULE      : enable_offence_business_units_fk.sql
*
* DESCRIPTION : Put back foreign key to the BUSINESS_UNITS table after loading the business units Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------------------------------------
* 21/05/2024    A Dennis    1.0         PO-373 Put back foreign key to the BUSINESS_UNITS table after loading the business units Reference Data from data held in Excel spreadsheet. 
*
**/

ALTER TABLE offences
ADD CONSTRAINT off_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);
