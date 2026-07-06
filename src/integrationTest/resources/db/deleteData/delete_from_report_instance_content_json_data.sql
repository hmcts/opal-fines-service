DELETE FROM report_instances
WHERE report_instance_id = 99000000354000;

UPDATE reports
SET permission = NULL
WHERE report_id = 'fp_register';
