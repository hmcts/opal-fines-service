DELETE FROM payments_in
WHERE payment_in_id IN (988051, 988052);

DELETE FROM tills
WHERE till_id IN (987951, 987952);

DELETE FROM interface_messages
WHERE interface_message_id IN (987851, 987852);

DELETE FROM interface_files
WHERE interface_file_id IN (987751, 987752);

DELETE FROM interface_jobs
WHERE interface_job_id IN (987651, 987652);
