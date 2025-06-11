/**
* OPAL Program
*
* MODULE      : alter_result_documents_doc_id.sql
*
* DESCRIPTION :Change the size of the column RESULT_DOCUMENTS.DOCUMENT_ID from varchar(10) to varchar(12) to match the parent primary key.. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------------------------
* 09/06/2025    A Dennis    1.0         PO-1846 Change the size of the column RESULT_DOCUMENTS.DOCUMENT_ID from varchar(10) to varchar(12) to match the parent primary key.
*
**/
ALTER TABLE result_documents
ALTER COLUMN document_id TYPE varchar(12);
