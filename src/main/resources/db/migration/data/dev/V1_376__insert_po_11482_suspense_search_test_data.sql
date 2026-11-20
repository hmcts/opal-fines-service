
/**
* OPAL Program
*
* MODULE      : PO-11482 Suspense Search Test Data
*
* DESCRIPTION : Recreates suspense search test records for
*               PO-9763 in the local Fines database.
*
* PARAMETERS  : None
*
* VERSION HISTORY:
*
* Date         Author      Version   Nature of Change
* -----------  ----------  --------  ----------------------------------------
* 19/11/2026   I Chandio   1.0       PO-11482. Create suspense search test data
*
**/

BEGIN;

-- ============================================================
-- 1. DELETE EXISTING PO-9763 TEST DATA
-- ============================================================

DELETE FROM suspense_item_number_index
WHERE suspense_item_number_index_id IN (
    99000000976326,
    99000000976373,
    99000000976377
);

DELETE FROM suspense_items
WHERE suspense_item_id IN (
    99000000976326,
    99000000976373,
    99000000976377
);

DELETE FROM suspense_accounts
WHERE suspense_account_id IN (
    99000000976326,
    99000000976373,
    99000000976377
);

-- ============================================================
-- 2. INSERT SUSPENSE ACCOUNTS
-- ============================================================

INSERT INTO suspense_accounts (
    suspense_account_id,
    business_unit_id,
    account_number
)
VALUES
    (99000000976326, 26, 'PO9763-26'),
    (99000000976373, 73, 'PO9763-73'),
    (99000000976377, 77, 'PO9763-77');

-- ============================================================
-- 3. INSERT SUSPENSE ITEMS
-- ============================================================

INSERT INTO suspense_items (
    suspense_item_id,
    suspense_account_id,
    suspense_item_number,
    suspense_item_type,
    created_date
)
VALUES
    (
        99000000976326,
        99000000976326,
        976326,
        'UN',
        '2026-11-16 10:00:00'
    ),
    (
        99000000976373,
        99000000976373,
        976373,
        'UN',
        '2026-11-16 10:00:00'
    ),
    (
        99000000976377,
        99000000976377,
        976377,
        'UN',
        '2026-11-16 10:00:00'
    );

-- ============================================================
-- 4. INSERT SUSPENSE ITEM NUMBER INDEX
-- ============================================================

INSERT INTO suspense_item_number_index (
    suspense_item_number_index_id,
    suspense_item_number,
    business_unit_id
)
VALUES
    (99000000976326, 976326, 26),
    (99000000976373, 976373, 73),
    (99000000976377, 976377, 77);

-- ============================================================
-- 5. VERIFY INSERTED RECORDS
-- ============================================================


 COMMIT;
