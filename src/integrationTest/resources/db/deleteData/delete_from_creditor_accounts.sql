/**
* OPAL Program
*
* MODULE      : delete_from_creditor_accounts.sql
*
* DESCRIPTION : Cleans up the rows inserted into the creditor_accounts
*               tables major_creditors for the Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 05/08/2025  J SHEEN     1.0      PO-2972 data for major creditors R1B disabled integration test
*/

DELETE FROM public.creditor_accounts
WHERE creditor_account_id = 100000000001;

DELETE FROM public.major_creditors
WHERE major_creditor_id = 0001;

