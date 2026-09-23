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

alter table public.reports
    alter column permission type varchar(50);

UPDATE public.reports
SET permission = 'OPERATIONAL_REPORT_BY_ENFORCEMENT'
 WHERE report_id = 'operational_report_enforcement';

UPDATE public.reports
SET permission = 'OPERATIONAL_REPORT_BY_PAYMENTS'
 WHERE report_id = 'operational_report_payment';

