/**
* OPAL Program
*
* MODULE      : result_documents.sql
*
* DESCRIPTION : Recreate the RESULT_DOCUMENTS table and sequence and unique index after reference data work done by Capita
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 14/01/2025    A Dennis    1.0         PO-970 Recreate the RESULT_DOCUMENTS table and sequence and unique index after reference data work done by Capita
*
**/

CREATE SEQUENCE IF NOT EXISTS result_document_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE result_documents(
    result_document_id bigint NOT NULL DEFAULT nextval('result_document_id_seq'),
    result_id varchar(6) NOT NULL,
    document_id varchar(10) NOT NULL,
    cy_document_id varchar(13));

ALTER SEQUENCE result_document_id_seq
    OWNED BY result_documents.result_document_id;

ALTER TABLE result_documents
    ADD CONSTRAINT result_documents_pk
        PRIMARY KEY (result_document_id),
    ADD CONSTRAINT rd_result_id_fk
        FOREIGN KEY (result_id)
        REFERENCES results (result_id),
    ADD CONSTRAINT rd_document_id_fk
        FOREIGN KEY (document_id)
        REFERENCES documents (document_id),
    ADD CONSTRAINT rd_cy_document_id_fk
        FOREIGN KEY (cy_document_id)
        REFERENCES documents (document_id);

CREATE UNIQUE INDEX rd_result_document_idx
    ON result_documents (result_id, document_id);