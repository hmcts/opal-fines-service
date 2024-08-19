/**
*
* OPAL Program
*
* MODULE      : add_date_used_from_offences.sql
*
* DESCRIPTION : Add the date_used_from column of the OFFENCE table
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ----------------------------------------------------------------
* 19/08/2024    I Readman    1.0         PO-613 Add column date_used_from to the OFFENCES table
*
**/     
ALTER TABLE OFFENCES ADD COLUMN date_used_from timestamp;
COMMENT ON COLUMN offences.date_used_from IS 'The date the offence was in use from.'; 
