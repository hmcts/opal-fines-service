/**
* OPAL Program
*
* MODULE      : increase_user_description.sql
*
* DESCRIPTION : Increase the size of the description column in USERS table to allow for more details of what the user can do.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 28/08/2024    A Dennis    1.0         PO-661 Increase the size of the description column in USERS table to allow for more details of what the user can do.
*
**/
ALTER TABLE USERS 
ALTER COLUMN description TYPE varchar(300);
