/**
* OPAL Program
*
* MODULE      : insert_operational_report_bu_warning_threshold_configuration_item.sql
*
* DESCRIPTION : Insert operational report business unit warning threshold configuration item
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------------------------
* 19/06/2026    C Cho       1.0         PO-7270 - Add operational report business unit warning threshold configuration item.
*
**/

INSERT INTO public.configuration_items (
    configuration_item_id,
    item_name,
    business_unit_id,
    item_value,
    item_values
)
VALUES (
    NEXTVAL('configuration_item_id_seq'),
    'OPERATIONAL_REPORT_BU_WARNING_THRESHOLD',
    NULL,
    '10',
    NULL
);
