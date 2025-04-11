/**
* OPAL Program
*
* MODULE      : enforcer_allocations.sql
*
* DESCRIPTION : Create the ENFORCER_ALLOCATIONS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 10/04/2025    Chi Cho      1.0        PO-1276 Create the ENFORCER_ALLOCATIONS table in the Fines model
*
**/

CREATE TABLE enforcer_allocations 
(
 enforcer_allocation_id        bigint       not null
,enforcer_id                   bigint       not null
,result_id                     varchar(6)   not null
,priority                      bigint
,name_range_start              varchar(1)
,name_range_end                varchar(1)
,maximum_enforcements          bigint
,CONSTRAINT enforcer_allocations_pk PRIMARY KEY (enforcer_allocation_id)
,CONSTRAINT ea_enforcer_id_fk FOREIGN KEY (enforcer_id) REFERENCES enforcers (enforcer_id)
,CONSTRAINT ea_result_id_fk FOREIGN KEY (result_id) REFERENCES results (result_id)
);

COMMENT ON COLUMN enforcer_allocations.enforcer_allocation_id IS 'Unique ID of this record';
COMMENT ON COLUMN enforcer_allocations.enforcer_id IS 'ID of the enforcer this allocation is for';
COMMENT ON COLUMN enforcer_allocations.result_id IS 'ID of the result this allocation is for';
COMMENT ON COLUMN enforcer_allocations.priority IS 'The order to allocate an action for this result with respect to other enforcers';
COMMENT ON COLUMN enforcer_allocations.name_range_start IS 'The start of the range of debtor names this allocation is for';
COMMENT ON COLUMN enforcer_allocations.name_range_end IS 'The end of the range of debtor names this allocation is for';
COMMENT ON COLUMN enforcer_allocations.maximum_enforcements IS 'The maximum number of enforcements for this enforcer and result';