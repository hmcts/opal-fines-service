DELETE FROM configuration_items
WHERE business_unit_id IN (31001, 31002, 31003, 31004)
  AND item_name IN ('BUSINESS_UNIT_LJA', 'BUSINESS_UNIT_ENFORCING_COURT');

DELETE FROM courts
WHERE court_id IN (900000000001, 900000000003, 900000000004);

DELETE FROM local_justice_areas
WHERE local_justice_area_id IN (30001, 30002, 30003);

DELETE FROM business_units
WHERE business_unit_id IN (31001, 31002, 31003, 31004);
