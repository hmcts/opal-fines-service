/**
* OPAL Program
*
* MODULE      : reset_sequences_for_dev_data.sql
*
* DESCRIPTION : Reset sequences after adding a complete dev defendant account data set with linked minor creditor history.
*
* VERSION HISTORY:
*
* Date          Author    Version     Nature of Change
* ----------    ------    --------    ----------------------------------------------------------------------------
* 07/07/2026    P Brumby  1.0         PO-8302 - Reset sequences after adding complete dev defendant account data set with linked minor creditor history.
*
**/

SELECT setval('public.party_id_seq', 99000000000919, true);
SELECT setval('public.alias_id_seq', 99000000001131, true);
SELECT setval('public.creditor_account_id_seq', 99000000000819, true);
SELECT setval('public.defendant_account_id_seq', 99000000000019, true);
SELECT setval('public.defendant_account_party_id_seq', 99000000002019, true);
SELECT setval('public.document_instance_id_seq', 99000000012019, true);
SELECT setval('public.report_entry_id_seq', 99000000009019, true);
SELECT setval('public.enforcement_id_seq', 99000000007019, true);
SELECT setval('public.payment_terms_id_seq', 99000000013019, true);
SELECT setval('public.imposition_id_seq', 99000000003020, true);
SELECT setval('public.defendant_transaction_id_seq', 99000000004020, true);
SELECT setval('public.allocation_id_seq', 99000000005020, true);
SELECT setval('public.creditor_transaction_id_seq', 99000000006020, true);
SELECT setval('public.note_id_seq', 99000000011020, true);
SELECT setval('public.amendment_id_seq', 99000000014020, true);
