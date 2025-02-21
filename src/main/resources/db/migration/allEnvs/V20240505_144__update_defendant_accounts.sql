/**
* OPAL Program
*
* MODULE      : update_account_transfers.sql
*
* DESCRIPTION : Update rows of test data in the DEFENDANT_ACCOUNTS table after the Local Justice Areas reference data load from spreadsheets to match them to new parent data.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 05/05/2024    A Dennis    1.0         PO-305 Update rows of test data in the DEFENDANT_ACCOUNTS table after the Local Justice Areas reference data load from spreadsheets to match them to new parent data.
**/
UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 1814
WHERE  enf_override_tfo_lja_id  = 2022;

UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 2052
WHERE  enf_override_tfo_lja_id  = 2216;

UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 2126
WHERE  enf_override_tfo_lja_id  = 2232;

UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 2530
WHERE  enf_override_tfo_lja_id  = 2651;

UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 2540
WHERE  enf_override_tfo_lja_id  = 2078;

UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 2619
WHERE  enf_override_tfo_lja_id  = 1755;

UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 3098
WHERE  enf_override_tfo_lja_id  = 2320;

UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 3119
WHERE  enf_override_tfo_lja_id  = 2160;

UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 3123
WHERE  enf_override_tfo_lja_id  = 3119;

UPDATE defendant_accounts
SET    enf_override_tfo_lja_id  = 3190
WHERE  enf_override_tfo_lja_id  = 2831;
