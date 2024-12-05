/**
* OPAL Program
*
* MODULE      : amendment_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table AMENDMENTS. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 23/04/2024    A Dennis    1.0         PO-284 Creates the Sequence to be used to generate the Primary key for the table AMENDMENTS
*
**/
CREATE SEQUENCE IF NOT EXISTS amendment_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY amendments.amendment_id;
