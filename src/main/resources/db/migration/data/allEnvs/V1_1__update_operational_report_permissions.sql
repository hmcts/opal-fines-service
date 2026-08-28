/**
* OPAL Program
*
* MODULE      : update_operational_report_permissions.sql
*
* DESCRIPTION : Update operational report permissions
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 28/08/2026    C Cho          1.0         PO-9721 - Update operational report permissions
*
**/

UPDATE public.reports
   SET permission = 'OPERATIONAL_REPORT_ENFORCEMENT'
 WHERE report_id = 'operational_report_enforcement';

UPDATE public.reports
   SET permission = 'OPERATIONAL_REPORT_PAYMENT'
 WHERE report_id = 'operational_report_payment';

