/**
* OPAL Program
*
* MODULE      : cleanup_for_business_units.sql
*
* DESCRIPTION : Clean up the data in the BUSINESS_UNITS table in order to be able to load Business Units Reference Data from the Legacy GoB environment. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------------------------------------
* 10/09/2024    A Dennis    1.0         PO-755 Clean up the data in the BUSINESS_UNITS table in order to be able to load Business Units Reference Data from the Legacy GoB environment.  
*
**/

-- Drop these constraints. They will be restored unsing other scripts after the load.

ALTER TABLE business_unit_users
DROP constraint IF EXISTS buu_business_unit_id_fk;

ALTER TABLE configuration_items
DROP constraint IF EXISTS ci_business_unit_id_fk;

ALTER TABLE courts
DROP constraint IF EXISTS crt_business_unit_id_fk;

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS da_business_unit_id_fk;

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS da_imposing_court_id_fk;

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS da_enforcing_court_id_fk;

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS da_last_hearing_court_id_fk;

ALTER TABLE document_instances
DROP constraint IF EXISTS di_business_unit_id_fk;

ALTER TABLE enforcers
DROP constraint IF EXISTS enf_business_unit_id_fk;

ALTER TABLE prisons
DROP constraint IF EXISTS pri_business_unit_id_fk;

ALTER TABLE tills
DROP constraint IF EXISTS till_business_unit_id_fk;

ALTER TABLE offences
DROP constraint IF EXISTS off_business_unit_id_fk;

ALTER TABLE major_creditors
DROP constraint IF EXISTS mc_business_unit_id_fk;

-- Remove the mocked up data that are not used
DELETE FROM account_transfers;
DELETE FROM document_instances;
DELETE FROM prisons;
DELETE FROM payments_in;
DELETE FROM tills;
DELETE FROM business_units;
