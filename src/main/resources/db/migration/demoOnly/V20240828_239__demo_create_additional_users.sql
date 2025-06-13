/**
* OPAL Program
*
* MODULE      : create_additional_users.sql
*
* DESCRIPTION : Create more users to enable us have different users with business units to test more scenarios. Some of these are users that also exist in Legacy GoB.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 28/08/2024    A Dennis    1.0         PO-661 Inserts rows of data into the USERS table. These are users that also exist in Legacy GoB.
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
 500000004
,'opal-test-5@HMCTS.NET'
,'5N1VKPdZbc34capVz8zFPA==!'
,'Recreating in Opal a User that belongs to Dyfed Powys business unit in Legacy GoB'
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
 500000005
,'opal-test-6@HMCTS.NET'
,'40CEMsGkilL7bHTZJrOIhg==!'
,'Recreating in Opal a User that belongs to North Wales business unit in Legacy GoB'
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
 500000006
,'opal-test-7@HMCTS.NET'
,'N4oGO4/bHblB409L3lhMfQ==!'
,'Recreating in Opal a User that belongs to Gwent business unit in Legacy GoB'
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
 500000007
,'opal-test-8@HMCTS.NET'
,'J3p4/AXIhkoXDppWzuq8TA==!'
,'Recreating in Opal a User that belongs to South Wales business unit in Legacy GoB'
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
 500000008
,'opal-test-9@HMCTS.NET'
,'138Adm/Tvj8hichDpq8/sg==!'
,'Recreating in Opal a User that belongs to Greater Manchester and North West Confiscation Unit business units in Legacy GoB'
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
 500000009
,'opal-test-10@HMCTS.NET'
,'Wn+WPl4FBHQMOmsQu21PpQ==!'
,'Recreating in Opal a User that belongs to 7 London business units in Legacy GoB'
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
 500000010
,'opal-test-11@HMCTS.NET'
,NULL
,'NOT YET IN BUSINESS UNITS'
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
 500000011
,'opal-test-12@HMCTS.NET'
,NULL
,'NOT YET IN BUSINESS UNITS'
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
 500000012
,'opal-test-13@HMCTS.NET'
,NULL
,'NOT YET IN BUSINESS UNITS'
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
 500000013
,'opal-test-14@HMCTS.NET'
,NULL
,'NOT YET IN BUSINESS UNITS'
);