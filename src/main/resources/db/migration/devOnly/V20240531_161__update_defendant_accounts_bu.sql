/**
* OPAL Program
*
* MODULE      : update_defendant_accounts_bu.sql
*
* DESCRIPTION : Update the business_unit_id foreign key ids in DEFENDANT_ACCOUNTS table to match the new business_unit_id in the BUSINESS_UNIT_USERS table after reference data load. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------
* 31/05/2024    A Dennis    1.0         PO-404  Update the business_unit_id foreign key ids in DEFENDANT_ACCOUNTS table to match the new business_unit_id in the BUSINESS_UNIT_USERS table after reference data load. 
*
**/

UPDATE defendant_accounts
SET originator_name    = 'Uxbridge court'
WHERE business_unit_id = 69;

UPDATE defendant_accounts
SET originator_name    = 'Hertford Magistrate Court'
  , business_unit_id   = 60
WHERE business_unit_id = 94;

UPDATE defendant_accounts
SET originator_name    = 'Avon and Sommerset NCES'
  , business_unit_id   = 43
WHERE business_unit_id = 95;

UPDATE defendant_accounts
SET originator_name    = 'Croydon Court'
  , business_unit_id   = 70
WHERE business_unit_id = 96;

UPDATE defendant_accounts
SET originator_name    = 'Consfication Enforcement'
  , business_unit_id   = 68
WHERE business_unit_id = 13;

UPDATE defendant_accounts
SET originator_name    = 'Enfield Magistrate Court'
  , business_unit_id   = 73
WHERE business_unit_id = 16;

UPDATE defendant_accounts
SET originator_name    = 'City of London Court'
  , business_unit_id   = 71
WHERE business_unit_id = 17;

UPDATE defendant_accounts
SET originator_name    = 'Highbury Court'
  , business_unit_id   = 67
WHERE business_unit_id = 19;

UPDATE defendant_accounts
SET originator_name    = 'Historic Debt'
  , business_unit_id   = 61
WHERE business_unit_id = 32;

UPDATE defendant_accounts
SET originator_name    = 'Wales MBEC'
  , business_unit_id   = 74
WHERE business_unit_id = 64;
