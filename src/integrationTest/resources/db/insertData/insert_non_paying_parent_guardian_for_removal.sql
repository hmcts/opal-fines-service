INSERT INTO defendant_accounts
    (defendant_account_id, version_number, business_unit_id, account_number,
     amount_paid, account_balance, amount_imposed, account_status,
     prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
     collection_order, payment_card_requested)
VALUES
    (24010, 0, 78, '24010A',
     0.00, 0.00, 0.00, 'L',
     '24010PCR', 'N', 'N', 'Fine',
     'N', 'N');

INSERT INTO parties
    (party_id, organisation, surname, forenames)
VALUES
    (24010, 'N', 'Account', 'Defendant'),
    (9900000999102, 'N', 'Removal', 'Guardian');

INSERT INTO defendant_account_parties
    (defendant_account_party_id, defendant_account_id, party_id, association_type, debtor)
VALUES
    (24010, 24010, 24010, 'Defendant', 'Y'),
    (9900000999101, 24010, 9900000999102, 'Parent/Guardian', 'N');

INSERT INTO debtor_detail (party_id)
VALUES (24010);
