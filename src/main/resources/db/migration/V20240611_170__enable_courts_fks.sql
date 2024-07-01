/**
* OPAL Program
*
* MODULE      : enable_courts_fks.sql
*
* DESCRIPTION : Put back foreign keys to the COURTS table after loading Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------------------
* 11/06/2024    A Dennis    1.0         PO-308 Put back foreign keys to the COURTS table after loading Reference Data from data held in Excel spreadsheet.  
*
**/

ALTER TABLE defendant_accounts
ADD CONSTRAINT da_imposing_court_id_fk FOREIGN KEY
(
  imposing_court_id 
)
REFERENCES courts
(
  court_id 
);


ALTER TABLE defendant_accounts
ADD CONSTRAINT da_enforcing_court_id_fk FOREIGN KEY
(
  enforcing_court_id 
)
REFERENCES courts
(
  court_id 
);

ALTER TABLE defendant_accounts
ADD CONSTRAINT da_last_hearing_court_id_fk FOREIGN KEY
(
  last_hearing_court_id 
)
REFERENCES courts
(
  court_id 
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
