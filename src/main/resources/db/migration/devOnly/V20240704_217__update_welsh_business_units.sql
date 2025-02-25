/**
* OPAL Program
*
* MODULE      : update_welsh_business_units.sql
*
* DESCRIPTION : Update the business_units table to indicate the Welsh business units. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/07/2024    A Dennis    1.0         PO-451 Update the business_units table to indicate the Welsh business units.  
*
**/

UPDATE business_units
SET    welsh_language       = TRUE
WHERE  business_unit_code   = '60'
AND    business_unit_type   = 'Area';

UPDATE business_units
SET    welsh_language       = TRUE
WHERE  business_unit_code   = '61'
AND    business_unit_type   = 'Area';

UPDATE business_units
SET    welsh_language       = TRUE
WHERE  business_unit_code   = '63'
AND    business_unit_type   = 'Area';
