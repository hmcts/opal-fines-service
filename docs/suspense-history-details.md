# Suspense history details: provisional mapping rules

These rules apply to `GET /suspense-items/{suspenseItemId}/history` in Opal mode.
They were agreed as provisional implementation assumptions on 25 November 2026.
They are **not verified against the database or API LLD**: no authoritative
transaction-to-details cross-reference was available. Confirm this document with
the feature/design owner before treating these rules as the final business contract.

The implementation is in `SuspenseTransactionRepository.findHistory` and
`SuspenseItemHistoryMapper`. `SuspenseHistoryDetailsIntegrationTest` exercises
the provisional rules; `SuspenseAccountsApiControllerDbTest` covers the original
automatic and unidentified payment cases. Passing these tests demonstrates the
implemented assumptions, not independent confirmation of their business meaning.

## Selection order

Rules are evaluated in the following order. Once a payment-specific rule selects
its details shape, it does not fall through to generic payer details.

| Priority | Condition | Details implementation and data |
| --- | --- | --- |
| 0 | Transaction type `XP`, regardless of associated reference | Null details; retain the transaction in history. |
| 1 | Transaction type `RV` | Null details, regardless of text or associated reference. |
| 1 | Transaction type `DQ` | `ReversalDetailsCommonStrict`: non-null transaction `text` becomes `reversal_description`; null text leaves details null. |
| 2 | Transaction type `IN`, associated type `impositions`, with a resolvable imposition, defendant identity/numeric account number and creditor identity/address | `AdjustmentDetailsCommonStrict`: imposition ID, defendant name/account number, creditor summary/address. |
| 3 | Transaction type `RP`, associated type `creditor_accounts` or `creditor_transactions`, resolving to a creditor with `repayment = true` and identity/address | `ThirdPartyRepayDetailsCommonStrict`: creditor account number, summary/address, and `Pay by BACS` when `pay_by_bacs` is true, otherwise `Pay by Cheque`. |
| 3 | Transaction type `RP`, associated type `defendant_accounts`, with a resolvable defendant identity/numeric account number | `DefendantDetailsCommonStrict`: defendant account number and individual or organisation name. |
| 4 | Linked payment, transaction type `CF` or `OC`, with exactly one resolvable court-fee record and an identified payer | `ManualPaymentCourtFeeDetailsCommonStrict`: linked fee code, payment information and payer details. Missing fee code or payer leaves details null. |
| 5 | Linked payment, transaction type `UN`, suspense item type `UN` and a nonempty originator reference | `AutoPaymentDetailsCommonStrict`: defendant account number, originator reference and payment source. |
| 5 | Linked payment, transaction type `UN`, suspense item type `UN` and a null or empty originator reference | `ManualPaymentUnidentifiedDetailsCommonStrict`: payment information and `suspense_reason_detail`, even when the payment has an identified payer. |
| 6 | Linked payment, transaction type `OF`, suspense item type `OF` and payment associated type `miscellaneous_accounts` | `ManualPaymentPayerDetailsCommonStrict`: payment information and the linked miscellaneous-account payer. |
| 6 | Linked payment, transaction type `OF`, suspense item type `OF` and any other payment associated type (including null) | `AutoPaymentDetailsCommonStrict`: defendant account number, originator reference and payment source. |
| 7 | Other linked payment with an identified payer | `ManualPaymentPayerDetailsCommonStrict`: payment information and payer details. Includes `UN` transactions on miscellaneous items and `OF` transactions on non-OF items. No identified payer leaves details null. |
| 8 | Associated type `creditor_accounts` or `creditor_transactions`, resolving to a creditor identity/address | `CreditorDetailsCommonStrict`: creditor account number and summary/address. |
| 9 | Associated type `defendant_accounts` or `defendant_transactions`, resolving to one defendant identity and a numeric account number | `DefendantDetailsCommonStrict`: defendant account number and name. |
| Otherwise | No supported combination or insufficient linked data for the selected rule | Null details; retain the transaction in history. |

The UN and OF payment rules reflect the advised mapper changes of 27 November
2026. They replace the earlier provisional selection rules. A nonempty originator
reference includes whitespace, matching the mapper's empty-string check.
`payments_in.auto_payment` does not select the details shape: a manual payment with
an originator reference on a UN item still returns automatic details, while an
imported payment without a reference returns unidentified details.

The OF miscellaneous-account decision reads `payments_in.associated_record_type`,
exposed as `paymentAssociatedRecordType` by the history query and projection.
The suspense transaction's own associated type is often null for linked payments
and is not used for this decision. The special OF rules apply only when both the
transaction and suspense item types are OF.

The transaction's `reversed` flag does not select reversal details; `DQ` uses the
transaction text and `RV` returns null. API transaction codes absent from
`SuspenseItemType` retain their raw code as the display name.

## Required fields and business-unit types

The manual payer shape requires both `payment_details` and `payer_details` keys,
but `payer_details` may be null when a miscellaneous-account payer cannot resolve.
It never falls back to stale JSON. The court-fee shape continues to require a
non-null payer. Reversal details require a non-null description; creditor,
third-party repayment and adjustment shapes require a creditor address.

Automatic details require the `payment_source` key, whose value may be a string
or null. This accommodates the existing UN/OF mapper behavior when the payment
and till have no source. Both nullable fields remain in their respective
`required` lists: an explicit null key preserves the distinction between the
payment details shapes in `suspenseTransaction.details.oneOf`.

The models are regenerated from this contract. Integration tests assert explicit
null keys and validate complete suspense history responses against the bundled
OpenAPI response schema. Contract cases also reject empty details objects,
incorrect field types, unknown payment codes and mixed details shapes. The mapper
selection rules are unchanged.

Shared `common.yaml` posted-user fields `posted_by` and `posted_by_name` are
optional and nullable; their maximum lengths remain 20 and 100 respectively.
`posted_date` remains required and non-null. The regenerated `PostedDetailsCommon`
uses nullable wrappers for the user fields. The affected history mappings and
creditor logging code wrap or unwrap those fields without changing user values;
logging continues to exclude absent, null and blank user IDs.

Nested manual `payment_details.received_from` accepts null as well as the existing
`Defendant`, `Third Party` and `Applicant` codes. `payment_details.payment_source`
may be a source object or null; a source object's code still uses the existing
enum. Unidentified `relevant_details` and a payer's optional `defendant_details`
link may also be null. These changes accommodate missing metadata without
fabricating a received-from value, source or defendant link.

Optional fields in shared `addressDetails` retain their non-null string schema.
Generated serialization omits missing address fields rather than emitting nulls,
using the repository's `x-extra-annotation` convention. Populated address lines
and postcodes are preserved. This resolves the creditor-address failures found
when full-response validation was enabled, without changing the shared address
model's Java getter types.

Automatic details default a missing or empty defendant account number to `0`.
An existing account number that is nonnumeric or outside int64 becomes null;
it does not discard automatic details. Originator references may also be null
in OF automatic details.

Nested manual payment source codes use the generated enum, including
`VARIANT_BANKING`; automatic details expose the source string directly.
The history `Business-Unit-Id` header is int16 (`Short` in Java) throughout the
controller, service and repository call chain. Values outside the smallint range
or nonnumeric values are rejected before lookup using the shared Opal
`406` type-mismatch problem response.

## References and payment data

- An associated type names a table. Canonical decimal references within the bigint
  range are converted on the reference side, leaving indexed numeric primary keys
  unwrapped. Nested guards reject malformed/overflowing references without raising
  database errors. Noncanonical text (such as leading zeros) stays unmatched,
  preserving the original exact-reference matching behaviour. No PostgreSQL
  version-specific safe-cast function is required.
- Defendant transactions resolve their owning defendant account; creditor
  transactions resolve their owning creditor account. No recursive traversal of
  further associated references is attempted.
- An imposition reference is supported only for `IN`. `IN` with a direct account
  reference remains unmatched. `RP` with an ordinary non-repayment creditor also
  remains unmatched.
- `RP` also supports a direct `defendant_accounts` reference. Missing, cross-unit,
  ambiguous or nonnumeric defendant data leaves details null. A
  `defendant_transactions` reference is not inferred to be a supported repayment.
- Existing till-allocation SQL writes a **null associated record type** and a
  `payments_in.payment_in_id` in `associated_record_id`. This fallback is limited
  to transaction types `UN`, `CF`, `OC`, `OF`, `FA` and `MS`. Other null-type references,
  cheque references, suspense-item references and unsupported types remain unmatched.
- Payment additional information may be an unwrapped object or wrapped inside
  `additional_information`. Manual payment source uses `transfer_source`, falling
  back to `tills.source`. Automatic payments always obtain their source from
  `tills.source`, even if payment JSON also contains a manual transfer source;
  `interface_name` is not used as a source.
- Received-from codes `D`, `A` and `T` become `Defendant`, `Applicant` and
  `Third Party`. Missing optional information stays null. Unknown received-from
  codes map to null without discarding otherwise matched payment/payer details.
- A court-fee code comes from `court_fees_received` linked by
  `suspense_transaction_id`, then `court_fees`. A single matching received row
  whose `court_fee_id` is null yields the fallback code `OC`. Missing or ambiguous
  received rows, or a non-null fee ID that cannot resolve within the business unit,
  leave details null. The fallback never uses a potentially stale `court_fee_code`
  in payment JSON; rows outside the suspense account's business unit are excluded.

## Party and creditor assumptions

- A defendant identity uses exactly one `Defendant` account-party association,
  not a parent/guardian association. Multiple defendant associations are ambiguous
  and leave defendant/adjustment details null. Names come from the linked party.
- Identified manual payers require `Individual` or `Organisation` and a nonblank
  name. A miscellaneous-account association uses its durable party; if that
  association cannot be resolved, do not fall back to stale JSON. Without such
  an association, use payment JSON. Individual names join title, forenames and
  surname with spaces; addresses join the available lines/postcode with commas.
- Payer `defendant_details`, defendant aliases and other identity fields are not
  inferred from payer name/address text. Individual defendant names emit explicit
  nulls for the required-but-nullable `date_of_birth`, `age`,
  `national_insurance_number` and `individual_aliases` keys because this history
  projection does not fetch those values. The shared identity schema remains
  unchanged.
- Minor creditor identity/address comes from its party; major creditor
  identity/address comes from `major_creditors`. Central Fund summary identifies
  the account/type, with its address taken from the business unit's single
  `CENTRAL_FUND_ACCOUNT` configuration item. Creditor, repayment and adjustment details require a creditor address;
  a missing address or required minor/major creditor identity leaves details null. Multiple
  Central Fund configuration rows are ambiguous and leave its details null.
- These are current linked records, not immutable historical name/address snapshots.
- The database stores defendant account numbers as strings, while these generated
  API detail models require int64. Conversion happens at mapping time. A number
  that is nonnumeric or outside int64 leaves defendant/adjustment details null;
  automatic UN/OF details instead retain the object with a null account number.
  A missing or empty number defaults to `0` in automatic details.
- Every account, payment, fee, imposition and business-unit-scoped reference must
  belong to the suspense account's business unit. Cross-unit references do not
  populate details. Outer joins preserve unmatched transactions and ordering.

When authoritative rules become available, update this document, the dispatcher
and its fixtures/assertions together. Do not expand a fallback based solely on a
coincidental matching ID.
