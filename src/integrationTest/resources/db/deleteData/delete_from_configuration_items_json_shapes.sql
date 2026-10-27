/**
* OPAL Program
*
* MODULE      : delete_from_configuration_items_json_shapes.sql
*
* DESCRIPTION : Deletes configuration_items rows used to verify JSON object and array item_values mapping.
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 03/09/2026    R DODD       1.0         PO-10530 Delete JSON shape test fixtures.
*
**/

DELETE FROM configuration_items WHERE configuration_item_id IN (95003, 95004);
