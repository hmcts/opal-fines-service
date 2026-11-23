-- PO-10764: all combinations of creditor/defendant organisation flags.
INSERT INTO public.parties (party_id, organisation, organisation_name, forenames, surname, account_type)
VALUES
    (10764001, FALSE, NULL, 'Alice', 'SearchCreditor', 'Creditor'),
    (10764002, FALSE, NULL, 'Beth', 'SearchCreditor', 'Creditor'),
    (10764003, TRUE, 'Search Creditor Ltd', NULL, NULL, 'Creditor'),
    (10764004, TRUE, 'Search Creditor PLC', NULL, NULL, 'Creditor'),
    (10764005, FALSE, NULL, 'Carol', 'SearchCreditor', 'Creditor'),
    (10764006, TRUE, 'Unassociated Creditor Ltd', NULL, NULL, 'Creditor'),
    (10764007, FALSE, NULL, 'David', 'SearchCreditor', 'Creditor'),
    (10764101, FALSE, NULL, 'John', 'SearchDefendant', 'Defendant'),
    (10764102, TRUE, 'Search Defendant Ltd', NULL, NULL, 'Defendant');

INSERT INTO public.creditor_accounts (
    creditor_account_id, business_unit_id, account_number, creditor_account_type,
    minor_creditor_party_id, repayment, hold_payout, pay_by_bacs, prosecution_service, version_number
)
VALUES
    (10764001, 10, 'PDPO6401', 'MN', 10764001, FALSE, FALSE, FALSE, FALSE, 1),
    (10764002, 10, 'PDPO6402', 'MN', 10764002, FALSE, FALSE, FALSE, FALSE, 1),
    (10764003, 10, 'PDPO6403', 'MN', 10764003, FALSE, FALSE, FALSE, FALSE, 1),
    (10764004, 10, 'PDPO6404', 'MN', 10764004, FALSE, FALSE, FALSE, FALSE, 1),
    (10764005, 10, 'PDPO6405', 'MN', 10764005, FALSE, FALSE, FALSE, FALSE, 1),
    (10764006, 10, 'PDPO6406', 'MN', 10764006, FALSE, FALSE, FALSE, FALSE, 1),
    (10764007, 10, 'PDPO6407', 'MN', 10764007, FALSE, FALSE, FALSE, FALSE, 1);

INSERT INTO public.defendant_accounts (
    defendant_account_id, business_unit_id, account_number, amount_imposed,
    amount_paid, account_balance, account_status, account_type
)
VALUES
    (10764101, 10, 'PDPO6411', 100, 0, 100, 'L', 'Fine'),
    (10764102, 10, 'PDPO6412', 100, 0, 100, 'L', 'Fine');

INSERT INTO public.defendant_account_parties (
    defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
)
VALUES
    (10764101, 10764101, 10764101, 'Defendant', TRUE),
    (10764102, 10764102, 10764102, 'Defendant', TRUE);

INSERT INTO public.impositions (
    imposition_id, defendant_account_id, posted_date, result_id, imposed_amount, paid_amount, creditor_account_id
)
VALUES
    (10764001, 10764101, '2026-10-01', 'FEES', 100, 0, 10764001),
    (10764002, 10764102, '2026-10-01', 'FEES', 100, 0, 10764002),
    (10764003, 10764101, '2026-10-01', 'FEES', 100, 0, 10764003),
    (10764004, 10764102, '2026-10-01', 'FEES', 100, 0, 10764004),
    (10764005, 10764101, '2026-10-01', 'FEES', 100, 0, 10764005);
