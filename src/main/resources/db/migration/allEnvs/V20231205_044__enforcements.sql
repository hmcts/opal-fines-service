/**
* CGI OPAL Program
*
* MODULE      : enforcements.sql
*
* DESCRIPTION : Creates the ENFORCEMENTS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 05/12/2023    A Dennis    1.0         PO-127 Creates the ENFORCEMENTS table for the Fines model
*
**/
CREATE TABLE enforcements 
(
 enforcement_id            bigint  not null
,defendant_account_id      bigint  not null
,posted_date               timestamp  not null          
,posted_by                 varchar(20)
,result_id                 varchar(10)
,reason                    varchar(50)
,enforcer_id               bigint 
,jail_days                 integer
,warrant_reference         varchar(20)
,case_reference            varchar(40)
,hearing_date              timestamp
,hearing_court_id          bigint
,account_type              varchar(20)
,CONSTRAINT enforcements_pk PRIMARY KEY 
 (
   enforcement_id	
 ) 
);

ALTER TABLE enforcements
ADD CONSTRAINT enf_defendant_account_id_fk FOREIGN KEY
(
  defendant_account_id 
)
REFERENCES defendant_accounts
(
  defendant_account_id 
);

ALTER TABLE enforcements
ADD CONSTRAINT enf_result_id_fk FOREIGN KEY
(
  result_id 
)
REFERENCES results
(
  result_id 
);

ALTER TABLE enforcements
ADD CONSTRAINT enf_enforcer_id_fk FOREIGN KEY
(
  enforcer_id 
)
REFERENCES enforcers
(
  enforcer_id 
);

ALTER TABLE enforcements
ADD CONSTRAINT enf_hearing_court_id_fk FOREIGN KEY
(
  hearing_court_id 
)
REFERENCES courts
(
  court_id 
);

COMMENT ON COLUMN enforcements.enforcement_id IS 'Unique ID of this record';
COMMENT ON COLUMN enforcements.defendant_account_id IS 'ID of the account this record belongs to';
COMMENT ON COLUMN enforcements.posted_date IS 'The date the record was posted to the account';
COMMENT ON COLUMN enforcements.posted_by IS 'ID of user responsible for posting this record';
COMMENT ON COLUMN enforcements.result_id IS 'The ID of the result imposed by the court that determines the type of imposition';
COMMENT ON COLUMN enforcements.reason IS 'The reason for this enforcement action';
COMMENT ON COLUMN enforcements.enforcer_id IS 'The enforcer/process server for this enforcement action';
COMMENT ON COLUMN enforcements.jail_days IS 'only applies to SC/CW';
COMMENT ON COLUMN enforcements.warrant_reference IS 'The reference number of the warrant generated from this action';
COMMENT ON COLUMN enforcements.case_reference IS 'The reference number of the case generated from this action';
COMMENT ON COLUMN enforcements.hearing_date IS 'The hearing date of the case generated from this action';
COMMENT ON COLUMN enforcements.hearing_court_id IS 'The hearing court of the case generated from this action';
COMMENT ON COLUMN enforcements.account_type IS 'The enforcement account type that auto-enforcement deemed this to be at the time of it applying this action';
