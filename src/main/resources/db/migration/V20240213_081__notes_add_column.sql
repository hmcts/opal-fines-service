/**
* CGI OPAL Program
*
* MODULE      : notes_add_column.sql
*
* DESCRIPTION : Add posted_by_aad column to NOTES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 13/02/2024    A Dennis    1.0         PO-186 Add posted_by_aad column to NOTES table for the Fines model
*
**/

ALTER TABLE notes
ADD COLUMN posted_by_aad varchar(100);

ALTER TABLE notes
ADD CONSTRAINT notes_posted_by_aad_fk FOREIGN KEY
(
  posted_by_aad 
)
REFERENCES users
(
  user_id 
);
