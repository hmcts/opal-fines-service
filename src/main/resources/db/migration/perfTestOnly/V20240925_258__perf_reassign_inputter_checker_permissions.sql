/**
* OPAL Program
*
* MODULE      : reassign_inputter_checker_permissions.sql
*
* DESCRIPTION : Reassign some inputter and checker permissions requested by system test. And also make correction to assigned business units caused by the removal of RM business units.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 25/09/2024    A Dennis    1.0         PO-802 and 810 Reassign some inputter and checker permissions requested by system test. And also make correction to assigned business units caused by the removal of RM business units.
*
**/

-- Put back opal-test@HMCTS.NET permission to Create and Manage Draft Accounts
INSERT INTO user_entitlements
  (
   user_entitlement_id           
  ,business_unit_user_id
  ,application_function_id          
  )
VALUES
  (
   112904
  ,'L065JG'
  ,35
  );


INSERT INTO user_entitlements
  (
   user_entitlement_id           
  ,business_unit_user_id
  ,application_function_id          
  )
VALUES
  (
   113138
  ,'L066JG'
  ,35
  );


INSERT INTO user_entitlements
  (
   user_entitlement_id           
  ,business_unit_user_id
  ,application_function_id          
  )
VALUES
  (
   113372
  ,'L067JG'
  ,35
  );

INSERT INTO user_entitlements
  (
   user_entitlement_id           
  ,business_unit_user_id
  ,application_function_id          
  )
VALUES
  (
   114776
  ,'L073JG'
  ,35
  );

INSERT INTO user_entitlements
  (
   user_entitlement_id           
  ,business_unit_user_id
  ,application_function_id          
  )
VALUES
  (
   115712
  ,'L077JG'
  ,35
  );

INSERT INTO user_entitlements
  (
   user_entitlement_id           
  ,business_unit_user_id
  ,application_function_id          
  )
VALUES
  (
   115946
  ,'L078JG'
  ,35
  );

INSERT INTO user_entitlements
  (
   user_entitlement_id           
  ,business_unit_user_id
  ,application_function_id          
  )
VALUES
  (
   116414
  ,'L080JG'
  ,35
  );

-- Give opal-test-8@HMCTS.NET permission to Create and Manage Draft Accounts
INSERT INTO user_entitlements(user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500039, 'L036DO', 35);

-- ======== Business Unit changes for opal-test@HMCTS.NET  =============================================================
UPDATE business_unit_users
SET    business_unit_id       = 78
WHERE  business_unit_user_id  = 'L078JG'
AND    user_id                = 500000000
AND    business_unit_id       = 106;

DELETE FROM user_entitlements            -- Delete because this is a duplicate, caused by the removal of RM business units
WHERE  business_unit_user_id = 'L067JG';

DELETE FROM business_unit_users         -- Delete because this is a duplicate, caused by the removal of RM business units
WHERE  business_unit_user_id  = 'L067JG'
AND    user_id                = 500000000
AND    business_unit_id       = 77; 

-- ========== Business Unit changes for opal-test-10@HMCTS.NET  ==========================================================
UPDATE business_unit_users
SET    business_unit_id       = 78
WHERE  business_unit_user_id  = 'L078AO'
AND    user_id                = 500000009
AND    business_unit_id       = 106;

DELETE FROM user_entitlements            -- Delete because this is a duplicate, caused by the removal of RM business units
WHERE  business_unit_user_id = 'L067AO';

DELETE FROM business_unit_users         -- Delete because this is a duplicate, caused by the removal of RM business units
WHERE  business_unit_user_id  = 'L067AO'
AND    user_id                = 500000009
AND    business_unit_id       = 77; 
