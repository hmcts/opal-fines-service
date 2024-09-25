/**
* OPAL Program
*
* MODULE      : assign_inputter_checker_permission.sql
*
* DESCRIPTION : Assign users to the Application Function (aka permission) named: Check and Validate Draft Accounrts and Create and Manage Draft Accounts.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 20/09/2024    A Dennis    1.0         PO-721 Assign users to the Application Function (aka permission) named: Check and Validate Draft Accounrts and Create and Manage Draft Accounts.
*
**/

-- Test user opal-test-1@HMCTS.NET should have the permission - Check and Validate Draft Accounts
INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500023,'L065JG',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500024,'L066JG',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500025,'L067JG',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500026,'L073JG',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500027,'L077JG',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500028,'L078JG',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500029,'L080JG',501);

-- The implication is that Test user opal-test-1@HMCTS.NET should NOT have the permission - Create and Manage Draft Accounts
DELETE FROM user_entitlements WHERE user_entitlement_id IN (112904, 113138, 113372, 114776, 115712, 115946, 116414);

-- Test user opal-test-3@HMCTS.NET
-- No change required

--  Test user opal-test-4@HMCTS.NET should have the permission - Check and Validate Draft Accounts
INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500030,'L047SA',501);

-- The implication is that Test user opal-test-4@HMCTS.NET should NOT have the permission - Create and Manage Draft Accounts
DELETE FROM user_entitlements WHERE user_entitlement_id = 223563;

-- Test user opal-test-5@HMCTS.NET
-- No change required 

-- Test user opal-test-6@HMCTS.NET
-- No change required 

-- Test user opal-test-7@HMCTS.NET
-- No change required

--  Test user opal-test-8@HMCTS.NET should have the permission - Check and Validate Draft Accounts
INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500031,'L036DO',501);

-- The implication is that Test user opal-test-8@HMCTS.NET should NOT have the permission - Create and Manage Draft Accounts
DELETE FROM user_entitlements WHERE user_entitlement_id = 390389;

-- Test user opal-test-9@HMCTS.NET
-- No change required

-- Test user opal-test-10@HMCTS.NET should have the permission - Check and Validate Draft Accounts
INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500032,'L065AO',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500033,'L066AO',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500034,'L067AO',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500035,'L073AO',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500036,'L077AO',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500037,'L078AO',501);

INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
VALUES(500038,'L080AO',501);

-- The implication is that Test user opal-test-10@HMCTS.NET should NOT have the permission - Create and Manage Draft Accounts
DELETE FROM user_entitlements WHERE user_entitlement_id IN (384202, 384448, 500013, 386416, 388630, 388384, 388876);
