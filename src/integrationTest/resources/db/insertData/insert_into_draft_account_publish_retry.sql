CREATE SEQUENCE draft_account_publish_retry_failure_seq
    START WITH 1
    INCREMENT BY 1;
@@

CREATE OR REPLACE FUNCTION fail_first_publish_retry_status_update()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.draft_account_id = 9999901
        AND OLD.account_number IS NULL
        AND NEW.account_number IS NOT NULL
        AND nextval('draft_account_publish_retry_failure_seq') <= 1 THEN
        RAISE EXCEPTION 'Test-only failure after defendant account creation and before final draft status update';
    END IF;

    RETURN NEW;
END;
$$;
@@

CREATE TRIGGER fail_first_publish_retry_status_update
BEFORE UPDATE ON draft_accounts
FOR EACH ROW
EXECUTE FUNCTION fail_first_publish_retry_status_update();
@@

INSERT INTO draft_accounts (
    draft_account_id,
    business_unit_id,
    created_date,
    submitted_by,
    validated_date,
    validated_by,
    account,
    account_type,
    account_id,
    account_snapshot,
    account_status,
    timeline_data,
    account_number,
    submitted_by_name,
    account_status_date,
    status_message,
    validated_by_name,
    version_number
)
VALUES (
    9999901,
    77,
    CURRENT_TIMESTAMP,
    'SUBMITTER',
    NULL,
    NULL,
    $${
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
    }$$::jsonb,
    'Fine',
    NULL,
    $${
      "account_type": "Fine",
      "submitted_by": "SUBMITTER",
      "submitted_by_name": "opal-publish-it"
    }$$::jsonb,
    'SUBMITTED',
    $$[
      {
        "status": "Submitted",
        "username": "SUBMITTER",
        "reason_text": null,
        "status_date": "2026-06-22"
      }
    ]$$::jsonb,
    NULL,
    'opal-publish-it',
    CURRENT_TIMESTAMP,
    NULL,
    NULL,
    0
);
@@
