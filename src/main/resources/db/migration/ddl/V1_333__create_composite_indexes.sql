/**
* OPAL Program
*
* MODULE      : create_composite_indexes.sql
*
* DESCRIPTION : Create SUSPENSE_ITEM_NUMBER_INDEX table and related components.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 04/10/2026    I Chandio     1.0        PO-10189 DB - Fines - Add indexes to support suspense item search
**/


-- Supports lookup of suspense accounts by business unit
-- and joining using suspense_account_id
CREATE INDEX IF NOT EXISTS sa_bu_id_suspense_account_id_idx
    ON suspense_accounts (business_unit_id, suspense_account_id);

-- Supports lookup of suspense items by suspense account
-- and suspense reference/item number
CREATE INDEX IF NOT EXISTS si_suspense_account_id_suspense_item_number_idx
    ON suspense_items (suspense_account_id, suspense_item_number);