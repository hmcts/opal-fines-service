/**
* OPAL Program
*
* MODULE      : insert_auto_cash_input_interface_job_test_data.sql
*
* DESCRIPTION : Insert test-only Payments In interface jobs for the Automatic Cash Input process tab.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 08/09/2026    C Cho       1.0         PO-10537 Add Payments In interface job test data.
*
**/

INSERT INTO public.interface_jobs (
    interface_job_id,
    business_unit_id,
    interface_name,
    status,
    created_datetime,
    started_datetime,
    completed_datetime
)
VALUES
    (
        99000000015001,
        77,
        'payments_in',
        'CREATED'::t_interface_job_status_enum,
        CURRENT_TIMESTAMP - INTERVAL '3 days',
        NULL,
        NULL
    ),
    (
        99000000015002,
        78,
        'payments_in',
        'CREATED'::t_interface_job_status_enum,
        CURRENT_TIMESTAMP - INTERVAL '2 days 12 hours',
        NULL,
        NULL
    ),
    (
        99000000015003,
        78,
        'payments_in',
        'FAILED'::t_interface_job_status_enum,
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '2 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '15 minutes'
    ),
    (
        99000000015004,
        77,
        'payments_in',
        'IGNORED'::t_interface_job_status_enum,
        CURRENT_TIMESTAMP - INTERVAL '1 day 2 hours',
        CURRENT_TIMESTAMP - INTERVAL '1 day 1 hour 55 minutes',
        CURRENT_TIMESTAMP - INTERVAL '1 day 1 hour 50 minutes'
    )
ON CONFLICT (interface_job_id) DO UPDATE
   SET business_unit_id = EXCLUDED.business_unit_id,
       interface_name = EXCLUDED.interface_name,
       status = EXCLUDED.status,
       created_datetime = EXCLUDED.created_datetime,
       started_datetime = EXCLUDED.started_datetime,
       completed_datetime = EXCLUDED.completed_datetime;

INSERT INTO public.interface_files (
    interface_file_id,
    interface_job_id,
    file_name,
    created_datetime,
    records,
    source,
    record_count,
    total_amount,
    override_inhibits
)
VALUES
    (
        99000000016001,
        99000000015001,
        'payments-in-camberwell-created-001.csv',
        CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '1 minute',
        '[]'::json,
        'NATWEST'::t_interface_file_source_enum,
        24,
        1240.50,
        false
    ),
    (
        99000000016002,
        99000000015002,
        'payments-in-thames-created-001.csv',
        CURRENT_TIMESTAMP - INTERVAL '2 days 12 hours' + INTERVAL '1 minute',
        '[]'::json,
        'ALLPAY'::t_interface_file_source_enum,
        18,
        865.25,
        false
    ),
    (
        99000000016003,
        99000000015003,
        'payments-in-thames-failed-001.csv',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '1 minute',
        '[]'::json,
        'DWP'::t_interface_file_source_enum,
        7,
        315.00,
        false
    ),
    (
        99000000016004,
        99000000015004,
        'payments-in-camberwell-ignored-001.csv',
        CURRENT_TIMESTAMP - INTERVAL '1 day 2 hours' + INTERVAL '1 minute',
        '[]'::json,
        'OTHER'::t_interface_file_source_enum,
        0,
        0.00,
        false
    )
ON CONFLICT (interface_file_id) DO UPDATE
   SET interface_job_id = EXCLUDED.interface_job_id,
       file_name = EXCLUDED.file_name,
       created_datetime = EXCLUDED.created_datetime,
       records = EXCLUDED.records,
       source = EXCLUDED.source,
       record_count = EXCLUDED.record_count,
       total_amount = EXCLUDED.total_amount,
       override_inhibits = EXCLUDED.override_inhibits;

SELECT setval(
    'public.interface_job_id_seq',
    (SELECT GREATEST(COALESCE(MAX(interface_job_id), 1), 99000000015004) FROM public.interface_jobs),
    true
);

SELECT setval(
    'public.interface_file_id_seq',
    (SELECT GREATEST(COALESCE(MAX(interface_file_id), 1), 99000000016004) FROM public.interface_files),
    true
);
