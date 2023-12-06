/**
* OPAL Program
*
* MODULE      : prison_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table PRISONS. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 06/12/2023    A Dennis    1.0         PO-127 Creates the Sequence to be used to generate the Primary key for the table PRISONS
*
**/
CREATE SEQUENCE IF NOT EXISTS prison_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY prisons.prison_id;
