/**
* OPAL Program
*
* MODULE      : reload_preferences_configuration_items.sql
*
* DESCRIPTION : Rerunning these preferences configuation items for them to use the new values from the sequence configuration_item_id_seq.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------
* 13/12/2024    Capita      1.0         PO-1064 Rerunning these preferences configuation items for them to use the new values from the sequence configuration_item_id_seq.
**/
DELETE FROM configuration_items WHERE item_name IN ('AUDIT_LOG_RETENTION_PERIOD_DAYS','DEFAULT_DOCUMENT_LANGUAGE_PREFERENCE','DEFAULT_HEARING_LANGUAGE_PREFERENCE');
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value, item_values) VALUES (nextval('configuration_item_id_seq'), 'AUDIT_LOG_RETENTION_PERIOD_DAYS', NULL, NULL, NULL);
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value, item_values) VALUES (nextval('configuration_item_id_seq'), 'DEFAULT_DOCUMENT_LANGUAGE_PREFERENCE', 106, 'CY', NULL);
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value, item_values) VALUES (nextval('configuration_item_id_seq'), 'DEFAULT_HEARING_LANGUAGE_PREFERENCE', 106, 'CY', NULL);
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value, item_values) VALUES (nextval('configuration_item_id_seq'), 'DEFAULT_DOCUMENT_LANGUAGE_PREFERENCE', 89, 'CY', NULL);
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value, item_values) VALUES (nextval('configuration_item_id_seq'), 'DEFAULT_HEARING_LANGUAGE_PREFERENCE', 89, 'CY', NULL);
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value, item_values) VALUES (nextval('configuration_item_id_seq'), 'DEFAULT_DOCUMENT_LANGUAGE_PREFERENCE', 60, 'CY', NULL);
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value, item_values) VALUES (nextval('configuration_item_id_seq'), 'DEFAULT_HEARING_LANGUAGE_PREFERENCE', 60, 'CY', NULL);
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value, item_values) VALUES (nextval('configuration_item_id_seq'), 'DEFAULT_DOCUMENT_LANGUAGE_PREFERENCE', 36, 'EN', NULL);
INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value, item_values) VALUES (nextval('configuration_item_id_seq'), 'DEFAULT_HEARING_LANGUAGE_PREFERENCE', 36, 'EN', NULL);
