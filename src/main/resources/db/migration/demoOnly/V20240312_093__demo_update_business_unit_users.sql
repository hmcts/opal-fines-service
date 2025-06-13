/**
* OPAL Program
*
* MODULE      : update_business_unit_users.sql
*
* DESCRIPTION : Update rows of test data in the BUSINESS_UNIT_USERS table after users.user_id was changed to bigint. These are users that also exist in Legacy GoB.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 12/03/2024    A Dennis    1.0         PO-237 Update rows of test data in the BUSINESS_UNIT_USERS table after users.user_id was changed to bigint. These are users that also exist in Legacy GoB.
*
**/
UPDATE business_unit_users
SET    user_id  = 500000000
WHERE  user_id  = 'gl.userfour';

UPDATE business_unit_users
SET    user_id  = 500000002
WHERE  user_id  = 'Suffolk.user';

UPDATE business_unit_users
SET    user_id  = 500000003       
WHERE  user_id  = 'humber.usertwo';
