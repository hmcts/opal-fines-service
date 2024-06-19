/**
* OPAL Program
*
* MODULE      : interface_files.sql
*
* DESCRIPTION : Create the table INTERFACE_FILES in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    -------      --------    ---------------------------------------------------------------------------------------------------------
* 06/06/2024    I Readman    1.0         PO-356 Create the table INTERFACE_FILES in the Fines model
*
**/
CREATE TABLE interface_files
(
 interface_file_id     bigint        not null
,business_unit_id      smallint      not null
,interface_name        varchar(100)  not null
,file_name             varchar(30)   not null
,created_date          timestamp     not null
,file_content          text     
,CONSTRAINT interface_files_pk PRIMARY KEY 
 (
  interface_file_id	
 )  
);

ALTER TABLE interface_files
ADD CONSTRAINT if_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN interface_files.interface_file_id IS 'Unique ID of this record';
COMMENT ON COLUMN interface_files.business_unit_id IS 'ID of the business unit that this file relates to';
COMMENT ON COLUMN interface_files.interface_name IS 'Interface name';
COMMENT ON COLUMN interface_files.file_name IS 'The name of the file';
COMMENT ON COLUMN interface_files.created_date IS 'Date the file was created';
COMMENT ON COLUMN interface_files.file_content IS 'The file content';
