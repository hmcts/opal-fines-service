/**
* OPAL Program
*
* MODULE      : local_justice_area_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table LOCAL_JUSTICE_AREAS. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the Sequence to be used to generate the Primary key for the table LOCAL_JUSTICE_AREAS
*
**/
CREATE SEQUENCE IF NOT EXISTS local_justice_area_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY local_justice_areas.local_justice_area_id;
