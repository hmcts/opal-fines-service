/**
* OPAL Program
*
* MODULE      : cleanup_business_unit_users.sql
*
* DESCRIPTION : Delete the initial business unit users and their entitlements, so we can later populate with users based on the business units reference data
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------
* 24/05/2024    A Dennis    1.0         PO-380 Delete the initial business unit users and their entitlements, so we can later populate with users based on the business units reference data
*/

-- Delete Business unit users and their entitlements so we can later pouplate with data based on the Business Units reference data
DELETE FROM user_entitlements;
DELETE FROM business_unit_users;

-- Update the description of the users taken from Legacy GoB
UPDATE users
SET description = 'Recreating a User with access to 7 business units in Legacy GoB'
WHERE user_id = 500000000;

UPDATE users
SET description = 'Recreating a User with access to 2 business units in Legacy GoB'
WHERE user_id = 500000003;
