/**
* OPAL Program
*
* MODULE      : insert_into_draft_accounts.sql
*
* DESCRIPTION : Inserts rows of data into the DRAFT_ACCOUNTS table.
*
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 27/01/2025    D CLARKE       1.0         Inserts two rows of data into the DEFENDANT_ACCOUNTS table
*
**/
INSERT INTO draft_accounts(
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by, account, account_type, account_id, account_snapshot, account_status, timeline_data, account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number)
VALUES
(
    1,
    77,
    '2024-12-10 16:27:01.023126',
    'user_001',
    NULL,
    NULL,
    '{
        "collection_order_made": true,
        "account_type": "Fine",
        "originator_name": "Police Force",
        "defendant": {
            "surname": "LNAME",
            "dob": "01/01/2000",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 1234,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "123AA"
                    }
                ],
                "date_of_sentence": "2023-11-15"
            }
        ],
        "payment_card_request": true,
        "defendant_type": "Adult",
        "collection_order_made_today": false,
        "account_sentence_date": "2023-12-01",
        "payment_terms": {
            "payment_terms_type_code": "P"
        },
        "enforcement_court_id": 101
    }',
    'Fixed Penalty Registration',
    NULL,
    '{
       "snapshot": "opal-test"
      }',
    'SUBMITTED',
    '[
         {
           "username": "opal-test",
           "status": "Submitted",
           "status_date": "2025-01-09",
           "reason_text": null
         }
       ]',
    NULL,
    'John Smith',
    '2024-12-10 16:27:01.023126',
    NULL,
    NULL,
    0
);

INSERT INTO draft_accounts(
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by, account, account_type, account_id, account_snapshot, account_status, timeline_data, account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number)
VALUES
(
    2,
    77,
    '2024-12-10 16:27:01.023126',
    'user_002',
    NULL,
    NULL,
    '{
        "collection_order_made": true,
        "account_type": "Fine",
        "originator_name": "Police Force",
        "defendant": {
            "surname": "LNAME",
            "dob": "01/01/2000",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 1234,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "123AA"
                    }
                ],
                "date_of_sentence": "2023-11-15"
            }
        ],
        "payment_card_request": true,
        "defendant_type": "Adult",
        "collection_order_made_today": false,
        "account_sentence_date": "2023-12-01",
        "payment_terms": {
            "payment_terms_type_code": "P"
        },
        "enforcement_court_id": 101
    }',
    'Fixed Penalty Registration',
    NULL,
    '{
       "snapshot": "opal-test"
      }',
    'SUBMITTED',
    '[
         {
           "username": "opal-test",
           "status": "Submitted",
           "status_date": "2025-01-09",
           "reason_text": null
         }
       ]',
    NULL,
    'Jane Doe',
    '2024-12-10 16:27:01.023126',
    NULL,
    NULL,
    0
);

INSERT INTO draft_accounts(
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by, account, account_type, account_id, account_snapshot, account_status, timeline_data, account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number)
VALUES
(
    3,
    73,
    '2024-12-10 16:27:01.023126',
    'user_003',
    NULL,
    NULL,
    '{
        "collection_order_made": true,
        "account_type": "Fine",
        "originator_name": "Police Force",
        "defendant": {
            "surname": "LNAME",
            "dob": "01/01/2000",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 1234,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "123AA"
                    }
                ],
                "date_of_sentence": "2023-11-15"
            }
        ],
        "payment_card_request": true,
        "defendant_type": "Adult",
        "collection_order_made_today": false,
        "account_sentence_date": "2023-12-01",
        "payment_terms": {
            "payment_terms_type_code": "P"
        },
        "enforcement_court_id": 101
    }',
    'Fixed Penalty Registration',
    NULL,
    '{
       "snapshot": "opal-test"
      }',
    'PUBLISHING_FAILED',
    '[
         {
           "username": "opal-test",
           "status": "Submitted",
           "status_date": "2025-01-09",
           "reason_text": null
         }
       ]',
    NULL,
    'Joe Bloggs',
    '2024-12-10 16:27:01.023126',
    NULL,
    NULL,
    0
);

INSERT INTO draft_accounts(
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by, account, account_type, account_id, account_snapshot, account_status, timeline_data, account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number)
VALUES
(
    4,
    78,
    '2024-12-10 16:27:01.023126',
    'user_004',
    NULL,
    NULL,
    '{
        "collection_order_made": true,
        "account_type": "Fine",
        "originator_name": "Police Force",
        "defendant": {
            "surname": "LNAME",
            "dob": "01/01/2000",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 1234,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "123AA"
                    }
                ],
                "date_of_sentence": "2023-11-15"
            }
        ],
        "payment_card_request": true,
        "defendant_type": "Adult",
        "collection_order_made_today": false,
        "account_sentence_date": "2023-12-01",
        "payment_terms": {
            "payment_terms_type_code": "P"
        },
        "enforcement_court_id": 101
    }',
    'Fixed Penalty Registration',
    NULL,
    '{
       "snapshot": "opal-test"
      }',
    'SUBMITTED',
    '[
         {
           "username": "opal-test",
           "status": "Submitted",
           "status_date": "2025-01-09",
           "reason_text": null
         }
       ]',
    NULL,
    'Joe Bloggs',
    '2024-12-10 16:27:01.023126',
    NULL,
    NULL,
    0
);

INSERT INTO draft_accounts(
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by, account, account_type, account_id, account_snapshot, account_status, timeline_data, account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number)
VALUES
(
    5,
    78,
    '2024-12-10 16:27:01.023126',
    'user_003',
    NULL,
    NULL,
    '{
        "collection_order_made": true,
        "account_type": "Fine",
        "originator_name": "Police Force",
        "defendant": {
            "surname": "LNAME",
            "dob": "01/01/2000",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 1234,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "123AA"
                    }
                ],
                "date_of_sentence": "2023-11-15"
            }
        ],
        "payment_card_request": true,
        "defendant_type": "Adult",
        "collection_order_made_today": false,
        "account_sentence_date": "2023-12-01",
        "payment_terms": {
            "payment_terms_type_code": "P"
        },
        "enforcement_court_id": 101
    }',
    'Fixed Penalty Registration',
    NULL,
    '{
       "snapshot": "opal-test"
      }',
    'SUBMITTED',
    '[
         {
           "username": "opal-test",
           "status": "Submitted",
           "status_date": "2025-01-09",
           "reason_text": null
         }
       ]',
    NULL,
    'Joe Bloggs',
    '2025-02-02 16:27:01.023126',
    NULL,
    NULL,
    0
);

INSERT INTO draft_accounts(
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by, account, account_type, account_id, account_snapshot, account_status, timeline_data, account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number)
VALUES
(
    6,
    78,
    '2024-12-10 16:27:01.023126',
    'user_003',
    NULL,
    NULL,
    '{
        "collection_order_made": true,
        "account_type": "Fine",
        "originator_name": "Police Force",
        "defendant": {
            "surname": "LNAME",
            "dob": "01/01/2000",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 1234,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "123AA"
                    }
                ],
                "date_of_sentence": "2023-11-15"
            }
        ],
        "payment_card_request": true,
        "defendant_type": "Adult",
        "collection_order_made_today": false,
        "account_sentence_date": "2023-12-01",
        "payment_terms": {
            "payment_terms_type_code": "P"
        },
        "enforcement_court_id": 101
    }',
    'Fixed Penalty Registration',
    NULL,
    '{
       "snapshot": "opal-test"
      }',
    'SUBMITTED',
    '[
         {
           "username": "opal-test",
           "status": "Submitted",
           "status_date": "2025-01-09",
           "reason_text": null
         }
       ]',
    NULL,
    'Joe Bloggs',
    '2025-02-04 16:27:01.023126',
    NULL,
    NULL,
    0
);

INSERT INTO draft_accounts(
    draft_account_id, business_unit_id, created_date, submitted_by, submitted_by_name,
    validated_date, validated_by, account_type, account_id, account_status,
    account_number, account_status_date, status_message, validated_by_name, version_number,
    account,
    account_snapshot,
    timeline_data
)
VALUES
(
    7, 78, '2024-12-10 16:27:01.023126', 'user_003', 'Joe Bloggs',
    NULL, NULL, 'Fixed Penalty Registration', NULL, 'SUBMITTED',
    NULL, '2025-02-03 16:27:01.023126', NULL, NULL, 0,
    '{
        "collection_order_made": true,
        "account_type": "Fine",
        "originator_name": "Police Force",
        "defendant": {
            "surname": "LNAME",
            "dob": "01/01/2000",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 1234,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "123AA"
                    }
                ],
                "date_of_sentence": "2023-11-15"
            }
        ],
        "payment_card_request": true,
        "defendant_type": "Adult",
        "collection_order_made_today": false,
        "account_sentence_date": "2023-12-01",
        "payment_terms": {
            "payment_terms_type_code": "P"
        },
        "enforcement_court_id": 650000000045
    }',
    '{
       "snapshot": "opal-test"
    }',
    '[
        {
           "username": "opal-test",
           "status": "Submitted",
           "status_date": "2025-01-09",
           "reason_text": null
        }
    ]'
), (
    8, 78, '2024-12-10 16:27:01.023126', 'user_003', 'Joe Bloggs',
    NULL, NULL, 'Fixed Penalty Registration', NULL, 'SUBMITTED',
    NULL, '2025-02-03 16:27:01.023126', NULL, NULL, 0,
    '{
        "collection_order_made": true,
        "account_type": "Fine",
        "originator_name": "Police Force",
        "defendant": {
            "surname": "LNAME",
            "dob": "01/01/2000",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 1234,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "123AA"
                    }
                ],
                "date_of_sentence": "2023-11-15"
            }
        ],
        "payment_card_request": true,
        "defendant_type": "Adult",
        "collection_order_made_today": false,
        "account_sentence_date": "2023-12-01",
        "payment_terms": {
            "payment_terms_type_code": "P"
        },
        "enforcement_court_id": 650000000045
    }',
    '{
       "snapshot": "opal-test"
    }',
    '[
        {
           "username": "opal-test",
           "status": "Submitted",
           "status_date": "2025-01-09",
           "reason_text": null
        }
    ]'
);

SELECT setval('draft_account_id_seq', 99);
