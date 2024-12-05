/**
* CGI OPAL Program
*
* MODULE      : update_configuration_items.sql
*
* DESCRIPTION : Update configuration_items table to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 10/09/2024    A Dennis    1.0         PO-755 Update configuration_items table to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*
**/
UPDATE configuration_items
SET   business_unit_id = 106
WHERE business_unit_id = 78;

UPDATE configuration_items
SET   business_unit_id = 89
WHERE business_unit_id = 58;

UPDATE configuration_items
SET   business_unit_id = 60
WHERE business_unit_id = 53;

UPDATE configuration_items
SET   business_unit_id = 36
WHERE business_unit_id = 85;
