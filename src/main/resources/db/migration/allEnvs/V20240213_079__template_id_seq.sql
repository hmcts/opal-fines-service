/**
* OPAL Program
*
* MODULE      : template_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table TEMPLATES. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 13/02/2024    A Dennis    1.0         PO-177 Creates the Sequence to be used to generate the Primary key for the table TEMPLATES
*
**/
CREATE SEQUENCE IF NOT EXISTS template_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY templates.template_id;
