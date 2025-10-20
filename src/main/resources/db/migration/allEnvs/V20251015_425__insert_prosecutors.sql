/**
* CGI OPAL Program
*
* MODULE      : insert_prosecutors.sql
*
* DESCRIPTION : Insert reference data into PROSECUTORS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------
* 09/10/2025    P Brumby    1.1         PO-1722 - Populate PROSECUTORS table with its reference data
*
**/

DELETE FROM prosecutors;

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(1,'Met Camera Processing Services / Traffic Offence Reports','001','PO Box 510','London',NULL,NULL,NULL,'DA15 0BQ',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(2,'Cumbria Central Ticket Office','003','PO Box 239','Penrith',NULL,NULL,NULL,'CA11 1EG',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(3,'Lancashire Safer Roads Unit','004','PO Box 1329','Preston',NULL,NULL,NULL,'PR2 0SX',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(4,'Merseyside Safer Roads Unit','005','PO Box 2122','Liverpool',NULL,NULL,NULL,'L3 3YW',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(5,'Manchester Central Ticket Office','006','Specialist Operations','PO Box 423','Hyde',NULL,NULL,'SK14 9DU',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(6,'Cheshire Central Ticket Office','007','PO Box 266','Winsford',NULL,NULL,NULL,'CW7 9FN',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(7,'Northumbria Criminal Justice Fixed Penalty Unit','010','PO Box 213','Bedlington',NULL,NULL,NULL,'NE63 3EU',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(8,'Durham Road Safety Bureau','011','Spennymoor Police Station','Wesleyan Road','County Durham',NULL,NULL,'DL16 6FB',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(9,'North Yorkshire Traffic Bureau','012','PO Box 809','York',NULL,NULL,NULL,'YO31 6DG',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(10,'West Yorkshire Prosecutions and Casualty Prevention Unit','013','Camera Process Unit','PO Box 1376','Bradford',NULL,NULL,'BD5 5EX',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(11,'South Yorkshire Police','014','PO Box 767','Maltby','Rotherham',NULL,NULL,'S66 6BD',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(12,'Humberside Police Central Ticket Office','016','PO Box 183','Beverley',NULL,NULL,NULL,'HU17 8GW',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(13,'Cleveland Police Central Ticket Office','017','St Marks House','St Marks Court','Thornaby','Stockton-on-Tees',NULL,'TS17 6QW',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(14,'West Midlands Central Ticket Office','020','PO Box 94','Birmingham',NULL,NULL,NULL,'B4 6ER',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(15,'Staffordshire Police Fixed Penalty Dept','021','Cannock Road','Stafford',NULL,NULL,NULL,'ST17 0QG',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(16,'West Mercia Camera Ticket Office','022','PO Box 25','Droitwich',NULL,NULL,NULL,'WR9 8UF',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(17,'Warwickshire Camera Enforcement Unit','023','PO Box 3273','Rugby',NULL,NULL,NULL,'CV21 2XT',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(18,'Derbyshire Constabulary','030','Casualty Reduction Enforcement Support Team','PO Box 6061','Ripley','Derbyshire',NULL,'DE5 3XB',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(19,'Nottinghamshire Central Police Station','031','Castle','Nottingham',NULL,NULL,NULL,'NG1 6HS',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(20,'Lincolnshire Traffic Process Unit','032','PO Box 999','Lincoln',NULL,NULL,NULL,'LN5 7PH',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(21,'Leicestershire Police Headquarters','033','St Johns','Leicester',NULL,NULL,NULL,'LE19 2BX',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(22,'Northamptonshire Central Ticket Office','034','Darby House','Darby Close','Wellingborough',NULL,NULL,'NN8 6GS',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(23,'Cambridgeshire Police Headquarters','035','Huntington','Cambridge',NULL,NULL,NULL,'PE29 6NP',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(24,'Norfolk Central Ticket Office','036','Norfolk Constabulary','PO Box 3293','Norfolk',NULL,NULL,'NR7 7ET',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(25,'Suffolk Central Ticket Office','037','Norfolk Constabulary','PO Box 3293','Norfolk',NULL,NULL,'NR7 7ET',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(26,'Bedfordshire Police Headquarters','040','Woburn Road','Kempston','Bedford',NULL,NULL,'MK43 9AX',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(27,'Essex Police Road Policing Support','042','PO Box 7807','Billericay',NULL,NULL,NULL,'CM12 9WF',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(28,'Hertfordshire Camera and Ticket Office ','041','PO Box 486','Stevenage','Hertfordshire',NULL,NULL,'SG1 9JB',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(29,'Thames Valley Fixed Penalty Support Unit','043','PO Box 156','Banbury',NULL,NULL,NULL,'OX16 2UX',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(30,'Hampshire Summary Justice Unit','044','PO Box 112','Winchester',NULL,NULL,NULL,'SO23 7YY',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(31,'Surrey Police','045','PO Box 930','Guildford','Surrey',NULL,NULL,'GU4 8WU',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(32,'Kent Traffic Enforcement Team','046','Kent Police','Church Road','Ashford','Kent',NULL,'TN23 1BT',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(33,'Sussex Camera and Ticket Process Team','047','PO Box 2106','West Sussex',NULL,NULL,NULL,'BN43 6WW',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(34,'City of London Central Ticket Office','048','PO Box 510','London',NULL,NULL,NULL,'SW1V 2JP',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(35,'Devon and Cornwall Road Safety Team','050','PO Box 206','Plymouth',NULL,NULL,NULL,'PL6 5WY',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(36,'Avon and Somerset Road Safety Support Unit','052','Police and Fire Headquarters','Valley Road','Portishead','North Somerset',NULL,'BS20 8QJ',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(37,'Gloucestershire Camera Enforcement','053','Criminal Justice Department','No 1 Waterwells, Waterwells Drive','Quedgeley','Gloucester',NULL,'GL2 2AN',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(38,'Wiltshire Justice Traffic','054','Gablecross Police Station','Swindon',NULL,NULL,NULL,'SN3 4RB',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(39,'Dorset Central Ticket Office','055','Winfrith','Dorchester','Dorset',NULL,NULL,'DT2 8DZ',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(40,'North Wales Central Ticket Office','060','Glyn','Colwyn Bay','Wales',NULL,NULL,'LL29 8AW',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(41,'Gwent Safety Camera Unit','061','PO Box 95','Treforest','Pontypridd',NULL,NULL,'CF37 9DH',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(42,'South Wales Central Ticket Office','062','7 Graig-Y-Forest','Treforest','Pontypridd',NULL,NULL,'CF37 1UJ',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(43,'Dyfed-Powys Central Ticket Office','063','PO Box 999','Llangunnor','Carmarthen',NULL,NULL,'SA31 2PF',NULL);

INSERT INTO prosecutors (prosecutor_id, name, prosecutor_code, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, end_date) VALUES 
(44,'British Transport Police Justice Department','093','Central London Police Station','16-24 Whitfield Street','London',NULL,NULL,'W1T 2RA',NULL);