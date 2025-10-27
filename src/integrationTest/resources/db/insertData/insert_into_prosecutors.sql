/**
* OPAL Program
*
* MODULE      : insert_into_prosecutors.sql
*
* DESCRIPTION : Insert test data for prosecutors integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------------
* 30/06/2025    R DODD        1.0         PO1787 Insert test data for prosecutors integration tests
* 15/10/2025    P Brumby      1.1         PO1722 Change test data for prosecutors integration tests so prosecutor_id does not clash with reference data
* 24/10/2025    T Gonella     1.2         PO2319 insert test data for prosecutor address line length change
*
**/

INSERT INTO prosecutors
(
prosecutor_id, name, prosecutor_code
,address_line_1, address_line_2, address_line_3
,address_line_4, address_line_5, postcode
,end_date
)
VALUES
(
001111, 'AA1 Chief Prosecutor', 'AA01'
, '9 Prosecutor Street', 'Prosecutorville', 'Prosecutorton'
, NULL, NULL, 'PR01 2PR'
, NULL
),
(
002222, 'AA2 Assistant Prosecutor', 'AA02'
, '49 Prosecutor Street', 'Prosecutorville', 'Prosecutorton'
, NULL, NULL, 'PR01 4PR'
, '2099-11-24 10:00:00'
),
(
003333, 'AA3 Junior Prosecutor', 'AA03'
, '99 Prosecutor Street', 'Prosecutorville', 'Prosecutorton'
, NULL, NULL, 'PR01 9PR'
, '2009-04-16 20:00:00'
),
(
009990, 'AA4 Boundary Prosecutor', 'AA04',
'123456789012345678901234567890123456789012345678901234567890',  -- 60 chars
'Boundaryville', 'Boundaryton',
NULL, NULL, 'PR06 0PR',
NULL
);

