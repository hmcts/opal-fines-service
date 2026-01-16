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
            "dob": "2000-01-01",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 30002,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "FO"
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
            "dob": "2000-01-01",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 30002,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "FO"
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
            "dob": "2000-01-01",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 30002,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "FO"
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
            "dob": "2000-01-01",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 30002,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "FO"
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
            "dob": "2000-01-01",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 30002,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "FO"
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
            "dob": "2000-01-01",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 30002,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "FO"
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
            "dob": "2000-01-01",
            "company_flag": false,
            "address_line_1": "123 Elm Street",
            "forenames": "FNAME"
        },
        "originator_id": 1234,
        "offences": [
            {
                "offence_id": 30002,
                "impositions": [
                    {
                        "amount_paid": 200,
                        "amount_imposed": 500,
                        "result_id": "FO"
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
    8, 65, '2024-12-10', 'user_003', 'Joe Bloggs',
    NULL, NULL, 'Fixed Penalty Registration', NULL, 'SUBMITTED',
    NULL, '2025-02-03', NULL, NULL, 0,
    '{
        "account_type": "Fixed Penalty",
        "defendant_type": "Adult",
        "originator_name": "LJS",
        "originator_id": 12345,
        "enforcement_court_id": 650000000045,
        "prosecutor_case_reference": "ABC123",
        "payment_card_request": true,
        "account_sentence_date": "2024-12-12",
        "collection_order_made": true,
        "collection_order_made_today": false,
        "collection_order_date": "2024-12-01",
        "suspended_committal_date": "2024-12-15",
        "defendant": {
            "company_flag": false,
            "title": "Mr",
            "surname": "Doe",
            "company_name": "",
            "forenames": "John",
            "dob": "1980-01-01",
            "address_line_1": "123 Main St",
            "address_line_2": "Apt 4B",
            "address_line_3": "",
            "address_line_4": "",
            "address_line_5": "",
            "post_code": "12345",
            "telephone_number_home": "555-1234",
            "telephone_number_business": "555-5678",
            "telephone_number_mobile": "555-8765",
            "email_address_1": "john.doe@example.com",
            "email_address_2": "j.doe@example.com",
            "national_insurance_number": "AB123456C",
            "driving_licence_number": "D1234567",
            "pnc_id": "PNC12345",
            "nationality_1": "British",
            "nationality_2": "American",
            "ethnicity_self_defined": "White",
            "ethnicity_observed": "White",
            "cro_number": "CRO12345",
            "occupation": "Engineer",
            "gender": "Male",
            "custody_status": "Released",
            "prison_number": "P12345",
            "interpreter_lang": "English",
            "debtor_detail": {
                "vehicle_make": "Toyota",
                "vehicle_registration_mark": "XYZ 1234",
                "document_language": "EN",
                "hearing_language": "EN",
                "employee_reference": "EMP123",
                "employer_company_name": "ABC Corp",
                "employer_address_line_1": "456 Corporate Blvd",
                "employer_address_line_2": "Suite 100",
                "employer_address_line_3": "",
                "employer_address_line_4": "",
                "employer_address_line_5": "",
                "employer_post_code": "67890",
                "employer_telephone_number": "555-9876",
                "employer_email_address": "hr@abccorp.com",
                "aliases": [
                    {
                        "alias_forenames": "Johnny",
                        "alias_surname": "Doe",
                        "alias_company_name": ""
                    }
                ]
            },
            "parent_guardian": {
                "company_flag": false,
                "company_name": "",
                "surname": "Doe",
                "forenames": "Jane",
                "dob": "1950-01-01",
                "national_insurance_number": "AB654321C",
                "address_line_1": "789 Parent St",
                "address_line_2": "",
                "address_line_3": "",
                "address_line_4": "",
                "address_line_5": "",
                "post_code": "54321",
                "telephone_number_home": "555-4321",
                "telephone_number_business": "555-8765",
                "telephone_number_mobile": "555-5678",
                "email_address_1": "jane.doe@example.com",
                "email_address_2": "j.doe@example.com",
                "debtor_detail": {
                    "vehicle_make": "Honda",
                    "vehicle_registration_mark": "ABC 9876",
                    "document_language": "English",
                    "hearing_language": "English",
                    "employee_reference": "EMP456",
                    "employer_company_name": "XYZ Corp",
                    "employer_address_line_1": "123 Business Rd",
                    "employer_address_line_2": "Floor 2",
                    "employer_address_line_3": "",
                    "employer_address_line_4": "",
                    "employer_address_line_5": "",
                    "employer_post_code": "98765",
                    "employer_telephone_number": "555-6789",
                    "employer_email_address": "hr@xyzcorp.com",
                    "aliases": [
                        {
                            "alias_forenames": "Janie",
                            "alias_surname": "Doe",
                            "alias_company_name": ""
                        }
                    ]
                }
            }
        },
        "offences": [
            {
                "date_of_sentence": "2023-01-01",
                "imposing_court_id": 650000000160,
                "offence_id": 30000,
                "impositions": [
                    {
                        "result_id": "FO",
                        "amount_imposed": 200.00,
                        "amount_paid": 50.00,
                        "major_creditor_id": 101112,
                        "minor_creditor": {
                            "company_flag": false,
                            "title": "Mr",
                            "company_name": "",
                            "surname": "Smith",
                            "forenames": "James",
                            "dob": "1970-01-01",
                            "address_line_1": "321 Minor St",
                            "address_line_2": "",
                            "address_line_3": "",
                            "address_line_4": "",
                            "address_line_5": "",
                            "post_code": "67890",
                            "telephone": "555-1234",
                            "email_address": "james.smith@example.com",
                            "payout_hold": false,
                            "pay_by_bacs": true,
                            "bank_account_type": "S",
                            "bank_sort_code": "123456",
                            "bank_account_number": "12345678",
                            "bank_account_name": "James Smith",
                            "bank_account_ref": "REF123"
                        }
                    },
                    {
                        "result_id": "FCPC",
                        "amount_imposed": 408.00,
                        "amount_paid": 61.00,
                        "major_creditor_id": 101112,
                        "minor_creditor": {
                            "company_flag": false,
                            "title": "Mr",
                            "company_name": "",
                            "surname": "Jackson",
                            "forenames": "Frank",
                            "dob": "1968-11-10",
                            "address_line_1": "97 Broad Road",
                            "address_line_2": "",
                            "address_line_3": "",
                            "address_line_4": "",
                            "address_line_5": "",
                            "post_code": "67890",
                            "telephone": "555-1234",
                            "email_address": "frank.jackson@yahoo.com",
                            "payout_hold": false,
                            "pay_by_bacs": true,
                            "bank_account_type": "C",
                            "bank_sort_code": "123456",
                            "bank_account_number": "12345678",
                            "bank_account_name": "James Smith",
                            "bank_account_ref": "REF123"
                        }
                    }
                ]
            },
            {
                "date_of_sentence": "2024-11-21",
                "imposing_court_id": 650000000160,
                "offence_id": 30001,
                "impositions": [
                    {
                        "result_id": "FCOST",
                        "amount_imposed": 1678.00,
                        "amount_paid": 77.00,
                        "major_creditor_id": 650000000041,
                        "minor_creditor": null
                    },
                    {
                        "result_id": "FCOST",
                        "amount_imposed": 3700.00,
                        "amount_paid": 120.00,
                        "major_creditor_id": null,
                        "minor_creditor": {
                            "company_flag": false,
                            "title": "Mr",
                            "company_name": "",
                            "surname": "Mohammed",
                            "forenames": "Koli Naser Ball",
                            "dob": "1956-09-05",
                            "address_line_1": "754 Gated Avenue",
                            "address_line_2": "",
                            "address_line_3": "",
                            "address_line_4": "",
                            "address_line_5": "",
                            "post_code": "67890",
                            "telephone": "555-1234",
                            "email_address": "mohammed.koli@aol.net",
                            "payout_hold": false,
                            "pay_by_bacs": true,
                            "bank_account_type": "S",
                            "bank_sort_code": "123456",
                            "bank_account_number": "12345678",
                            "bank_account_name": "James Smith",
                            "bank_account_ref": "REF123"
                        }
                    }
                ]
            },
            {
                "date_of_sentence": "2024-11-21",
                "imposing_court_id": 650000000160,
                "offence_id": 30002,
                "impositions": [
                    {
                        "result_id": "FCMP",
                        "amount_imposed": 6926.00,
                        "amount_paid": 743.00,
                        "major_creditor_id": 650000000021,
                        "minor_creditor": null
                    },
                    {
                        "result_id": "FCOMP",
                        "amount_imposed": 836.00,
                        "amount_paid": 183.00,
                        "major_creditor_id": null,
                        "minor_creditor": {
                            "company_flag": false,
                            "title": "Dr",
                            "company_name": "",
                            "surname": "Emma",
                            "forenames": "Jane Fox",
                            "dob": "1979-05-07",
                            "address_line_1": "The Cottage",
                            "address_line_2": "45 Silverstorm Road",
                            "address_line_3": "Sutton Coldfield",
                            "address_line_4": "West Midlands",
                            "address_line_5": "",
                            "post_code": "BX11FS",
                            "telephone": "0121422893",
                            "email_address": "emmajfox@yahoo.co.uk",
                            "payout_hold": false,
                            "pay_by_bacs": true,
                            "bank_account_type": "S",
                            "bank_sort_code": "040105",
                            "bank_account_number": "654833",
                            "bank_account_name": "Instant Save",
                            "bank_account_ref": "IS2025"
                        }
                    }
                ]
            }
        ],
        "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2023-06-01",
            "time_of_issue": "14:30",
            "fp_registration_number": "REG123",
            "notice_to_owner_hirer": "Owner",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "D1234567"
        },
        "payment_terms": {
            "payment_terms_type_code": "B",
            "effective_date": null,
            "instalment_period": "M",
            "lump_sum_amount": 500.00,
            "instalment_amount": 50.00,
            "default_days_in_jail": 30,
            "enforcements": [
                {
                    "result_id": "NOENF",
                    "enforcement_result_responses": [
                        {
                            "parameter_name": "Response2",
                            "response": "No"
                        }
                    ]
                },
                {
                    "result_id": "PRIS",
                    "enforcement_result_responses": [
                        {
                            "parameter_name": "Response1",
                            "response": "Yes"
                        }
                    ]
                },
                {
                    "result_id": "COLLO",
                    "enforcement_result_responses": [
                        {
                            "parameter_name": "Response3",
                            "response": "Yes"
                        }
                    ]
                }
            ]
        },
        "account_notes": [
            {
                "account_note_serial": 1,
                "account_note_text": "First comment",
                "note_type": "AC"
            },
            {
                "account_note_serial": 3,
                "account_note_text": "Second AA note",
                "note_type": "AA"
            },
            {
                "account_note_serial": 2,
                "account_note_text": "First AA note",
                "note_type": "AA"
            }
        ]
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
    9, 65, '2024-12-10', 'user_003', 'Joe Bloggs',
    NULL, NULL, 'Fixed Penalty Registration', NULL, 'SUBMITTED',
    NULL, '2025-02-03', NULL, NULL, 0,
    '{
            "account_type": "Fixed Penalty",
            "defendant_type": "Adult",
            "originator_name": "LJS",
            "originator_id": 12345,
            "enforcement_court_id": 650000000045,
            "prosecutor_case_reference": "ABC123",
            "payment_card_request": true,
            "account_sentence_date": "2024-12-12",
            "collection_order_made": true,
            "collection_order_made_today": false,
            "collection_order_date": "2024-12-01",
            "suspended_committal_date": "2024-12-15",
            "defendant": {
                "company_flag": false,
                "title": "Mr",
                "surname": "Doe",
                "company_name": "",
                "forenames": "John",
                "dob": "1980-01-01",
                "address_line_1": "123 Main St",
                "address_line_2": "Apt 4B",
                "address_line_3": "",
                "address_line_4": "",
                "address_line_5": "",
                "post_code": "12345",
                "telephone_number_home": "555-1234",
                "telephone_number_business": "555-5678",
                "telephone_number_mobile": "555-8765",
                "email_address_1": "john.doe@example.com",
                "email_address_2": "j.doe@example.com",
                "national_insurance_number": "AB123456C",
                "driving_licence_number": "D1234567",
                "pnc_id": "PNC12345",
                "nationality_1": "British",
                "nationality_2": "American",
                "ethnicity_self_defined": "White",
                "ethnicity_observed": "White",
                "cro_number": "CRO12345",
                "occupation": "Engineer",
                "gender": "Male",
                "custody_status": "Released",
                "prison_number": "P12345",
                "interpreter_lang": "English",
                "debtor_detail": {
                    "vehicle_make": "Toyota",
                    "vehicle_registration_mark": "XYZ 1234",
                    "document_language": "EN",
                    "hearing_language": "EN",
                    "employee_reference": "EMP123",
                    "employer_company_name": "ABC Corp",
                    "employer_address_line_1": "456 Corporate Blvd",
                    "employer_address_line_2": "Suite 100",
                    "employer_address_line_3": "",
                    "employer_address_line_4": "",
                    "employer_address_line_5": "",
                    "employer_post_code": "67890",
                    "employer_telephone_number": "555-9876",
                    "employer_email_address": "hr@abccorp.com",
                    "aliases": [
                        {
                            "alias_forenames": "Johnny",
                            "alias_surname": "Doe",
                            "alias_company_name": ""
                        }
                    ]
                },
                "parent_guardian": {
                    "company_flag": false,
                    "company_name": "",
                    "surname": "Doe",
                    "forenames": "Jane",
                    "dob": "1950-01-01",
                    "national_insurance_number": "AB654321C",
                    "address_line_1": "789 Parent St",
                    "address_line_2": "",
                    "address_line_3": "",
                    "address_line_4": "",
                    "address_line_5": "",
                    "post_code": "54321",
                    "telephone_number_home": "555-4321",
                    "telephone_number_business": "555-8765",
                    "telephone_number_mobile": "555-5678",
                    "email_address_1": "jane.doe@example.com",
                    "email_address_2": "j.doe@example.com",
                    "debtor_detail": {
                        "vehicle_make": "Honda",
                        "vehicle_registration_mark": "ABC 9876",
                        "document_language": "English",
                        "hearing_language": "English",
                        "employee_reference": "EMP456",
                        "employer_company_name": "XYZ Corp",
                        "employer_address_line_1": "123 Business Rd",
                        "employer_address_line_2": "Floor 2",
                        "employer_address_line_3": "",
                        "employer_address_line_4": "",
                        "employer_address_line_5": "",
                        "employer_post_code": "98765",
                        "employer_telephone_number": "555-6789",
                        "employer_email_address": "hr@xyzcorp.com",
                        "aliases": [
                            {
                                "alias_forenames": "Janie",
                                "alias_surname": "Doe",
                                "alias_company_name": ""
                            }
                        ]
                    }
                }
            },
            "offences": [
                {
                    "date_of_sentence": "2023-01-01",
                    "imposing_court_id": 650000000160,
                    "offence_id": 30000,
                    "impositions": [
                        {
                            "result_id": "FO",
                            "amount_imposed": 200.00,
                            "amount_paid": 50.00,
                            "major_creditor_id": 101112,
                            "minor_creditor": {
                                "company_flag": false,
                                "title": "Mr",
                                "company_name": "",
                                "surname": "Smith",
                                "forenames": "James",
                                "dob": "1970-01-01",
                                "address_line_1": "321 Minor St",
                                "address_line_2": "",
                                "address_line_3": "",
                                "address_line_4": "",
                                "address_line_5": "",
                                "post_code": "67890",
                                "telephone": "555-1234",
                                "email_address": "james.smith@example.com",
                                "payout_hold": false,
                                "pay_by_bacs": true,
                                "bank_account_type": "S",
                                "bank_sort_code": "123456",
                                "bank_account_number": "12345678",
                                "bank_account_name": "James Smith",
                                "bank_account_ref": "REF123"
                            }
                        },
                        {
                            "result_id": "FCPC",
                            "amount_imposed": 408.00,
                            "amount_paid": 61.00,
                            "major_creditor_id": 101112,
                            "minor_creditor": {
                                "company_flag": false,
                                "title": "Mr",
                                "company_name": "",
                                "surname": "Jackson",
                                "forenames": "Frank",
                                "dob": "1968-11-10",
                                "address_line_1": "97 Broad Road",
                                "address_line_2": "",
                                "address_line_3": "",
                                "address_line_4": "",
                                "address_line_5": "",
                                "post_code": "67890",
                                "telephone": "555-1234",
                                "email_address": "frank.jackson@yahoo.com",
                                "payout_hold": false,
                                "pay_by_bacs": true,
                                "bank_account_type": "C",
                                "bank_sort_code": "123456",
                                "bank_account_number": "12345678",
                                "bank_account_name": "James Smith",
                                "bank_account_ref": "REF123"
                            }
                        }
                    ]
                },
                {
                    "date_of_sentence": "2024-11-21",
                    "imposing_court_id": 650000000160,
                    "offence_id": 30001,
                    "impositions": [
                        {
                            "result_id": "FCOST",
                            "amount_imposed": 1678.00,
                            "amount_paid": 77.00,
                            "major_creditor_id": 650000000041,
                            "minor_creditor": null
                        },
                        {
                            "result_id": "FCOST",
                            "amount_imposed": 3700.00,
                            "amount_paid": 120.00,
                            "major_creditor_id": null,
                            "minor_creditor": {
                                "company_flag": false,
                                "title": "Mr",
                                "company_name": "",
                                "surname": "Mohammed",
                                "forenames": "Koli Naser Ball",
                                "dob": "1956-09-05",
                                "address_line_1": "754 Gated Avenue",
                                "address_line_2": "",
                                "address_line_3": "",
                                "address_line_4": "",
                                "address_line_5": "",
                                "post_code": "67890",
                                "telephone": "555-1234",
                                "email_address": "mohammed.koli@aol.net",
                                "payout_hold": false,
                                "pay_by_bacs": true,
                                "bank_account_type": "S",
                                "bank_sort_code": "123456",
                                "bank_account_number": "12345678",
                                "bank_account_name": "James Smith",
                                "bank_account_ref": "REF123"
                            }
                        }
                    ]
                },
                {
                    "date_of_sentence": "2024-11-21",
                    "imposing_court_id": 650000000160,
                    "offence_id": 30002,
                    "impositions": [
                        {
                            "result_id": "FCMP",
                            "amount_imposed": 6926.00,
                            "amount_paid": 743.00,
                            "major_creditor_id": 650000000021,
                            "minor_creditor": null
                        },
                        {
                            "result_id": "FCOMP",
                            "amount_imposed": 836.00,
                            "amount_paid": 183.00,
                            "major_creditor_id": null,
                            "minor_creditor": {
                                "company_flag": false,
                                "title": "Dr",
                                "company_name": "",
                                "surname": "Emma",
                                "forenames": "Jane Fox",
                                "dob": "1979-05-07",
                                "address_line_1": "The Cottage",
                                "address_line_2": "45 Silverstorm Road",
                                "address_line_3": "Sutton Coldfield",
                                "address_line_4": "West Midlands",
                                "address_line_5": "",
                                "post_code": "BX11FS",
                                "telephone": "0121422893",
                                "email_address": "emmajfox@yahoo.co.uk",
                                "payout_hold": false,
                                "pay_by_bacs": true,
                                "bank_account_type": "S",
                                "bank_sort_code": "040105",
                                "bank_account_number": "654833",
                                "bank_account_name": "Instant Save",
                                "bank_account_ref": "IS2025"
                            }
                        }
                    ]
                }
            ],
            "fp_ticket_detail": {
                "notice_number": "FP12345",
                "date_of_issue": "2023-06-01",
                "time_of_issue": "14:30",
                "fp_registration_number": "REG123",
                "notice_to_owner_hirer": "Owner",
                "place_of_offence": "Main Street",
                "fp_driving_licence_number": "D1234567"
            },
            "payment_terms": {
                "payment_terms_type_code": "B",
                "effective_date": null,
                "instalment_period": "M",
                "lump_sum_amount": 500.00,
                "instalment_amount": 50.00,
                "default_days_in_jail": 30,
                "enforcements": [
                    {
                        "result_id": "NOENF",
                        "enforcement_result_responses": [
                            {
                                "parameter_name": "Response2",
                                "response": "No"
                            }
                        ]
                    },
                    {
                        "result_id": "PRIS",
                        "enforcement_result_responses": [
                            {
                                "parameter_name": "Response1",
                                "response": "Yes"
                            }
                        ]
                    },
                    {
                        "result_id": "COLLO",
                        "enforcement_result_responses": [
                            {
                                "parameter_name": "Response3",
                                "response": "Yes"
                            }
                        ]
                    }
                ]
            },
            "account_notes": [
                {
                    "account_note_serial": 1,
                    "account_note_text": "First comment",
                    "note_type": "AC"
                },
                {
                    "account_note_serial": 3,
                    "account_note_text": "Second AA note",
                    "note_type": "AA"
                },
                {
                    "account_note_serial": 2,
                    "account_note_text": "First AA note",
                    "note_type": "AA"
                }
            ]
        }',
    '{
       "snapshot": "opal-test"
    }',
    '[ ]'
);
INSERT INTO draft_accounts (
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by,
    account, account_type, account_id, account_snapshot, account_status, timeline_data,
    account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number
) VALUES
    (
        100,
        65,
        now(),
        'test_user_100',
        NULL,
        NULL,
        '{
          "account_type": "Fixed Penalty",
          "defendant_type": "adultOrYouthOnly",
          "originator_name": "LJS",
          "originator_id": 123,
          "prosecutor_case_reference": null,
          "enforcement_court_id": 456,
          "collection_order_made": null,
          "collection_order_made_today": null,
          "collection_order_date": null,
          "suspended_committal_date": null,
          "payment_card_request": false,
          "account_sentence_date": "2025-10-01",
          "defendant": {
            "company_flag": false,
            "title": null,
            "surname": "Smith",
            "company_name": null,
            "forenames": "John",
            "dob": "1985-07-20",
            "address_line_1": "1 Justice Road"
          },
          "offences": [
            {
              "date_of_sentence": "2025-10-01",
              "imposing_court_id": 789,
              "offence_id": 10,
              "impositions": [
                {
                  "result_id": "FINE",
                  "amount_imposed": 100.00,
                  "amount_paid": 0.00,
                  "major_creditor_id": null,
                  "minor_creditor": null
                }
              ]
            }
          ],
          "fp_ticket_detail": null,
          "payment_terms": { "payment_terms_type_code": "B" },
          "account_notes": null
        }',
        'Fixed Penalty Registration',
        NULL,
        '{"snapshot":"test"}',
        'SUBMITTED',
        '[
          {
            "username": "johndoe123",
            "status": "Active",
            "status_date": "2025-10-15",
            "reason_text": "Account created for testing"
          }
        ]',
        NULL,
        'Business User 1',
        now(),
        NULL,
        NULL,
        0
    )
ON CONFLICT (draft_account_id) DO UPDATE
    SET
        account = EXCLUDED.account,
        account_type = EXCLUDED.account_type,
        account_snapshot = EXCLUDED.account_snapshot,
        account_status = EXCLUDED.account_status,
        timeline_data = EXCLUDED.timeline_data,
        submitted_by_name = EXCLUDED.submitted_by_name,
        account_status_date = EXCLUDED.account_status_date,
        version_number = EXCLUDED.version_number;


INSERT INTO draft_accounts (
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by,
    account, account_type, account_id, account_snapshot, account_status, timeline_data,
    account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number
) VALUES
    (
        101,
        65,
        now(),
        'test_user_101',
        NULL,
        NULL,
        '{
          "account_type": "Fines",
          "defendant_type": "adultOrYouthOnly",
          "originator_name": "LJS",
          "originator_id": 123,
          "prosecutor_case_reference": null,
          "enforcement_court_id": 456,
          "collection_order_made": null,
          "collection_order_made_today": null,
          "collection_order_date": null,
          "suspended_committal_date": null,
          "payment_card_request": false,
          "account_sentence_date": "2025-10-01",
          "defendant": {
            "company_flag": false,
            "title": null,
            "surname": "Smith",
            "company_name": null,
            "forenames": "John",
            "dob": "1985-07-20",
            "address_line_1": "1 Justice Road",
            "address_line_2": null,
            "address_line_3": null,
            "address_line_4": null,
            "address_line_5": null,
            "post_code": "AB1 2CD",
            "telephone_number_home": null,
            "telephone_number_business": null,
            "telephone_number_mobile": "07123456789",
            "email_address_1": "john.smith@example.com",
            "email_address_2": null,
            "national_insurance_number": "QQ123456C",
            "driving_licence_number": null,
            "pnc_id": null,
            "nationality_1": "British",
            "nationality_2": null,
            "ethnicity_self_defined": null,
            "ethnicity_observed": null,
            "cro_number": null,
            "occupation": "Engineer",
            "gender": "M",
            "custody_status": null,
            "prison_number": null,
            "interpreter_lang": null,
            "debtor_detail": null,
            "parent_guardian": null
          },
          "offences": [
            {
              "date_of_sentence": "2025-10-01",
              "imposing_court_id": 789,
              "offence_id": 10,
              "impositions": [
                {
                  "result_id": "FINE",
                  "amount_imposed": 100,
                  "amount_paid": 0,
                  "major_creditor_id": null,
                  "minor_creditor": {
                    "company_flag": false,
                    "title": null,
                    "company_name": null,
                    "surname": "Minor",
                    "forenames": "Alice",
                    "dob": "2010-05-05",
                    "address_line_1": "5 Minor St",
                    "address_line_2": null,
                    "address_line_3": null,
                    "address_line_4": null,
                    "address_line_5": null,
                    "post_code": "MN1 2OP",
                    "telephone": null,
                    "email_address": null,
                    "payout_hold": false,
                    "pay_by_bacs": false,
                    "bank_account_type": null,
                    "bank_sort_code": null,
                    "bank_account_number": null,
                    "bank_account_name": null,
                    "bank_account_ref": null
                  }
                }
              ]
            }
          ],
          "fp_ticket_detail": null,
          "payment_terms": {
            "payment_terms_type_code": "B",
            "effective_date": null,
            "instalment_period": null,
            "lump_sum_amount": null,
            "instalment_amount": null,
            "default_days_in_jail": null,
            "enforcements": null
          },
          "account_notes": null
        }',
        'Fixed Penalty Registration',
        NULL,
        '{"snapshot":"test"}',
        'SUBMITTED',
        '[
          {
            "username": "alice_creator",
            "status": "Active",
            "status_date": "2025-09-02",
            "reason_text": "Account for minor-creditor test"
          }
        ]',
        NULL,
        'Business User 2',
        now(),
        NULL,
        NULL,
        0
    )
ON CONFLICT (draft_account_id) DO UPDATE
    SET
        account = EXCLUDED.account,
        account_type = EXCLUDED.account_type,
        account_snapshot = EXCLUDED.account_snapshot,
        account_status = EXCLUDED.account_status,
        timeline_data = EXCLUDED.timeline_data,
        submitted_by_name = EXCLUDED.submitted_by_name,
        account_status_date = EXCLUDED.account_status_date,
        version_number = EXCLUDED.version_number;


INSERT INTO draft_accounts (
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by,
    account, account_type, account_id, account_snapshot, account_status, timeline_data,
    account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number
) VALUES
    (
        102,
        66,
        now(),
        'test_user_102',
        NULL,
        NULL,
        '{
          "account_type": "Fixed Penalty",
          "defendant_type": "pgToPay",
          "originator_name": "LJS",
          "originator_id": 113,
          "defendant": {
            "company_flag": false,
            "surname": "Taylor",
            "forenames": "Sam",
            "address_line_1": "4 Test Ave",
            "parent_guardian": {
              "company_flag": false,
              "surname": "TaylorSr",
              "forenames": "Pat",
              "address_line_1": "5 Parent St",
              "dob": "1950-01-01",
              "post_code": "PT1 1AA"
            }
          },
          "offences": [
            {
              "date_of_sentence": "2024-03-01",
              "imposing_court_id": 789,
              "offence_id": 1003,
              "impositions": [
                {
                  "result_id": "FO",
                  "amount_imposed": 75.00,
                  "amount_paid": 0.00,
                  "major_creditor_id": null,
                  "minor_creditor": null
                }
              ]
            }
          ],
          "payment_terms": { "payment_terms_type_code": "B" }
        }',
        'Fixed Penalty Registration',
        NULL,
        '{"snapshot":"test"}',
        'SUBMITTED',
        '[
          {
            "username": "johndoe456",
            "status": "Active",
            "status_date": "2025-10-20",
            "reason_text": "Account for pgToPay test"
          }
        ]',
        NULL,
        'Business User 3',
        now(),
        NULL,
        NULL,
        0
    )
ON CONFLICT (draft_account_id) DO UPDATE
    SET
        account = EXCLUDED.account,
        account_type = EXCLUDED.account_type,
        account_snapshot = EXCLUDED.account_snapshot,
        account_status = EXCLUDED.account_status,
        timeline_data = EXCLUDED.timeline_data,
        submitted_by_name = EXCLUDED.submitted_by_name,
        account_status_date = EXCLUDED.account_status_date,
        version_number = EXCLUDED.version_number;


INSERT INTO draft_accounts (
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by,
    account, account_type, account_id, account_snapshot, account_status, timeline_data,
    account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number
) VALUES
    (
        103,
        78,
        now(),
        'test_user_103',
        NULL,
        NULL,
        '{
          "account_type": "Fixed Penalty",
          "defendant_type": "company",
          "originator_name": "LJS",
          "originator_id": 12345,
          "enforcement_court_id": 650000000045,
          "prosecutor_case_reference": "ABC123",
          "payment_card_request": true,
          "account_sentence_date": "2024-12-12",
          "collection_order_made": true,
          "collection_order_made_today": false,
          "collection_order_date": "2024-12-01",
          "suspended_committal_date": "2024-12-15",
          "defendant": {
            "company_flag": true,
            "title": "",
            "surname": "",
            "company_name": "wawd",
            "forenames": "",
            "dob": "1980-01-01",
            "address_line_1": "123 Main St",
            "address_line_2": "Apt 4B",
            "address_line_3": "",
            "address_line_4": "",
            "address_line_5": "",
            "post_code": "12345",
            "telephone_number_home": "555-1234",
            "telephone_number_business": "555-5678",
            "telephone_number_mobile": "555-8765",
            "email_address_1": "john.doe@example.com",
            "email_address_2": "j.doe@example.com",
            "national_insurance_number": "AB123456C",
            "driving_licence_number": "D1234567",
            "pnc_id": "PNC12345",
            "nationality_1": "British",
            "nationality_2": "American",
            "ethnicity_self_defined": "White",
            "ethnicity_observed": "White",
            "cro_number": "CRO12345",
            "occupation": "Engineer",
            "gender": "Male",
            "custody_status": "Released",
            "prison_number": "P12345",
            "interpreter_lang": "English",
            "debtor_detail": {
              "vehicle_make": "Toyota",
              "vehicle_registration_mark": "XYZ 1234",
              "document_language": "EN",
              "hearing_language": "EN",
              "employee_reference": "EMP123",
              "employer_company_name": "ABC Corp",
              "employer_address_line_1": "456 Corporate Blvd",
              "employer_address_line_2": "Suite 100",
              "employer_address_line_3": "",
              "employer_address_line_4": "",
              "employer_address_line_5": "",
              "employer_post_code": "67890",
              "employer_telephone_number": "555-9876",
              "employer_email_address": "hr@abccorp.com",
              "aliases": [
                {
                  "alias_forenames": "Johnny",
                  "alias_surname": "Doe",
                  "alias_company_name": ""
                }
              ]
            },
            "parent_guardian": {
              "company_flag": false,
              "company_name": "",
              "surname": "Doe",
              "forenames": "Jane",
              "dob": "1950-01-01",
              "national_insurance_number": "AB654321C",
              "address_line_1": "789 Parent St",
              "address_line_2": "",
              "address_line_3": "",
              "address_line_4": "",
              "address_line_5": "",
              "post_code": "54321",
              "telephone_number_home": "555-4321",
              "telephone_number_business": "555-8765",
              "telephone_number_mobile": "555-5678",
              "email_address_1": "jane.doe@example.com",
              "email_address_2": "j.doe@example.com",
              "debtor_detail": {
                "vehicle_make": "Honda",
                "vehicle_registration_mark": "ABC 9876",
                "document_language": "English",
                "hearing_language": "English",
                "employee_reference": "EMP456",
                "employer_company_name": "XYZ Corp",
                "employer_address_line_1": "123 Business Rd",
                "employer_address_line_2": "Floor 2",
                "employer_address_line_3": "",
                "employer_address_line_4": "",
                "employer_address_line_5": "",
                "employer_post_code": "98765",
                "employer_telephone_number": "555-6789",
                "employer_email_address": "hr@xyzcorp.com",
                "aliases": [
                  {
                    "alias_forenames": "Janie",
                    "alias_surname": "Doe",
                    "alias_company_name": ""
                  }
                ]
              }
            }
          },
          "offences": [
            {
              "date_of_sentence": "2023-01-01",
              "imposing_court_id": 650000000160,
              "offence_id": 30000,
              "impositions": [
                {
                  "result_id": "FO",
                  "amount_imposed": 200.00,
                  "amount_paid": 50.00,
                  "major_creditor_id": 101112,
                  "minor_creditor": {
                    "company_flag": false,
                    "title": "Mr",
                    "company_name": "",
                    "surname": "Smith",
                    "forenames": "James",
                    "dob": "1970-01-01",
                    "address_line_1": "321 Minor St",
                    "address_line_2": "",
                    "address_line_3": "",
                    "address_line_4": "",
                    "address_line_5": "",
                    "post_code": "67890",
                    "telephone": "555-1234",
                    "email_address": "james.smith@example.com",
                    "payout_hold": false,
                    "pay_by_bacs": true,
                    "bank_account_type": "S",
                    "bank_sort_code": "123456",
                    "bank_account_number": "12345678",
                    "bank_account_name": "James Smith",
                    "bank_account_ref": "REF123"
                  }
                },
                {
                  "result_id": "FCPC",
                  "amount_imposed": 408.00,
                  "amount_paid": 61.00,
                  "major_creditor_id": 101112,
                  "minor_creditor": {
                    "company_flag": false,
                    "title": "Mr",
                    "company_name": "",
                    "surname": "Jackson",
                    "forenames": "Frank",
                    "dob": "1968-11-10",
                    "address_line_1": "97 Broad Road",
                    "address_line_2": "",
                    "address_line_3": "",
                    "address_line_4": "",
                    "address_line_5": "",
                    "post_code": "67890",
                    "telephone": "555-1234",
                    "email_address": "frank.jackson@yahoo.com",
                    "payout_hold": false,
                    "pay_by_bacs": true,
                    "bank_account_type": "C",
                    "bank_sort_code": "123456",
                    "bank_account_number": "12345678",
                    "bank_account_name": "James Smith",
                    "bank_account_ref": "REF123"
                  }
                }
              ]
            },
            {
              "date_of_sentence": "2024-11-21",
              "imposing_court_id": 650000000160,
              "offence_id": 30001,
              "impositions": null
            },
            {
              "date_of_sentence": "2024-11-21",
              "imposing_court_id": 650000000160,
              "offence_id": 30002,
              "impositions": [
                {
                  "result_id": "FCMP",
                  "amount_imposed": 6926.00,
                  "amount_paid": 743.00,
                  "major_creditor_id": 101112,
                  "minor_creditor": null
                },
                {
                  "result_id": "FCOMP",
                  "amount_imposed": 836.00,
                  "amount_paid": 183.00,
                  "major_creditor_id": null,
                  "minor_creditor": {
                    "company_flag": false,
                    "title": "Dr",
                    "company_name": "",
                    "surname": "Emma",
                    "forenames": "Jane Fox",
                    "dob": "1979-05-07",
                    "address_line_1": "The Cottage",
                    "address_line_2": "45 Silverstorm Road",
                    "address_line_3": "Sutton Coldfield",
                    "address_line_4": "West Midlands",
                    "address_line_5": "",
                    "post_code": "BX11FS",
                    "telephone": "0121422893",
                    "email_address": "emmajfox@yahoo.co.uk",
                    "payout_hold": false,
                    "pay_by_bacs": true,
                    "bank_account_type": "S",
                    "bank_sort_code": "040105",
                    "bank_account_number": "654833",
                    "bank_account_name": "Instant Save",
                    "bank_account_ref": "IS2025"
                  }
                }
              ]
            }
          ],
          "fp_ticket_detail": {
            "notice_number": "FP12345",
            "date_of_issue": "2023-06-01",
            "time_of_issue": "14:30",
            "fp_registration_number": "REG123",
            "notice_to_owner_hirer": "Owner",
            "place_of_offence": "Main Street",
            "fp_driving_licence_number": "D1234567"
          },
          "payment_terms": {
            "payment_terms_type_code": "B",
            "effective_date": null,
            "instalment_period": "M",
            "lump_sum_amount": 500.00,
            "instalment_amount": 50.00,
            "default_days_in_jail": 30,
            "enforcements": [
              {
                "result_id": "NOENF",
                "enforcement_result_responses": [
                  {
                    "parameter_name": "Response2",
                    "response": "No"
                  }
                ]
              },
              {
                "result_id": "PRIS",
                "enforcement_result_responses": [
                  {
                    "parameter_name": "Response1",
                    "response": "Yes"
                  }
                ]
              },
              {
                "result_id": "COLLO",
                "enforcement_result_responses": [
                  {
                    "parameter_name": "Response3",
                    "response": "Yes"
                  }
                ]
              }
            ]
          },
          "account_notes": [
            {
              "account_note_serial": 1,
              "account_note_text": "First comment",
              "note_type": "AC"
            },
            {
              "account_note_serial": 3,
              "account_note_text": "Second AA note",
              "note_type": "AA"
            },
            {
              "account_note_serial": 2,
              "account_note_text": "First AA note",
              "note_type": "AA"
            }
          ]
        }',
        'Fixed Penalty Registration',
        NULL,
        '{"snapshot":"test"}',
        'SUBMITTED',
        '[
          {
            "username": "company_creator",
            "status": "Active",
            "status_date": "2025-10-20",
            "reason_text": "Account for company test"
          }
        ]',
        NULL,
        'Business User 4',
        now(),
        NULL,
        NULL,
        0
    )
ON CONFLICT (draft_account_id) DO UPDATE
    SET
        account = EXCLUDED.account,
        account_type = EXCLUDED.account_type,
        account_snapshot = EXCLUDED.account_snapshot,
        account_status = EXCLUDED.account_status,
        timeline_data = EXCLUDED.timeline_data,
        submitted_by_name = EXCLUDED.submitted_by_name,
        account_status_date = EXCLUDED.account_status_date,
        version_number = EXCLUDED.version_number;

-- Ensure sequence is above current ids
SELECT setval('draft_account_id_seq', 200);