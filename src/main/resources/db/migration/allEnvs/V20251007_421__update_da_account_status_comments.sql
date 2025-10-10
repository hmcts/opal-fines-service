/**
* OPAL Program
*
* MODULE      : update_da_account_status_comments.sql
*
* DESCRIPTION : Update comments on DEFENDANT_ACCOUNTS.ACCOUNT_STATUS column
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 07/10/2025    C Cho       1.0         PO-2244  Update comments on DEFENDANT_ACCOUNTS.ACCOUNT_STATUS column
*
**/

COMMENT ON COLUMN defendant_accounts.account_status IS 'The status of the account. L (Live), C-(Completed), TO (Transfer Out Pending), TS (Transfer Out to NI/Scotland Pending), TA (Transfer Out Acknowledged), Consolidated (CS), Written Off (WO).';