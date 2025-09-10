/**
* OPAL Program
*
* MODULE      : insert_users_modified.sql
*
* DESCRIPTION : Inserts rows of data into the USERS table after user_id was changed to bigint. These are users that also exist in Legacy GoB - when you look in the business_unit_users table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 12/03/2024    A Dennis    1.0         PO-237 Inserts rows of data into the USERS table after user_id was changed to bigint. These are users that also exist in Legacy GoB - when you look in the business_unit_users table.
*
**/
INSERT INTO users
(               
 user_id                 
,username       
,password             
,description                                  
)
VALUES
(
 500000000                      -- gl.userfour
,'opal-test@HMCTS.NET'
,'ZjaCcP/VFBjsWL15Py2bGw==!'
,'Recreating a User with access to 27 business units in Legacy GoB'
);

INSERT INTO users
(               
 user_id                 
,username       
,password             
,description                                  
)
VALUES
(
 500000001                    -- gl.firstuser
,'opal-test-2@HMCTS.NET'
,NULL
,'Recreating a User with no access to a business unit in Legacy GoB'
);

INSERT INTO users
(               
 user_id                 
,username       
,password             
,description                                  
)
VALUES
(
 500000002                 -- 'Suffolk.user'
,'opal-test-3@HMCTS.NET'
,'OQWXJPc6mWmEXf5BRsVmDg==!'
,'Recreating a User with access to just 1 business unit in Legacy GoB'
);

INSERT INTO users
(               
 user_id                 
,username       
,password             
,description                                  
)
VALUES
(
 500000003              -- 'humber.usertwo'
,'opal-test-4@HMCTS.NET'
,'W4rdKl2YsaZS6n/XLZQtrA==!'
,'Recreating a User with access to 3 business units in Legacy GoB'
);
