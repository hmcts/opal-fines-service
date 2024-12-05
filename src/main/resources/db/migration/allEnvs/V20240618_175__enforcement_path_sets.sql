/**
* OPAL Program
*
* MODULE      : enforcement_path_sets.sql
*
* DESCRIPTION : Create the ENFORCEMENT_PATH_SETS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 18/06/2024    I Readman    1.0         PO-390 Create the ENFORCEMENT_PATH_SETS table in the Fines model
*
**/ 

CREATE TABLE enforcement_path_sets 
(
 enforcement_path_set_id    bigint         not null
,description                varchar(240) 
,business_unit_id           smallint       not null
,CONSTRAINT enforcement_path_sets_pk PRIMARY KEY (enforcement_path_set_id)
,CONSTRAINT eps_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id) 
);

COMMENT ON COLUMN enforcement_path_sets.enforcement_path_set_id IS 'Unique ID of this record';
COMMENT ON COLUMN enforcement_path_sets.description IS 'A name for this set of account enforcement paths';
COMMENT ON COLUMN enforcement_path_sets.business_unit_id IS 'ID of the business unit this account type belongs to';