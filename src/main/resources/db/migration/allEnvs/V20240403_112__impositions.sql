/**
* CGI OPAL Program
*
* MODULE      : impositions.sql
*
* DESCRIPTION : Creates the IMPOSITIONS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/04/2024    A Dennis    1.0         PO-200 Creates the IMPOSITIONS table for the Fines model
*
**/
CREATE TABLE impositions 
(
 impositions_id          bigint         not null
,defendant_account_id    bigint         not null
,posted_date             timestamp      not null
,posted_by               varchar(20)
,posted_by_user_id       bigint         not null
,original_posted_date    timestamp    
,result_id               varchar(6)     not null
,imposing_court_id       bigint   
,imposed_date            timestamp
,imposed_amount          decimal(18,2)  not null
,paid_amount             decimal(18,2)  not null
,offence_id              smallint       not null
,creditor_account_id     bigint         not null
,unit_fine_adjusted      boolean      
,unit_fine_units         smallint
,completed               boolean
,CONSTRAINT impositions_pk PRIMARY KEY 
(
   impositions_id	
 )  
);

ALTER TABLE impositions
ADD CONSTRAINT imp_defendant_account_id_fk FOREIGN KEY
(
  defendant_account_id 
)
REFERENCES defendant_accounts
(
  defendant_account_id 
);

ALTER TABLE impositions
ADD CONSTRAINT imp_posted_by_user_id_fk FOREIGN KEY
(
  posted_by_user_id 
)
REFERENCES users
(
  user_id 
);

ALTER TABLE impositions
ADD CONSTRAINT imp_result_id_fk FOREIGN KEY
(
  result_id 
)
REFERENCES results
(
  result_id 
);

ALTER TABLE impositions
ADD CONSTRAINT imp_imposing_court_id_fk FOREIGN KEY
(
  imposing_court_id 
)
REFERENCES courts
(
  court_id 
);

ALTER TABLE impositions
ADD CONSTRAINT imp_offence_id_fk FOREIGN KEY
(
  offence_id 
)
REFERENCES offences
(
  offence_id 
);

ALTER TABLE impositions
ADD CONSTRAINT imp_creditor_account_id_fk FOREIGN KEY
(
  creditor_account_id 
)
REFERENCES creditor_accounts
(
  creditor_account_id 
);

COMMENT ON COLUMN impositions.impositions_id IS 'Unique ID of this record';
COMMENT ON COLUMN impositions.defendant_account_id IS 'ID of the defendant account this record belongs to';
COMMENT ON COLUMN impositions.posted_date IS 'The date the record was posted to the account';
COMMENT ON COLUMN impositions.posted_by IS 'ID of user responsible for posting this record';
COMMENT ON COLUMN impositions.posted_by_user_id IS 'The user ID and is the foreign key to Users table but can be NULL, so if a not null value is put then it is enforced.';
COMMENT ON COLUMN impositions.original_posted_date IS 'Posted date of the original imposition if this imposition is a duplicate of the original which was written off by legacy account consolidation';
COMMENT ON COLUMN impositions.result_id IS 'The ID of the result imposed by the court that determines the type of imposition';
COMMENT ON COLUMN impositions.imposing_court_id IS 'The ID of the court that imposed this penalty';
COMMENT ON COLUMN impositions.imposed_date IS 'The date this financial penalty was imposed in a court hearing';
COMMENT ON COLUMN impositions.imposed_amount IS 'The amount imposed by court';
COMMENT ON COLUMN impositions.paid_amount IS 'The amount paid so far';
COMMENT ON COLUMN impositions.offence_id IS 'The offence for which this penalty was imposed';
COMMENT ON COLUMN impositions.creditor_account_id IS 'ID of the creditor account to be allocated payments received against this imposition';
COMMENT ON COLUMN impositions.unit_fine_adjusted IS 'Whether a "Unit Fine Adjustment under s.18(7) CJA 1991" was made';
COMMENT ON COLUMN impositions.unit_fine_units IS 'Number of units';
COMMENT ON COLUMN impositions.completed IS 'If the imposition has been paid in full';
