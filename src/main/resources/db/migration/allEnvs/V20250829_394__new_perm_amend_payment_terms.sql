/**
* CGI OPAL Program
*
* MODULE      : new_perm_amend_payment_terms.sql
*
* DESCRIPTION : Add new permission for Amend Payment Terms
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------
* 26/08/2025    TMc         1.0         PO-1693 - Fines - Add new permission for Amend Payment Terms
*
**/
DO $$
DECLARE
    v_next_id BIGINT;
BEGIN
    -- Get the next available ID for the new permission
    SELECT MAX(application_function_id) + 1 INTO v_next_id FROM application_functions;
    
    --Insert new APPLICATION_FUNCTION record for Amend Payment Terms
    INSERT INTO application_functions (application_function_id, function_name)
    VALUES (v_next_id, 'Amend Payment Terms');

    --Insert new records, for the system test users, into USER_ENTITLEMENTS for the new APPLICATION_FUNCTION record for Amend Payment Terms
    WITH max_ue AS (
        SELECT max(user_entitlement_id) AS max_ue_id
          FROM user_entitlements
    )
    INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
        SELECT max_ue.max_ue_id + ROW_NUMBER() OVER (ORDER BY buu.business_unit_user_id) AS user_entitlement_id
             , buu.business_unit_user_id
             , v_next_id AS application_function_id
          FROM users u 
          JOIN business_unit_users buu
            ON u.user_id = buu.user_id
         CROSS JOIN max_ue
         WHERE (u.username = 'opal-test@HMCTS.NET' AND buu.business_unit_id IN (65, 66, 73, 77, 78, 80))
            OR (u.username = 'opal-test-3@HMCTS.NET' AND buu.business_unit_id = 26)
            OR (u.username = 'opal-test-4@HMCTS.NET' AND buu.business_unit_id = 47)
            OR (u.username = 'opal-test-5@HMCTS.NET' AND buu.business_unit_id = 60)
            OR (u.username = 'opal-test-6@HMCTS.NET' AND buu.business_unit_id = 106)
            OR (u.username = 'opal-test-7@HMCTS.NET' AND buu.business_unit_id = 89)
            OR (u.username = 'opal-test-8@HMCTS.NET' AND buu.business_unit_id = 36)
            OR (u.username = 'opal-test-9@HMCTS.NET' AND buu.business_unit_id IN (45, 82))
            OR (u.username = 'opal-test-10@HMCTS.NET' AND buu.business_unit_id IN (65, 66, 77, 78, 80));
END $$;