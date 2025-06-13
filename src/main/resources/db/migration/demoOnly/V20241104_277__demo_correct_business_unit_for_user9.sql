/**
* OPAL Program
*
* MODULE      : correct_business_unit_id_for_user9.sql
*
* DESCRIPTION : In the BUSINESS_UNIT_USERS table correct the business_unit_ids for user opal-test-9@HMCTS.NET (500000008) so that they match their business_unit_user_id
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------
* 04/11/2024    A Dennis    1.0         PO-959 In the BUSINESS_UNIT_USERS table correct the business_unit_ids for user opal-test-9@HMCTS.NET (500000008) so that they match their business_unit_user_id
*
**/

UPDATE business_unit_users
SET    business_unit_id        = 45
WHERE  business_unit_user_id   = 'L045EO'
AND    business_unit_id        = 82
AND    user_id                 = 500000008; 

UPDATE business_unit_users
SET    business_unit_id        = 82
WHERE  business_unit_user_id   = 'L082EO'
AND    business_unit_id        = 45
AND    user_id                 = 500000008; 
