/**
* OPAL Program
*
* MODULE      : drop_fks_to_business_units.sql
*
* DESCRIPTION : Drop foreign key constraints to the business_units table to allow for data reload.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------------------------
* 24/06/2025    C Cho       1.0         PO-1020 Drop foreign key constraints to business_units table to prepare for data reload.
*
**/

ALTER TABLE creditor_accounts 
DROP CONSTRAINT IF EXISTS ca_business_unit_id_fk;

ALTER TABLE courts 
DROP CONSTRAINT IF EXISTS crt_business_unit_id_fk;

ALTER TABLE enforcers 
DROP CONSTRAINT IF EXISTS enf_business_unit_id_fk;

ALTER TABLE prisons 
DROP CONSTRAINT IF EXISTS pri_business_unit_id_fk;

ALTER TABLE major_creditors 
DROP CONSTRAINT IF EXISTS mc_business_unit_id_fk;

ALTER TABLE business_unit_users 
DROP CONSTRAINT IF EXISTS buu_business_unit_id_fk;

ALTER TABLE configuration_items 
DROP CONSTRAINT IF EXISTS ci_business_unit_id_fk;
