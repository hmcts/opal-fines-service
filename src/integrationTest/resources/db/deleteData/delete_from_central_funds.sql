/**
* OPAL Program
*
* MODULE      : delete_from_central_funds.sql
*
* DESCRIPTION : Cleans up configuration item data inserted for the Central Fund integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 21/05/2026  E Adeleke   1.0      PO-2320 Deletes Central Fund configuration item test data.
*
*/

DELETE FROM public.configuration_items
WHERE configuration_item_id = 60000000232000;
