INSERT INTO public.configuration_items (
    configuration_item_id,
    item_name,
    business_unit_id,
    item_value,
    item_values
)
SELECT
    60000000000014,
    'OPERATIONAL_REPORT_BU_WARNING_THRESHOLD',
    NULL,
    '10',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM public.configuration_items
    WHERE item_name = 'OPERATIONAL_REPORT_BU_WARNING_THRESHOLD'
      AND business_unit_id IS NULL
);
