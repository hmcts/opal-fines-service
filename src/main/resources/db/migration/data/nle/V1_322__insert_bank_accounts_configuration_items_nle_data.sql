/**
* OPAL Program
*
* MODULE      : insert_bank_accounts_configuration_items_nle_data.sql
*
* DESCRIPTION : Insert BANK_ACCOUNTS configuration items for all NLE business units.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 08/09/2026    C Cho       1.0         PO-10529 Add Payments In bank account configuration for all NLE BUs.
*
**/

SELECT setval(
    'configuration_item_id_seq',
    MAX(configuration_item_id),
    true
)
FROM configuration_items
HAVING MAX(configuration_item_id) IS NOT NULL;

WITH bank_accounts AS (
    SELECT bu.business_unit_id,
           '123456' AS sort_code,
           '01234567' AS account_number,
           bu.business_unit_name || ' Test Bank' AS account_name
    FROM business_units bu
)
INSERT INTO configuration_items (
    configuration_item_id,
    item_name,
    business_unit_id,
    item_value,
    item_values
)
SELECT
    nextval('configuration_item_id_seq'),
    'BANK_ACCOUNTS',
    ba.business_unit_id,
    NULL,
    json_build_array(json_build_object(
        'sort_code', ba.sort_code,
        'account_number', ba.account_number,
        'name', ba.account_name
    ))
FROM bank_accounts ba
WHERE NOT EXISTS (
    SELECT 1
    FROM configuration_items ci
    WHERE ci.item_name = 'BANK_ACCOUNTS'
      AND ci.business_unit_id = ba.business_unit_id
);
