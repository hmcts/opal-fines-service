/**
* CGI OPAL Program
*
* MODULE      : V20250704_327__alter_posted_by_user_id.sql
*
* DESCRIPTION : Alter POSTED_BY_USER_ID column. Rename to POSTED_BY_NAME and change datatype to varchar(100)
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------
* 02/07/2025    TMc         1.0         PO-1437 Alter POSTED_BY_USER_ID column. Drop FK, rename to POSTED_BY_NAME and change datatype to varchar(100)
*
**/

--Drop related foreign keys, rename column and change datatype
--creditor_transactions
ALTER TABLE creditor_transactions
DROP CONSTRAINT IF EXISTS ct_posted_by_user_id_fk;

ALTER TABLE creditor_transactions
RENAME COLUMN posted_by_user_id TO posted_by_name;

ALTER TABLE creditor_transactions
ALTER COLUMN posted_by_name TYPE VARCHAR(100);


--defendant_transactions
ALTER TABLE defendant_transactions
DROP CONSTRAINT IF EXISTS dt_posted_by_user_id_fk;

ALTER TABLE defendant_transactions
RENAME COLUMN posted_by_user_id TO posted_by_name;

ALTER TABLE defendant_transactions
ALTER COLUMN posted_by_name TYPE VARCHAR(100);


--enforcements
ALTER TABLE enforcements
DROP CONSTRAINT IF EXISTS enf_posted_by_user_id_fk;

ALTER TABLE enforcements
RENAME COLUMN posted_by_user_id TO posted_by_name;

ALTER TABLE enforcements
ALTER COLUMN posted_by_name TYPE VARCHAR(100);


--impositions
ALTER TABLE impositions
DROP CONSTRAINT IF EXISTS imp_posted_by_user_id_fk;

ALTER TABLE impositions
RENAME COLUMN posted_by_user_id TO posted_by_name;

ALTER TABLE impositions
ALTER COLUMN posted_by_name TYPE VARCHAR(100);


--notes
ALTER TABLE notes
DROP CONSTRAINT IF EXISTS notes_posted_by_user_id_fk;

ALTER TABLE notes
RENAME COLUMN posted_by_user_id TO posted_by_name;

ALTER TABLE notes
ALTER COLUMN posted_by_name TYPE VARCHAR(100);


--payment_terms
ALTER TABLE payment_terms
DROP CONSTRAINT IF EXISTS pt_posted_by_user_id_fk;

ALTER TABLE payment_terms
RENAME COLUMN posted_by_user_id TO posted_by_name;

ALTER TABLE payment_terms
ALTER COLUMN posted_by_name TYPE VARCHAR(100);


--suspense_transactions
ALTER TABLE suspense_transactions
DROP CONSTRAINT IF EXISTS st_posted_by_user_id_fk;

ALTER TABLE suspense_transactions
RENAME COLUMN posted_by_user_id TO posted_by_name;

ALTER TABLE suspense_transactions
ALTER COLUMN posted_by_name TYPE VARCHAR(100);
