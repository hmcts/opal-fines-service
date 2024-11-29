/**
* CGI OPAL Program
*
* MODULE      : modify_table_columns.sql
*
* DESCRIPTION : Change payments_in.till_id from SMALLINT to BIGINT to match what is in the tills table for the Fines model
*               Change mis_debtors.account_number from Varchar(1) to Varchar(36) in the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------
* 19/01/2024    A Dennis    1.0         PO-162 Change payments_in.till_id from SMALLINT to BIGINT to match what is in the tills table for the Fines model
*                                              Change mis_debtors.account_number from Varchar(1) to Varchar(36) in the Fines model.
*
**/
ALTER TABLE payments_in
ALTER COLUMN till_id TYPE BIGINT;

ALTER TABLE mis_debtors
ALTER COLUMN account_number TYPE VARCHAR(36);
