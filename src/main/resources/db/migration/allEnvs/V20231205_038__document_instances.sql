/**
* CGI OPAL Program
*
* MODULE      : document_instances.sql
*
* DESCRIPTION : Creates the DOCUMENT_INSTANCES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 05/12/2023    A Dennis    1.0         PO-127 Creates the DOCUMENT_INSTANCES table for the Fines model
*
**/
CREATE TABLE document_instances 
(
 document_instance_id    bigint       not null
,document_id             bigint       not null
,business_unit_id        smallint     not null
,generated_date          timestamp    not null
,generated_by            varchar(20)  not null
,content                 xml          not null
,CONSTRAINT document_instances_pk PRIMARY KEY 
 (
   document_instance_id	
 ) 
);

COMMENT ON COLUMN document_instances.document_instance_id IS 'Unique ID for this record';
COMMENT ON COLUMN document_instances.document_id IS 'ID of the report being generated';
COMMENT ON COLUMN document_instances.business_unit_id IS 'ID of the business unit this report instance was generated for';
COMMENT ON COLUMN document_instances.generated_date IS 'The date the document was generated';
COMMENT ON COLUMN document_instances.generated_by IS 'ID of the user that generated this instance of the document';
COMMENT ON COLUMN document_instances.content IS 'The structured document content';
