/**
* OPAL Program
*
* MODULE      : delete_from_amendments.sql
*
* DESCRIPTION : Cleans up the rows inserted into the amendments table for the Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author   Version  Nature of Change
* ----------  -------  -------  -------------------------------------------------------------
* 23/09/2025  R Dodd   1.0      PO-1590 Deletes rows of data from the AMENDMENTS tables.
*
*/

-- Delete test creditor transactions first (FK references creditor_accounts)
DELETE FROM amendments WHERE amendment_id >= 60000000000000;
