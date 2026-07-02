/**
*
* OPAL Program
*
* MODULE      : update_impositions_dev_test_data_for_neg_imposed_amount.sql
*
* DESCRIPTION : Update impositions dev test data to set imposed_amount to be stored as negative values.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 30/06/2026    P Brumby    1.0         PO-3457 Update IMPOSITIONS dev test data to set imposed_amount to be stored as negative values.
*
**/

-- NLE data correction only: imposed_amount must be stored as negative.
UPDATE impositions
SET imposed_amount = -imposed_amount
WHERE imposed_amount > 0;
