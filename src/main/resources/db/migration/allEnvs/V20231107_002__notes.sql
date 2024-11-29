/**
* CGI OPAL Program
*
* MODULE      : notes.sql
*
* DESCRIPTION : Creates the NOTES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 09/11/2023    A Dennis    1.0         PO-39 Creates the NOTES table for the Fines model
*
**/
CREATE TABLE notes 
(
 note_id	               bigint  not null
,note_type	             varchar(2)
,associated_record_type  varchar(30)
,associated_record_id    varchar(30)
,note_text               text
,posted_date             timestamp
,posted_by               varchar(20)
,CONSTRAINT note_id_pk PRIMARY KEY 
 (
   note_id	
 ) 
);
COMMENT ON COLUMN notes.note_id IS 'Unique ID of this record';
COMMENT ON COLUMN notes.note_type IS 'AC (Account Comment), A1 (Free Text Note 1), A2 (Free Text Note 2), A3 (Free Text Note 3), NT (Standard note)';
COMMENT ON COLUMN notes.associated_record_type IS 'The type of record this note relates to. Note can currently relate to defendants_accounts, creditor_transactions, suspense_transactions';
COMMENT ON COLUMN notes.associated_record_id IS 'ID of the record the notes apply to'; 
COMMENT ON COLUMN notes.note_text IS 'Note text';
COMMENT ON COLUMN notes.posted_date IS 'The date the note was posted to the relating item. Not recorded for account comments oraccount free text notes';
COMMENT ON COLUMN notes.posted_by IS 'ID of the user that posted the note. Not recorded for account notes or suspense transaction notes';
