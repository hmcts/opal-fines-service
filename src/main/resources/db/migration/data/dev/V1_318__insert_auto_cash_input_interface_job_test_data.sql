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
    (99000000015001, 77, 'payments_in', 'CREATED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '7 days', NULL, NULL),
    (99000000015002, 78, 'payments_in', 'CREATED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '6 days', NULL, NULL),
    (99000000015003, 77, 'payments_in', 'CREATED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, NULL),
    (99000000015004, 78, 'payments_in', 'CREATED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '4 days', NULL, NULL),
    (99000000015005, 77, 'payments_in', 'CREATED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '3 days', NULL, NULL),
    (99000000015006, 78, 'payments_in', 'CREATED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '2 days', NULL, NULL),
    (99000000015007, 77, 'payments_in', 'CREATED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, NULL),
    (99000000015008, 78, 'payments_in', 'FAILED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '2 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '15 minutes'),
    (99000000015009, 77, 'payments_in', 'FAILED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '2 minutes',
     CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '15 minutes'),
    (99000000015010, 78, 'payments_in', 'FAILED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '2 minutes',
     CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '15 minutes'),
    (99000000015011, 77, 'payments_in', 'FAILED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days' + INTERVAL '2 minutes',
     CURRENT_TIMESTAMP - INTERVAL '4 days' + INTERVAL '15 minutes'),
    (99000000015012, 78, 'payments_in', 'FAILED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '2 minutes',
     CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '15 minutes'),
    (99000000015013, 77, 'payments_in', 'FAILED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '6 days' + INTERVAL '2 minutes',
     CURRENT_TIMESTAMP - INTERVAL '6 days' + INTERVAL '15 minutes'),
    (99000000015014, 78, 'payments_in', 'FAILED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '7 days' + INTERVAL '2 minutes',
     CURRENT_TIMESTAMP - INTERVAL '7 days' + INTERVAL '15 minutes'),
    (99000000015015, 77, 'payments_in', 'IGNORED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '1 day 2 hours', CURRENT_TIMESTAMP - INTERVAL '1 day 1 hour 58 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 day 1 hour 50 minutes'),
    (99000000015016, 78, 'payments_in', 'IGNORED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '2 days 2 hours', CURRENT_TIMESTAMP - INTERVAL '2 days 1 hour 58 minutes',
     CURRENT_TIMESTAMP - INTERVAL '2 days 1 hour 50 minutes'),
    (99000000015017, 77, 'payments_in', 'IGNORED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '3 days 2 hours', CURRENT_TIMESTAMP - INTERVAL '3 days 1 hour 58 minutes',
     CURRENT_TIMESTAMP - INTERVAL '3 days 1 hour 50 minutes'),
    (99000000015018, 78, 'payments_in', 'IGNORED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '4 days 2 hours', CURRENT_TIMESTAMP - INTERVAL '4 days 1 hour 58 minutes',
     CURRENT_TIMESTAMP - INTERVAL '4 days 1 hour 50 minutes'),
    (99000000015019, 77, 'payments_in', 'IGNORED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '5 days 2 hours', CURRENT_TIMESTAMP - INTERVAL '5 days 1 hour 58 minutes',
     CURRENT_TIMESTAMP - INTERVAL '5 days 1 hour 50 minutes'),
    (99000000015020, 78, 'payments_in', 'IGNORED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '6 days 2 hours', CURRENT_TIMESTAMP - INTERVAL '6 days 1 hour 58 minutes',
     CURRENT_TIMESTAMP - INTERVAL '6 days 1 hour 50 minutes'),
    (99000000015021, 77, 'payments_in', 'IGNORED'::t_interface_job_status_enum,
     CURRENT_TIMESTAMP - INTERVAL '7 days 2 hours', CURRENT_TIMESTAMP - INTERVAL '7 days 1 hour 58 minutes',
     CURRENT_TIMESTAMP - INTERVAL '7 days 1 hour 50 minutes')
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
    (99000000016001, 99000000015001, 'payments-in-camberwell-created-001.csv',
     CURRENT_TIMESTAMP - INTERVAL '7 days' + INTERVAL '1 minute', '[]'::json,
     'NATWEST'::t_interface_file_source_enum, 11, 125.75, false),
    (99000000016002, 99000000015002, 'payments-in-thames-created-002.csv',
     CURRENT_TIMESTAMP - INTERVAL '6 days' + INTERVAL '1 minute', '[]'::json,
     'ALLPAY'::t_interface_file_source_enum, 12, 151.50, false),
    (99000000016003, 99000000015003, 'payments-in-camberwell-created-003.csv',
     CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '1 minute', '[]'::json,
     'DWP'::t_interface_file_source_enum, 13, 177.25, false),
    (99000000016004, 99000000015004, 'payments-in-thames-created-004.csv',
     CURRENT_TIMESTAMP - INTERVAL '4 days' + INTERVAL '1 minute', '[]'::json,
     'ALLPAY'::t_interface_file_source_enum, 14, 203.00, false),
    (99000000016005, 99000000015005, 'payments-in-camberwell-created-005.csv',
     CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '1 minute', '[]'::json,
     'BARCLAYCARD'::t_interface_file_source_enum, 15, 228.75, false),
    (99000000016006, 99000000015006, 'payments-in-thames-created-006.csv',
     CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '1 minute', '[]'::json,
     'ALLPAY_DD'::t_interface_file_source_enum, 16, 254.50, false),
    (99000000016007, 99000000015007, 'payments-in-camberwell-created-007.csv',
     CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '1 minute', '[]'::json,
     'NATWEST'::t_interface_file_source_enum, 17, 280.25, false),
    (99000000016008, 99000000015008, 'payments-in-thames-failed-008.csv',
     CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '1 minute', '[]'::json,
     'DWP'::t_interface_file_source_enum, 18, 306.00, false),
    (99000000016009, 99000000015009, 'payments-in-camberwell-failed-009.csv',
     CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '1 minute', '[]'::json,
     'ALLPAY_DD'::t_interface_file_source_enum, 19, 331.75, false),
    (99000000016010, 99000000015010, 'payments-in-thames-failed-010.csv',
     CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '1 minute', '[]'::json,
     'DWP'::t_interface_file_source_enum, 20, 357.50, false),
    (99000000016011, 99000000015011, 'payments-in-camberwell-failed-011.csv',
     CURRENT_TIMESTAMP - INTERVAL '4 days' + INTERVAL '1 minute', '[]'::json,
     'NATWEST'::t_interface_file_source_enum, 21, 383.25, false),
    (99000000016012, 99000000015012, 'payments-in-thames-failed-012.csv',
     CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '1 minute', '[]'::json,
     'ALLPAY_DD'::t_interface_file_source_enum, 22, 409.00, false),
    (99000000016013, 99000000015013, 'payments-in-camberwell-failed-013.csv',
     CURRENT_TIMESTAMP - INTERVAL '6 days' + INTERVAL '1 minute', '[]'::json,
     'NATWEST'::t_interface_file_source_enum, 23, 434.75, false),
    (99000000016014, 99000000015014, 'payments-in-thames-failed-014.csv',
     CURRENT_TIMESTAMP - INTERVAL '7 days' + INTERVAL '1 minute', '[]'::json,
     'ALLPAY'::t_interface_file_source_enum, 24, 460.50, false),
    (99000000016015, 99000000015015, 'payments-in-camberwell-ignored-015.csv',
     CURRENT_TIMESTAMP - INTERVAL '1 day 2 hours' + INTERVAL '1 minute', '[]'::json,
     'DWP'::t_interface_file_source_enum, 0, 0.00, false),
    (99000000016016, 99000000015016, 'payments-in-thames-ignored-016.csv',
     CURRENT_TIMESTAMP - INTERVAL '2 days 2 hours' + INTERVAL '1 minute', '[]'::json,
     'ALLPAY'::t_interface_file_source_enum, 0, 0.00, false),
    (99000000016017, 99000000015017, 'payments-in-camberwell-ignored-017.csv',
     CURRENT_TIMESTAMP - INTERVAL '3 days 2 hours' + INTERVAL '1 minute', '[]'::json,
     'DWP'::t_interface_file_source_enum, 0, 0.00, false),
    (99000000016018, 99000000015018, 'payments-in-thames-ignored-018.csv',
     CURRENT_TIMESTAMP - INTERVAL '4 days 2 hours' + INTERVAL '1 minute', '[]'::json,
     'ALLPAY_DD'::t_interface_file_source_enum, 0, 0.00, false),
    (99000000016019, 99000000015019, 'payments-in-camberwell-ignored-019.csv',
     CURRENT_TIMESTAMP - INTERVAL '5 days 2 hours' + INTERVAL '1 minute', '[]'::json,
     'NATWEST'::t_interface_file_source_enum, 0, 0.00, false),
    (99000000016020, 99000000015020, 'payments-in-thames-ignored-020.csv',
     CURRENT_TIMESTAMP - INTERVAL '6 days 2 hours' + INTERVAL '1 minute', '[]'::json,
     'BARCLAYCARD'::t_interface_file_source_enum, 0, 0.00, false),
    (99000000016021, 99000000015021, 'payments-in-camberwell-ignored-021.csv',
     CURRENT_TIMESTAMP - INTERVAL '7 days 2 hours' + INTERVAL '1 minute', '[]'::json,
     'ALLPAY_DD'::t_interface_file_source_enum, 0, 0.00, false)
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
    (SELECT GREATEST(COALESCE(MAX(interface_job_id), 1), 99000000015021) FROM public.interface_jobs),
    true
);

SELECT setval(
    'public.interface_file_id_seq',
    (SELECT GREATEST(COALESCE(MAX(interface_file_id), 1), 99000000016021) FROM public.interface_files),
    true
);
