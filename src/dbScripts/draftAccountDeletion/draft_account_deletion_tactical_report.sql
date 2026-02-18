/**
* CGI OPAL Program
*
* MODULE           : draft_account_deletion_tactical_report.sql
*
* DESCRIPTION      : This is a PSQL script that generates the Draft Account Deletion Tactical report as a CSV file.
*                    It prompts for the date range the report should be run for and generates the CSV filename based on the given values.
* 
* RUN INSTRUCTIONS : To run this script the following PSQL command can be used:
*                    psql -h <host> -p <port> -U <DB username> -d opal-fines-db -f draft_account_deletion_tactical_report.sql
* 
*                    NOTE: PSQL will prompt for the password of the <DB username> user, if .pgpass is not configured.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------------------------------------------------
* 13/02/2026    T McCallion    1.0         PO-2745 - DB - Create ad-hoc script to generate the Draft Account Deletion Tactical Report for Manual Account Creation
*
**/

-- Set the security classification text to be used for the header and footer
\set report_security_classification 'OFFICIAL - SENSITIVE'

\set ON_ERROR_STOP on
\set QUIET on

SET datestyle = 'ISO, YMD';
SET client_min_messages = warning;

-- Default the start_date and end_date to be the first and last days of the previous month
SELECT date_trunc('month', CURRENT_DATE - INTERVAL '1 month')::date AS start_date \gset
SELECT (date_trunc('month', CURRENT_DATE) - INTERVAL '1 day')::date AS end_date \gset

\echo 'Default report start date: ' :start_date
\echo 'Default report end date:   ' :end_date

-- Prompt to override the default dates
\prompt 'Press Enter to accept default start date, or type a new start date (YYYY-MM-DD): ' start_date_input
\prompt 'Press Enter to accept default end date, or type a new end date (YYYY-MM-DD): ' end_date_input

SELECT (:'start_date_input' <> '') AS override_start \gset
\if :override_start
    \set start_date :start_date_input
\endif

SELECT (:'end_date_input' <> '') AS override_end \gset
\if :override_end
    \set end_date :end_date_input
\endif

-- Validate start_date and end_date values
\set ON_ERROR_STOP off

SELECT to_date(:'start_date', 'YYYY-MM-DD')::text AS valid_start \gset
\set start_error :ERROR

\if :start_error
  \echo 'ERROR: Invalid start date [' :start_date ']. Must be a real date in YYYY-MM-DD format.'
  \quit
\endif

SELECT to_date(:'end_date', 'YYYY-MM-DD')::text AS valid_end \gset
\set end_error :ERROR

\if :end_error
  \echo 'ERROR: Invalid end date [' :end_date ']. Must be a real date in YYYY-MM-DD format.'
  \quit
\endif

\set ON_ERROR_STOP on

-- Generate the CSV filename. Format = deleted_draft_accounts_YYYY-MM-DD_YYYY-MM-DD.csv
\set outfile 'deleted_draft_accounts_' :start_date '_' :end_date '.csv'

-- Display values and prompt to continue
\echo
\echo 'Using start_date =' :start_date ', end_date =' :end_date
\echo 'CSV file name: ' :outfile
\echo
\echo 'WARNING: If a file with the same name exists, it will be overwritten.'
\echo '         The CSV file should always be checked. If an error occurs a CSV file may be produced but with partial information.'
\echo

\prompt 'Press Enter to continue, or type X to cancel:' continue_input

SELECT (lower(:'continue_input') LIKE 'x%') AS cancel_flag \gset

\if :cancel_flag
  \echo 'Cancelled by user.'
  \quit
\endif

-- Create temporary view for the report SQL, with columns in the order as they should appear in the CSV file
\echo 'Generating report CSV file...'

DROP VIEW IF EXISTS tv_da_deletion_tactical_report;

CREATE TEMP VIEW tv_da_deletion_tactical_report AS (
    SELECT bu.business_unit_name                                   AS "Business unit"
         , da.account_type                                         AS "Account type"
         , CASE WHEN (account::jsonb) @@ '$.defendant.company_flag == true'
                THEN account -> 'defendant' ->> 'company_name'
                ELSE (account -> 'defendant' ->> 'forenames') || ' ' || (account -> 'defendant' ->> 'surname')
           END                                                     AS "Defendant Name"
         , to_date(account -> 'defendant' ->> 'dob', 'YYYY-MM-DD') AS "Date of birth"
         , account ->> 'originator_name'                           AS "Sending area / LJA"
         , account ->> 'prosecutor_case_reference'                 AS "PCR"
         , SUM((impositions ->> 'amount_imposed')::NUMERIC - (impositions ->> 'amount_paid')::NUMERIC) AS "Balance"
         , da.submitted_by_name                                    AS "Created by"
         , to_char(da.created_date, 'YYYY-MM-DD HH24:MI:SS')       AS "Created date"            --Using TO_CHAR to ensure the formatting (i.e. don't output TZ offset)
         , timeline_deleted.tl_elem ->> 'username'                 AS "Deleted by"
         , to_date(timeline_deleted.tl_elem ->> 'status_date', 'YYYY-MM-DD') AS "Date deleted"  --Defined as DATE in timelineData.json schema
         , timeline_deleted.tl_elem ->> 'reason_text'              AS "Reason deleted"
      FROM draft_accounts da
      JOIN business_units bu 
        ON bu.business_unit_id = da.business_unit_id 
      CROSS JOIN LATERAL jsonb_array_elements(da.account::jsonb -> 'offences') AS offences
      CROSS JOIN LATERAL jsonb_array_elements(offences -> 'impositions') AS impositions
      LEFT JOIN LATERAL (SELECT tl_elem
                           FROM jsonb_array_elements(da.timeline_data::jsonb) AS tl(tl_elem)
                          WHERE tl_elem ->> 'status' = 'Deleted'
                         LIMIT 1
                        ) timeline_deleted ON TRUE
     WHERE da.account_status = 'DELETED'
       AND da.account_status_date >= to_timestamp(:'start_date' || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS')
       AND da.account_status_date <= to_timestamp(:'end_date'   || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS')
     GROUP BY bu.business_unit_name, da.account_type, "Defendant Name", "Date of birth", "Sending area / LJA", "PCR"
            , da.submitted_by_name, da.created_date, timeline_deleted.tl_elem, da.account_status_date
     ORDER BY "Date deleted", da.account_status_date
);

-- Direct output to the CSV file, write header, query results and footer to the CSV file
\o :outfile
\qecho :report_security_classification

-- Append the data with column headers to the CSV file
\copy (SELECT * FROM tv_da_deletion_tactical_report) TO STDOUT WITH (FORMAT csv, HEADER true)

-- Append footer to CSV file
\qecho :report_security_classification
\o

\echo 'Done. Please check the generated CSV file: ' :outfile