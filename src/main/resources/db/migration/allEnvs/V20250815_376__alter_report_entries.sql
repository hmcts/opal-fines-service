/**
* CGI OPAL Program
*
* MODULE      : alter_report_entries.sql
*
* DESCRIPTION : Alter column REPORTED_TIMESTAMP on the REPORT_ENTRIES table to allow NULLs
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 01/08/2025    TMc         1.0         PO-1995 - Alter column REPORTED_TIMESTAMP on the REPORT_ENTRIES table to allow NULLs
*
**/
ALTER TABLE report_entries 
    ALTER COLUMN reported_timestamp DROP NOT NULL;
