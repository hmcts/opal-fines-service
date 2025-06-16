/**
* OPAL Program
*
* MODULE      : cleanup_business_units.sql
*
* DESCRIPTION : Clean up the data in the BUSINESS_UNITS table in order to be able to load Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------
* 13/05/2024    A Dennis    1.0         PO-306 Clean up the data in the BUSINESS_UNITS table in order to be able to load Reference Data from data held in Excel spreadsheet.  
*
**/

ALTER TABLE business_unit_users
DROP constraint IF EXISTS buu_business_unit_id_fk;

ALTER TABLE configuration_items
DROP constraint IF EXISTS ci_business_unit_id_fk;

ALTER TABLE courts
DROP constraint IF EXISTS crt_business_unit_id_fk;

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS da_business_unit_id_fk;

ALTER TABLE document_instances
DROP constraint IF EXISTS di_business_unit_id_fk;

ALTER TABLE enforcers
DROP constraint IF EXISTS enf_business_unit_id_fk;

ALTER TABLE prisons
DROP constraint IF EXISTS pri_business_unit_id_fk;

ALTER TABLE tills
DROP constraint IF EXISTS till_business_unit_id_fk;

DELETE FROM business_units WHERE business_unit_id <= 96;

UPDATE business_units 
SET business_unit_name = 'THIS DUMMY BUSINESS UNIT'
   ,business_unit_code = 'NOPE';
