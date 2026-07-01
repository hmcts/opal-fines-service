/**
* OPAL Program
*
* MODULE      : delete_from_major_creditor_at_a_glance_central_fund.sql
*
* DESCRIPTION : Cleans up central fund configuration item data inserted for major creditor at-a-glance tests.
*/

DELETE FROM public.configuration_items
WHERE configuration_item_id = 60000000213200;
