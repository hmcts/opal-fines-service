/**
* CGI OPAL Program
*
* MODULE      : add_it_health_check_users.sql
*
* DESCRIPTION : Add IT Health Check test users with permissions
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -----------------------------------------------------------------
* 11/09/2025    C Cho       1.0         PO-2139 Add Alex Hayman and Thomas Smith users for IT Health Check testing
*
**/

-- Declare variables to store IDs
DO $$
DECLARE
    v_max_user_id BIGINT;
    v_alex_user_id BIGINT;
    v_thomas_user_id BIGINT;
    v_max_user_entitlement_id BIGINT;
    v_check_validate_function_id BIGINT;
    v_collection_order_function_id BIGINT;
    v_create_manage_function_id BIGINT;
BEGIN
    -- Get the next available user ID
    SELECT MAX(user_id) INTO v_max_user_id FROM users;
    
    v_alex_user_id := v_max_user_id + 1;
    v_thomas_user_id := v_max_user_id + 2;
    
    -- Insert new users
    INSERT INTO users (user_id, username, password, description)
    VALUES 
        (v_alex_user_id, 'alex.hayman@hmcts.net', NULL, 'User Alex to be user for IT Health Check testing'),
        (v_thomas_user_id, 'Thomas.Smith@HMCTS.NET', NULL, 'User Thomas to be user for IT Health Check testing');
    
    -- Insert business unit users
    INSERT INTO business_unit_users (business_unit_user_id, business_unit_id, user_id)
    VALUES
        ('L065BO', 65, v_alex_user_id),
        ('L065CO', 65, v_thomas_user_id);
    
    -- Get application function IDs for the required permissions
    SELECT application_function_id INTO v_check_validate_function_id 
    FROM application_functions 
    WHERE function_name = 'Check and Validate Draft Accounts';
    
    SELECT application_function_id INTO v_collection_order_function_id 
    FROM application_functions 
    WHERE function_name = 'Collection Order';
    
    SELECT application_function_id INTO v_create_manage_function_id 
    FROM application_functions 
    WHERE function_name = 'Create and Manage Draft Accounts';
    
    -- Get the maximum user entitlement ID
    SELECT MAX(user_entitlement_id) INTO v_max_user_entitlement_id FROM user_entitlements;
    
    -- Insert user entitlements
    INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
    VALUES
        -- Alex Hayman (L065BO) permissions
        (v_max_user_entitlement_id + 1, 'L065BO', v_check_validate_function_id),
        (v_max_user_entitlement_id + 2, 'L065BO', v_collection_order_function_id),
        (v_max_user_entitlement_id + 3, 'L065BO', v_create_manage_function_id),
        
        -- Thomas Smith (L065CO) permissions
        (v_max_user_entitlement_id + 4, 'L065CO', v_check_validate_function_id),
        (v_max_user_entitlement_id + 5, 'L065CO', v_collection_order_function_id),
        (v_max_user_entitlement_id + 6, 'L065CO', v_create_manage_function_id);
END $$;