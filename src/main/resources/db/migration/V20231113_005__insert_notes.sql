/**
* OPAL Program
*
* MODULE      : insert_notes.sql
*
* DESCRIPTION : Inserts one row of data into the NOTES table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 13/11/2023    A Dennis    1.0         Inserts one row of data into the NOTES table
*
**/
INSERT INTO NOTES
  (
   note_id
  ,note_type
  ,associated_record_type
  ,associated_record_id
  ,note_text
  ,posted_date
  ,posted_by
  )
VALUES
  (
   nextval('note_id_seq')
  ,'AC'
  ,'BBBBB'
  ,'CCCCCCCCCC'
  ,'DDDDD EEEEE FFFFFF'
  ,current_timestamp
  ,'DDDDDDDDDDDDDD'
  );
