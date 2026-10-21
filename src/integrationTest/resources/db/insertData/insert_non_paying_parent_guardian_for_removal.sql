INSERT INTO parties
    (party_id, organisation, surname, forenames)
VALUES
    (9900000999102, 'N', 'Removal', 'Guardian');

INSERT INTO defendant_account_parties
    (defendant_account_party_id, defendant_account_id, party_id, association_type, debtor)
VALUES
    (9900000999101, 24010, 9900000999102, 'Parent/Guardian', 'N');
