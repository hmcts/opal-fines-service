update public.reports
set retention_period = 'P14D'
where report_id in ('operational_report_enforcement', 'operational_report_payment');