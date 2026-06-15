INSERT INTO public.configuration_items (
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
ON CONFLICT (configuration_item_id) DO NOTHING;
