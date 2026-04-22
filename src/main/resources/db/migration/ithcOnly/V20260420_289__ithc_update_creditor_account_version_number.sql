/**
* CGI OPAL Program
*
* MODULE      : update_creditor_account_version_number.sql
*
* DESCRIPTION : Amend the CREDITOR_ACCOUNTS table to set version_number to 1 for all the test data, so the endpoint can provide the expected 
*               'etag' and 'creditor_accounts.version' values.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -------------------------------------------------------------------------------------------------------------------------------
* 20/04/2026    P Brumby       1.0         PO-3697 -  Amend data to populate version_number in the CREDITOR_ACCOUNTS table
*
**/

--Update version_number column on creditor_accounts

UPDATE creditor_accounts 
   SET version_number = 1
 WHERE version_number IS NULL;
