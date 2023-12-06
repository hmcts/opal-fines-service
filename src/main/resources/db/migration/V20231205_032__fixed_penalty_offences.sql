/**
* CGI OPAL Program
*
* MODULE      : fixed_penalty_offences.sql
*
* DESCRIPTION : Creates the FIXED_PENALTY_OFFENCES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the FIXED_PENALTY_OFFENCES table for the Fines model
*
**/
CREATE TABLE fixed_penalty_offences 
(
 defendant_account_id       bigint        not null
,ticket_number              varchar(120)  not null
,vehicle_registration       varchar(10)
,offence_location           varchar(30)
,notice_number              varchar(10)
,issued_date                timestamp
,licence_number             varchar(20)
,CONSTRAINT fixed_penalty_offences_pk PRIMARY KEY 
 (
   defendant_account_id	
 ) 
);

ALTER TABLE fixed_penalty_offences
ADD CONSTRAINT fpo_defendant_account_id_fk FOREIGN KEY
(
  defendant_account_id 
)
REFERENCES defendant_accounts
(
  defendant_account_id 
);

COMMENT ON COLUMN fixed_penalty_offences.defendant_account_id IS 'Unique ID of this record';
COMMENT ON COLUMN fixed_penalty_offences.ticket_number IS 'Fixed penalty ticket number';
COMMENT ON COLUMN fixed_penalty_offences.vehicle_registration IS 'Vehicle registration or NV for non-vehicle fixed penalties';
COMMENT ON COLUMN fixed_penalty_offences.offence_location IS 'Place of offence';
COMMENT ON COLUMN fixed_penalty_offences.notice_number IS 'Notice to owner/hirer number';
COMMENT ON COLUMN fixed_penalty_offences.issued_date IS 'Date and time the ticket was issued';
COMMENT ON COLUMN fixed_penalty_offences.licence_number IS 'The driver''s licence number';
