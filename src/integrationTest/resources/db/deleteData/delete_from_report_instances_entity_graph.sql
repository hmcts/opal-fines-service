/**
* OPAL Program
*
* DESCRIPTION : Deletes targeted report instances entity graph test data after integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 03/06/2026  A Reeves    1.0      Remove report instances data and entity graph.
*
*/
DELETE FROM public.report_instances WHERE report_instance_id IN (123, 234, 345, 400, 567);

DELETE FROM public.business_units WHERE business_unit_id IN (1,2);

DELETE FROM public.reports WHERE report_id IN ('full_report_single_bu', 'full_report_multi_bus', 'no_supported_filetypes');