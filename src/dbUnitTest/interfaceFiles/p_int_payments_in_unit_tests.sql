SET SCHEMA 'public';
/*

Defendant Accounts

ID ACC       STATUS LASTENF MASTER
1  25000001A L      FSN     
2  25000002B CS             25000001A
3  25000003C CS             25000001A
4  25000004D CS
5  25000005E WO
6  25000006F TA
7  25000007G TS
8  25000008H TO
9  25000009I L
10 25000010J L      BWTD
11 25000011K L      

Payments

1  OK      FIN 1
2  OK      FIN 222
3  OK      FIN 33333
4  CS WARN SUS 4444
5  WO WARN SUS 5555
6  TA WARN SUS 6666
7  TS WARN SUS 7777
8  TO WARN SUS 8888
9  ZB WARN SUS 9999
10 LE WARN SUS 1000000
11 CR      FIN 20000000
12 NF WARN SUS 300000000
13 TR WARN SUS 1000001
14 ZA      IGN 0
15 UB EXC  REJ 99999999999
16 UB EXC  REJ 1
17 UB EXC  REJ 1
18 UB EXC  REJ 1
19 ZA      IGN 0
20 ZA      IGN 0

amounts
fin           200335.56
sus          3020433.30
tot acc      3220768.86
rej       1000000000.02
ign                0.00
tot file  1003220768.88

counts
4 Exc
3 Ign
9 Warn
4 ok
*/
BEGIN;

DO $$
DECLARE
    i integer;
    v_test_status TEXT;
BEGIN
    DELETE FROM defendant_transactions WHERE defendant_account_id <= 11; 
    DELETE FROM defendant_accounts WHERE defendant_account_id <= 11;
    DELETE FROM interface_messages WHERE interface_job_id IN (1,2,3,4);
    DELETE FROM interface_files WHERE interface_job_id IN (1,2,3,4);
    DELETE FROM interface_jobs WHERE interface_job_id IN (1,2,3,4);
    -- config
    DELETE FROM configuration_items WHERE item_name IN ('BANKLIST','INHIBIT_PAYMENT_RESULTS') AND business_unit_id IN (4,18);
    INSERT INTO configuration_items (configuration_item_id,item_name,business_unit_id,item_values)
    VALUES (nextval('configuration_item_id_seq'),'BANKLIST',4,'[{ "sort_code":"123456", "account_number":"01234567", "name":"Bury St Edmunds" },{ "sort_code":"123457", "account_number":"01234568", "name":"Ipswich" }]');
    INSERT INTO configuration_items (configuration_item_id,item_name,business_unit_id,item_values)
    VALUES (nextval('configuration_item_id_seq'),'INHIBIT_PAYMENT_RESULTS',4,'["BWTD","DW","CW"]');
    INSERT INTO configuration_items (configuration_item_id,item_name,business_unit_id,item_values)
    VALUES (nextval('configuration_item_id_seq'),'BANKLIST',18,'[{ "sort_code":"000005", "account_number":"00000005", "name":"BU5A" },{ "sort_code":"000005", "account_number":"10000005", "name":"BU5B" }]');
    INSERT INTO configuration_items (configuration_item_id,item_name,business_unit_id,item_values)
    VALUES (nextval('configuration_item_id_seq'),'INHIBIT_PAYMENT_RESULTS',18,'["CW"]');
    -- accounts
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (1, 4, '25000001A', 'Fines', -1000, 200, -800, 'L', 'FSN');
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (2, 4, '25000002B', 'Fines', -50, 50, 0, 'CS', NULL);
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (3, 4, '25000003C', 'Fines', -100, 100, 0, 'CS', NULL);
    insert into defendant_transactions (defendant_transaction_id, defendant_account_id, transaction_type, posted_date, transaction_amount, text, associated_record_type, associated_record_id)
    values (1, 2, 'WRTOFF', now(), 50, 'CONSOLIDATED - 25000003C', 'defendant_accounts', '3');
    insert into defendant_transactions (defendant_transaction_id, defendant_account_id, transaction_type, posted_date, transaction_amount, text, associated_record_type, associated_record_id)
    values (2, 3, 'WRTOFF', now(), 100, 'CONSOLIDATED - 25000001A', 'defendant_accounts', '1');
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (4, 4, '25000004D', 'Fines', -240, 240, 0, 'CS', NULL);
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (5, 4, '25000005E', 'Fines', -200, 200, 0, 'WO', NULL);
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (6, 4, '25000006F', 'Fines', -300, 300, 0, 'TA', NULL);
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (7, 4, '25000007G', 'Fines', -400, 400, 0, 'TS', NULL);
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (8, 4, '25000008H', 'Fines', -500, 500, 0, 'TO', NULL);
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (9, 4, '25000009I', 'Fines', -400, 400, 0, 'L', NULL);
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (10, 4, '25000010J', 'Fines', -400, 100, -300, 'L', 'BWTD');
    insert into defendant_accounts (defendant_account_id, business_unit_id, account_number, account_type, amount_imposed, amount_paid, account_balance, account_status, last_enforcement)
    values (11, 4, '25000011K', 'Fines', -400, 400.51, 0.51, 'L', NULL);
    -- jobs
    INSERT INTO interface_jobs(interface_job_id,business_unit_id,interface_name)
    VALUES      (1,4,'p_int_payments_in');
    INSERT INTO interface_jobs(interface_job_id,business_unit_id,interface_name)
    VALUES      (2,18,'p_int_payments_in');
    INSERT INTO interface_jobs(interface_job_id,business_unit_id,interface_name)
    VALUES      (3,24,'p_int_payments_in');
    INSERT INTO interface_jobs(interface_job_id,business_unit_id,interface_name)
    VALUES      (4,4,'p_int_payments_in');
    -- files
    INSERT INTO interface_files(interface_file_id,interface_job_id,file_name,records)
    VALUES      (1,1,'test-a',
        '[{"id":1,"receiving_sort_code":"123456","receiving_bank_account_number":"01234567","receiving_account_type":"5","transaction_code":"99","originator_sort_code":"654321","originator_bank_account_number":"98765432","amount_pence":"1","originator_name":"John Smith","originator_reference":"25000001A","originator_beneficiary_name":"BENA"}
        , {"id":2,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"222","originator_name":"John Smith","originator_reference":"25000002B","originator_beneficiary_name":"BENB"}
        , {"id":3,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"33333","originator_name":"John Smith","originator_reference":"25000003C","originator_beneficiary_name":"BENC"}
        , {"id":4,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"4444","originator_name":"Bill Bates","originator_reference":"25000004D","originator_beneficiary_name":"BEND"}
        , {"id":5,"receiving_sort_code":"123456","receiving_bank_account_number":"01234567","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"5555","originator_name":"Jon Bird","originator_reference":"25000005E","originator_beneficiary_name":"BENE"}
        , {"id":6,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"6666","originator_name":"Carly Schmidt","originator_reference":"25000006F","originator_beneficiary_name":"BENF"}
        , {"id":7,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"7777","originator_name":"Kevin Peters","originator_reference":"25000007G","originator_beneficiary_name":"BENG"}
        , {"id":8,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"8888","originator_name":"Paul Jacobs","originator_reference":"25000008H","originator_beneficiary_name":"BENH"}
        , {"id":9,"receiving_sort_code":"123456","receiving_bank_account_number":"01234567","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"9999","originator_name":"Richard Webb","originator_reference":"25000009I","originator_beneficiary_name":"BENI"}
        , {"id":10,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"1000000","originator_name":"Lisa Walker","originator_reference":"25000010J","originator_beneficiary_name":"BENJ"}
        , {"id":11,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"20000000","originator_name":"Abdul Ahmad","originator_reference":"25000011K","originator_beneficiary_name":"BENK"}
        , {"id":12,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"300000000","originator_name":"Sally Smith","originator_reference":"25000026$","originator_beneficiary_name":"BENZ"}
        , {"id":13,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"01","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"1000001","originator_name":"Richard Webb","originator_reference":"25123456Z","originator_beneficiary_name":"BENI"}
        , {"id":14,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"0","originator_name":"Richard Webb","originator_reference":"25000009I","originator_beneficiary_name":"BENI"}
        , {"id":15,"receiving_sort_code":"000000","receiving_bank_account_number":"00000088","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"99999999999","originator_name":"Frank Graham","originator_reference":"25000009I","originator_beneficiary_name":"BENI"}
        , {"id":16,"receiving_sort_code":"000000","receiving_bank_account_number":":~@<^&^&&*(*>?/","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"1","originator_name":"Frank Graham","originator_reference":"$%^$&*&^_","originator_beneficiary_name":"BENI"}
        , {"id":17,"receiving_sort_code":"^&*$\"sz","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"1","originator_name":"Frank Graham","originator_reference":"_________","originator_beneficiary_name":"BENI"}
        , {"id":18,"receiving_sort_code":"bcnbmn","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"1","originator_name":"Frank Graham","originator_reference":"25000025$","originator_beneficiary_name":"BENI"}
        , {"id":19,"receiving_sort_code":"123456","receiving_bank_account_number":"01234567","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"00000000000","originator_name":"Frank Graham","originator_reference":"25000009I","originator_beneficiary_name":"BENI"}
        , {"id":20,"receiving_sort_code":"123457","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"0","originator_name":"Frank Graham","originator_reference":"25000009I","originator_beneficiary_name":"BENI"}
        ]');
    INSERT INTO interface_files(interface_file_id,interface_job_id,file_name,records)
    VALUES      (2,2,'test-b',
    '[{"receiving_sort_code":"000000","receiving_bank_account_number":"01234568","receiving_account_type":"5","transaction_code":"68","originator_sort_code":"664321","originator_bank_account_number":"99765432","amount_pence":"200002","originator_name":"Fred Jones","originator_reference":"25000002B","originator_beneficiary_name":"Mr & Mrs Jones"}]');
    INSERT INTO interface_files(interface_file_id,interface_job_id,file_name,records)
    VALUES      (3,3,'test-c','[{}]');
    -- execute stored prcoedure
    CALL p_int_payments_in(1);
    CALL p_int_payments_in(2);
    CALL p_int_payments_in(3);
    CALL p_int_payments_in(4);
    -- verify results
    /*
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT SUM(amount) FROM payments_in) = 9;
    RAISE NOTICE 'Test 1: %', COALESCE(v_test_status,'Failed');
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE message_type = 'Info') = 7;
    RAISE NOTICE 'Test 2: %', COALESCE(v_test_status,'Failed');
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE message_type = 'Exception') = 4;
    RAISE NOTICE 'Test 3: %', COALESCE(v_test_status,'Failed');
    */
/* Test 1
To verify that, if any payment is valid, a till is created for the correct business unit with the next available till number for
that business unit
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM tills
    WHERE till_number = currval('till_number_4_seq')
        AND business_unit_id = 4;
    RAISE NOTICE 'Test 1: %', COALESCE(v_test_status,'Failed');
/* Test 2
To verify that a payment in is created for fines (destination type F) and associated with the new till where the payment destination 
account is live, has a balance and no last enforcement action that inhibits payments.
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE associated_record_type = 'defendant_accounts'
        AND associated_record_id = '1'
        AND destination_type = 'F'
        AND additional_information like '%25000001A%'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 2: %', COALESCE(v_test_status,'Failed');
/* Test 3
To verify that the destination payment account is changed to the top-level master account where the payment destination account
has been consolidated.
*/
    SELECT 'Passed'
    INTO v_test_status
    WHERE (
        SELECT COUNT(*)
        FROM payments_in
        WHERE associated_record_type = 'defendant_accounts'
        AND associated_record_id = '1'
        AND till_id = currval('till_id_seq')
        ) = 3; /* only 1 payment directly assigned to this account but 2 payments were for child accounts */
    RAISE NOTICE 'Test 3: %', COALESCE(v_test_status,'Failed');
/* Test 4
To verify that an 'Account consolidated' message is created where the destination account status is consolidated (CS)
*/
    SELECT 'Passed'
    INTO v_test_status
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Account consolidated') = 1;
    RAISE NOTICE 'Test 4: %', COALESCE(v_test_status,'Failed');
/* Test 5
To verify that a payment in is created for suspense (destination type S) and associated with the new till where the payment
destination account status is consolidated (CS)
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE additional_information like '%25000004D%'
        AND destination_type = 'S'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 5: %', COALESCE(v_test_status,'Failed');
/* Test 6
To verify that an 'Account written off' message is created where the destination account status is written off (WO)
*/
    SELECT 'Passed'
    INTO v_test_status
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Account written off') = 1;
    RAISE NOTICE 'Test 6: %', COALESCE(v_test_status,'Failed');
/* Test 7
To verify that a payment in is created for suspense (destination type S) and associated with the new till where the payment
destination account status is written off (WO)
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE additional_information like '%25000005E%'
        AND destination_type = 'S'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 7: %', COALESCE(v_test_status,'Failed');
/* Test 8
To verify that an 'Account transferred out (acknowledged)' message is created where the destination account status is 
transferred out acknoweldged (TA)
*/
    SELECT 'Passed'
    INTO v_test_status
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Account transferred out (acknowledged)') = 1;
    RAISE NOTICE 'Test 8: %', COALESCE(v_test_status,'Failed');
/* Test 9
To verify that a payment in is created for suspense (destination type S) and associated with the new till where the payment
destination account status is transferred out acknoweldged (TA)
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE additional_information like '%25000006F%'
        AND destination_type = 'S'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 9: %', COALESCE(v_test_status,'Failed');
/* Test 10
To verify that an 'Account transferred out to Scotland/NI' message is created where the destination account status is transferred out
to Scotland/NI (TS)
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Account transferred out to Scotland/NI') = 1;
    RAISE NOTICE 'Test 10: %', COALESCE(v_test_status,'Failed');
/* Test 11
To verify that a payment in is created for suspense (destination type S) and associated with the new till where the payment
destination account status is transferred out to Scotland/NI (TS)
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE additional_information like '%25000007G%'
        AND destination_type = 'S'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 11: %', COALESCE(v_test_status,'Failed');
/* Test 12
To verify that an 'Account transferred out' message is created where the destination account status is transferred out (TO)
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Account transferred out') = 1;
    RAISE NOTICE 'Test 12: %', COALESCE(v_test_status,'Failed');
/* Test 13
To verify that a payment in is created for suspense (destination type S) and associated with the new till where the payment
destination account status is transferred out (TO)
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE additional_information like '%25000008H%'
        AND destination_type = 'S'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 13: %', COALESCE(v_test_status,'Failed');
/* Test 14
To verify that an 'Account has a zero balance' message is created where the destination account balance = 0
*/
    SELECT 'Passed'
    INTO v_test_status
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Account has a zero balance') = 1;
    RAISE NOTICE 'Test 14: %', COALESCE(v_test_status,'Failed');
/* Test 15
To verify that a payment in is created for suspense (destination type S) and associated with the new till where the payment 
destination account balance = 0
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE additional_information like '%25000009I%'
        AND destination_type = 'S'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 15: %', COALESCE(v_test_status,'Failed');
/* Test 16
To verify that an 'Account last enforcement inhibits payments' message is created where the destination account last enforcement action
is in the list of results to inhibit payments
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Account last enforcement inhibits payments') = 1;
    RAISE NOTICE 'Test 16: %', COALESCE(v_test_status,'Failed');
/* Test 17
To verify that a payment in is created for suspense (destination type S) and associated with the new till where the payment 
destination account last enforcement action is in the list of results to inhibit payments
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE additional_information like '%25000010J%'
        AND destination_type = 'S'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 17: %', COALESCE(v_test_status,'Failed');
/* Test 18
To verify that an 'Account not found' message is created where the destination account is not found
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Account not found') = 1;
    RAISE NOTICE 'Test 18: %', COALESCE(v_test_status,'Failed');
/* Test 19
To verify that a payment in is created for suspense (destination type S) and associated with the new till where the payment 
destination account is not found
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE additional_information like '%25000026$%'
        AND destination_type = 'S'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 19: %', COALESCE(v_test_status,'Failed');
/* Test 20
To verify that an 'Invalid transaction code' message is created where the payment transaction code is not 00, 68, or 99
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Invalid transaction code') = 1;
    RAISE NOTICE 'Test 20: %', COALESCE(v_test_status,'Failed');
/* Test 21
To verify that a payment in is created for suspense (destination type S) and associated with the new till where the payment in 
transaction code is not 00, 68, or 99
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE additional_information like '%25123456Z%'
        AND destination_type = 'S'
        AND till_id = currval('till_id_seq');
    RAISE NOTICE 'Test 21: %', COALESCE(v_test_status,'Failed');
/* Test 22
To verify that an 'Unknown bank details' message is created where the payment transaction receiving bank account number and 
sort code are not within the business unit’s banks accounts from the configuration item BANKLIST.
*/
    SELECT 'Passed'
    INTO v_test_status
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text = 'Unknown bank details') = 4;
    RAISE NOTICE 'Test 22: %', COALESCE(v_test_status,'Failed');
/* Test 23
To verify that an interface message is created showing the till allocated
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text LIKE 'Till allocated: %') = 1;
    RAISE NOTICE 'Test 23: %', COALESCE(v_test_status,'Failed');
/* Test 24
To verify that an interface message is created showing the count and value for records processed
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text LIKE '%processed: 20,%value: £1003220768.88%') = 1;
    RAISE NOTICE 'Test 24: %', COALESCE(v_test_status,'Failed');
/* Test 25
To verify that an interface message is created showing the count and value for records accepted and that this 
matches with count and sum of payments in records created
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text LIKE '%accepted: 13,%value: £3220768.86%') = 1
        AND (SELECT SUM(payment_amount) FROM payments_in WHERE till_id = currval('till_id_seq')) = 3220768.86;
    RAISE NOTICE 'Test 25: %', COALESCE(v_test_status,'Failed');
/* Test 26
To verify that an interface message is created showing the count and value for records assigned to fines and that this 
matches with count and sum of payments in records created for fines (destination_type = 'F')
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text LIKE '%(fines): 4,%value: £200335.56%') = 1
        AND (SELECT SUM(payment_amount) FROM payments_in WHERE till_id = currval('till_id_seq') AND destination_type = 'F') = 200335.56;
    RAISE NOTICE 'Test 26: %', COALESCE(v_test_status,'Failed');
/* Test 27
To verify that an interface message is created showing the count and value for records assigned to suspense and that this 
matches with count and sum of payments in records created for suspense (destination_type = 'S')
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text LIKE '%(suspense): 9,%value: £3020433.30%') = 1
        AND (SELECT SUM(payment_amount) FROM payments_in WHERE till_id = currval('till_id_seq') AND destination_type = 'S') = 3020433.30;
    RAISE NOTICE 'Test 27: %', COALESCE(v_test_status,'Failed');
/* Test 28
To verify that an interface message is created showing the count and value for payments rejected if records were rejected
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text LIKE '%rejected: 4,%value: £1000000000.02%') = 1;
    RAISE NOTICE 'Test 28: %', COALESCE(v_test_status,'Failed');
/* Test 29
To verify that an interface message is created showing the count and value for payments ignored (amount zero pence) if records were ignored
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (SELECT COUNT(*) FROM interface_messages WHERE interface_job_id = 1 AND message_text LIKE '%ignored: 3') = 1;
    RAISE NOTICE 'Test 29: %', COALESCE(v_test_status,'Failed');
/* Test 30
To verify that all payments created have payment method 'CT' and auto_payment true, allocated false, receipt false, additional info 
showing originator reference, and are either for fines with no allocation_type or for suspense with 'UN' (unidentified) allocation_type
*/
    SELECT 'Passed'
    INTO v_test_status 
    WHERE (
        SELECT COUNT(*)
        FROM payments_in
        WHERE till_id = currval('till_id_seq')
        AND ((destination_type = 'S' AND allocation_type = 'UN') OR (destination_type = 'F' AND allocation_type IS NULL))
        AND payment_method = 'CT'
        AND auto_payment = true
        AND allocated = false
        AND receipt = false
        AND additional_information IS NOT NULL
        ) = 13;
    RAISE NOTICE 'Test 30: %', COALESCE(v_test_status,'Failed');
/* Test 31
To verify that all messages created are either Exception/Warning with a message text, relating file ID, record_index, and record_detail; or 
are Info with a message text
*/
    SELECT 'Passed'
    INTO v_test_status
    WHERE (
        SELECT COUNT(*)
        FROM interface_messages
        WHERE interface_job_id IN (1,2,3,4) 
            AND (message_text IS NULL 
                OR (message_type IN ('Exception','Warning') AND (record_index IS NULL OR record_detail IS NULL OR interface_file_id IS NULL))
            )
        ) = 0;
    RAISE NOTICE 'Test 31: %', COALESCE(v_test_status,'Failed');
/* Test 32
To verify that processing an empty file still completes and produces the relevant info messages for processed, accepted fines, suspense 
counts and totals. This shouldn't happen in real world.
*/
    SELECT 'Passed'
    INTO v_test_status
    WHERE (
        SELECT COUNT(*)
        FROM interface_messages
        WHERE interface_job_id IN (3,4)
            AND message_type = 'Info'
            AND message_text LIKE '%: 0%,%: £0%'
        ) = 8;
    RAISE NOTICE 'Test 32: %', COALESCE(v_test_status,'Failed');
/* Test 33
To verify that a payment is accepted, assigned to fines and associated with the payment destination account where the account is in credit (i.e. positive balance)
*/
    SELECT 'Passed'
    INTO v_test_status
    FROM payments_in
    WHERE till_id = currval('till_id_seq')
        AND associated_record_type = 'defendant_accounts'
        AND associated_record_id = '11';
    RAISE NOTICE 'Test 33: %', COALESCE(v_test_status,'Failed');
END $$;

ROLLBACK;
