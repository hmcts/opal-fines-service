INSERT INTO parties (party_id, organisation, forenames, surname, account_type)
VALUES (920020, false, 'Second', 'Guardian', 'Defendant');

INSERT INTO defendant_account_parties
    (defendant_account_party_id, defendant_account_id, party_id, association_type, debtor)
VALUES (920021, 20010, 920020, 'Parent/Guardian', false);
