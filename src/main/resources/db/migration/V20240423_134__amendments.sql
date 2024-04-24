/**
* OPAL Program
*
* MODULE      : amendments.sql
*
* DESCRIPTION : Create the table AMENDMENTS in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 23/04/2024    A Dennis    1.0         PO-284 Create the table AMENDMENTS in the Fines model
*
**/
CREATE TABLE amendments 
(
 amendment_id               bigint       not null
,business_unit_id           smallint     not null
,associated_record_type     varchar(30)  not null
,associated_record_id        varchar(30)  not null
,amended_date               timestamp    not null
,amended_by                 varchar(20)  not null
,field_code                 smallint     not null
,old_value                  varchar(200)  
,new_value                  varchar(200)
,case_reference             varchar(40)
,function_code              varchar(30)
,CONSTRAINT amendments_pk PRIMARY KEY 
 (
  amendment_id	
 )  
);

ALTER TABLE amendments
ADD CONSTRAINT amdt_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN amendments.amendment_id IS 'Unique ID of this record';
COMMENT ON COLUMN amendments.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN amendments.associated_record_type IS 'ID of the account amended';
COMMENT ON COLUMN amendments.associated_record_id IS 'ID of the creditor account amended';
COMMENT ON COLUMN amendments.amended_date IS 'Date the amendment was made';
COMMENT ON COLUMN amendments.amended_by IS 'ID of the user that made the amendment';
COMMENT ON COLUMN amendments.field_code IS 'The fied modified';
COMMENT ON COLUMN amendments.old_value IS 'Field value before amendment';
COMMENT ON COLUMN amendments.new_value IS 'Field value after amendment';
COMMENT ON COLUMN amendments.case_reference IS 'Case number if modified by Case Management';
COMMENT ON COLUMN amendments.function_code IS 'Function from which the amendment was made';
