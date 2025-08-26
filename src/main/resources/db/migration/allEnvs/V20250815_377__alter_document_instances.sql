/**
* CGI OPAL Program
*
* MODULE      : alter_document_instances.sql
*
* DESCRIPTION : Alter column DOCUMENT_CONTENT on the DOCUMENT_INSTANCES table to allow NULLs
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------
* 25/07/2025    TMc         1.0         PO-1591 - Alter column DOCUMENT_CONTENT on the DOCUMENT_INSTANCES table to allow NULLs
*
**/
ALTER TABLE document_instances 
    ALTER COLUMN document_content DROP NOT NULL;
