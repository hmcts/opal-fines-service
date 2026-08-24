UPDATE reports
   SET permission = 'SEARCH_AND_VIEW_ACCOUNTS'
 WHERE report_id = 'operational_report_enforcement';

DELETE FROM configuration_items
 WHERE item_name = 'OPERATIONAL_REPORT_BU_WARNING_THRESHOLD';
