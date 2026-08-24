/**
 * OPAL Program
 *
 * MODULE      : insert_into_courts.sql
 *
 * DESCRIPTION : Insert rows of data into COURTS table for the Integration Tests
 *
 * VERSION HISTORY:
 *
 * Date          Author       Version     Nature of Change
 * ----------    --------     --------    ----------------------------------------------------------------
 * 23/05/2025    R DODD        1.0         Insert rows of data into COURTS table for the Integration Tests
 * 29/05/2025    Updated       1.1         Added court_type and division values, changed business_unit_id
 *
 **/

INSERT INTO business_units
(
business_unit_id,business_unit_name,business_unit_code,business_unit_type,
account_number_prefix,parent_business_unit_id,opal_domain,welsh_language
)
VALUES
(
991,'Business Unit 99','BU99',
 'Area', 'XX',NULL,
 'Fines',false
);

INSERT INTO courts
(
court_id, business_unit_id, court_code, parent_court_id, name, name_cy
, address_line_1, address_line_2, address_line_3
, address_line_1_cy, address_line_2_cy, address_line_3_cy, postcode
, local_justice_area_id, national_court_code, gob_enforcing_court_code
, lja, court_type, division, session
, start_time, max_load, record_session_times, max_court_duration, group_code
)
VALUES
(
000000000007, 991, 007, NULL, 'AAA Test Court', NULL
, 'TestVille', 'TestShire', NULL
, NULL, NULL, NULL, NULL
, 1013, NULL, NULL
, 1013, 'MC', '01', NULL
, NULL, NULL, NULL, NULL, NULL
);
