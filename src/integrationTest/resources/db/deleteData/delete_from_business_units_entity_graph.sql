/**
* OPAL Program
*
* MODULE      : delete_from_business_units_entity_graph.sql
*
* DESCRIPTION : Deletes Business Units and Configuration Items for Business Unit entity graph integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 14/04/2026    S WILLIAMS   1.0         PO-2880 Delete rows of data from BUSINESS_UNITS and CONFIGURATION_ITEMS tables for integration tests
*
**/

DELETE FROM configuration_items WHERE configuration_item_id IN (95001, 95002);
DELETE FROM business_units WHERE business_unit_id IN (501, 599);
