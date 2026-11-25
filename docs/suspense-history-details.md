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
| 1 | Transaction type `RV`, with nonblank transaction text | `ReversalDetailsCommonStrict`: transaction `text` becomes `reversal_description`. An associated record is not required. |
| 2 | Transaction type `IN`, associated type `impositions`, with a resolvable imposition, defendant identity/numeric account number and creditor identity | `AdjustmentDetailsCommonStrict`: imposition ID, defendant name/account number, creditor summary/address. |
| 3 | Transaction type `RP`, associated type `creditor_accounts` or `creditor_transactions`, resolving to a creditor with `repayment = true` | `ThirdPartyRepayDetailsCommonStrict`: creditor account number, summary/address, and `Pay by BACS` when `pay_by_bacs` is true, otherwise `Pay by Cheque`. |
| 4 | Eligible linked payment with `auto_payment = true` | `AutoPaymentDetailsCommonStrict`: originator reference, payment source and optional numeric defendant account number. |
| 5 | Eligible linked manual payment, transaction type `CF` or `OC`, with exactly one resolvable court-fee record | `ManualPaymentCourtFeeDetailsCommonStrict`: linked fee code, payment information and available payer details. |
| 6 | Eligible linked manual payment, transaction type `UN` and suspense item type `UN` | `ManualPaymentUnidentifiedDetailsCommonStrict`: payment information and `suspense_reason_detail`. |
| 7 | Other eligible linked manual payment with an identified payer | `ManualPaymentPayerDetailsCommonStrict`: payment information and payer details. This includes `UN` transactions on miscellaneous items when a payer is identified. |
| 8 | Associated type `creditor_accounts` or `creditor_transactions`, resolving to a creditor identity | `CreditorDetailsCommonStrict`: creditor account number and summary/address. |
| 9 | Associated type `defendant_accounts` or `defendant_transactions`, resolving to one defendant identity and a numeric account number | `DefendantDetailsCommonStrict`: defendant account number and name. |
| Otherwise | No supported combination or insufficient required linked data | Null details; retain the transaction in history. |

The transaction's `reversed` flag does not select reversal details. The provisional
reversal rule is specifically `RV`; no rule is inferred for `DQ`. API transaction
codes absent from `SuspenseItemType` retain their raw code as the display name.

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
  codes or an unknown automatic/manual classification leave payment details null.
- A court-fee code comes from `court_fees_received` linked by
  `suspense_transaction_id`, then `court_fees`. Missing/ambiguous links are not
  replaced by a potentially stale `court_fee_code` in payment JSON.

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
  `CENTRAL_FUND_ACCOUNT` configuration item. Missing optional addresses stay null;
  missing required minor/major creditor identities leave details null. Multiple
  Central Fund configuration rows leave its optional address null.
- These are current linked records, not immutable historical name/address snapshots.
- The database stores defendant account numbers as strings, while these generated
  API detail models require int64. Conversion happens at mapping time. A number
  that is nonnumeric or outside int64 leaves defendant/adjustment details null;
  automatic-payment details can still be returned with a null account number.
- Every account, payment, fee, imposition and business-unit-scoped reference must
  belong to the suspense account's business unit. Cross-unit references do not
  populate details. Outer joins preserve unmatched transactions and ordering.

When authoritative rules become available, update this document, the dispatcher
and its fixtures/assertions together. Do not expand a fallback based solely on a
coincidental matching ID.
