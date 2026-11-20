/**
* OPAL Program
*
* MODULE      : alter_interface_files_add_source_json_id_unique_constraint.sql
*
* DESCRIPTION : Add a unique constraint to INTERFACE_FILES.SOURCE_JSON_ID.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ---------------------------------------------------------------
* 26/10/2026    T Tang         1.0         PO-6460 - Make INTERFACE_FILES.SOURCE_JSON_ID unique.
*
**/

ALTER TABLE interface_files
    ADD CONSTRAINT interface_files_source_json_id_uk UNIQUE (source_json_id);
