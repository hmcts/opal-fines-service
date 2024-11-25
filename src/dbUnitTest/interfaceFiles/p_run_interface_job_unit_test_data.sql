/*
p_run_interface_job_unit_test_data.sql - Test Data Script

Test to validate old completed jobs are created
The procedure p_run_interface_job performs commit/rollback there cannot be a single process to commit
test data and then run the procedure.
So this test must be run manually as it is in 2 parts:

Part 1 Test data - this script creates the following
    19 interface jobs created:
        1 new PAYMENTS_IN 
        1 new PRESENTED_CHEQUES
        Note: payment card requests interface creates its own job
        8 old jobs for deletion as they are > 100 days (4 payments in, 2 pres cheques, 2 pcr)
        3 old jobs that should not be deleted as they are not the status completed/failed (1 of each interface)
        6 jobs that should not be deleted as they are 100 days old exactly (2 of each interface)
        
Part 2 execute p_run_interface_job_unit_tests.sql
    Test runs each interface in turn and validates results
*/
SET SCHEMA 'public';

BEGIN;

-- clear existing data
DELETE FROM interface_messages;
DELETE FROM interface_files;
DELETE FROM interface_jobs;

-- new jobs to be run
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status) 
VALUES      (nextval('interface_job_id_seq'), 4, 'PAYMENTS_IN', 'Created');
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status) 
VALUES      (nextval('interface_job_id_seq'), 4, 'PRESENTED_CHEQUES', 'Created');

/* jobs for deletion */
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PAYMENTS_IN', 'Failed',CURRENT_DATE-101);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PAYMENTS_IN', 'Failed',CURRENT_DATE-101);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PAYMENTS_IN', 'Completed',CURRENT_DATE-101);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PAYMENTS_IN', 'Completed',CURRENT_DATE-101);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime)
VALUES      (nextval('interface_job_id_seq'), 18, 'PRESENTED_CHEQUES', 'Completed',CURRENT_DATE-101);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime)
VALUES      (nextval('interface_job_id_seq'), 18, 'PRESENTED_CHEQUES', 'Failed',CURRENT_DATE-101);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime)
VALUES      (nextval('interface_job_id_seq'), NULL, 'PAYMENT_CARD_REQUESTS', 'Completed',CURRENT_DATE-101);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime)
VALUES      (nextval('interface_job_id_seq'), NULL, 'PAYMENT_CARD_REQUESTS', 'Failed',CURRENT_DATE-101);
-- not old enough
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PAYMENTS_IN', 'Failed',CURRENT_DATE-100);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PAYMENTS_IN', 'Completed',CURRENT_DATE-100);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PRESENTED_CHEQUES', 'Failed',CURRENT_DATE-100);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PRESENTED_CHEQUES', 'Completed',CURRENT_DATE-100);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), NULL, 'PAYMENT_CARD_REQUESTS', 'Failed',CURRENT_DATE-100);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), NULL, 'PAYMENT_CARD_REQUESTS', 'Completed',CURRENT_DATE-100);
-- not completed/failed
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PAYMENTS_IN', 'Created', CURRENT_DATE-101);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), 18, 'PRESENTED_CHEQUES', 'Created', CURRENT_DATE-101);
INSERT INTO interface_jobs (interface_job_id, business_unit_id, interface_name, status, completed_datetime) 
VALUES      (nextval('interface_job_id_seq'), NULL, 'PAYMENT_CARD_REQUESTS', 'Written', CURRENT_DATE-101);

END;
