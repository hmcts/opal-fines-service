INSERT INTO configuration_items (
    configuration_item_id,
    item_name,
    business_unit_id,
    item_values
) VALUES (
    99000000401003,
    'BANK_ACCOUNTS',
    77,
    '[{"sort_code":"123456","account_number":"01234567","name":"Interface Job Test Bank"}]'::json
), (
    99000000401004,
    'RESULTS_TO_INHIBIT_PAYMENTS',
    77,
    '[]'::json
);

INSERT INTO defendant_accounts (
    defendant_account_id,
    business_unit_id,
    account_number,
    amount_imposed,
    amount_paid,
    account_balance,
    account_status,
    account_type,
    version_number
) VALUES (
    99000000401002,
    77,
    '99000001A',
    100.00,
    0.00,
    100.00,
    'L',
    'Fine',
    1
);

INSERT INTO interface_jobs (
    interface_job_id,
    business_unit_id,
    interface_name,
    status,
    started_datetime
) VALUES (
    99000000401000,
    77,
    'PAYMENTS_IN',
    'PROCESSING',
    CURRENT_TIMESTAMP
);

INSERT INTO interface_files (
    interface_file_id,
    interface_job_id,
    file_name,
    source,
    records
) VALUES (
    99000000401001,
    99000000401000,
    'payments-in-int-01.json',
    'NATWEST',
    '[{
        "receiving_sort_code": "123456",
        "receiving_bank_account_number": "01234567",
        "receiving_account_type": "5",
        "transaction_code": "68",
        "originator_sort_code": "654321",
        "originator_bank_account_number": "98765432",
        "amount_pence": "12345",
        "originator_name": "Test Payer",
        "originator_reference": "99000001A",
        "originator_beneficiary_name": "Test Court"
    }]'::json
);
