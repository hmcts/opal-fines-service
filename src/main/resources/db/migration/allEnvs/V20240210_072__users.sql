/**
* CGI OPAL Program
*
* MODULE      : users.sql
*
* DESCRIPTION : Creates the USERS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 11/02/2024    A Dennis    1.0         PO-177 Creates the USERS table for the Fines model
*
**/
CREATE TABLE users 
(
 user_id             varchar(100)   not null
,username            varchar(100)   not null
,password            varchar(1000)
,description         varchar(100)   
,CONSTRAINT users_pk PRIMARY KEY 
 (
   user_id	
 ) 
);

COMMENT ON COLUMN users.user_id IS 'Unique ID of this record';
COMMENT ON COLUMN users.username IS 'User name/email based on Azure Active Directory ID';
COMMENT ON COLUMN users.username IS 'Password';
COMMENT ON COLUMN users.description IS 'Description of the user';
