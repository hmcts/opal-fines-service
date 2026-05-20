/**
* OPAL Program
*
* MODULE      : exported_court_diff_insert.sql
*
* DESCRIPTION : Insert and update court records to align with the courts in the NLE environments.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 19/05/2026    C Faulkner  1.0         PO-2957 Reference Data Extract & Environment Parity Verification
*
**/

----- ****** go into devOnly, stgOnly, demoOnly, pertfTestOnly, ithcOnly **** -------------
UPDATE courts SET address_line_2 = 'Swansea City' WHERE court_id = 360000000001;
UPDATE courts SET name = 'Merthyr Tydfill CF' WHERE court_id = 360000000023;
UPDATE courts SET address_line_1 = '123 The High Road' WHERE court_id = 730000000042;

--INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (360000000001, 36, 101, 'Aberdare 101', '123 Aberdare', 'Swansea City', NULL, NULL, NULL);
--INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (360000000023, 36, 123, 'Merthyr Tydfill CF', 'Merthyr Tydfill', 'Merthyr Tydfill', NULL, NULL, NULL);
--INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (730000000042, 73, 249, 'Johns 249 Maintenance Court', '123 The High Road', 'The Town', NULL, NULL, NULL);
INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (730000000124, 73, 110, '110 Enforcement Court', '110 Enff Court', 'South West London', NULL, NULL, NULL);
INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (730000000125, 73, 828, '828 For ATCM Fines', 'Address 1', 'Address 2', NULL, NULL, NULL);
INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (730000000126, 73, 248, 'John''s Test Maint 248 Court', 'Maint Court Addres Line 1', 'Maint Court Address Line 2', NULL, NULL, NULL);
INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (730000000146, 73, 251, 'Maint Court 251', 'The Court House 251', 'West London', NULL, NULL, NULL);
INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (730000000147, 73, 998, 'Maint Court 998', 'Maint Court 998', 'The Court House Address Line 2', NULL, NULL, NULL);
INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (730000000166, 73, 104, 'KAYAS COURT TEST', 'Westway', 'London', NULL, NULL, NULL);
INSERT INTO courts (court_id, business_unit_id, court_code, name, address_line_1, address_line_2, address_line_3, postcode, national_court_code) VALUES (730000000167, 73, 167, 'DAVID TEST COURT', NULL, NULL, NULL, NULL, NULL);
