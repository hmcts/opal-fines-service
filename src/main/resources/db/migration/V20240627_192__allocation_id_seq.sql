/**
* OPAL Program
*
* MODULE      : allocation_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table ALLOCATIONS. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-392 Create the sequence to be used to generate the Primary key for the table ALLOCATIONS
*
**/ 

CREATE SEQUENCE IF NOT EXISTS allocation_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY allocations.allocation_id;
