/**
* CGI OPAL Program
*
* MODULE      : offences.sql
*
* DESCRIPTION : Creates the OFFENCES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/04/2024    A Dennis    1.0         PO-200 Creates the OFFENCES table for the Fines model
*
**/
CREATE TABLE offences 
(
 offence_id                  smallint       not null
,cjs_code                    varchar(10)    not null
,business_unit_id            smallint       not null
,offence_title               varchar(120)   not null
,offence_title_cy            varchar(120)   not null
,CONSTRAINT offences_pk PRIMARY KEY 
 (
   offence_id	
 ) 
);

ALTER TABLE offences
ADD CONSTRAINT off_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN offences.offence_id IS 'Unique ID of this record';
COMMENT ON COLUMN offences.cjs_code IS 'Offence cjs code';
COMMENT ON COLUMN offences.business_unit_id IS 'Indicates the area in which this is a local offence. NULL for national offences.';
COMMENT ON COLUMN offences.offence_title IS 'Offence title';
COMMENT ON COLUMN offences.offence_title_cy IS 'Offence title in Welsh';
