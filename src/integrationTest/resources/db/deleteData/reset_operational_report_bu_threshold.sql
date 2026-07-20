INSERT INTO configuration_items (
    configuration_item_id,
    item_name,
    business_unit_id,
    item_value,
    item_values
) VALUES (
    60000000000014,
    'OPERATIONAL_REPORT_BU_WARNING_THRESHOLD',
    NULL,
    '10',
    NULL
)
ON CONFLICT (configuration_item_id) DO UPDATE
   SET item_name = EXCLUDED.item_name,
       business_unit_id = EXCLUDED.business_unit_id,
       item_value = EXCLUDED.item_value,
       item_values = EXCLUDED.item_values;

UPDATE reports
   SET permission = 'SEARCH_AND_VIEW_ACCOUNTS'
 WHERE report_id = 'operational_report_enforcement';
