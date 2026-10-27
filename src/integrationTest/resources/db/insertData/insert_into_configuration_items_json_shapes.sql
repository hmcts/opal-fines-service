/**
* OPAL Program
*
* MODULE      : insert_into_configuration_items_json_shapes.sql
*
* DESCRIPTION : Inserts configuration_items rows used to verify JSON object and array item_values mapping.
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 03/09/2026    R DODD       1.0         PO-10530 Insert JSON shape test fixtures.
*
**/

INSERT INTO configuration_items (
    configuration_item_id, item_name, business_unit_id, item_value, item_values
)
VALUES
(95003, 'BANK_ACCOUNTS', 501, NULL,
    '[{"sort_code":"123456","account_number":"01234567","name":"Interface Job Test Bank"}]'::json),
(95004, 'GRAPH_OBJECT_ITEM', 501, NULL, '{"name":"Graph Object Item"}'::json);
