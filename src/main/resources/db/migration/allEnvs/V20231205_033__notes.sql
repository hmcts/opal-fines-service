/**
* CGI OPAL Program
*
* MODULE      : notes.sql
*
* DESCRIPTION : Add not null to columns in NOTES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 05/12/2023    A Dennis    1.0         PO-127 Add not null to columns in NOTES table for the Fines model
*
**/

ALTER TABLE notes
ALTER COLUMN note_type SET NOT NULL,
ALTER COLUMN associated_record_type SET NOT NULL,
ALTER COLUMN associated_record_id SET NOT NULL,
ALTER COLUMN note_text SET NOT NULL;