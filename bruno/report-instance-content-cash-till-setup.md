# Cash Till Report Instance Content Setup

Use this guide to set up local data for the cash till report instance content requests in Bruno.

## Preconditions

- the service and local Postgres are running
- Azurite is running with a `reports` container
- Bruno is using the local fines-service environment
- `opal-shared-infrastructure/docker-files/.env.shared` contains:

```text
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://azurite:10000/devstoreaccount1;
REPORT_AZURE_STORAGE_CONTAINER=reports
```

## 1. Set up the report and report instance

The retention period is already configured. Copy and run the following SQL to update the supported file types for the `cash_till` report and insert the report instance:

```sql
UPDATE reports
SET supported_file_types = ARRAY['CSV', 'PDF', 'JSON']::r_supported_file_type_enum[]
WHERE report_id = 'cash_till';

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
    ARRAY[77]::smallint[],
    1,
    12345678,
    '{"till_id":99000000353100,"allocated_report":false}'::json,
    '00000000-0000-0000-0000-000000353000',
    '2026-05-27 09:00:00',
    CAST('READY' AS ri_generation_status_enum),
    'opal-test'
);
```

## 2. Upload the report content to Azurite

Upload a blob to the `reports` container with this name:

```text
00000000-0000-0000-0000-000000353000
```

Use the following content as the blob body:

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
      "pdpo_party_ids": [
        {
          "id": "99000000353000",
          "type": "DEFENDANT_ACCOUNT"
        }
      ]
    }
  }
}
```

For example, save the content to `cash-till-report.json` and upload it with Azure CLI:

```bash
az storage blob upload \
  --container-name reports \
  --name 00000000-0000-0000-0000-000000353000 \
  --file cash-till-report.json \
  --overwrite true \
  --connection-string "UseDevelopmentStorage=true"
```

## 3. Run the Bruno requests

The requests use report instance ID `99000000353000`:

- [Get Report Instance Content - JSON](collections/Release%201C/Release%201C%20-%20Enforcement%20Operational%20Reporting/Report%20Instance%20Content/Get%20Report%20Instance%20Content%20-%20JSON.bru)
- [Get Report Instance Content - CSV](collections/Release%201C/Release%201C%20-%20Enforcement%20Operational%20Reporting/Report%20Instance%20Content/Get%20Report%20Instance%20Content%20-%20CSV.bru)

## 4. Clean up

Remove the seeded report instance:

```sql
DELETE FROM report_instances
WHERE report_instance_id = 99000000353000;
```

Remove the blob from Azurite:

```bash
az storage blob delete \
  --container-name reports \
  --name 00000000-0000-0000-0000-000000353000 \
  --connection-string "UseDevelopmentStorage=true"
```
