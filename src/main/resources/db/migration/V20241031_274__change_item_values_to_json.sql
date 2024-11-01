/**
* OPAL Program
*
* MODULE      : change_item_values_to_json.sql
*
* DESCRIPTION : Change the datatype of the column configuration_items.item_values from varchar(500)[] to json to allow for configurations that have multiple attributes, for example, court bank details
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 31/10/2024    A Dennis    1.0         PO-904  Change the datatype of the column configuration_items.item_values from varchar(500)[] to json to allow for configurations that have multiple attributes, for example, court bank details
*
**/
-- There is no data in the column so we can safely drop it and create with the correct data type
ALTER TABLE configuration_items
DROP COLUMN item_values;

ALTER TABLE configuration_items
ADD COLUMN item_values  json;
