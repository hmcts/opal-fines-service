DELETE FROM report_instances
WHERE requested_by_name = 'interface-jobs'
AND report_parameters ->> 'till_id' IN (
    SELECT till_id::text
    FROM tills
    WHERE interface_file_id = 99000000401001
);

DELETE FROM payments_in
WHERE associated_record_id = '99000000401002'
OR till_id IN (
    SELECT till_id
    FROM tills
    WHERE interface_file_id = 99000000401001
);

DELETE FROM interface_messages
WHERE interface_job_id = 99000000401000;

DELETE FROM tills
WHERE interface_file_id = 99000000401001;

DELETE FROM interface_files
WHERE interface_file_id = 99000000401001;

DELETE FROM interface_jobs
WHERE interface_job_id = 99000000401000;

DELETE FROM defendant_accounts
WHERE defendant_account_id = 99000000401002;

DELETE FROM configuration_items
WHERE configuration_item_id IN (99000000401003, 99000000401004);
