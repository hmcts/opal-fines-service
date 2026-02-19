/**
* CGI OPAL Program
*
* MODULE      : insert_into_local_justice_area.sql
*
* DESCRIPTION : Load the Local Justice Area table with reference data for the Integration Tests
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------
* 05/05/2024    R DODD      1.0         PO-1047 Load the Local Justice Area table with reference data for the Integration Tests
*
**/

INSERT INTO local_justice_areas
(
local_justice_area_id, name
, address_line_1, address_line_2, address_line_3
, postcode, lja_code, lja_type, address_line_4, address_line_5, end_date
)
VALUES
(
001,  'AAAA Trial Court'
, 'Alpha Trial Courts', 'Court Quarter', '666 Trial Street'
, 'TR12 1TR', '0007', 'TYPE_1', 'Trialton', 'Trialshire', NULL
),

(
  010,  'BBBB Trial Court'
, 'Beta Trial Courts', 'Court Quarter', '666 Trial Street'
, 'TR12 1TR', '0008', 'TYPE_2', 'Trialton', 'Trialshire', NULL
),

(
  020,  'CCCC Trial Court'
, 'Charlie Trial Courts', 'Court Quarter', '666 Trial Street'
, 'TR12 1TR', '0009', 'TYPE_2', 'Trialton', 'Trialshire', NULL
);
