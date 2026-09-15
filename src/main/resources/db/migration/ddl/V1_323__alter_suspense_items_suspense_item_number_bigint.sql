/**
* OPAL Program
*
* MODULE      : alter_suspense_items_suspense_item_number_bigint.sql
*
* DESCRIPTION : Alter SUSPENSE_ITEMS.SUSPENSE_ITEM_NUMBER to BIGINT.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 10/09/2026    C Cho       1.0         PO-3390 Alter SUSPENSE_ITEMS.SUSPENSE_ITEM_NUMBER to BIGINT.
*
**/

ALTER TABLE suspense_items
    ALTER COLUMN suspense_item_number TYPE BIGINT;
