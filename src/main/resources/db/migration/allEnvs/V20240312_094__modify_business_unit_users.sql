/**
* OPAL Program
*
* MODULE      : modify_business_unit_users.sql
*
* DESCRIPTION : Modify the BUSINESS_UNIT_USERS table after user_id was changed to bigint.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 12/03/2024    A Dennis    1.0         PO-237 Modify the BUSINESS_UNIT_USERS table after user_id was changed to bigint.
*
**/
ALTER TABLE business_unit_users
ALTER COLUMN user_id TYPE bigint
USING user_id::bigint; 

ALTER TABLE business_unit_users
ADD CONSTRAINT buu_user_id_fk FOREIGN KEY
(
  user_id 
)
REFERENCES users
(
  user_id 
);
