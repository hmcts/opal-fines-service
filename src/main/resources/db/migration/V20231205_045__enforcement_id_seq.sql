/**
* OPAL Program
*
* MODULE      : enforcement_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table ENFORCEMENTS. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 05/12/2023    A Dennis    1.0         PO-127 Creates the Sequence to be used to generate the Primary key for the table ENFORCEMENTS
*
**/
CREATE SEQUENCE IF NOT EXISTS enforcement_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY enforcements.enforcement_id;
