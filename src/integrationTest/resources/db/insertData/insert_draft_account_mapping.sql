INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type,
                            account_number_prefix, opal_domain, welsh_language)
VALUES (31001, 'VPFPO mapping test', 'VM01', 'Area', 'XX', 'Fines', false),
       (31002, 'VPFPO missing court test', 'VM02', 'Area', 'XX', 'Fines', false),
       (31003, 'VPFPO ambiguous mapping A', 'VM03', 'Area', 'XX', 'Fines', false),
       (31004, 'VPFPO ambiguous mapping B', 'VM04', 'Area', 'XX', 'Fines', false);

INSERT INTO local_justice_areas (local_justice_area_id, name, address_line_1)
VALUES (30001, 'VPFPO mapped LJA', 'Test address'),
       (30002, 'VPFPO missing court LJA', 'Test address'),
       (30003, 'VPFPO ambiguous LJA', 'Test address');

INSERT INTO courts (court_id, business_unit_id, court_code, name)
VALUES (900000000001, 31001, 1, 'VPFPO mapped court'),
       (900000000003, 31003, 1, 'VPFPO ambiguous court A'),
       (900000000004, 31004, 1, 'VPFPO ambiguous court B');

INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_value)
VALUES (nextval('configuration_item_id_seq'), 'BUSINESS_UNIT_LJA', 31001, '30001'),
       (nextval('configuration_item_id_seq'), 'BUSINESS_UNIT_ENFORCING_COURT', 31001, '1'),
       (nextval('configuration_item_id_seq'), 'BUSINESS_UNIT_LJA', 31002, '30002'),
       (nextval('configuration_item_id_seq'), 'BUSINESS_UNIT_LJA', 31003, '30003'),
       (nextval('configuration_item_id_seq'), 'BUSINESS_UNIT_ENFORCING_COURT', 31003, '1'),
       (nextval('configuration_item_id_seq'), 'BUSINESS_UNIT_LJA', 31004, '30003'),
       (nextval('configuration_item_id_seq'), 'BUSINESS_UNIT_ENFORCING_COURT', 31004, '1');
