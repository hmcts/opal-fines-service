INSERT INTO interface_jobs (
    interface_job_id,
    business_unit_id,
    interface_name,
    status,
    created_datetime
)
VALUES
    (990001, 77, 'PROCESS_AND_ALLOCATE_PAYMENTS', 'CREATED', CURRENT_TIMESTAMP),
    (990002, 78, 'PROCESS_AND_ALLOCATE_PAYMENTS', 'FAILED', CURRENT_TIMESTAMP),
    (990003, 77, 'PROCESS_AND_ALLOCATE_PAYMENTS', 'COMPLETED', CURRENT_TIMESTAMP);

INSERT INTO interface_files (
    interface_file_id,
    interface_job_id,
    file_name,
    created_datetime,
    records,
    override_inhibits
)
VALUES
    (991001, 990001, 'po2593-created-1.csv', CURRENT_TIMESTAMP, '[]', false),
    (991002, 990001, 'po2593-created-2.csv', CURRENT_TIMESTAMP, '[]', false),
    (991003, 990002, 'po2593-failed.csv', CURRENT_TIMESTAMP, '[]', false),
    (991004, 990003, 'po2593-completed.csv', CURRENT_TIMESTAMP, '[]', false);
