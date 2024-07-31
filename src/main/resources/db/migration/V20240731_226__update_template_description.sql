/**
* OPAL Program
*
* MODULE      : update_welsh_ad_business_units.sql
*
* DESCRIPTION : Update the business_units table to indicate the Welsh business units for business unit type of Accounting Division. This is to replace previous work done for business unit type of Area
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 25/07/2024    A Dennis    1.0         PO-562 Update the business_units table to indicate the Welsh business units for business unit type of Accounting Division. This is to replace previous work done for business unit type of Area  
*
**/
-- Nullify previous work where business unit type was Area
UPDATE business_units
SET    welsh_language       = NULL
WHERE  business_unit_id     IN (26, 15, 11, 31);

-- Set it for where business unit type is Accounting Division
UPDATE business_units
SET    welsh_language       = TRUE
WHERE  business_unit_id     IN (78, 58, 53, 85);  -- these are the Accounting Divisions for North Wales, Gwent, Dyfed Powys, South Wales in that order
