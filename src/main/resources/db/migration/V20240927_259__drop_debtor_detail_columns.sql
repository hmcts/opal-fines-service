/**
* OPAL Program
*
* MODULE      : drop_debtor_detail_columns.sql
*
* DESCRIPTION : Drop some contact details columns from DEBTOR_DETAIL because they are in PARTIES.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 27/09/2024    A Dennis    1.0         PO-655 Drop some contact details columns from DEBTOR_DETAIL because they are in PARTIES.
*
**/

ALTER TABLE DEBTOR_DETAIL
DROP COLUMN IF EXISTS telephone_home,
DROP COLUMN IF EXISTS telephone_business,
DROP COLUMN IF EXISTS telephone_mobile,
DROP COLUMN IF EXISTS email_1,
DROP COLUMN IF EXISTS email_2;
