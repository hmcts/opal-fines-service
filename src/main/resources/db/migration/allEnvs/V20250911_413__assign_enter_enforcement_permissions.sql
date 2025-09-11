/**
* CGI OPAL Program
*
* MODULE      : assign_enter_enforcement_permission.sql
*
* DESCRIPTION : Assign new permission for Enter Enforcement to business unit user accounts
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------------------------------------
* 04/09/2025    P Brumby    1.0         PO-1788 - Fines - Assign new permission for Enter Enforcement to the appropriate business units and user accounts.
*
**/
DO $$
DECLARE
    v_app_fn_id BIGINT;
BEGIN

    -- Get the application_function_id from APPLICATION_FUNCTION for the Enter Enforcement permission using STRICT
    SELECT application_function_id 
    INTO STRICT v_app_fn_id 
    FROM application_functions 
    WHERE function_name = 'Enter Enforcement';
    
    --Insert new records, for the system test users, into USER_ENTITLEMENTS for the new APPLICATION_FUNCTION record for the Enter Enforcement permission
    WITH max_ue AS (
        SELECT max(user_entitlement_id) AS max_ue_id
          FROM user_entitlements
    )
    INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
        SELECT max_ue.max_ue_id + ROW_NUMBER() OVER (ORDER BY buu.business_unit_user_id) AS user_entitlement_id
             , buu.business_unit_user_id
             , v_app_fn_id AS application_function_id
          FROM users u 
          JOIN business_unit_users buu
            ON u.user_id = buu.user_id
         CROSS JOIN max_ue
         WHERE u.username LIKE 'opal-test%@HMCTS.NET'
           AND u.username != 'opal-test-2@HMCTS.NET'
           AND NOT EXISTS (
                           SELECT 1 
                           FROM   business_unit_users b 
                           WHERE  b.user_id = u.user_id 
                           AND    b.business_unit_id = 73
                           AND    b.business_unit_user_id = buu.business_unit_user_id 
                           AND    u.username = 'opal-test-10@HMCTS.NET'
          );

END $$;