/**
* OPAL Program
*
* MODULE      : insert_prison_data.sql
*
* DESCRIPTION : Insert data into PRISONS table
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 17/05/2025    C Cho        1.0         PO-940 Insert reference data for prisons
*
**/
INSERT INTO prisons (
    prison_id, business_unit_id, prison_code, name, 
    address_line_1, address_line_2, address_line_3, postcode
) VALUES
(50000000001, 5, 'CARD', 'Cardiff', 'Knox Road', 'Cardiff', ' ', 'CF24 OUG'),
(50000000002, 5, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(50000000003, 5, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(260000000021, 26, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(260000000022, 26, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(260000000023, 26, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(300000000001, 30, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(300000000002, 30, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(300000000003, 30, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(470000000001, 47, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(470000000002, 47, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(470000000003, 47, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(570000000001, 57, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(570000000002, 57, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(570000000003, 57, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(650000000001, 65, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(650000000002, 65, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(650000000003, 65, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(780000000001, 78, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(780000000002, 78, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(780000000003, 78, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(800000000001, 80, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(800000000002, 80, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(800000000003, 80, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(820000000001, 82, 'CARD', 'Cardiff', 'Knox Road', 'Cardiff', ' ', 'CF24 OUG'),
(820000000002, 82, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(820000000003, 82, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(1030000000001, 103, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(1030000000002, 103, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(1030000000003, 103, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(890000000001, 89, 'CARD', 'Cardiff', 'Knox Road', 'Cardiff', ' ', 'CF24 OUG'),
(890000000002, 89, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(890000000003, 89, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(1060000000001, 106, 'CARD', 'Cardiff', 'Knox Road', 'Cardiff', ' ', 'CF24 OUG'),
(1060000000002, 106, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(1060000000003, 106, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(600000000001, 60, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(600000000002, 60, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(600000000003, 60, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(360000000001, 36, 'CARD', 'Cardiff', 'Knox Road', 'Cardiff', ' ', 'CF24 OUG'),
(360000000002, 36, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(360000000003, 36, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE'),
(730000000001, 73, 'BELM', 'Belmarsh', 'Western Way', 'Thamesmead', 'London', 'SE28 0EB'),
(730000000002, 73, 'READ', 'Reading', 'Forbury Road', 'Reading', 'Berkshire', 'RG1 3HY'),
(730000000003, 73, 'WORM', 'Wormwood Scubs', 'P.O.Box 757', 'Du Cane Road', 'London', 'W12 OAE');
