DELETE FROM public.impositions WHERE imposition_id BETWEEN 10764001 AND 10764005;
DELETE FROM public.defendant_account_parties WHERE defendant_account_party_id BETWEEN 10764101 AND 10764102;
DELETE FROM public.defendant_accounts WHERE defendant_account_id BETWEEN 10764101 AND 10764102;
DELETE FROM public.creditor_accounts WHERE creditor_account_id BETWEEN 10764001 AND 10764007;
DELETE FROM public.parties WHERE party_id BETWEEN 10764001 AND 10764007 OR party_id BETWEEN 10764101 AND 10764102;
