/**
* OPAL Program
*
* MODULE      : alter_print_job_id.sql
*
* DESCRIPTION : This script alters the print_job table to set the default value of the print_job_id column to the next
* value of the print_job_id_seq sequence. It also sets the print_job_id_seq sequence to be owned by the print_job_id
* column of the print_job table. This ensures that the sequence and table are correctly linked, and that new entries in
* the print_job table will automatically receive a unique print_job_id.

* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 21/06/2024    T Reed      1.0         PO-210 Link sequence to table and set default value for print_job_id
*
**/
ALTER TABLE print_job ALTER COLUMN print_job_id SET DEFAULT nextval('print_job_id_seq');
ALTER SEQUENCE print_job_id_seq OWNED BY print_job.print_job_id;
