/**
* CGI OPAL Program
*
* MODULE      : insert_warrant_register_report.sql
*
* DESCRIPTION : Load the REPORTS table with warrant register report data for the Fines DB
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------------------------
* 23/09/2025    C Cho       1.0         PO-2232 Insert warrant register report into REPORTS table
*
**/

INSERT INTO reports (report_id, report_title, report_group, report_parameters, audited_report) VALUES
('warrant_register', 'Warrant Register', 'Account Management', '[{ "name":"enforcer", "prompt":"Enforcer", "type":"enforcers", "min":1, "max":1 }]', TRUE);