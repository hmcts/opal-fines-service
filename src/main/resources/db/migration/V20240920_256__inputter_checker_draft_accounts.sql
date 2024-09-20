/**
* OPAL Program
*
* MODULE      : inputter_checker_draft_accounts.sql
*
* DESCRIPTION : In the Opal Fines Service - Introduce the new Application Function (aka permission) named: Check and Validate Draft Accounrts. Update the existing Manual Account Creation permission to be called Create and Manage Draft Accounts.
*               Introduce the 2 new templates and their template mappings: Create and Manage Draft Accounts - Authorised Powers    and also  Check and Validate Draft Accounts - Authorised Powers
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 20/09/2024    A Dennis    1.0         PO-721 In the Opal Fines Service - Introduce the new Application Function (aka permission) named: Check and Validate Draft Accounrts. Update the existing Manual Account Creation permission to be called Create and Manage Draft Accounts. Then assign these permissions to specified users. 
*                                       Introduce the 2 new templates and their template mappings: Create and Manage Draft Accounts - Authorised Powers    and also  Check and Validate Draft Accounts - Authorised Powers
*
**/

-- APPLICATION_FUNCTIONS
UPDATE application_functions
SET    function_name          = 'Create and Manage Draft Accounts'
WHERE  application_function_id = 35;

INSERT INTO application_functions (application_function_id, function_name)
VALUES(501, 'Check and Validate Draft Accounts');

-- TEMPLATES
INSERT INTO templates (template_id, template_name)
VALUES(500000005, 'Create and Manage Draft Accounts - Authorised Powers');

INSERT INTO templates (template_id, template_name)
VALUES(500000006, 'Check and Validate Draft Accounts - Authorised Powers');

-- TEMPLATE_MAPPINGS
INSERT INTO template_mappings (template_id, application_function_id)
VALUES (500000005, 35);

INSERT INTO template_mappings (template_id, application_function_id)
VALUES (500000006, 501);

-- Delete the template mapping between Enforcement and and Manual Account Creation and also that between Maintenance and Manual Account Creation since they are not required
DELETE FROM template_mappings 
WHERE template_id             IN (500000000, 500000002) 
AND   application_function_id = 35;
