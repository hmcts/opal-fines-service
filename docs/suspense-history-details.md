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

Rules are evaluated in the following order. A failed special rule returns null;
it does not fall through to a generic account or payment shape.

| Priority | Condition | Details implementation and data |
| --- | --- | --- |
| 0 | Transaction type `XP`, regardless of associated reference | Null details; retain the payment transaction in history. |
| 1 | Transaction type `RV` | Null details, regardless of text or associated reference. |
| 1 | Transaction type `DQ` | `ReversalDetailsCommonStrict`: non-null transaction `text` becomes `reversal_description`; null text leaves details null because the description is required. |
| 2 | Transaction type `IN`, associated type `impositions`, with a resolvable imposition, defendant identity/numeric account number and creditor identity | `AdjustmentDetailsCommonStrict`: imposition ID, defendant name/account number, creditor summary/address. |
| 3 | Transaction type `RP`, associated type `creditor_accounts` or `creditor_transactions`, resolving to a creditor with `repayment = true` | `ThirdPartyRepayDetailsCommonStrict`: creditor account number, summary/address, and `Pay by BACS` when `pay_by_bacs` is true, otherwise `Pay by Cheque`. |
| 3 | Transaction type `RP`, associated type `defendant_accounts`, with a resolvable defendant identity/numeric account number | `DefendantDetailsCommonStrict`: defendant account number and individual or organisation name. Confirmed by the requested repayment correction on 26 November 2026. |
| 5 | Eligible linked payment, transaction type `CF` or `OC`, with exactly one resolvable court-fee record and an identified payer | `ManualPaymentCourtFeeDetailsCommonStrict`: linked fee code, payment information and available payer details. |
| 6 | Eligible linked payment, transaction type `UN` and suspense item type `UN` | `ManualPaymentUnidentifiedDetailsCommonStrict`: payment information and `suspense_reason_detail`. |
| 7 | Other eligible linked payment with an identified payer | `ManualPaymentPayerDetailsCommonStrict`: payment information and payer details. This includes `UN` transactions on miscellaneous items when a payer is identified. |
| 8 | Associated type `creditor_accounts` or `creditor_transactions`, resolving to a creditor identity | `CreditorDetailsCommonStrict`: creditor account number and summary/address. |
| 9 | Associated type `defendant_accounts` or `defendant_transactions`, resolving to one defendant identity and a numeric account number | `DefendantDetailsCommonStrict`: defendant account number and name. |
| Otherwise | No supported combination or insufficient required linked data | Null details; retain the transaction in history. |

The transaction's `reversed` flag does not select reversal details. The current
reversal-details rule is specifically `DQ`; `RV` returns null details. The automatic-
payment branch is currently disabled, so automatic/manual flags do not select a
different details shape. API transaction
codes absent from `SuspenseItemType` retain their raw code as the display name.

## Required fields and business-unit types

The current API contract requires payer details on manual payer/court-fee shapes,
a non-null reversal description, and a creditor address on creditor, third-party
repayment and adjustment shapes. When these are unavailable, retain the transaction
with null details instead of emitting a partially populated, invalid details object.
Payment source codes are mapped to the generated enum, including `VARIANT_BANKING`.
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
  to transaction types `UN`, `CF`, `OC`, `OF` and `FA`. Other null-type references,
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
- Payer `defendant_details`, defendant aliases and other optional identity fields
  are not inferred from payer name/address text.
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
  imported `UN` payments instead use unidentified-payment details and do not
  expose a defendant account number.
- Every account, payment, fee, imposition and business-unit-scoped reference must
  belong to the suspense account's business unit. Cross-unit references do not
  populate details. Outer joins preserve unmatched transactions and ordering.

When authoritative rules become available, update this document, the dispatcher
and its fixtures/assertions together. Do not expand a fallback based solely on a
coincidental matching ID.
