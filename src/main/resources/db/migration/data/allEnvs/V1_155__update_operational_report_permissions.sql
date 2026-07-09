/**
* OPAL Program
*
* MODULE      : update_operational_report_permissions.sql
*
* DESCRIPTION : Update operational report permissions
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------------------------
* 19/06/2026    C Cho       1.0         PO-7274 - Set operational report permissions to search and view accounts.
*
**/

UPDATE public.reports
   SET permission = 'SEARCH_AND_VIEW_ACCOUNTS'
 WHERE report_id IN (
           'operational_report_enforcement',
           'operational_report_payment'
       );
