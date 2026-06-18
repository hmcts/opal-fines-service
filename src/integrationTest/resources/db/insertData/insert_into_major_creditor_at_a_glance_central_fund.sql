/**
* OPAL Program
*
* MODULE      : insert_into_major_creditor_at_a_glance_central_fund.sql
*
* DESCRIPTION : Inserts central fund configuration item data for major creditor at-a-glance tests.
*/

INSERT INTO public.configuration_items (
  configuration_item_id, item_name, business_unit_id, item_value, item_values
)
VALUES (
  60000000213200,
  'CENTRAL_FUND_ACCOUNT',
  77,
  NULL,
  '{ "name": "West London Central Fund", "address_line_1": "1 HMCTS Way", "address_line_2": "London", "address_line_3": "Westminster" }'
);
