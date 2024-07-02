/**
* OPAL Program
*
* MODULE      : result_documents.sql
*
* DESCRIPTION : Create the RESULT_DOCUMENTS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-399 Create the RESULT_DOCUMENTS table in the Fines model
*
**/ 

CREATE TABLE result_documents
(
 result_document_id    bigint
,result_id             varchar(6)     not null 
,document_id           varchar(10)    not null
,cy_document_id        varchar(10)    not null
,CONSTRAINT result_documents_pk PRIMARY KEY (result_document_id)
,CONSTRAINT rd_result_id_fk FOREIGN KEY (result_id) REFERENCES results (result_id)
,CONSTRAINT rd_document_id_fk FOREIGN KEY (document_id) REFERENCES documents (document_id)
,CONSTRAINT rd_cy_document_id_fk FOREIGN KEY (cy_document_id) REFERENCES documents (document_id)
);

COMMENT ON COLUMN result_documents.result_document_id IS 'Unique ID of this record';
COMMENT ON COLUMN result_documents.result_id IS 'The result the document is associated with';
COMMENT ON COLUMN result_documents.document_id IS 'The document associated with the result';
COMMENT ON COLUMN result_documents.cy_document_id IS 'The welsh version of the associated document if required';