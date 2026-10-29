INSERT INTO business_units (
    business_unit_id, business_unit_name, business_unit_code, business_unit_type, opal_domain
) VALUES (6460, 'Interface File Processor', 'BC12', 'Area', 'Fines');

INSERT INTO configuration_items (
    configuration_item_id, item_name, business_unit_id, item_values
) VALUES (
    99000000646001, 'BANK_ACCOUNTS', 6460,
    '[{"sort_code":"646001","account_number":"00006460"}]'::json
);
