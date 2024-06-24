/**
* OPAL Program
*
* MODULE      : print_job_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table PRINT_JOB.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 21/06/2024    T Reed      1.0         PO-210 Creates the Sequence to be used to generate the Primary key for the table PRINT_JOB
*
**/
CREATE SEQUENCE IF NOT EXISTS print_job_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20;

