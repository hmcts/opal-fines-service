/**
*
* OPAL Program
*
* MODULE      : reports.sql
*
* DESCRIPTION : Create the table REPORTS in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-400 Create the table REPORTS in the Fines model
*
**/      

CREATE TABLE reports
(
 report_id                          varchar(20)
,report_title                       varchar(50)        not null
,report_group                       varchar(50)        not null
,user_entries                       json               not null
,audited_report                     boolean            not null
,CONSTRAINT reports_pk PRIMARY KEY (report_id)
);

COMMENT ON COLUMN reports.report_id IS 'Unique ID of this record';
COMMENT ON COLUMN reports.report_title IS 'Report title, for e.g. List Monies Under Warrant';
COMMENT ON COLUMN reports.report_group IS 'The name of the group which this report is part of';
COMMENT ON COLUMN reports.user_entries IS 'The parameters required by the user to generate this report';
COMMENT ON COLUMN reports.audited_report IS 'Whether this is an audited report or not. Each audited report for each business unit has a sequence that must not skip. Audited reports lock the last report generated for that report and business unit and generate a new one with the number in the sequence. Report instances for audited reports';