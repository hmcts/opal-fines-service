/**
* OPAL Program
*
* MODULE      : insert_into_business_units_entity_graph.sql
*
* DESCRIPTION : Inserts Business Units and Configuration Items for Business Unit entity graph integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 14/04/2026    S WILLIAMS   1.0         PO-2880 Insert rows of data into BUSINESS_UNITS and CONFIGURATION_ITEMS tables for integration tests
*
**/

INSERT INTO business_units
(
business_unit_id,business_unit_name,business_unit_code,business_unit_type,
account_number_prefix,parent_business_unit_id,opal_domain,welsh_language
)
VALUES
(599,'Graph Parent Business Unit','GPBU','Area','GP',NULL,'Fines',false),
(501,'Graph Child Business Unit','GCBU','Area','GC',599,'Fines',true);

INSERT INTO configuration_items
(
configuration_item_id,item_name,business_unit_id,item_value,item_values
)
VALUES
(95001,'GRAPH_ITEM_ONE',501,'Graph Value One',NULL),
(95002,'GRAPH_ITEM_TWO',501,'Graph Value Two',NULL);
