DELETE FROM control_totals WHERE business_unit_id = 32760;
DELETE FROM business_units WHERE business_unit_id = 32760;

INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type,
                            welsh_language)
VALUES (32760, 'Adjust Control Totals Integration Test', 'CTIT', 'Accounting Division', false);
