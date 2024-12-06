/**
* OPAL Program
*
* MODULE      : enforcement_runs.sql
*
* DESCRIPTION : Create the ENFORCEMENT_RUNS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 18/06/2024    I Readman    1.0         PO-390 Create the ENFORCEMENT_RUNS table in the Fines model
*
**/ 

CREATE TABLE enforcement_runs 
(
 enforcement_run_id    bigint       not null 
,business_unit_id      smallint      
,run_name              varchar(30)  
,frequency_period      varchar(1) 
,next_run_date         timestamp
,name_range_start      varchar(20)
,name_range_end        varchar(20)
,CONSTRAINT enforcement_runs_pk PRIMARY KEY (enforcement_run_id)
,CONSTRAINT es_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id) 
);

COMMENT ON COLUMN enforcement_runs.enforcement_run_id IS 'Unique ID of this record';
COMMENT ON COLUMN enforcement_runs.business_unit_id IS 'ID of the business unit this account type belongs to';
COMMENT ON COLUMN enforcement_runs.run_name IS 'The name for this run';
COMMENT ON COLUMN enforcement_runs.frequency_period IS 'How often the run will be initiated';
COMMENT ON COLUMN enforcement_runs.next_run_date IS 'The date the next run will initiated';
COMMENT ON COLUMN enforcement_runs.name_range_start IS 'The start of the range of debtor names to be enforced in this run';
COMMENT ON COLUMN enforcement_runs.name_range_end IS 'The end of the range of debtor names to be enforced in this run';