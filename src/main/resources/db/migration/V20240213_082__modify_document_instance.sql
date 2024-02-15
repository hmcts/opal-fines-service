/**
* CGI OPAL Program
*
* MODULE      : modify_document_instance.sql
*
* DESCRIPTION : Change document_instances.document_id from BIGINT varchar(10) to match what is in the DOCUMENTS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------
* 13/02/2024    A Dennis    1.0         PO-195 Change document_instances.document_id from BIGINT varchar(10) to match what is in the DOCUMENTS table for the Fines model
*
**/
ALTER TABLE document_instances
ALTER COLUMN document_id TYPE VARCHAR(10);

ALTER TABLE document_instances
ADD CONSTRAINT di_document_id_fk FOREIGN KEY
(
  document_id 
)
REFERENCES documents
(
  document_id 
);
