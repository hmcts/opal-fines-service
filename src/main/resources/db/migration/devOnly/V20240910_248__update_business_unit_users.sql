/**
* CGI OPAL Program
*
* MODULE      : update_business_unit_users.sql
*
* DESCRIPTION : Update business unit users to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*               Only Business Units for Fines and Confiscation are included. No Reciprocal Maintenance business units.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 10/09/2024    A Dennis    1.0         PO-755 Update business unit users to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database
*                                       Only Business Units for Fines and Confiscation are included. No Reciprocal Maintenance business units.
*                 
**/

DELETE FROM user_entitlements
WHERE business_unit_user_id = 'L111SA';  -- this is RM

DELETE FROM business_unit_users
WHERE business_unit_id = 74;  -- this RM

UPDATE business_unit_users
SET   business_unit_id = 26
WHERE business_unit_id = 60;

UPDATE business_unit_users
SET   business_unit_id = 36
WHERE business_unit_id = 85;

UPDATE business_unit_users
SET   business_unit_id = 82
WHERE business_unit_id = 57;

UPDATE business_unit_users
SET   business_unit_id = 47
WHERE business_unit_id = 43;

UPDATE business_unit_users
SET   business_unit_id = 60
WHERE business_unit_id = 53;

UPDATE business_unit_users
SET   business_unit_id = 65
WHERE business_unit_id = 70;

UPDATE business_unit_users
SET   business_unit_id = 66
WHERE business_unit_id = 68;

UPDATE business_unit_users
SET   business_unit_id = 67
WHERE business_unit_id = 73;

UPDATE business_unit_users
SET   business_unit_id = 73
WHERE business_unit_id = 71;

UPDATE business_unit_users
SET   business_unit_id = 77
WHERE business_unit_id = 67;

UPDATE business_unit_users
SET   business_unit_id = 78
WHERE business_unit_id = 69;

UPDATE business_unit_users
SET   business_unit_id = 80
WHERE business_unit_id = 61;

UPDATE business_unit_users
SET   business_unit_id = 45
WHERE business_unit_id = 79;

UPDATE business_unit_users
SET   business_unit_id = 89
WHERE business_unit_id = 58;

UPDATE business_unit_users
SET   business_unit_id = 106
WHERE business_unit_id = 78;
