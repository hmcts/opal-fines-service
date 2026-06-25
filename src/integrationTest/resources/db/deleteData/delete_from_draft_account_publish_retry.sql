DROP TRIGGER IF EXISTS fail_first_publish_retry_status_update ON draft_accounts;
DROP FUNCTION IF EXISTS fail_first_publish_retry_status_update();
DROP SEQUENCE IF EXISTS draft_account_publish_retry_failure_seq;

DROP TRIGGER IF EXISTS fail_first_defendant_account_insert ON defendant_accounts;
DROP FUNCTION IF EXISTS fail_first_defendant_account_insert();
DROP SEQUENCE IF EXISTS defendant_account_publish_sp_failure_seq;

DROP TABLE IF EXISTS publish_retry_account_ids;
DROP TABLE IF EXISTS publish_retry_imposition_ids;
DROP TABLE IF EXISTS publish_retry_party_ids;
DROP TABLE IF EXISTS publish_retry_account_numbers;

CREATE TEMP TABLE publish_retry_account_ids AS
SELECT defendant_account_id
FROM defendant_accounts
WHERE prosecutor_case_reference = 'PUBLISH-RETRY-IT';

CREATE TEMP TABLE publish_retry_imposition_ids AS
SELECT imposition_id
FROM impositions
WHERE defendant_account_id IN (SELECT defendant_account_id FROM publish_retry_account_ids);

CREATE TEMP TABLE publish_retry_party_ids AS
SELECT party_id
FROM defendant_account_parties
WHERE defendant_account_id IN (SELECT defendant_account_id FROM publish_retry_account_ids);

CREATE TEMP TABLE publish_retry_account_numbers AS
SELECT account_number
FROM defendant_accounts
WHERE defendant_account_id IN (SELECT defendant_account_id FROM publish_retry_account_ids);

DELETE FROM report_entries
WHERE associated_record_type = 'defendant_accounts'
  AND associated_record_id IN (
      SELECT defendant_account_id::varchar
      FROM publish_retry_account_ids
  );

DELETE FROM document_instances
WHERE associated_record_type = 'defendant_accounts'
  AND associated_record_id IN (
      SELECT defendant_account_id::varchar
      FROM publish_retry_account_ids
  );

DELETE FROM document_instances
WHERE associated_record_type = 'impositions'
  AND associated_record_id IN (
      SELECT imposition_id::varchar
      FROM publish_retry_imposition_ids
  );

DELETE FROM allocations
WHERE imposition_id IN (
    SELECT imposition_id
    FROM publish_retry_imposition_ids
);

DELETE FROM impositions
WHERE imposition_id IN (
    SELECT imposition_id
    FROM publish_retry_imposition_ids
);

DELETE FROM defendant_transactions
WHERE defendant_account_id IN (
    SELECT defendant_account_id
    FROM publish_retry_account_ids
);

DELETE FROM payment_terms
WHERE defendant_account_id IN (
    SELECT defendant_account_id
    FROM publish_retry_account_ids
);

DELETE FROM payment_card_requests
WHERE defendant_account_id IN (
    SELECT defendant_account_id
    FROM publish_retry_account_ids
);

DELETE FROM enforcements
WHERE defendant_account_id IN (
    SELECT defendant_account_id
    FROM publish_retry_account_ids
);

DELETE FROM fixed_penalty_offences
WHERE defendant_account_id IN (
    SELECT defendant_account_id
    FROM publish_retry_account_ids
);

DELETE FROM notes
WHERE associated_record_type = 'defendant_accounts'
  AND associated_record_id IN (
      SELECT defendant_account_id::text
      FROM publish_retry_account_ids
  );

DELETE FROM aliases
WHERE party_id IN (
    SELECT party_id
    FROM publish_retry_party_ids
);

DELETE FROM debtor_detail
WHERE party_id IN (
    SELECT party_id
    FROM publish_retry_party_ids
);

DELETE FROM defendant_account_parties
WHERE defendant_account_id IN (
    SELECT defendant_account_id
    FROM publish_retry_account_ids
);

DELETE FROM parties
WHERE party_id IN (
    SELECT party_id
    FROM publish_retry_party_ids
);

DELETE FROM account_number_index
WHERE account_number IN (
    SELECT account_number
    FROM publish_retry_account_numbers
);

DELETE FROM defendant_accounts
WHERE defendant_account_id IN (
    SELECT defendant_account_id
    FROM publish_retry_account_ids
);

DELETE FROM draft_accounts
WHERE draft_account_id = 9999901;

DROP TABLE IF EXISTS publish_retry_account_numbers;
DROP TABLE IF EXISTS publish_retry_party_ids;
DROP TABLE IF EXISTS publish_retry_imposition_ids;
DROP TABLE IF EXISTS publish_retry_account_ids;
