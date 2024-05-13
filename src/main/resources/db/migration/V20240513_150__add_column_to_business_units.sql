/**
* OPAL Program
*
* MODULE      : add_column_to_business_units.sql
*
* DESCRIPTION : Add a new column to the BUSINESS_UNITS table in order to be able to load Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------
* 13/05/2024    A Dennis    1.0         PO-306 Add a new column to the BUSINESS_UNITS table in order to be able to load Reference Data from data held in Excel spreadsheet.
*
**/
ALTER TABLE business_units
ADD COLUMN opal_domain        varchar(30);

COMMENT ON COLUMN business_units.opal_domain IS 'When business unit type is Accounting Division, then this value is the opal domain that the business uint is owned by';
