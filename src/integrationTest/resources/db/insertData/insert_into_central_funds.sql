/**
* OPAL Program
*
* MODULE      : insert_into_central_funds.sql
*
* DESCRIPTION : Inserts configuration item data for the Central Fund integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 21/05/2026  E Adeleke   1.0      PO-2320 Inserts Central Fund configuration item test data.
*
*/

INSERT INTO public.configuration_items (
  configuration_item_id, item_name, business_unit_id, item_value, item_values
)
VALUES (
  60000000232000,
  'CENTRAL_FUND_ACCOUNT',
  73,
  NULL,
  '{ "name": "West London Central Fund" }'
);
