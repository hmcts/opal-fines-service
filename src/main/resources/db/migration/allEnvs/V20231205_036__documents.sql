/**
* CGI OPAL Program
*
* MODULE      : documents.sql
*
* DESCRIPTION : Creates the DOCUMENTS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 05/12/2023    A Dennis    1.0         PO-127 Creates the DOCUMENTS table for the Fines model
*
**/
CREATE TABLE documents 
(
 document_id            varchar(10) not null
,recipient              varchar(4)  not null
,document_language      varchar(2)  not null
,signature_source       varchar(4)  not null
,priority               smallint    not null
,header_type            varchar(2)  not null
,document_elements      json        not null
,CONSTRAINT documents_pk PRIMARY KEY 
 (
   document_id	
 ) 
);

COMMENT ON COLUMN documents.document_id IS 'Unique ID of this record';
COMMENT ON COLUMN documents.recipient IS 'The type of party that this document will be addressed to';
COMMENT ON COLUMN documents.document_language IS 'the language the document is written in';
COMMENT ON COLUMN documents.signature_source IS 'Source of the document signature (Area, LJA or null)';
COMMENT ON COLUMN documents.priority IS 'Determines the order of printing with respect to other documents in the same batch';
COMMENT ON COLUMN documents.header_type IS 'The type of header output on the document (EO, A, MC, ME, MA, MF, AP or null)';
COMMENT ON COLUMN documents.document_elements IS 'Details of the structured data items to be included in the document content';
