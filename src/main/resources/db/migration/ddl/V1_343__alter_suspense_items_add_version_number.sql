
/**
* OPAL Program
*
* MODULE      : alter_suspense_items_add_version_number.sql
*
* DESCRIPTION : Add VERSION_NUMBER column to the SUSPENSE_ITEMS table
*               as a nullable BIGINT.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------
* 14/10/2026    TBD         1.0         PO-10343 Add VERSION_NUMBER column to SUSPENSE_ITEMS table.
*
**/

ALTER TABLE suspense_items
    ADD COLUMN version_number BIGINT NULL;

COMMENT ON COLUMN suspense_items.version_number IS
    'Used to check that related items have not changed since retrieval and prior to being amended.';
