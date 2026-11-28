INSERT INTO major_creditors (major_creditor_id, business_unit_id, major_creditor_code, name)
VALUES (770000009901, 78, 'TCPS', 'Test CPS creditor'),
       (770000009902, 78, 'TNCP', 'Test non CPS creditor');

INSERT INTO creditor_accounts (creditor_account_id, business_unit_id, account_number, creditor_account_type,
                               prosecution_service, major_creditor_id, repayment, hold_payout, pay_by_bacs)
VALUES (770000009901, 78, 'TESTCPS', 'MJ', true, 770000009901, false, false, false),
       (770000009902, 78, 'TESTNONCPS', 'MJ', false, 770000009902, false, false, false);
