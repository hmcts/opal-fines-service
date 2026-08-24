UPDATE public.reports
   SET permission = NULL
 WHERE report_id IN ('operational_report_enforcement', 'operational_report_payment');
