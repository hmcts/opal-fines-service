/**
* CGI OPAL Program
*
* MODULE      : insert_reports.sql
*
* DESCRIPTION : Load the REPORTS table with reference data for the Fines model as per script (load-reports.sql) from Capita
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------------------------
* 30/07/2025    TMc         1.0         PO-1810 - Load the REPORTS table with reference data for the Fines model as per script (load-reports.sql) from Capita
*
**/
DELETE FROM reports;

INSERT INTO reports (report_id, report_title, report_group, report_parameters, audited_report) VALUES
('tfo_in_register', 'Transfer of Fine Orders In Register', 'Fines', NULL, TRUE),
('fp_register', 'Fixed Penalty Register', 'Fines', NULL, TRUE),
('list_amendments', 'List Amendments', 'Account Management', '[{ "name":"amendment_date", "prompt":"Amendment Date", "type":"date" }]', TRUE),
('list_extend_ttp', 'List of Extensions of Time to Pay', 'Account Management', NULL, TRUE);