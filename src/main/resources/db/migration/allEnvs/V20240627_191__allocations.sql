/**
* OPAL Program
*
* MODULE      : allocations.sql
*
* DESCRIPTION : Create the ALLOCATIONS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-392 Create the ALLOCATIONS table in the Fines model
*
**/ 

CREATE TABLE allocations
(
 allocation_id              bigint 
,imposition_id              bigint          not null
,allocated_date             timestamp       not null
,allocated_amount           decimal(18,2)   not null
,transaction_type           varchar(10)     not null
,allocation_function        varchar(30)     not null
,defendant_transaction_id   bigint
,CONSTRAINT allocations_pk PRIMARY KEY (allocation_id) 
,CONSTRAINT all_imposition_id_fk FOREIGN KEY (imposition_id) REFERENCES impositions (imposition_id)
,CONSTRAINT all_defendant_transaction_id_fk FOREIGN KEY (defendant_transaction_id) REFERENCES defendant_transactions (defendant_transaction_id) 
);

COMMENT ON COLUMN allocations.allocation_id IS 'Unique ID of this record';
COMMENT ON COLUMN allocations.imposition_id IS 'Imposition account Number';
COMMENT ON COLUMN allocations.allocated_date IS 'Allocation timestamp';
COMMENT ON COLUMN allocations.allocated_amount IS 'Allocation amount';
COMMENT ON COLUMN allocations.transaction_type IS 'The type of transaction this allocation is associated with';
COMMENT ON COLUMN allocations.allocation_function IS 'The function used to create the allocation';
COMMENT ON COLUMN allocations.defendant_transaction_id IS 'The transaction being allocated to the imposition';
