/**
* OPAL Program
*
* MODULE      : update_business_units_dedup_codes.sql
*
* DESCRIPTION : Update business_units.business_unit_codes for duplicate test records before unique constraint can be added.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 10/08/2026    T McCallion    1.0         PO-6322 - Add unique constraint to BUSINESS_UNITS.BUSINESS_UNIT_CODE
*
**/

--Ensure business_unit_type is set to 'Accounting Division' when it has a parent_business_unit_id set
UPDATE business_units 
   SET business_unit_type = 'Accounting Division'
 WHERE parent_business_unit_id IS NOT NULL;  

--Add prefix ('T') to the business_unit_code for duplicate test business unit records
UPDATE business_units
   SET business_unit_code = 'T' || business_unit_code 
 WHERE business_unit_id IN (13, 16, 17, 32, 40, 62, 63);