/**
* OPAL Program
*
* MODULE      : delete_from_reports.sql
*
* DESCRIPTION : Cleans up the rows inserted into the reports table for the Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 28/05/2026  A Reeves    2.0      PO-2252 Deletes rows of data from the REPORTS table.
*
*/
DELETE FROM public.report_instances WHERE report_id IN ('IT-report-1','IT-report-2','it_report_full');
DELETE FROM public.reports
       WHERE report_id IN ('it_report_full', 'it_report_optional', 'it_report_order','IT-report-1','IT-report-2');