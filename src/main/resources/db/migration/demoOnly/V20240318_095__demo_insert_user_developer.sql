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
* 18/03/2024    R. Dodd     1.0         PO-183 As an extension of the work to restrict BE functionality to users with the correct roles and permission, create a 'developer' user.
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
 0
,'developer-user'
,'ZjaCcP/VFBjsWL15Py2bGw==!'
,'Creating a catch-all User for developers when running and debugging locally on developer machines'
);
