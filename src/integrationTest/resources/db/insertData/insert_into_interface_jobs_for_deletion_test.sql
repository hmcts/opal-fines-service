INSERT INTO interface_jobs (interface_job_id, interface_name)
VALUES
    (987651, 'test_interface'),
    (987652, 'test_interface');

INSERT INTO interface_files (interface_file_id, interface_job_id, file_name)
VALUES
    (987751, 987651, 'test_file_1.csv'),
    (987752, 987652, 'test_file_2.csv');

INSERT INTO interface_messages (
    interface_message_id,
    interface_job_id,
    interface_file_id,
    message_type,
    message_text
)
VALUES
    (987851, 987651, 987751, 'Info', 'test message'),
    (987852, 987652, 987752, 'Info', 'test message');

INSERT INTO tills (
    till_id,
    business_unit_id,
    till_number,
    owned_by,
    interface_file_id,
    owned_by_name
)
VALUES
    (987951, (SELECT MIN(business_unit_id) FROM business_units), 1, 'test-user', 987751, 'Test User'),
    (987952, (SELECT MIN(business_unit_id) FROM business_units), 2, 'test-user', 987752, 'Test User');

INSERT INTO payments_in (
    payment_in_id,
    till_id,
    payment_amount,
    payment_date,
    payment_method,
    destination_type,
    receipt,
    allocated,
    auto_payment
)
VALUES
    (988051, 987951, 12.34, CURRENT_TIMESTAMP, CAST('NC' AS t_payment_method_enum),
     CAST('F' AS t_pi_destination_type_enum), false, false, true),
    (988052, 987952, 12.34, CURRENT_TIMESTAMP, CAST('NC' AS t_payment_method_enum),
     CAST('F' AS t_pi_destination_type_enum), false, false, true);
