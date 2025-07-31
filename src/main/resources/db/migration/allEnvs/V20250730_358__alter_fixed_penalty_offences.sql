/**
* CGI OPAL Program
*
* MODULE      : alter_fixed_penalty_offences.sql
*
* DESCRIPTION : Add columns OFFENCE_DATE and OFFENCE_TIME to the FIXED_PENALTY_OFFENCES table and change datatype of ISSUED_DATE to DATE
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------------------------------------------------------------
* 25/07/2025    TMc         1.0         PO-1133 - Add columns OFFENCE_DATE and OFFENCE_TIME to the FIXED_PENALTY_OFFENCES table and change datatype of ISSUED_DATE to DATE
*
**/
ALTER TABLE fixed_penalty_offences 
    ADD COLUMN offence_date DATE,
    ADD COLUMN offence_time VARCHAR(5),
    ALTER COLUMN issued_date TYPE DATE;

COMMENT ON COLUMN fixed_penalty_offences.offence_date IS 'Date of offence';
COMMENT ON COLUMN fixed_penalty_offences.offence_time IS 'Time of offence. Stored as a string, as entered (not converted to UTC): "HH:SS"';
COMMENT ON COLUMN fixed_penalty_offences.issued_date IS 'Date the ticket was issued';