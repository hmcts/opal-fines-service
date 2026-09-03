/**
* OPAL Program
*
* MODULE      : insert_central_fund_account_configuration_items_nle_data.sql
*
* DESCRIPTION : Insert central fund account configuration items for business units with CF creditor accounts.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 22/07/2026    C Cho       1.0         PO-8963 Insert central fund account configuration items for NLE data.
*
**/

SELECT setval(
    'configuration_item_id_seq',
    MAX(configuration_item_id),
    true
)
FROM configuration_items
HAVING MAX(configuration_item_id) IS NOT NULL;

INSERT INTO configuration_items (
    configuration_item_id,
    item_name,
    business_unit_id,
    item_value,
    item_values
)
SELECT
    nextval('configuration_item_id_seq'),
    'CENTRAL_FUND_ACCOUNT',
    ca.business_unit_id,
    NULL,
    json_build_object(
        'name', 'HM Courts & Tribunals Service',
        'address_line_1', 'HMCS add 1',
        'address_line_2', 'HMCS add 2',
        'address_line_3', 'HMCS add 3',
        'pay_by_bacs', 'N'
    )
FROM (
    SELECT DISTINCT business_unit_id
    FROM creditor_accounts
    WHERE creditor_account_type = 'CF'
      AND business_unit_id IS NOT NULL
) ca
WHERE NOT EXISTS (
    SELECT 1
    FROM configuration_items ci
    WHERE ci.item_name = 'CENTRAL_FUND_ACCOUNT'
      AND ci.business_unit_id = ca.business_unit_id
);
