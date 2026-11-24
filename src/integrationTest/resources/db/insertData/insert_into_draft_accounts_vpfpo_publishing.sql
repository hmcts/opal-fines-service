/**
* OPAL Program
*
* MODULE      : insert_into_draft_accounts_vpfpo.sql
*
* DESCRIPTION : Inserts rows of data into the DRAFT_ACCOUNTS table for testing the Banking interfaces
* feature flag interaction.
*
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 23/11/2026    T Croasdale 1.0         Inserts two rows of data into the DEFENDANT_ACCOUNTS table
*
**/


INSERT INTO draft_accounts(
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by, account, account_type, account_id, account_snapshot, account_status, timeline_data, account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number, source, source_reference)
VALUES
    (
        21,
        77,
        '2024-12-10 16:27:01.023126',
        'user_001',
        NULL,
        NULL,
        '{
      "account_type": "Fine",
      "defendant_type": "adultOrYouthOnly",
      "originator_id": 409,
      "originator_name": "Burnley Crown Court",
      "originator_type": "NEW",
      "account_sentence_date": "2023-02-15",
      "enforcement_court_id": 770000000021,
      "collection_order_made": true,
      "collection_order_date": "2023-02-22",
      "payment_card_request": null,
      "prosecutor_case_reference": "PUBLISH-RETRY-IT",
      "defendant": {
        "company_flag": false,
        "surname": "TEST",
        "forenames": "Publish",
        "dob": "1990-01-01",
        "address_line_1": "1 Test Street",
        "post_code": "TE1 1ST",
        "telephone_number_home": "02070000000",
        "debtor_detail": {
          "document_language": "EN",
          "hearing_language": null,
          "aliases": null
        }
      },
      "offences": [
        {
          "offence_id": 33369,
          "date_of_sentence": "2023-02-15",
          "imposing_court_id": 770000000021,
          "impositions": [
            {
              "result_id": "FO",
              "amount_imposed": 100.00,
              "amount_paid": 0.00,
              "minor_creditor": null,
              "major_creditor_id": null
            }
          ]
        }
      ],
      "payment_terms": {
        "payment_terms_type_code": "B",
        "effective_date": "2023-03-22",
        "instalment_period": null,
        "instalment_amount": null,
        "lump_sum_amount": null,
        "default_days_in_jail": null,
        "enforcements": null
      },
      "account_notes": null,
      "fp_ticket_detail": null,
      "suspended_committal_date": null
    }',
        'Fixed Penalty',
        NULL,
        '{
          "snapshot": "opal-test"
        }',
        'PUBLISHING_PENDING',
        '[
          {
            "username": "opal-test",
            "status": "Created",
            "status_date": "2025-01-09",
            "reason_text": null
          }
        ]',
        NULL,
        'John Smith',
        '2024-12-10 16:27:01.023126',
        NULL,
        NULL,
        0,
        'VPFPO',
        800
    );

INSERT INTO draft_accounts(
    draft_account_id, business_unit_id, created_date, submitted_by, validated_date, validated_by, account, account_type, account_id, account_snapshot, account_status, timeline_data, account_number, submitted_by_name, account_status_date, status_message, validated_by_name, version_number, source, source_reference)
VALUES
    (
        31,
        77,
        '2024-12-10 16:27:01.023126',
        'user_001',
        NULL,
        NULL,
        '{
      "account_type": "Fine",
      "defendant_type": "adultOrYouthOnly",
      "originator_id": 409,
      "originator_name": "Invalid Originator",
      "originator_type": "NEW",
      "account_sentence_date": "2023-02-15",
      "enforcement_court_id": 770000000021,
      "collection_order_made": true,
      "collection_order_date": "2023-02-22",
      "payment_card_request": null,
      "prosecutor_case_reference": "PUBLISH-RETRY-IT",
      "defendant": {
        "company_flag": false,
        "surname": "TEST",
        "forenames": "Publish",
        "dob": "1990-01-01",
        "address_line_1": "1 Test Street",
        "post_code": "TE1 1ST",
        "telephone_number_home": "02070000000",
        "debtor_detail": {
          "document_language": "EN",
          "hearing_language": null,
          "aliases": null
        }
      },
      "offences": [
        {
          "offence_id": 33369,
          "date_of_sentence": "2023-02-15",
          "imposing_court_id": 770000000021,
          "impositions": [
            {
              "result_id": "FO",
              "amount_imposed": 100.00,
              "amount_paid": 0.00,
              "minor_creditor": null,
              "major_creditor_id": null
            }
          ]
        }
      ],
      "payment_terms": {
        "payment_terms_type_code": "B",
        "effective_date": "2023-03-22",
        "instalment_period": null,
        "instalment_amount": null,
        "lump_sum_amount": null,
        "default_days_in_jail": null,
        "enforcements": null
      },
      "account_notes": null,
      "fp_ticket_detail": null,
      "suspended_committal_date": null
    }',
        'Fixed Penalty',
        NULL,
        '{
          "snapshot": "opal-test"
        }',
        'PUBLISHING_PENDING',
        '[
          {
            "username": "opal-test",
            "status": "Created",
            "status_date": "2025-01-09",
            "reason_text": null
          }
        ]',
        NULL,
        'John Smith',
        '2024-12-10 16:27:01.023126',
        NULL,
        NULL,
        0,
        'VPFPO',
        800
    );

-- Ensure sequence is above current ids
SELECT setval('draft_account_id_seq', 200);
