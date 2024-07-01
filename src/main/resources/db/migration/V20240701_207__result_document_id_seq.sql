/**
* OPAL Program
*
* MODULE      : result_documents_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table RESULT_DOCUMENTS. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-399 Create the sequence to be used to generate the Primary key for the table RESULTS_DOCUMENTS
*
**/

CREATE SEQUENCE IF NOT EXISTS result_document_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY result_documents.result_document_id;