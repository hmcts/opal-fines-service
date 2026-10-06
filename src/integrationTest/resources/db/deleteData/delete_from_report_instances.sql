DELETE FROM public.report_instances
WHERE report_instance_id IN (9001, 9002, 9003, 9004, 9005);

DELETE FROM public.reports
WHERE report_id IN ('it_report_instances', 'it_report_noperm', 'it_report_diff_perm');
