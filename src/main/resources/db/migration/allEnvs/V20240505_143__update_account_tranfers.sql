/**
* OPAL Program
*
* MODULE      : update_account_transfers.sql
*
* DESCRIPTION : Update rows of test data in the ACCOUNT_TRANSFERS table after the Local Justice Areas reference data load from spreadsheets to match them to new parent data.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 05/05/2024    A Dennis    1.0         PO-305 Update rows of test data in the ACCOUNT_TRANSFERS table after the Local Justice Areas reference data load from spreadsheets to match them to new parent data.
**/
UPDATE account_transfers
SET    destination_lja_id  = 1814
WHERE  destination_lja_id  = 2022;

UPDATE account_transfers
SET    destination_lja_id  = 2052
WHERE  destination_lja_id  = 2216;

UPDATE account_transfers
SET    destination_lja_id  = 2126
WHERE  destination_lja_id  = 2232;

UPDATE account_transfers
SET    destination_lja_id  = 2530
WHERE  destination_lja_id  = 2651;

UPDATE account_transfers
SET    destination_lja_id  = 2540
WHERE  destination_lja_id  = 2078;

UPDATE account_transfers
SET    destination_lja_id  = 2619
WHERE  destination_lja_id  = 1755;

UPDATE account_transfers
SET    destination_lja_id  = 3098
WHERE  destination_lja_id  = 2320;

UPDATE account_transfers
SET    destination_lja_id  = 3119
WHERE  destination_lja_id  = 2160;

UPDATE account_transfers
SET    destination_lja_id  = 3123
WHERE  destination_lja_id  = 3119;

UPDATE account_transfers
SET    destination_lja_id  = 3190
WHERE  destination_lja_id  = 2831;
