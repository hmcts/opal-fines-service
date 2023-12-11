/**
* CGI OPAL Program
*
* MODULE      : committal_warrant_progress.sql
*
* DESCRIPTION : Creates the COMMITTAL_WARRANT_PROGRESS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 06/12/2023    A Dennis    1.0         PO-127 Creates the COMMITTAL_WARRANT_PROGRESS table for the Fines model
*
**/
CREATE TABLE committal_warrant_progress 
(
 defendant_account_id        bigint         not null
,enforcement_id              bigint         not null
,amount                      decimal(18,2)
,body_receipt_date           timestamp
,certificate_part_a_date     timestamp
,certificate_part_b_date     timestamp
,prison_id                   bigint
,CONSTRAINT committal_warrant_progress_pk PRIMARY KEY 
 (
   defendant_account_id	
 ) 
);

ALTER TABLE committal_warrant_progress
ADD CONSTRAINT cwp_defendant_account_id_fk FOREIGN KEY
(
  defendant_account_id 
)
REFERENCES defendant_accounts
(
  defendant_account_id 
);

ALTER TABLE committal_warrant_progress
ADD CONSTRAINT enf_enforcement_id_fk FOREIGN KEY
(
  enforcement_id 
)
REFERENCES enforcements
(
  enforcement_id 
);

ALTER TABLE committal_warrant_progress
ADD CONSTRAINT enf_prison_id_fk FOREIGN KEY
(
  prison_id 
)
REFERENCES prisons
(
  prison_id 
);

COMMENT ON COLUMN committal_warrant_progress.defendant_account_id IS 'Unique ID of this record';
COMMENT ON COLUMN committal_warrant_progress.enforcement_id IS 'Associated CW enforcement ID containing CW date and warrant reference';
COMMENT ON COLUMN committal_warrant_progress.amount IS 'Committal Warrant amount';
COMMENT ON COLUMN committal_warrant_progress.body_receipt_date IS 'Committal Warrant date of body receipt';
COMMENT ON COLUMN committal_warrant_progress.certificate_part_a_date IS 'Committal Warrant date of Certificate of Imprisonment Part A';
COMMENT ON COLUMN committal_warrant_progress.certificate_part_b_date IS 'Committal Warrant date of Certificate of Imprisonment Part B';
COMMENT ON COLUMN committal_warrant_progress.prison_id IS 'Unique identifier of prison committed to';
