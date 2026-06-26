# Cash Till Report Instance Content Setup

This page documents the minimum local setup to exercise `GET /report-instances/{id}/content` for the `cash_till`
report while testing through Bruno.

It matches the current integration-test setup for:

- report id: `cash_till`
- report instance id: `99000000353000`
- blob location: `stored-cash-till-report-location`
- business unit id: `1778`

## Preconditions

- the service is running locally
- local Postgres is available
- Azurite blob storage is running
- Bruno is using your local fines-service environment

The Bruno request already exists at:

- [Get Report Instance Content.bru](/Users/krishna.sapkota/hmcts/opal-fines-service/bruno/collections/Release%201C%20-%20Fines%20Service/Report%20Instance%20Content/Get%20Report%20Instance%20Content.bru)

## 1. Update the `cash_till` report row

Run this SQL first:

```sql
UPDATE reports
SET retention_period = 'P14D',
    permission = 'SEARCH_AND_VIEW_ACCOUNTS'
WHERE report_id = 'cash_till';
```

This matters because the content path reads the `reports` row through JPA, and the local baseline `cash_till` data may
still have:

- `retention_period = '14'`
- `permission = NULL`

## 2. Seed cash till report instance data

Run the following SQL:

```sql
INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
) VALUES (
    1778,
    'Cash Till Business Unit',
    'CTIL',
    CAST('Area' AS t_business_unit_type_enum),
    false
);

INSERT INTO tills (
    till_id,
    business_unit_id,
    till_number,
    owned_by
) VALUES (
    99000000353100,
    1778,
    9011,
    'L080JG'
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
    99000000353200,
    1778,
    'ACC456',
    250.00,
    125.50,
    124.50,
    CAST('L' AS t_da_account_status_enum),
    CAST('Fine' AS t_da_account_type_enum),
    1
);

INSERT INTO payments_in (
    payment_in_id,
    till_id,
    payment_amount,
    payment_date,
    payment_method,
    destination_type,
    allocation_type,
    associated_record_type,
    associated_record_id,
    third_party_payer_name,
    additional_information,
    receipt,
    allocated,
    auto_payment
) VALUES (
    99000000353300,
    99000000353100,
    125.50,
    '2026-05-26 14:30:00',
    'NC',
    'F',
    'FULL',
    'defendant_accounts',
    '99000000353200',
    'A Payer',
    'Account payment',
    true,
    false,
    false
);

INSERT INTO report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    requested_by,
    report_parameters,
    location,
    requested_at,
    generation_status,
    requested_by_name
) VALUES (
    99000000353000,
    'cash_till',
    ARRAY[1778]::smallint[],
    1,
    12345678,
    '{"till_id":99000000353100,"allocated_report":false}'::json,
    'stored-cash-till-report-location',
    '2026-05-27 09:00:00',
    CAST('READY' AS ri_generation_status_enum),
    'opal-test'
);
```

## 3. Upload stored report content to Azurite

The application will read blob content from:

- container: `reports`
- blob name: `stored-cash-till-report-location`

Use this JSON as the blob body:

```json
{
  "reportData": {
    "rows": [
      {
        "business_unit": "Cash Till Business Unit",
        "cash_till_number": "9011",
        "cashier": "opal-test",
        "payment_date_time": "2026-05-26T14:30:00",
        "destination_type": "FA",
        "details": "ACC456",
        "auto_payment": false,
        "payment_method": "NC",
        "amount": 125.50,
        "receipt": true,
        "balance": 124.50,
        "allocated": false
      }
    ],
    "allocated_report": false,
    "report_meta_data": {
      "pdpo_party_ids": []
    }
  }
}
```

### Option A: Azure CLI

If your local app is using the default emulator connection string, this works:

```bash
az storage container create \
  --name reports \
  --connection-string "UseDevelopmentStorage=true"
```

Save the JSON above to a local file, then upload it:

```bash
az storage blob upload \
  --container-name reports \
  --name stored-cash-till-report-location \
  --file /absolute/path/to/cash-till-stored-report.json \
  --overwrite true \
  --connection-string "UseDevelopmentStorage=true"
```

### Option B: Azure Storage Explorer

Create the `reports` container if it does not exist, then upload a blob named
`stored-cash-till-report-location` with the JSON body above.

## 4. Run the Bruno request

Use the existing request and set the path id to:

```text
99000000353000
```

Examples:

- `Accept: application/json`
- `Accept: application/csv`

Expected results:

- `application/json` returns the stored JSON body
- `application/csv` returns CSV generated from the stored JSON through `CashTillReportService`

## 5. Cleanup

If you want to remove the seeded data afterwards:

```sql
DELETE FROM payments_in
WHERE payment_in_id = 99000000353300;

DELETE FROM report_instances
WHERE report_instance_id = 99000000353000;

DELETE FROM defendant_accounts
WHERE defendant_account_id = 99000000353200;

DELETE FROM tills
WHERE till_id = 99000000353100;

DELETE FROM business_units
WHERE business_unit_id = 1778;

UPDATE reports
SET retention_period = '14',
    permission = NULL
WHERE report_id = 'cash_till';
```
