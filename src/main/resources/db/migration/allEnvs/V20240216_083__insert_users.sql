/**
* OPAL Program
*
* MODULE      : insert_users.sql
*
* DESCRIPTION : Inserts rows of data into the USERS table. These are users that also exist in Legacy GoB.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 16/02/2024    A Dennis    1.0         PO-179 Inserts rows of data into the USERS table. These are users that also exist in Legacy GoB.
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
 'gl.userfour'
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
 'gl.firstuser'
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
 'Suffolk.user'
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
 'humber.usertwo'
,'opal-test-4@HMCTS.NET'
,'W4rdKl2YsaZS6n/XLZQtrA==!'
,'Recreating a User with access to 3 business units in Legacy GoB'
);
