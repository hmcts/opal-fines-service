/**
* OPAL Program
*
* MODULE      : enforcement_paths.sql
*
* DESCRIPTION : Create the ENFORCEMENT_PATHS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 18/06/2024    I Readman    1.0         PO-390 Create the ENFORCEMENT_PATHS table in the Fines model
*
**/ 

CREATE TABLE enforcement_paths 
(
 enforcement_path_id           bigint       not null
,enforcement_path_set_id       bigint       not null 
,enforcement_account_type_id   bigint       not null
,missed_weeks                  smallint 
,missed_fortnights             smallint
,missed_months                 smallint
,action_1_result_id            varchar(6)
,action_2_result_id            varchar(6)
,action_2_days                 smallint
,action_3_result_id            varchar(6)
,action_3_days                 smallint
,action_4_result_id            varchar(6)
,action_4_days                 smallint
,hearing_days_from             smallint
,hearing_days_to               smallint
,CONSTRAINT enforcement_paths_pk PRIMARY KEY (enforcement_path_id)
,CONSTRAINT ep_enforcement_path_set_id_fk FOREIGN KEY (enforcement_path_set_id) REFERENCES enforcement_path_sets (enforcement_path_set_id)
,CONSTRAINT ep_enforcement_account_type_id_fk FOREIGN KEY (enforcement_account_type_id) REFERENCES enforcement_account_types (enforcement_account_type_id) 
);

COMMENT ON COLUMN enforcement_paths.enforcement_path_id IS 'Unique ID of this record';
COMMENT ON COLUMN enforcement_paths.enforcement_path_set_id IS 'ID of the path set this is part of';
COMMENT ON COLUMN enforcement_paths.enforcement_account_type_id IS 'ID of the account type this path is for';
COMMENT ON COLUMN enforcement_paths.missed_weeks IS 'Number of weeks of missed payments before enforcement';
COMMENT ON COLUMN enforcement_paths.missed_fortnights IS 'Number of fortnights of missed payments before enforcement';
COMMENT ON COLUMN enforcement_paths.missed_months IS 'Number of months of missed payments before enforcement';
COMMENT ON COLUMN enforcement_paths.action_1_result_id IS 'The first enforcement action to take when enforcing this account';
COMMENT ON COLUMN enforcement_paths.action_2_result_id IS 'The enforcement action to apply if action 1 has been applied';
COMMENT ON COLUMN enforcement_paths.action_2_days IS 'Number of days after action 1 before action 2 can be applied';
COMMENT ON COLUMN enforcement_paths.action_3_result_id IS 'The enforcement action to apply if action 2 has been applied';
COMMENT ON COLUMN enforcement_paths.action_3_days IS 'Number of days after action 2 before action 3 can be applied';
COMMENT ON COLUMN enforcement_paths.action_4_result_id IS 'The enforcement action to apply if action 3 has been applied';
COMMENT ON COLUMN enforcement_paths.action_4_days IS 'Number of days after action 3 before action 4 can be applied';
COMMENT ON COLUMN enforcement_paths.hearing_days_from IS 'Minimum number of days before a new hearing can be scheduled'; 
COMMENT ON COLUMN enforcement_paths.hearing_days_to IS 'Maximum number of days before a new hearing can be scheduled';