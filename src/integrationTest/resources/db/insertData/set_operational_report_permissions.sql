UPDATE reports
   SET permission = 'SEARCH_AND_VIEW_ACCOUNTS'
 WHERE report_id IN ('operational_report_enforcement', 'operational_report_payment');
