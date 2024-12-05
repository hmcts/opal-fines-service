/**
* CGI OPAL Program
*
* MODULE      : modify_users.sql
*
* DESCRIPTION : Change users.user_id from varchar(100) to BIGINT for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------
* 11/03/2024    A Dennis    1.0         PO-237 Change users.user_id from varchar(100) to BIGINT for the Fines model
*
**/

-- USERS.user_id serves as the foreign key in many tables. So to change its data type we will first clean up and modify thos child tables:-
-- Drop the foreign key relationship in the child table
-- Rename the column appropriately in the child table
-- Change the column data type in the child table
-- We then make the change in the USERS (parent) table
-- Then at the end recreate the foreign key relationship ib the child table

-- NOTES table
ALTER TABLE IF EXISTS notes 
DROP constraint IF EXISTS notes_posted_by_aad_fk;

ALTER TABLE IF EXISTS notes
RENAME COLUMN posted_by_aad TO posted_by_user_id;

ALTER TABLE notes
ALTER COLUMN posted_by_user_id TYPE bigint
USING posted_by_user_id::bigint; 

-- DEFENDANT_TRANSACTIONS table
ALTER TABLE IF EXISTS defendant_transactions 
DROP constraint IF EXISTS dt_posted_by_aad_fk;

ALTER TABLE IF EXISTS defendant_transactions
RENAME COLUMN posted_by_aad TO posted_by_user_id;

ALTER TABLE defendant_transactions
ALTER COLUMN posted_by_user_id TYPE bigint
USING posted_by_user_id::bigint; 

-- PAYMENT_TERMS table
ALTER TABLE IF EXISTS payment_terms 
DROP constraint IF EXISTS pt_posted_by_aad_fk;

ALTER TABLE IF EXISTS payment_terms
RENAME COLUMN posted_by_aad TO posted_by_user_id;

ALTER TABLE payment_terms
ALTER COLUMN posted_by_user_id TYPE bigint
USING posted_by_user_id::bigint; 

-- ENFORCEMENTS table
ALTER TABLE IF EXISTS enforcements 
DROP constraint IF EXISTS enf_posted_by_aad_fk;

ALTER TABLE IF EXISTS enforcements
RENAME COLUMN posted_by_aad TO posted_by_user_id;

ALTER TABLE enforcements
ALTER COLUMN posted_by_user_id TYPE bigint
USING posted_by_user_id::bigint; 

-- BUSINESS_UNIT_USERS table
ALTER TABLE IF EXISTS business_unit_users 
DROP constraint IF EXISTS buu_user_id_fk;

-- USERS table primary key change from varchar to bigint
TRUNCATE TABLE users;

ALTER TABLE users
ALTER COLUMN user_id TYPE bigint 
USING user_id::bigint;

-- Recreate foreign keys for the child tables
ALTER TABLE notes
ADD CONSTRAINT notes_posted_by_user_id_fk FOREIGN KEY
(
  posted_by_user_id 
)
REFERENCES users
(
  user_id 
);

ALTER TABLE defendant_transactions
ADD CONSTRAINT dt_posted_by_user_id_fk FOREIGN KEY
(
  posted_by_user_id 
)
REFERENCES users
(
  user_id 
);

ALTER TABLE payment_terms
ADD CONSTRAINT pt_posted_by_user_id_fk FOREIGN KEY
(
  posted_by_user_id 
)
REFERENCES users
(
  user_id 
);

ALTER TABLE enforcements
ADD CONSTRAINT enf_posted_by_user_id_fk FOREIGN KEY
(
  posted_by_user_id 
)
REFERENCES users
(
  user_id 
);
