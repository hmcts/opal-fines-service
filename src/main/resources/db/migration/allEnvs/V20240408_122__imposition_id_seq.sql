/**
* OPAL Program
*
* MODULE      : imposition_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table IMPOSITIONS. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 08/04/2024    A Dennis    1.0         PO-264 Creates the Sequence to be used to generate the Primary key for the table IMPOSITIONS
*
**/
CREATE SEQUENCE IF NOT EXISTS imposition_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY impositions.imposition_id;
