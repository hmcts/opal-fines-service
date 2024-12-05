/**
* OPAL Program
*
* MODULE      : alias_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table ALIASES. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 19/04/2024    A Dennis    1.0         PO-284 Creates the Sequence to be used to generate the Primary key for the table ALIASES
*
**/
CREATE SEQUENCE IF NOT EXISTS alias_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY aliases.alias_id;
