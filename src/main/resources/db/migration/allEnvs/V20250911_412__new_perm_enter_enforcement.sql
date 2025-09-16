/**
* CGI OPAL Program
*
* MODULE      : new_perm_enter_enforcement.sql
*
* DESCRIPTION : Add new permission for Enter Enforcement
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------
* 04/09/2025    P Brumby    1.0         PO-1788 - Fines - Add new permission for Enter Enforcement.
*
**/
DO $$
BEGIN
    -- Get the next available ID and insert the new permission record
    INSERT INTO application_functions (application_function_id, function_name)
    SELECT MAX(application_function_id) + 1, 'Enter Enforcement'
    FROM application_functions;  

END $$;