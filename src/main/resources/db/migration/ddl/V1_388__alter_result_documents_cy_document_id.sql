/**
* OPAL Program
*
* MODULE      : alter_result_documents_cy_document_id.sql
*
* DESCRIPTION : Reduce the maximum length of RESULT_DOCUMENTS.CY_DOCUMENT_ID.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------
* 25/11/2026    TT          1.0         PO-11377 Reduce cy_document_id from VARCHAR(13) to VARCHAR(12).
*
**/

ALTER TABLE result_documents
    ALTER COLUMN cy_document_id TYPE VARCHAR(12);
