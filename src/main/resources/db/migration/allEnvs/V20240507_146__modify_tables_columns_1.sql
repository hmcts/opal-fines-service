/**
* OPAL Program
*
* MODULE      : modify_tables_columns_1.sql
*
* DESCRIPTION : The columns in this script are being modified due to typos in the data model spreadsheet for the Fines model. Included under this Jira ticket for ease.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------------------------------------------------------
* 07/05/2024    A Dennis    1.0         PO-305 The columns in this script are being modified due to typos in the data model spreadsheet for the Fines model. Included under this Jira ticket for ease.
*
**/

ALTER TABLE enforcements
ALTER COLUMN result_id TYPE varchar(6);

ALTER TABLE impositions
ALTER COLUMN posted_by_user_id DROP NOT NULL;

ALTER TABLE creditor_transactions
ALTER COLUMN posted_by_user_id DROP NOT NULL;

ALTER TABLE suspense_transactions
ALTER COLUMN posted_by_user_id DROP NOT NULL;
