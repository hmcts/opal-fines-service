/**
* CGI OPAL Program
*
* MODULE      : configuration_items_index.sql
*
* DESCRIPTION : Creates an index on CONFIGURATION_ITEMS table for (item_name,business_unit_id)
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------
* 13/12/2024    A Dennis    1.0         PO-972 Creates an index on CONFIGURATION_ITEMS table for (item_name,business_unit_id)
*
**/

DROP INDEX IF EXISTS ci_item_name_idx;
CREATE INDEX IF NOT EXISTS ci_item_name_bu_idx
ON configuration_items(item_name,business_unit_id);
