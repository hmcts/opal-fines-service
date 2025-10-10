/**
* CGI OPAL Program
*
* MODULE      : create_dev_test_data.sql
*
* DESCRIPTION : Create test data to be used by Frontend for development testing
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------------------------
* 03/10/2025    C Cho       1.0         PO-2222 Create test data to be used by Frontend for development testing
*
**/

-- 1) DEFENDANT_ACCOUNTS (10)
INSERT INTO public.defendant_accounts
(defendant_account_id, business_unit_id, account_number, amount_imposed, amount_paid, account_balance, account_status, account_type, imposed_hearing_date, last_changed_date, last_movement_date, imposing_court_id, enforcing_court_id, last_hearing_court_id, last_hearing_date, last_enforcement, originator_name, originator_type, allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period, enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id, collection_order, collection_order_date, further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date, consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by, prosecutor_case_reference, originator_id, account_comments, account_note_1, account_note_2, account_note_3, jail_days, version_number, payment_card_requested_by_name) VALUES
(99000000000001, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'SP-2024-001235', 120.00, 0.00, 120.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '18 days', 'WP', 'Westminster Magistrates Court', 'AUTO_CP', false, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id LIMIT 1), false, NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', NULL, CURRENT_TIMESTAMP - INTERVAL '15 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L080JG', 'PROS/2024/001235', 'WMC001235', 'Speeding offence - automated camera detection', 'Vehicle registered to defendant', 'Payment card requested for online payment', NULL, 14, 1, 'Test User 1'),
(99000000000002, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'PK-2024-002847', 80.00, 40.00, 40.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '22 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '25 days', 'REM', 'Brighton Magistrates Court', 'AUTO_CP', true, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 1 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id LIMIT 1), NULL, false, NULL, CURRENT_TIMESTAMP - INTERVAL '20 days', NULL, CURRENT_TIMESTAMP - INTERVAL '22 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '12 days', 'L080JG', 'PROS/2024/002847', 'BMC002847', 'Parking violation in restricted area', 'Partial payment received', 'Card payment facility available', 'Reminder sent to defendant', NULL, 1, 'Test User 2'),
(99000000000003, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'TR-2024-003691', 200.00, 200.00, 0.00, 'C', 'Fine', CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '48 days', NULL, 'Leeds Magistrates Court', 'AUTO_CP', false, true, 5, 3, NULL, NULL, NULL, false, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '45 days', NULL, NULL, false, NULL, NULL, 'PROS/2024/003691', 'LMC003691', 'Traffic violation - account completed', 'Full payment received', 'Account closed successfully', 'Payment received in full', NULL, 1, NULL),
(99000000000004, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'SP-2024-004582', 100.00, 25.00, 75.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '11 days', 'ENF1', 'City of London Magistrates', 'AUTO_CP', true, true, 7, 4, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id LIMIT 1), false, NULL, CURRENT_TIMESTAMP - INTERVAL '3 days', NULL, CURRENT_TIMESTAMP - INTERVAL '8 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L047SA', 'PROS/2024/004582', 'CLM004582', 'Speeding in central London zone', 'First reminder sent', 'Payment plan available', 'Enforcement action initiated', 28, 1, 'Test User 4'),
(99000000000005, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'MV-2024-005739', 150.00, 0.00, 150.00, 'TS', 'Fine', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '33 days', 'WP', 'Cambridge Magistrates Court', 'AUTO_CP', false, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 2 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 1 LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id OFFSET 1 LIMIT 1), true, CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '30 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L060FO', 'PROS/2024/005739', 'CMC005739', 'Moving traffic violation on A14', 'Collection order active', 'Further steps notice issued', 'Transfer to Scotland pending', 21, 1, 'Test User 5'),
(99000000000006, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'DA-000006', 600.00, 60.00, 540.00, 'TO', 'Fine', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '9 days', 'ENF2', 'Test Court 6', 'MANUAL', true, true, 7, 5, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 1 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 1 LIMIT 1), NULL, false, NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, CURRENT_TIMESTAMP - INTERVAL '6 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L106CO', 'PROS/TEST/000006', 'TC6-000006', 'Test case for development', 'Enforcement action pending', 'Test data - do not action', 'Used for frontend testing', 42, 1, 'Test User 6'),
(99000000000007, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'DA-000007', 700.00, 70.00, 630.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '10 days', 'WP', 'Test Court 7', 'MANUAL', true, true, 7, 5, NULL, NULL, NULL, false, NULL, CURRENT_TIMESTAMP - INTERVAL '6 days', NULL, CURRENT_TIMESTAMP - INTERVAL '7 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L089BO', 'PROS/TEST/000007', 'TC7-000007', 'Development test account 7', 'Payment instalments set up', 'Regular monitoring required', 'Test environment only', 35, 1, 'Test User 7'),
(99000000000008, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'DA-000008', 800.00, 80.00, 720.00, 'TA', 'Fine', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '11 days', 'ENF1', 'Test Court 8', 'MANUAL', true, false, 0, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 2 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 2 LIMIT 1), NULL, false, NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, CURRENT_TIMESTAMP - INTERVAL '8 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L036DO', 'PROS/TEST/000008', 'TC8-000008', 'No cheque payments allowed', 'Enforcement action initiated', 'Card payments only', 'High value account', 35, 1, 'Test User 8'),
(99000000000009, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'DA-000009', 900.00, 90.00, 810.00, 'TA', 'Fine', CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '12 days', 'REM', 'Test Court 9', 'MANUAL', false, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 3 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 3 LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id OFFSET 1 LIMIT 1), true, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '2 days', NULL, false, NULL, NULL, 'PROS/TEST/000009', 'TC9-000009', 'Complex enforcement case', 'Multiple enforcement actions', 'Suspended committal applied', 'Requires careful monitoring', 56, 1, NULL),
(99000000000010, (SELECT business_unit_id FROM public.business_units ORDER BY business_unit_id LIMIT 1), 'DA-000010', 1000.00, 100.00, 900.00, 'CS', 'Fine', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '13 days', 'WP', 'Test Court 10', 'MANUAL', true, true, 7, 5, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 4 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 4 LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id OFFSET 2 LIMIT 1), false, NULL, CURRENT_TIMESTAMP - INTERVAL '6 days', NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', NULL, 'M', true, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L065AO', 'PROS/TEST/000010', 'TC10-000010', 'Master account for consolidation', 'Consolidated account - master', 'Multiple linked accounts', 'Highest value test case', 70, 1, 'Test User 10')
ON CONFLICT DO NOTHING;

-- 2) PARTIES (defendants)
INSERT INTO public.parties
(party_id, organisation, surname, forenames, title, address_line_1, address_line_2, postcode, birth_date) VALUES
(99000000000001, false, 'Johnson', 'Michael James', 'Mr', '42 Oak Avenue', 'Hammersmith', 'W6 8RF', DATE '1985-03-15'),
(99000000000002, false, 'Williams', 'Sarah Louise', 'Ms', '15 Victoria Street', 'Brighton', 'BN1 4ER', DATE '1992-11-08'),
(99000000000003, false, 'Brown', 'David Alexander', 'Mr', 'Flat 7, Maple Court', 'Leeds', 'LS2 9JT', DATE '1978-07-22'),
(99000000000004, false, 'Davis', 'Emma Charlotte', 'Mrs', '128 High Street', 'Oxford', 'OX1 4DH', DATE '1988-01-30'),
(99000000000005, false, 'Wilson', 'James Robert', 'Mr', '9 Church Lane', 'Cambridge', 'CB2 1AG', DATE '1995-09-12'),
(99000000000006, false, 'TestSurname6', 'TestForename6', 'Mr', 'Flat 6, Test House', 'Test Road', 'ZZ1 06ZZ', DATE '1980-01-01' + INTERVAL '6 years'),
(99000000000007, false, 'TestSurname7', 'TestForename7', 'Mr', 'Flat 7, Test House', 'Test Road', 'ZZ1 07ZZ', DATE '1980-01-01' + INTERVAL '7 years'),
(99000000000008, false, 'TestSurname8', 'TestForename8', 'Mr', 'Flat 8, Test House', 'Test Road', 'ZZ1 08ZZ', DATE '1980-01-01' + INTERVAL '8 years'),
(99000000000009, false, 'TestSurname9', 'TestForename9', 'Mr', 'Flat 9, Test House', 'Test Road', 'ZZ1 09ZZ', DATE '1980-01-01' + INTERVAL '9 years'),
(99000000000010, false, 'TestSurname10', 'TestForename10', 'Mr', 'Flat 10, Test House', 'Test Road', 'ZZ1 10ZZ', DATE '1980-01-01' + INTERVAL '10 years')
ON CONFLICT DO NOTHING;

-- 3) ALIASES
INSERT INTO public.aliases
(alias_id, party_id, sequence_number, surname, forenames) VALUES
(99000000001001, 99000000000001, 1, 'Johnstone', 'Mike'),
(99000000001002, 99000000000002, 1, 'Williams-Smith', 'Sarah'),
(99000000001003, 99000000000003, 1, 'Browne', 'Dave'),
(99000000001004, 99000000000004, 1, 'Davies', 'Emma'),
(99000000001005, 99000000000005, 1, 'Williamson', 'Jimmy'),
(99000000001006, 99000000000006, 1, 'AltSurname6', 'AltForename6'),
(99000000001007, 99000000000007, 1, 'AltSurname7', 'AltForename7'),
(99000000001008, 99000000000008, 1, 'AltSurname8', 'AltForename8'),
(99000000001009, 99000000000009, 1, 'AltSurname9', 'AltForename9'),
(99000000001010, 99000000000010, 1, 'AltSurname10', 'AltForename10')
ON CONFLICT DO NOTHING;

-- 4) DEBTOR_DETAIL
INSERT INTO public.debtor_detail
(party_id, employer_name, employer_address_line_1, employer_address_line_2, employer_postcode, vehicle_make, vehicle_registration) VALUES
(99000000000001, 'TechCorp Solutions Ltd', '1 Business Park', 'Canary Wharf', 'E14 5AB', 'Ford', 'BX67 KLM'),
(99000000000002, 'Brighton Healthcare NHS', '45 Medical Centre', 'Brighton', 'BN2 5RT', 'Volkswagen', 'FG19 XYZ'),
(99000000000003, 'Leeds Manufacturing Co', '12 Industrial Estate', 'Leeds', 'LS10 2PQ', 'BMW', 'HJ21 ABC'),
(99000000000004, 'Oxford University Press', '23 Academic Way', 'Oxford', 'OX2 6DP', 'Audi', 'LK18 DEF'),
(99000000000005, 'Cambridge Consulting', '8 Innovation Hub', 'Cambridge', 'CB3 0HE', 'Mercedes', 'NM20 GHI'),
(99000000000006, 'Employer 6', '1 Business Park', 'Unit 6', 'ZZ9 9ZZ', 'Make6', 'REG0006'),
(99000000000007, 'Employer 7', '1 Business Park', 'Unit 7', 'ZZ9 9ZZ', 'Make7', 'REG0007'),
(99000000000008, 'Employer 8', '1 Business Park', 'Unit 8', 'ZZ9 9ZZ', 'Make8', 'REG0008'),
(99000000000009, 'Employer 9', '1 Business Park', 'Unit 9', 'ZZ9 9ZZ', 'Make9', 'REG0009'),
(99000000000010, 'Employer 10', '1 Business Park', 'Unit 10', 'ZZ9 9ZZ', 'Make10', 'REG0010')
ON CONFLICT DO NOTHING;

-- 5) DEFENDANT_ACCOUNT_PARTIES
INSERT INTO public.defendant_account_parties
(defendant_account_party_id, defendant_account_id, party_id, association_type, debtor) VALUES
(99000000002001, 99000000000001, 99000000000001, 'Defendant', true),
(99000000002002, 99000000000002, 99000000000002, 'Defendant', true),
(99000000002003, 99000000000003, 99000000000003, 'Defendant', true),
(99000000002004, 99000000000004, 99000000000004, 'Defendant', true),
(99000000002005, 99000000000005, 99000000000005, 'Defendant', true),
(99000000002006, 99000000000006, 99000000000006, 'Defendant', true),
(99000000002007, 99000000000007, 99000000000007, 'Defendant', true),
(99000000002008, 99000000000008, 99000000000008, 'Defendant', true),
(99000000002009, 99000000000009, 99000000000009, 'Defendant', true),
(99000000002010, 99000000000010, 99000000000010, 'Defendant', true)
ON CONFLICT DO NOTHING;

-- 6) PAYMENT_CARD_REQUESTS
INSERT INTO public.payment_card_requests (defendant_account_id) VALUES
(99000000000001),
(99000000000002),
(99000000000003),
(99000000000004),
(99000000000005),
(99000000000006),
(99000000000007),
(99000000000008),
(99000000000009),
(99000000000010)
ON CONFLICT DO NOTHING;

-- 7) Minor Creditor Party + Creditor Account (type 'MN')
INSERT INTO public.parties (party_id, organisation, organisation_name, address_line_1, postcode, account_type) VALUES
(99000000000900, true, 'Minor Creditor Test Ltd', '1 Test Street', 'ZZ1 1ZZ', 'Creditor')
ON CONFLICT DO NOTHING;

INSERT INTO public.creditor_accounts
(creditor_account_id, business_unit_id, account_number, creditor_account_type, prosecution_service, major_creditor_id, minor_creditor_party_id, from_suspense, hold_payout, pay_by_bacs, bank_sort_code, bank_account_number, bank_account_name, bank_account_reference) VALUES
(99000000000800, 65, 'CR-800000', 'MN', false, NULL, 99000000000900, false, false, true, '000000', '00000000', 'Minor Creditor', 'REF')
ON CONFLICT DO NOTHING;

-- 8) IMPOSITIONS (one per account) – result_id reused from reference data
INSERT INTO public.impositions
(imposition_id, defendant_account_id, posted_date, posted_by, posted_by_name, result_id, imposed_amount, paid_amount, creditor_account_id, offence_title, offence_code) VALUES
(99000000003001, 99000000000001, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'opal-test', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 100.00, 10.00, 99000000000800, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
(99000000003002, 99000000000002, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'opal-test-2', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 200.00, 20.00, 99000000000800, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
(99000000003003, 99000000000003, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'opal-test-3', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 300.00, 30.00, 99000000000800, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
(99000000003004, 99000000000004, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'opal-test-4', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 400.00, 40.00, 99000000000800, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
(99000000003005, 99000000000005, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'opal-test-5', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 500.00, 50.00, 99000000000800, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
(99000000003006, 99000000000006, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'opal-test-6', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 600.00, 60.00, 99000000000800, 'Test Offence 6', 'OFF0006'),
(99000000003007, 99000000000007, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'opal-test-7', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 700.00, 70.00, 99000000000800, 'Test Offence 7', 'OFF0007'),
(99000000003008, 99000000000008, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'opal-test-8', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 800.00, 80.00, 99000000000800, 'Test Offence 8', 'OFF0008'),
(99000000003009, 99000000000009, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'opal-test-9', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 900.00, 90.00, 99000000000800, 'Test Offence 9', 'OFF0009'),
(99000000003010, 99000000000010, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO', 'opal-test-10', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 1000.00, 100.00, 99000000000800, 'Test Offence 10', 'OFF0010')
ON CONFLICT DO NOTHING;

-- 9) DEFENDANT_TRANSACTIONS
INSERT INTO public.defendant_transactions
(defendant_transaction_id, defendant_account_id, posted_date, posted_by, transaction_type, transaction_amount, payment_method, payment_reference, text, status, status_date, posted_by_name) VALUES
(99000000004001, 99000000000001, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'PAY', 10.00, 'CC', 'PMT000001', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '1 days', 'opal-test'),
(99000000004002, 99000000000002, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'PAY', 20.00, 'CC', 'PMT000002', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '2 days', 'opal-test-2'),
(99000000004003, 99000000000003, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'PAY', 30.00, 'CC', 'PMT000003', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '3 days', 'opal-test-3'),
(99000000004004, 99000000000004, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'PAY', 40.00, 'CC', 'PMT000004', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '4 days', 'opal-test-4'),
(99000000004005, 99000000000005, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'PAY', 50.00, 'CC', 'PMT000005', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '5 days', 'opal-test-5'),
(99000000004006, 99000000000006, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'PAY', 60.00, 'CC', 'PMT000006', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '6 days', 'opal-test-6'),
(99000000004007, 99000000000007, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'PAY', 70.00, 'CC', 'PMT000007', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '7 days', 'opal-test-7'),
(99000000004008, 99000000000008, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'PAY', 80.00, 'CC', 'PMT000008', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '8 days', 'opal-test-8'),
(99000000004009, 99000000000009, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'PAY', 90.00, 'CC', 'PMT000009', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '9 days', 'opal-test-9'),
(99000000004010, 99000000000010, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO', 'PAY', 100.00, 'CC', 'PMT000010', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '10 days', 'opal-test-10')
ON CONFLICT DO NOTHING;

-- 10) ALLOCATIONS (link transactions to impositions)
INSERT INTO public.allocations
(allocation_id, imposition_id, allocated_date, allocated_amount, transaction_type, allocation_function, defendant_transaction_id) VALUES
(99000000005001, 99000000003001, CURRENT_TIMESTAMP - INTERVAL '1 days', 10.00, 'PAYMENT', 'C104A', 99000000004001),
(99000000005002, 99000000003002, CURRENT_TIMESTAMP - INTERVAL '2 days', 20.00, 'PAYMENT', 'C104A', 99000000004002),
(99000000005003, 99000000003003, CURRENT_TIMESTAMP - INTERVAL '3 days', 30.00, 'PAYMENT', 'C104A', 99000000004003),
(99000000005004, 99000000003004, CURRENT_TIMESTAMP - INTERVAL '4 days', 40.00, 'PAYMENT', 'C104A', 99000000004004),
(99000000005005, 99000000003005, CURRENT_TIMESTAMP - INTERVAL '5 days', 50.00, 'PAYMENT', 'C104A', 99000000004005),
(99000000005006, 99000000003006, CURRENT_TIMESTAMP - INTERVAL '6 days', 60.00, 'PAYMENT', 'AutoTest', 99000000004006),
(99000000005007, 99000000003007, CURRENT_TIMESTAMP - INTERVAL '7 days', 70.00, 'PAYMENT', 'AutoTest', 99000000004007),
(99000000005008, 99000000003008, CURRENT_TIMESTAMP - INTERVAL '8 days', 80.00, 'PAYMENT', 'AutoTest', 99000000004008),
(99000000005009, 99000000003009, CURRENT_TIMESTAMP - INTERVAL '9 days', 90.00, 'PAYMENT', 'AutoTest', 99000000004009),
(99000000005010, 99000000003010, CURRENT_TIMESTAMP - INTERVAL '10 days', 100.00, 'PAYMENT', 'AutoTest', 99000000004010)
ON CONFLICT DO NOTHING;

-- 11) CREDITOR_TRANSACTIONS
INSERT INTO public.creditor_transactions
(creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name, transaction_type, transaction_amount, imposition_result_id, payment_processed, payment_reference, status, status_date) VALUES
(99000000006001, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'opal-test', 'PAYMNT', 10.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000001', 'C', CURRENT_TIMESTAMP - INTERVAL '1 days'),
(99000000006002, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'opal-test-2', 'PAYMNT', 20.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000002', 'C', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(99000000006003, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'opal-test-3', 'PAYMNT', 30.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000003', 'C', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(99000000006004, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'opal-test-4', 'PAYMNT', 40.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000004', 'C', CURRENT_TIMESTAMP - INTERVAL '4 days'),
(99000000006005, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'opal-test-5', 'PAYMNT', 50.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000005', 'C', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(99000000006006, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'opal-test-6', 'PAYMNT', 60.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000006', 'C', CURRENT_TIMESTAMP - INTERVAL '6 days'),
(99000000006007, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'opal-test-7', 'PAYMNT', 70.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000007', 'C', CURRENT_TIMESTAMP - INTERVAL '7 days'),
(99000000006008, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'opal-test-8', 'PAYMNT', 80.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000008', 'C', CURRENT_TIMESTAMP - INTERVAL '8 days'),
(99000000006009, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'opal-test-9', 'PAYMNT', 90.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000009', 'C', CURRENT_TIMESTAMP - INTERVAL '9 days'),
(99000000006010, 99000000000800, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO', 'opal-test-10', 'PAYMNT', 100.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000010', 'C', CURRENT_TIMESTAMP - INTERVAL '10 days')
ON CONFLICT DO NOTHING;

-- 12) FIXED_PENALTY_OFFENCES
INSERT INTO public.fixed_penalty_offences
(defendant_account_id, ticket_number, vehicle_registration, offence_location, issued_date, offence_date, offence_time) VALUES
(99000000000001, 'SP240001235', 'BX67KLM', 'High Street, Central London', CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE - INTERVAL '15 days', '14:30'),
(99000000000002, 'PK240002847', 'FG19XYZ', 'Seafront Car Park, Brighton', CURRENT_DATE - INTERVAL '22 days', CURRENT_DATE - INTERVAL '22 days', '09:15'),
(99000000000003, 'TR240003691', 'HJ21ABC', 'M1 Motorway Junction 45', CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE - INTERVAL '45 days', '16:45'),
(99000000000004, 'SP240004582', 'LK18DEF', 'Oxford Street, London', CURRENT_DATE - INTERVAL '8 days', CURRENT_DATE - INTERVAL '8 days', '11:20'),
(99000000000005, 'MV240005739', 'NM20GHI', 'A14 Cambridge Bypass', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '30 days', '08:45'),
(99000000000006, 'TICKET-000006', 'VR0006', 'Testville', CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE - INTERVAL '6 days', '10:00'),
(99000000000007, 'TICKET-000007', 'VR0007', 'Testville', CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE - INTERVAL '7 days', '10:00'),
(99000000000008, 'TICKET-000008', 'VR0008', 'Testville', CURRENT_DATE - INTERVAL '8 days', CURRENT_DATE - INTERVAL '8 days', '10:00'),
(99000000000009, 'TICKET-000009', 'VR0009', 'Testville', CURRENT_DATE - INTERVAL '9 days', CURRENT_DATE - INTERVAL '9 days', '10:00'),
(99000000000010, 'TICKET-000010', 'VR0010', 'Testville', CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE - INTERVAL '10 days', '10:00')
ON CONFLICT DO NOTHING;

-- 13) ENFORCEMENTS
INSERT INTO public.enforcements
(enforcement_id, defendant_account_id, posted_date, posted_by, reason, result_responses, warrant_reference, posted_by_name) VALUES
(99000000007001, 99000000000001, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'Test enforcement', '{"k":"v"}'::json, '001/25/00001', 'opal-test'),
(99000000007002, 99000000000002, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'Test enforcement', '{"k":"v"}'::json, '001/25/00002', 'opal-test-2'),
(99000000007003, 99000000000003, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'Test enforcement', '{"k":"v"}'::json, '002/25/00003', 'opal-test-3'),
(99000000007004, 99000000000004, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'Test enforcement', '{"k":"v"}'::json, '002/25/00004', 'opal-test-4'),
(99000000007005, 99000000000005, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'Test enforcement', '{"k":"v"}'::json, '003/25/00005', 'opal-test-5'),
(99000000007006, 99000000000006, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'Test enforcement', '{"k":"v"}'::json, '003/25/00006', 'opal-test-6'),
(99000000007007, 99000000000007, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'Test enforcement', '{"k":"v"}'::json, '004/25/00007', 'opal-test-7'),
(99000000007008, 99000000000008, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'Test enforcement', '{"k":"v"}'::json, '004/25/00008', 'opal-test-8'),
(99000000007009, 99000000000009, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'Test enforcement', '{"k":"v"}'::json, '005/25/00009', 'opal-test-9'),
(99000000007010, 99000000000010, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO', 'Test enforcement', '{"k":"v"}'::json, '005/25/00010', 'opal-test-10')
ON CONFLICT DO NOTHING;

-- 14) REPORT_INSTANCES (1)
INSERT INTO public.report_instances
(report_instance_id, report_id, business_unit_id, audit_sequence, generated_date, generated_by, report_parameters, content) VALUES
(99000000008000, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), 65, 1, CURRENT_TIMESTAMP, 'L080JG', '{"from":"auto"}'::json, XMLPARSE(DOCUMENT '<report><title>Auto Test</title></report>'))
ON CONFLICT DO NOTHING;

-- 15) REPORT_ENTRIES (10)
INSERT INTO public.report_entries
(report_entry_id, business_unit_id, report_id, entry_timestamp, reported_timestamp, associated_record_type, associated_record_id, report_instance_id) VALUES
(99000000009001, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000001', 99000000008000),
(99000000009002, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000002', 99000000008000),
(99000000009003, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000003', 99000000008000),
(99000000009004, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000004', 99000000008000),
(99000000009005, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000005', 99000000008000),
(99000000009006, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000006', 99000000008000),
(99000000009007, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000007', 99000000008000),
(99000000009008, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000008', 99000000008000),
(99000000009009, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000009', 99000000008000),
(99000000009010, 65, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000010', 99000000008000)
ON CONFLICT DO NOTHING;

-- 16) CONTROL_TOTALS (3)
INSERT INTO public.control_totals
(control_total_id, business_unit_id, item_number, amount, associated_record_type, associated_record_id, ct_report_instance_id) VALUES
(99000000009501, 65, 1, 100.00, 'REPORT_INSTANCE', '99000000008000', 99000000008000),
(99000000009502, 65, 2, 200.00, 'REPORT_INSTANCE', '99000000008000', 99000000008000),
(99000000009503, 65, 3, 300.00, 'REPORT_INSTANCE', '99000000008000', 99000000008000)
ON CONFLICT DO NOTHING;

-- 17) TILLS (1)
INSERT INTO public.tills
(till_id, business_unit_id, till_number, owned_by) VALUES
(99000000010000, 65, 9001, 'L080JG')
ON CONFLICT DO NOTHING;

-- 18) NOTES (AA only)
INSERT INTO public.notes
(note_id, note_type, associated_record_type, associated_record_id, note_text, posted_date, posted_by, posted_by_name) VALUES
(99000000011001, 'AA', 'defendant_accounts', '99000000000001', 'Auto test note for account 99000000000001', CURRENT_TIMESTAMP, 'L080JG', 'opal-test'),
(99000000011002, 'AA', 'defendant_accounts', '99000000000002', 'Auto test note for account 99000000000002', CURRENT_TIMESTAMP, 'L080JG', 'opal-test-2'),
(99000000011003, 'AA', 'defendant_accounts', '99000000000003', 'Auto test note for account 99000000000003', CURRENT_TIMESTAMP, 'L026SH', 'opal-test-3'),
(99000000011004, 'AA', 'defendant_accounts', '99000000000004', 'Auto test note for account 99000000000004', CURRENT_TIMESTAMP, 'L047SA', 'opal-test-4'),
(99000000011005, 'AA', 'defendant_accounts', '99000000000005', 'Auto test note for account 99000000000005', CURRENT_TIMESTAMP, 'L060FO', 'opal-test-5'),
(99000000011006, 'AA', 'defendant_accounts', '99000000000006', 'Auto test note for account 99000000000006', CURRENT_TIMESTAMP, 'L106CO', 'opal-test-6'),
(99000000011007, 'AA', 'defendant_accounts', '99000000000007', 'Auto test note for account 99000000000007', CURRENT_TIMESTAMP, 'L089BO', 'opal-test-7'),
(99000000011008, 'AA', 'defendant_accounts', '99000000000008', 'Auto test note for account 99000000000008', CURRENT_TIMESTAMP, 'L036DO', 'opal-test-8'),
(99000000011009, 'AA', 'defendant_accounts', '99000000000009', 'Auto test note for account 99000000000009', CURRENT_TIMESTAMP, 'L045EO', 'opal-test-9'),
(99000000011010, 'AA', 'defendant_accounts', '99000000000010', 'Auto test note for account 99000000000010', CURRENT_TIMESTAMP, 'L065AO', 'opal-test-10')
ON CONFLICT DO NOTHING;

-- 19) DOCUMENT_INSTANCES (10) – reuses existing document_id
INSERT INTO public.document_instances
(document_instance_id, document_id, business_unit_id, generated_date, generated_by, associated_record_type, associated_record_id, status, document_content) VALUES
(99000000012001, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L080JG', 'defendant_accounts', '99000000000001', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000001</account></doc>')),
(99000000012002, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L080JG', 'defendant_accounts', '99000000000002', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000002</account></doc>')),
(99000000012003, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L026SH', 'defendant_accounts', '99000000000003', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000003</account></doc>')),
(99000000012004, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L047SA', 'defendant_accounts', '99000000000004', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000004</account></doc>')),
(99000000012005, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L060FO', 'defendant_accounts', '99000000000005', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000005</account></doc>')),
(99000000012006, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L106CO', 'defendant_accounts', '99000000000006', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000006</account></doc>')),
(99000000012007, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L089BO', 'defendant_accounts', '99000000000007', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000007</account></doc>')),
(99000000012008, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L036DO', 'defendant_accounts', '99000000000008', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000008</account></doc>')),
(99000000012009, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L045EO', 'defendant_accounts', '99000000000009', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000009</account></doc>')),
(99000000012010, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 65, CURRENT_TIMESTAMP, 'L065AO', 'defendant_accounts', '99000000000010', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000010</account></doc>'))
ON CONFLICT DO NOTHING;

-- 20) PAYMENT_TERMS (10)
INSERT INTO public.payment_terms
(payment_terms_id, defendant_account_id, posted_date, posted_by, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum, jail_days, extension, account_balance, posted_by_name, active) VALUES
(99000000013001, 99000000000001, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'I', CURRENT_DATE, 'M', 10.00, 0.00, NULL, false, 90.00, 'opal-test', true),
(99000000013002, 99000000000002, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'I', CURRENT_DATE, 'M', 20.00, 0.00, NULL, false, 180.00, 'opal-test-2', true),
(99000000013003, 99000000000003, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'I', CURRENT_DATE, 'M', 30.00, 0.00, NULL, false, 270.00, 'opal-test-3', true),
(99000000013004, 99000000000004, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'I', CURRENT_DATE, 'M', 40.00, 0.00, NULL, false, 360.00, 'opal-test-4', true),
(99000000013005, 99000000000005, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'I', CURRENT_DATE, 'M', 50.00, 0.00, NULL, false, 450.00, 'opal-test-5', true),
(99000000013006, 99000000000006, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'I', CURRENT_DATE, 'M', 60.00, 0.00, NULL, false, 540.00, 'opal-test-6', true),
(99000000013007, 99000000000007, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'I', CURRENT_DATE, 'M', 70.00, 0.00, NULL, false, 630.00, 'opal-test-7', true),
(99000000013008, 99000000000008, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'I', CURRENT_DATE, 'M', 80.00, 0.00, NULL, false, 720.00, 'opal-test-8', true),
(99000000013009, 99000000000009, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'I', CURRENT_DATE, 'M', 90.00, 0.00, NULL, false, 810.00, 'opal-test-9', true),
(99000000013010, 99000000000010, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO', 'I', CURRENT_DATE, 'M', 100.00, 0.00, NULL, false, 900.00, 'opal-test-10', true)
ON CONFLICT DO NOTHING;

COMMIT;
