/**
* OPAL Program
*
* MODULE      : reload_interfaces_configuration_items.sql
*
* DESCRIPTION : This script was written by Capita. It creates configuration items required for interface files. Rerunning it because the start value for the sequence configuration_item_id_seq has changed.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 13/12/2024    Capita      1.0         PO-1064 This script was written by Capita. It creates configuration items required for interface files. Rerunning it because the start value for the sequence configuration_item_id_seq has changed.
*
**/
/* Interfaces */
DELETE FROM configuration_items WHERE item_name IN ('INTERFACE_PRESENTED_CHEQUES','INTERFACE_PAYMENTS_IN','INTERFACE_PAYMENT_CARD_REQUESTS');
INSERT INTO configuration_items (configuration_item_id, item_name, item_values)
VALUES (nextval('configuration_item_id_seq'), 'INTERFACE_PRESENTED_CHEQUES', '{ "direction":"inbound", "days_before_deletion":100, "stored_procedure":"p_int_presented_cheques" }');
INSERT INTO configuration_items (configuration_item_id, item_name, item_values)
VALUES (nextval('configuration_item_id_seq'), 'INTERFACE_PAYMENTS_IN', '{ "direction":"inbound", "days_before_deletion":100, "stored_procedure":"p_int_payments_in" }');
INSERT INTO configuration_items (configuration_item_id, item_name, item_values)
VALUES (nextval('configuration_item_id_seq'), 'INTERFACE_PAYMENT_CARD_REQUESTS', '{ "direction":"outbound", "days_before_deletion":100, "stored_procedure":"p_int_payment_card_requests" }');
