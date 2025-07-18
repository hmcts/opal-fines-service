/**
* CGI OPAL Program
*
* MODULE      : create_new_and_update_permissions.sql
*
* DESCRIPTION : Fines - Create new permission and tidy up existing ones
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -----------------------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1583 - Fines - Create new permission and tidy up existing ones
*
**/

--Temporarily drop FK's to APPLICATION_FUNCTION from TEMPLATE_MAPPINGS and USER_ENTITLEMENTS
ALTER TABLE template_mappings 
    DROP CONSTRAINT IF EXISTS tm_application_function_id_fk;

ALTER TABLE user_entitlements 
    DROP CONSTRAINT IF EXISTS ue_application_function_id_fk;


--Delete from and re-insert records into APPLICATION_FUNCTION with new IDs 
DELETE FROM application_functions;

INSERT INTO application_functions (application_function_id, function_name) VALUES
(1, 'Create and Manage Draft Accounts'),   --was 35
(2, 'Account Enquiry - Account Notes'),    --was 41
(3, 'Account Enquiry'),                    --was 54
(4, 'Collection Order'),                   --was 500
(5, 'Check and Validate Draft Accounts'),  --was 501
(6, 'Search and view accounts')            --new
;


--Update IDs in TEMPLATE_MAPPINGS
UPDATE template_mappings
   SET application_function_id = CASE application_function_id
                                    WHEN 35 THEN 1
                                    WHEN 41 THEN 2
                                    WHEN 54 THEN 3
                                    WHEN 500 THEN 4
                                    WHEN 501 THEN 5
                                    ELSE application_function_id
                                 END
 WHERE application_function_id IN (35, 41, 54, 500, 501);

--Insert new record into TEMPLATE_MAPPINGS (Enforcement template) for new APPLICATION_FUNCTION record
DELETE FROM template_mappings WHERE application_function_id = 6;
 
INSERT INTO template_mappings (template_id, application_function_id) VALUES 
(500000000, 6);


--Update IDs in USER_ENTITLEMENTS
UPDATE user_entitlements
   SET application_function_id = CASE application_function_id
                                    WHEN 35 THEN 1
                                    WHEN 41 THEN 2
                                    WHEN 54 THEN 3
                                    WHEN 500 THEN 4
                                    WHEN 501 THEN 5
                                    ELSE application_function_id
                                 END
 WHERE application_function_id IN (35, 41, 54, 500, 501);

 
--Insert new records, for the system test users, into USER_ENTITLEMENTS for the new APPLICATION_FUNCTION record
DELETE FROM user_entitlements WHERE application_function_id = 6;
 
WITH max_ue AS (
    SELECT max(user_entitlement_id) AS max_ue_id
      FROM user_entitlements
)
INSERT INTO user_entitlements (user_entitlement_id, business_unit_user_id, application_function_id)
    SELECT max_ue.max_ue_id + ROW_NUMBER() OVER (ORDER BY buu.business_unit_user_id) AS user_entitlement_id
         , buu.business_unit_user_id
         , 6 AS application_function_id
      FROM users u 
      JOIN business_unit_users buu
        ON u.user_id = buu.user_id
     CROSS JOIN max_ue
     WHERE u.username ILIKE 'opal-test%@HMCTS.NET'
       AND u.username != 'opal-test-2@HMCTS.NET'
;


--Add back the FK's to APPLICATION_FUNCTION on TEMPLATE_MAPPINGS and USER_ENTITLEMENTS
ALTER TABLE template_mappings 
    ADD CONSTRAINT tm_application_function_id_fk FOREIGN KEY (application_function_id) REFERENCES application_functions(application_function_id);

ALTER TABLE user_entitlements 
    ADD CONSTRAINT ue_application_function_id_fk FOREIGN KEY (application_function_id) REFERENCES application_functions(application_function_id);