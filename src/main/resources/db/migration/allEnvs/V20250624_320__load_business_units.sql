/**
* OPAL Program
*
* MODULE      : load_business_units.sql
*
* DESCRIPTION : Load business_units table with reference data
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------------------------
* 24/06/2025    C Cho       1.0         PO-1020 Load business_units table with reference data
*
**/

DELETE FROM business_units;

INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1042,'Dyfed Powys','Area','63',NULL,NULL,NULL,NULL,TRUE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1041,'South Wales','Area','62',NULL,NULL,NULL,NULL,TRUE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1040,'Gwent','Area','61',NULL,NULL,NULL,NULL,TRUE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1039,'North Wales','Area','60',NULL,NULL,NULL,NULL,TRUE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1038,'Dorset','Area','55',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1037,'Wiltshire','Area','54',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1036,'Gloucestershire','Area','53',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1035,'Avon and Somerset','Area','52',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1034,'Devon and Cornwall','Area','50',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1033,'Sussex','Area','47',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1032,'Kent','Area','46',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1031,'Surrey','Area','45',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1030,'Hampshire and Isle of Wight','Area','44',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1029,'Thames Valley','Area','43',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1028,'Essex','Area','42',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1027,'Hertfordshire','Area','41',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1026,'Bedfordshire','Area','40',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1025,'Suffolk','Area','37',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1024,'Norfolk','Area','36',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1023,'Cambridgeshire','Area','35',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1022,'Northamptonshire','Area','34',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1021,'Leicestershire','Area','33',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1020,'Lincolnshire','Area','32',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1019,'Nottinghamshire','Area','31',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1018,'Derbyshire','Area','30',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1017,'Warwickshire','Area','23',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1016,'West Mercia','Area','22',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1015,'Staffordshire','Area','21',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1014,'West Midlands','Area','20',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1013,'Cleveland','Area','17',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1012,'Humberside','Area','16',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1011,'South Yorkshire','Area','14',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1010,'West Yorkshire','Area','13',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1009,'North Yorkshire','Area','12',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1008,'Durham','Area','11',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1007,'Northumbria','Area','10',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1006,'Cheshire','Area','07',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1005,'Greater Manchester','Area','06',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1004,'Merseyside','Area','05',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1003,'Lancashire','Area','04',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1002,'Cumbria','Area','03',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (1001,'Greater London','Area','01',NULL,NULL,NULL,NULL,FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (139,'North Cheshire (Div 1)','Accounting Division','0139',NULL,'CH',1006,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (138,'Wiltshire','Accounting Division','0138',NULL,'WI',1037,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (135,'Bradford, Keighley & Halifax','Accounting Division','0135',NULL,'WY',1010,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (130,'Birmingham & Sutton','Accounting Division','0130',NULL,'BS',1014,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (129,'West Mercia','Accounting Division','0129',NULL,'TF',1016,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (128,'Warwickshire','Accounting Division','0128',NULL,'WA',1017,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (126,'East  Sussex','Accounting Division','0123',NULL,'BM',1033,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (125,'Surrey','Accounting Division','0122',NULL,'SR',1031,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (124,'Sheffield','Accounting Division','0117',NULL,'SY',1011,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (119,'Nottingham','Accounting Division','0110',NULL,'NT',1019,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (116,'Tynedale','Accounting Division','0107',NULL,'EM',1007,'Confiscation',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (113,'North Tyneside','Accounting Division','0104',NULL,'CO',1007,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (112,'Newcastle, Anderson + Broadacre','Accounting Division','0103',NULL,'NU',1007,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (110,'Gateshead','Accounting Division','0101',NULL,'C2',1007,'Confiscation',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (109,'Berwick','Accounting Division','0100',NULL,'CU',1007,'Confiscation',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (107,'Alnwick','Accounting Division','0098',NULL,'CU',1007,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (106,'North Wales','Accounting Division','0094',NULL,'NW',1039,'Fines',TRUE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (105,'Liverpool','Accounting Division','0090',NULL,'MD',1004,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (103,'Pennine','Accounting Division','0080',NULL,'LA',1003,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (99,'West Kent','Accounting Division','0076',NULL,'WK',1032,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (97,'Confiscation','Accounting Division','0074',NULL,'CO',1032,'Confiscation',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (96,'South Bank','Accounting Division','0073',NULL,'HR',1012,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (92,'S + SE Hampshire','Accounting Division','0066',NULL,'HA',1030,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (89,'Gwent','Accounting Division','0063',NULL,'GW',1040,'Fines',TRUE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (82,'Bolton','Accounting Division','0056',NULL,'GM',1005,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (80,'Westminster - North (Wells Street)','Accounting Division','0049',NULL,'HD',1001,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (78,'N E Region','Accounting Division','0047',NULL,'TH',1001,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (77,'Camberwell Green','Accounting Division','0046',NULL,'CG',1001,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (73,'West London','Accounting Division','0042',NULL,'WL',1001,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (66,'CAO London Confiscation Orders','Accounting Division','0035',NULL,'66',1001,'Confiscation',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (65,'Camden and Islington','Accounting Division','0034',NULL,'HC',1001,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (61,'Essex','Accounting Division','0023',NULL,'EX',1028,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (60,'Dyfed Powys','Accounting Division','0022',NULL,'DP',1042,'Fines',TRUE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (57,'Plymouth District','Accounting Division','0017',NULL,'PL',1034,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (52,'Bedfordshire','Accounting Division','0008',NULL,'BD',1026,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (47,'Bristol','Accounting Division','0003',NULL,'AS',1035,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (45,'Confiscation','Accounting Division','0055',NULL,'C1',1005,'Confiscation',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (38,'Staffordshire','Accounting Division','0118',NULL,'ST',1015,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (36,'LCIS (South + Mid Glamorgan)','Accounting Division','0113',NULL,'SW',1041,'Fines',TRUE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (31,'Oxfordshire','Accounting Division','0127',NULL,'OX',1029,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (30,'Norwich','Accounting Division','0093',NULL,'NF',1024,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (29,'Northants','Accounting Division','0096',NULL,'NO',1022,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (28,'North Yorkshire','Accounting Division','0095',NULL,'NY',1009,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (26,'Hertfordshire','Accounting Division','0069',NULL,'HE',1027,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (24,'Lowestoft','Accounting Division','0121',NULL,'SF',1025,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (22,'Lincolnshire','Accounting Division','0084',NULL,'LN',1020,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (21,'Leicester','Accounting Division','0082',NULL,'LE',1021,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (14,'Gloucestershire','Accounting Division','0051',NULL,'GL',1036,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (12,'Durham','Accounting Division','0021',NULL,'DU',1008,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (11,'Dorset','Accounting Division','0020',NULL,'10',1038,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (10,'Derbyshire','Accounting Division','0013',NULL,'DY',1018,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (9,'Cumbria','Accounting Division','0012',NULL,'EC',1002,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (8,'Cleveland','Accounting Division','0011',NULL,'CL',1013,'Fines',FALSE);
INSERT INTO business_units (business_unit_id,business_unit_name,business_unit_type,business_unit_code,account_number_prefix,account_number_suffix,parent_business_unit_id,opal_domain,welsh_language)
VALUES (5,'Cambridgeshire','Accounting Division','0009',NULL,'CB',1023,'Fines',FALSE);

UPDATE business_units SET account_number_suffix = 'CB' WHERE business_unit_id = 5;
UPDATE business_units SET account_number_suffix = 'CL' WHERE business_unit_id = 8;
UPDATE business_units SET account_number_suffix = 'EC' WHERE business_unit_id = 9;
UPDATE business_units SET account_number_suffix = 'DY' WHERE business_unit_id = 10;
UPDATE business_units SET account_number_suffix = '10' WHERE business_unit_id = 11;
UPDATE business_units SET account_number_suffix = 'DU' WHERE business_unit_id = 12;
UPDATE business_units SET account_number_suffix = 'GL' WHERE business_unit_id = 14;
UPDATE business_units SET account_number_suffix = 'LE' WHERE business_unit_id = 21;
UPDATE business_units SET account_number_suffix = 'LN' WHERE business_unit_id = 22;
UPDATE business_units SET account_number_suffix = 'SF' WHERE business_unit_id = 24;
UPDATE business_units SET account_number_suffix = 'HE' WHERE business_unit_id = 26;
UPDATE business_units SET account_number_suffix = 'NY' WHERE business_unit_id = 28;
UPDATE business_units SET account_number_suffix = 'NO' WHERE business_unit_id = 29;
UPDATE business_units SET account_number_suffix = 'NF' WHERE business_unit_id = 30;
UPDATE business_units SET account_number_suffix = 'OX' WHERE business_unit_id = 31;
UPDATE business_units SET account_number_suffix = 'SW' WHERE business_unit_id = 36;
UPDATE business_units SET account_number_suffix = 'ST' WHERE business_unit_id = 38;
UPDATE business_units SET account_number_suffix = 'C1' WHERE business_unit_id = 45;
UPDATE business_units SET account_number_suffix = 'AS' WHERE business_unit_id = 47;
UPDATE business_units SET account_number_suffix = 'BD' WHERE business_unit_id = 52;
UPDATE business_units SET account_number_suffix = 'PL' WHERE business_unit_id = 57;
UPDATE business_units SET account_number_suffix = 'DP' WHERE business_unit_id = 60;
UPDATE business_units SET account_number_suffix = 'EX' WHERE business_unit_id = 61;
UPDATE business_units SET account_number_suffix = 'HC' WHERE business_unit_id = 65;
UPDATE business_units SET account_number_suffix = '66' WHERE business_unit_id = 66;
UPDATE business_units SET account_number_suffix = 'WL' WHERE business_unit_id = 73;
UPDATE business_units SET account_number_suffix = 'CG' WHERE business_unit_id = 77;
UPDATE business_units SET account_number_suffix = 'TH' WHERE business_unit_id = 78;
UPDATE business_units SET account_number_suffix = 'HD' WHERE business_unit_id = 80;
UPDATE business_units SET account_number_suffix = 'GM' WHERE business_unit_id = 82;
UPDATE business_units SET account_number_suffix = 'GW' WHERE business_unit_id = 89;
UPDATE business_units SET account_number_suffix = 'HA' WHERE business_unit_id = 92;
UPDATE business_units SET account_number_suffix = 'HR' WHERE business_unit_id = 96;
UPDATE business_units SET account_number_suffix = 'CO' WHERE business_unit_id = 97;
UPDATE business_units SET account_number_suffix = 'WK' WHERE business_unit_id = 99;
UPDATE business_units SET account_number_suffix = 'LA' WHERE business_unit_id = 103;
UPDATE business_units SET account_number_suffix = 'MD' WHERE business_unit_id = 105;
UPDATE business_units SET account_number_suffix = 'NW' WHERE business_unit_id = 106;
UPDATE business_units SET account_number_suffix = 'CU' WHERE business_unit_id = 107;
UPDATE business_units SET account_number_suffix = 'CU' WHERE business_unit_id = 109;
UPDATE business_units SET account_number_suffix = 'C2' WHERE business_unit_id = 110;
UPDATE business_units SET account_number_suffix = 'NU' WHERE business_unit_id = 112;
UPDATE business_units SET account_number_suffix = 'CO' WHERE business_unit_id = 113;
UPDATE business_units SET account_number_suffix = 'EM' WHERE business_unit_id = 116;
UPDATE business_units SET account_number_suffix = 'NT' WHERE business_unit_id = 119;
UPDATE business_units SET account_number_suffix = 'SY' WHERE business_unit_id = 124;
UPDATE business_units SET account_number_suffix = 'SR' WHERE business_unit_id = 125;
UPDATE business_units SET account_number_suffix = 'BM' WHERE business_unit_id = 126;
UPDATE business_units SET account_number_suffix = 'WA' WHERE business_unit_id = 128;
UPDATE business_units SET account_number_suffix = 'TF' WHERE business_unit_id = 129;
UPDATE business_units SET account_number_suffix = 'BS' WHERE business_unit_id = 130;
UPDATE business_units SET account_number_suffix = 'WY' WHERE business_unit_id = 135;
UPDATE business_units SET account_number_suffix = 'WI' WHERE business_unit_id = 138;
UPDATE business_units SET account_number_suffix = 'CH' WHERE business_unit_id = 139;
