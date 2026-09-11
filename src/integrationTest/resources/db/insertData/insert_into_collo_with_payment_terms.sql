INSERT INTO public.defendant_accounts (
    defendant_account_id,
    business_unit_id,
    account_number,
    amount_imposed,
    amount_paid,
    account_balance,
    account_status,
    originator_type,
    account_type,
    version_number
) VALUES (
    90007, 77, '900007A', -700.00, 70.00, -630.00, 'L', 'NEW', 'Fine', 1
);

INSERT INTO public.parties (party_id, organisation, surname, forenames, title, birth_date)
VALUES (91007, false, 'Anderson', 'Patricia Helen', 'Mrs', TIMESTAMP '1987-01-01 00:00:00');

INSERT INTO public.defendant_account_parties (
    defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
) VALUES (
    92007, 90007, 91007, 'Defendant', true
);

INSERT INTO public.payment_terms (
    payment_terms_id, defendant_account_id, posted_date, terms_type_code, active
) VALUES (
    99007, 90007, TIMESTAMP '2026-05-05 11:37:55.196682', 'I', true
);
