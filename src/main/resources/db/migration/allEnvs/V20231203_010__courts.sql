/**
* CGI OPAL Program
*
* MODULE      : courts.sql
*
* DESCRIPTION : Creates the COURTS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/12/2023    A Dennis    1.0         PO-127 Creates the COURTS table for the Fines model
*
**/
CREATE TABLE courts 
(
 court_id                bigint       not null
,business_unit_id        smallint     not null
,court_code              smallint     not null
,parent_court_id         bigint
,name                    varchar(35)  not null
,name_cy                 varchar(35)
,address_line_1          varchar(35)
,address_line_2          varchar(35)
,address_line_3          varchar(35)
,address_line_1_cy       varchar(35)
,address_line_2_cy       varchar(35)
,address_line_3_cy       varchar(35)
,postcode                varchar(8)
,local_justice_area_id   smallint      not null
,national_court_code     varchar(7)
,CONSTRAINT court_id_pk PRIMARY KEY 
 (
   court_id	
 ) 
);

ALTER TABLE courts
ADD CONSTRAINT crt_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE courts
ADD CONSTRAINT crt_parent_court_id_fk FOREIGN KEY
(
  parent_court_id 
)
REFERENCES courts
(
  court_id 
);

COMMENT ON COLUMN courts.court_id IS 'Unique ID of this record';
COMMENT ON COLUMN courts.business_unit_id IS 'ID of the relating till to which this till belongs';
COMMENT ON COLUMN courts.court_code IS 'Court code unique within the business unit';
COMMENT ON COLUMN courts.parent_court_id IS 'ID of parent court for enforcement/admin purposes';
COMMENT ON COLUMN courts.name IS 'Court name';
COMMENT ON COLUMN courts.name_cy IS 'Court name in welsh';
COMMENT ON COLUMN courts.address_line_1 IS 'Court address line 1';
COMMENT ON COLUMN courts.address_line_2 IS 'Court address line 2';
COMMENT ON COLUMN courts.address_line_3 IS 'Court address line 3, not stored in legacy GoB';
COMMENT ON COLUMN courts.address_line_1_cy IS 'Court address line 1 in welsh';
COMMENT ON COLUMN courts.address_line_2_cy IS 'Court address line 2 in welsh';
COMMENT ON COLUMN courts.address_line_3_cy IS 'Court address line 3 in welsh, not stored in legacy GoB';
COMMENT ON COLUMN courts.postcode IS 'Court postcode, not stored in legacy GoB';
COMMENT ON COLUMN courts.local_justice_area_id IS 'Local justice area ID';
COMMENT ON COLUMN courts.national_court_code IS 'National court location code (OU code). New field for future development with Common Platform';
