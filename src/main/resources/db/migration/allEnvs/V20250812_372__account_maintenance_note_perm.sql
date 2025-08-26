/**
* CGI OPAL Program
*
* MODULE      : account_maintenance_note_perm.sql
*
* DESCRIPTION : Add new account maintenance and notes permissions
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -----------------------------------------------------------------
* 12/08/2025    C Cho       1.0         PO-1643 Add new account maintenance and notes permissions
*
**/

-- Declare variables to store IDs
DO $$
DECLARE
    v_next_id BIGINT;
    v_account_maintenance_id BIGINT;
    v_add_notes_id BIGINT;
    v_max_user_entitlement_id BIGINT;
BEGIN
    -- Get the next available ID for the new permissions
    SELECT MAX(application_function_id) INTO v_next_id FROM application_functions;
    
    v_account_maintenance_id := v_next_id + 1;
    v_add_notes_id := v_next_id + 2;
    
    INSERT INTO application_functions (application_function_id, function_name)
    VALUES 
        (v_account_maintenance_id, 'Account Maintenance'),
        (v_add_notes_id, 'Add Account Activity Notes');
    
    -- Get the maximum user entitlement ID
    SELECT MAX(user_entitlement_id) INTO v_max_user_entitlement_id FROM user_entitlements;
    
    INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
    VALUES
        -- opal-test@HMCTS.NET users (business unit IDs: 80, 66, 73, 77, 78, 65)
        (v_max_user_entitlement_id + 1, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 80 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_account_maintenance_id),
        (v_max_user_entitlement_id + 2, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 80 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_add_notes_id),
        (v_max_user_entitlement_id + 3, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 66 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_account_maintenance_id),
        (v_max_user_entitlement_id + 4, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 66 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_add_notes_id),
        (v_max_user_entitlement_id + 5, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 73 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_account_maintenance_id),
        (v_max_user_entitlement_id + 6, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 73 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_add_notes_id),
        (v_max_user_entitlement_id + 7, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 77 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_account_maintenance_id),
        (v_max_user_entitlement_id + 8, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 77 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_add_notes_id),
        (v_max_user_entitlement_id + 9, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 78 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_account_maintenance_id),
        (v_max_user_entitlement_id + 10, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 78 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_add_notes_id),
        (v_max_user_entitlement_id + 11, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 65 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_account_maintenance_id),
        (v_max_user_entitlement_id + 12, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 65 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test@HMCTS.NET')), v_add_notes_id),
        
        -- opal-test-10@HMCTS.NET users (business unit IDs: 80, 77, 65, 66, 73, 78)
        (v_max_user_entitlement_id + 13, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 80 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test-10@HMCTS.NET')), v_account_maintenance_id),
        (v_max_user_entitlement_id + 14, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 77 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test-10@HMCTS.NET')), v_account_maintenance_id),
        (v_max_user_entitlement_id + 15, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 77 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test-10@HMCTS.NET')), v_add_notes_id),
        (v_max_user_entitlement_id + 16, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 65 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test-10@HMCTS.NET')), v_account_maintenance_id),
        (v_max_user_entitlement_id + 17, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 66 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test-10@HMCTS.NET')), v_add_notes_id),
        (v_max_user_entitlement_id + 18, (SELECT business_unit_user_id FROM business_unit_users WHERE business_unit_id = 78 AND user_id = (SELECT user_id FROM users WHERE username = 'opal-test-10@HMCTS.NET')), v_add_notes_id);
END $$;
