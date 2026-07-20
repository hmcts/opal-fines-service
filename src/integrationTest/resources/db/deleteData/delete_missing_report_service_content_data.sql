UPDATE report_instances
   SET report_id = 'cash_till'
 WHERE report_instance_id = 99000000353000;

DELETE FROM reports
 WHERE report_id = 'missing_service_report';
