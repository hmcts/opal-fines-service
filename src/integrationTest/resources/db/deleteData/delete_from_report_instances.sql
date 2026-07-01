DELETE FROM public.report_instances
WHERE report_instance_id IN (9001, 9002, 9003);

DELETE FROM public.reports
WHERE report_id = 'it_report_instances';

