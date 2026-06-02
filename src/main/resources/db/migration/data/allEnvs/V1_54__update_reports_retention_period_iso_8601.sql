/**
* OPAL Program
*
* MODULE      : update_reports_retention_period_iso_8601.sql
*
* DESCRIPTION : Update REPORTS.RETENTION_PERIOD values to ISO-8601 format for
*               existing operational report rows.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 02/06/2026    C Cho       1.0         PO-3561 Update REPORTS.RETENTION_PERIOD from 14 to P14D for operational report rows.
*
**/

UPDATE reports
   SET retention_period = 'P14D'
 WHERE report_id IN (
           'operational_report_enforcement',
           'operational_report_payment'
       )
   AND retention_period = '14';
