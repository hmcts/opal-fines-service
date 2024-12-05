/**
* CGI OPAL Program
*
* MODULE      : enforcers.sql
*
* DESCRIPTION : Creates the ENFORCERS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the ENFORCERS table for the Fines model
*
**/
CREATE TABLE enforcers 
(
 enforcer_id                   bigint       not null
,business_unit_id              smallint     not null
,enforcer_code                 smallint     not null
,name                          varchar(35)  not null
,name_cy                       varchar(35)
,address_line_1                varchar(35)
,address_line_2                varchar(35)
,address_line_3                varchar(35)
,address_line_1_cy             varchar(35)
,address_line_2_cy             varchar(35)
,address_line_3_cy             varchar(35)
,postcode                      varchar(8)
,warrant_reference_sequence    varchar(20)
,warrant_register_sequence     integer
,CONSTRAINT enforcer_id_pk PRIMARY KEY 
 (
   enforcer_id	
 ) 
);

ALTER TABLE enforcers
ADD CONSTRAINT enf_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN enforcers.enforcer_id IS 'Unique ID of this record';
COMMENT ON COLUMN enforcers.business_unit_id IS 'ID of the relating till to which this till belongs';
COMMENT ON COLUMN enforcers.enforcer_code IS 'Enforcer code unique within the business unit';
COMMENT ON COLUMN enforcers.name IS 'Enforcer name';
COMMENT ON COLUMN enforcers.name_cy IS 'Enforcer name in welsh';
COMMENT ON COLUMN enforcers.address_line_1 IS 'Enforcer address line 1';
COMMENT ON COLUMN enforcers.address_line_2 IS 'Enforcer address line 2';
COMMENT ON COLUMN enforcers.address_line_3 IS 'Enforcer address line 3';
COMMENT ON COLUMN enforcers.address_line_1_cy IS 'Enforcer address line 1 in welsh';
COMMENT ON COLUMN enforcers.address_line_2_cy IS 'Enforcer address line 2 in welsh';
COMMENT ON COLUMN enforcers.address_line_3_cy IS 'Enforcer address line 3 in welsh';
COMMENT ON COLUMN enforcers.postcode IS 'Enforcer postcode';
COMMENT ON COLUMN enforcers.warrant_reference_sequence IS 'Last generated warrant reference for this enforcer';
COMMENT ON COLUMN enforcers.warrant_register_sequence IS 'Last generated warrant register serial number for this enforcer';
