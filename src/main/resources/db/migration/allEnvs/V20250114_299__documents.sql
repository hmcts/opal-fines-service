/**
* OPAL Program
*
* MODULE      : documents.sql
*
* DESCRIPTION : Recreate the DOCUMENTS table and check constraints after reference data work done by Capita
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------
* 14/01/2025    A Dennis    1.0         PO-970 Recreate the DOCUMENTS table and check constraints after reference data work done by Capita
*
**/

CREATE TABLE documents(
    document_id varchar(12) NOT NULL,
    recipient varchar(4) NOT NULL,
    document_language varchar(2) NOT NULL,
    priority smallint NOT NULL,
    header_type varchar(2),
    signature_source varchar(4),
    document_template varchar(30),
    document_elements json,
    print_parameters json
);

ALTER TABLE documents
    ADD CONSTRAINT documents_pk
        PRIMARY KEY (document_id),
    ADD CONSTRAINT d_recipient_cc
        CHECK (recipient IN ('BENA','CLAC','CRED','DEF','EMP','FRA','OTHC','PRIS')),
    ADD CONSTRAINT d_document_language_cc
        CHECK (document_language IN ('EN','CY')),
    ADD CONSTRAINT d_priority_cc
        CHECK (priority IN (0,1,2)),
    ADD CONSTRAINT d_header_type_cc
        CHECK (COALESCE(header_type) IN ('A','AP','EO')),
    ADD CONSTRAINT d_signature_source_cc
        CHECK (COALESCE(signature_source) IN ('Area','LJA'));

COMMENT ON COLUMN public.documents.document_id IS 'Unique ID of this record';

COMMENT ON COLUMN public.documents.recipient IS 'The type of party that this document will be addressed to';

COMMENT ON COLUMN public.documents.document_language IS 'the language the document is written in';

COMMENT ON COLUMN public.documents.priority IS 'Determines the order of printing with respect to other documents in the same batch';

COMMENT ON COLUMN public.documents.header_type IS 'The type of header output on the document (EO, A, MC, ME, MA, MF, AP or null)';

COMMENT ON COLUMN public.documents.signature_source IS 'Source of the document signature (Area, LJA or null)';

COMMENT ON COLUMN public.documents.document_elements IS 'Details of the structured data items to be included in the document content';
