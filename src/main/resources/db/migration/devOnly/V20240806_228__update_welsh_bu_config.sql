/**
*
* OPAL Program
*
* MODULE      : update_bu_config.sql
*
* DESCRIPTION : Sets 3 Welsh BUs to default Welsh, 1 Welsh BU to default English
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    -------------------------------------------------------------
* 06/08/2024    I Readman    1.0         PO-573 Update Welsh Language configuration for 4 Welsh BUs
*
**/     
UPDATE configuration_items SET item_value = 'CY' WHERE business_unit_id IN (53, 58, 78);
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value) 
  VALUES (500000007, 'DEFAULT_DOCUMENT_LANGUAGE_PREFERENCE', 85, 'EN');
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value) 
  VALUES (500000008, 'DEFAULT_HEARING_LANGUAGE_PREFERENCE', 85, 'EN');
