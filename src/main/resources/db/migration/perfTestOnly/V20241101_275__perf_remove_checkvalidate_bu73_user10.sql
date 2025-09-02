/**
* OPAL Program
*
* MODULE      : remove_checkvalidate_bu73_user10.sql
*
* DESCRIPTION : Remove the 'Check and Validate Draft Accounts' permission from opal-test-10@hmcts.net in business unit 73 West London to allow more test scenarios
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------
* 01/11/2024    A Dennis    1.0         PO-952 Remove the 'Check and Validate Draft Accounts' permission from opal-test-10@hmcts.net in business unit 73 West London to allow more test scenarios
*
**/

DELETE FROM user_entitlements
WHERE user_entitlement_id      =  500035
AND   business_unit_user_id    =  'L073AO'
AND   application_function_id  =  501;
