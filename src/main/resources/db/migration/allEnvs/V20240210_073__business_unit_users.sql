/**
* CGI OPAL Program
*
* MODULE      : business_unit_users.sql
*
* DESCRIPTION : Creates the BUSINESS UNIT USERS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 11/02/2024    A Dennis    1.0         PO-177 Creates the BUSINESS UNIT USERS table for the Fines model
*
**/
CREATE TABLE business_unit_users 
(
 business_unit_user_id       varchar(6)      not null
,business_unit_id            smallint        not null
,user_id                     varchar(100)    not null
,CONSTRAINT business_unit_users_pk PRIMARY KEY 
 (
   business_unit_user_id	
 ) 
);

ALTER TABLE business_unit_users
ADD CONSTRAINT buu_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE business_unit_users
ADD CONSTRAINT buu_user_id_fk FOREIGN KEY
(
  user_id 
)
REFERENCES users
(
  user_id 
);

COMMENT ON COLUMN business_unit_users.business_unit_user_id IS 'Unique ID of this record';
COMMENT ON COLUMN business_unit_users.business_unit_id IS 'ID of the business unit the user belongs to';
COMMENT ON COLUMN business_unit_users.user_id IS 'The user ID, based on AAD, and is the foreign key to the Users table';
