/**
* OPAL Program
*
* MODULE      : update_account_status_comments.sql
*
* DESCRIPTION : Add additional comments to the DEFENDANT_ACCOUNTS.ACCOUNT_STATUS column
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 19/08/2025    C Cho       1.0         PO-1645  Add additional comments to the DEFENDANT_ACCOUNTS.ACCOUNT_STATUS column
*
**/

COMMENT ON COLUMN defendant_accounts.account_status IS 'The status of the account. L (Live), C (Completed), TO (Transfer Out Pending), TS (Transfer Out to NI/Scotland Pending), TA (Transfer Out Acknowledged), CS (Consolidated), WO (Account written off), TFOA (TFO to be acknowledged), TFOAK (TFO acknowledged), FPR (Fixed penalty registration).';