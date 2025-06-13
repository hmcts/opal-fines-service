/**
* OPAL Program
*
* MODULE      : tidyup_configuration_items.sql
*
* DESCRIPTION : Tidy up the CONFIGURATION_ITEMS tables to remove the dummy data inserted during Discovery+. We now have actual data.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------
* 29/07/2024    A Dennis    1.0         PO-509 Tidy up the CONFIGURATION_ITEMS tables to remove the dummy data inserted during Discovery+. We now have actual data.
*
**/
DELETE FROM configuration_items
WHERE configuration_item_id IN (500000001, 500000002, 500000003);
