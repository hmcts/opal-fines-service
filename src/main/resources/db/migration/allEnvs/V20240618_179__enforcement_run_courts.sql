/**
* OPAL Program
*
* MODULE      : enforcement_run_courts.sql
*
* DESCRIPTION : Create the ENFORCEMENT_RUN_COURTS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 18/06/2024    I Readman    1.0         PO-390 Create the ENFORCEMENT_RUN_COURTS table in the Fines model
*
**/ 

CREATE TABLE enforcement_run_courts 
(
 enforcement_run_court_id    bigint   not null
,enforcement_run_id          bigint   not null  
,court_id                    bigint   not null
,CONSTRAINT enforcement_run_courts_pk PRIMARY KEY (enforcement_run_court_id)
,CONSTRAINT erc_enforcement_run_id_fk FOREIGN KEY (enforcement_run_id) REFERENCES enforcement_runs (enforcement_run_id)
,CONSTRAINT erc_court_id_fk FOREIGN KEY (court_id) REFERENCES courts (court_id)
);

COMMENT ON COLUMN enforcement_run_courts.enforcement_run_court_id IS 'Unique ID of this record';
COMMENT ON COLUMN enforcement_run_courts.enforcement_run_id IS 'ID of the enforcement run that includes the court';
COMMENT ON COLUMN enforcement_run_courts.court_id IS 'ID of the court for which accounts will be enforced';